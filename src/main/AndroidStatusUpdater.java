package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;

import model.DeviceStatus;

public class AndroidStatusUpdater {

	private final static Logger LOG = Logger.getLogger(AndroidStatusUpdater.class.getName());
	
	public static void main(String[] args) {
		LOG.info("Testing log4j setup success!");
		
		//initialize device list
		List<DeviceStatus> deviceStatuses = null;
		int numOfDevices;
		LOG.info("Initialized device list and number of devices");
		
		//initialize shell process
		String line = "";
		String command = "adb devices";
		
		Runtime run = Runtime.getRuntime();
		LOG.info("Running command = " + command);
		try {
			Process process = run.exec(command);//execute command
			process.waitFor();//wait for process to finish and terminate
			LOG.info("Executed command = " + command);
			
			//capture output from command
			BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
			LOG.info("Capturing output from execution...");
			
			while ((line=bf.readLine()) != null) {
				LOG.info(line);
			}//end while
		} catch (IOException e) {
			LOG.error("Unable to execute command: " + command, e);			
		} catch (InterruptedException e) {
			LOG.error("Could not wait for process to terminate.", e);
//			e.printStackTrace();
		}
		
	}

}
