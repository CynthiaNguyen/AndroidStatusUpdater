package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import model.DeviceStatus;

public class AndroidStatusUpdater {

	private final static Logger LOG = Logger.getLogger(AndroidStatusUpdater.class.getName());
	
	public static void main(String[] args) {
		LOG.info("Testing log4j setup success!");
		
		//initialize device list
		List<DeviceStatus> deviceStatuses = null;
		List<DeviceStatus> currentStatuses = null;
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
			
			//capture output from command
			BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
			LOG.info("-------------------------");
			LOG.info("Output");
			LOG.info("-------------------------");
			
			while ((line=bf.readLine()) != null) {
				LOG.info(line);
								
				
				//save current output
				DeviceStatus currentDevice = parseAdbOutput(line);
				
				if (currentDevice != null) {
					currentStatuses.add(currentDevice);
				}
				
				parseAdbOutput(line);
				//parse line for udid and device status
				
				//save in device status list
				
			}//end while
			
			bf.close();			
		} catch (IOException e) {
			LOG.error("Unable to execute command: " + command, e);			
		} catch (InterruptedException e) {
			LOG.error("Could not wait for process to terminate.", e);
//			e.printStackTrace();
		}
		
	}//end main

	/**
	 * Given a line from the "adb devices" output,
	 * return a udid and status if there is a match.
	 * @param line
	 */
	public static DeviceStatus parseAdbOutput(String line) {
		Pattern pattern = Pattern.compile("^([a-zA-Z0-9\\-]+)(\\s+)(\\w+)");
		Matcher matcher;
		
		if (line.matches(pattern.pattern())) {
			matcher = pattern.matcher(line);
			
			if (matcher.find()) {
				LOG.info("udid = " + matcher.group(1));//return device
				LOG.info("status = " + matcher.group(3));//return status
				
				return new DeviceStatus(matcher.group(1), matcher.group(3));
			}//end if
		}//end if
		
		return null;
	}//end method
}//end class
