package com.github.jhaucke.smarthome.datacollectorservice;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import com.github.jhaucke.smarthome.datacollectorservice.sqlite.SQLiteJDBC;
import com.github.jhaucke.smarthome.fritzboxconnector.FritzBoxConnector;
import com.github.jhaucke.smarthome.fritzboxconnector.HttpInterface;

/**
 * This class is the entry point to start the data-collector-service.<br>
 * It continuously collects readings from the actuators.
 */
public class Collector {

	public static void main(String[] args) throws IOException, JAXBException, InterruptedException {

		FritzBoxConnector fritzBoxConnector = null;

		if (args.length == 2) {
			fritzBoxConnector = new FritzBoxConnector(args[0], args[1], null);
		} else if (args.length == 3) {
			fritzBoxConnector = new FritzBoxConnector(args[0], args[1], args[2]);
		}

		HttpInterface httpInterface = fritzBoxConnector.getHttpInterface();
		SQLiteJDBC db = new SQLiteJDBC();
		String ain = httpInterface.getSwitchList().trim();

		while (true) {
			db.insertPowerData(httpInterface.getSwitchPower(ain));
			Thread.sleep(10000);
		}
	}
}
