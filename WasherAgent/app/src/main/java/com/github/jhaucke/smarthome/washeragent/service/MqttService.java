package com.github.jhaucke.smarthome.washeragent.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.github.jhaucke.smarthome.washeragent.Constants;
import com.github.jhaucke.smarthome.washeragent.R;

public class MqttService extends Service {

    private static MyMqttClient client = null;
    ReconnectAlarmReceiver reconnectAlarmReceiver;
    ConnectivityChangReceiver connectivityChangReceiver;
    private Context serviceContext;
    private Handler toastHandler;

    public static MyMqttClient getClient() {
        return client;
    }

    @Override
    public void onCreate() {
        serviceContext = getApplicationContext();
        registerReceiver();

        toastHandler = new Handler(Looper.getMainLooper());
        LogWriter.appendLog("MqttService started");
        toastHandler.post(new ToastRunnable(serviceContext.getResources().getString(R.string.toast_service_started)));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        client = new MyMqttClient(serviceContext);
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        client.closeConnection();
        unregisterReceiver();
        cancelNotification();
        LogWriter.appendLog("MqttService stopped");
        toastHandler.post(new ToastRunnable(serviceContext.getResources().getString(R.string.toast_service_stopped)));
    }

    private void cancelNotification() {
        NotificationManager notifyMgr = (NotificationManager) serviceContext.getSystemService(serviceContext.NOTIFICATION_SERVICE);
        notifyMgr.cancel(Constants.NOTIFICATION_ID);
    }

    private void registerReceiver() {
        reconnectAlarmReceiver = new ReconnectAlarmReceiver();
        serviceContext.registerReceiver(reconnectAlarmReceiver, new IntentFilter(Constants.RECONNECT_ALARM_ACTION));

        connectivityChangReceiver = new ConnectivityChangReceiver();
        serviceContext.registerReceiver(connectivityChangReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unregisterReceiver() {
        serviceContext.unregisterReceiver(reconnectAlarmReceiver);
        AlarmManager alarmMgr = (AlarmManager) serviceContext.getSystemService(Service.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(serviceContext, 0, new Intent(Constants.RECONNECT_ALARM_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.cancel(alarmIntent);

        serviceContext.unregisterReceiver(connectivityChangReceiver);
    }

    private class ToastRunnable implements Runnable {
        String message;

        public ToastRunnable(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
