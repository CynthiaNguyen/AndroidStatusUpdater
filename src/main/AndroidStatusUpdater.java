package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
		List<DeviceStatus> previousStatuses = null;
		List<DeviceStatus> currentStatuses = null;
		int numOfDevices;
		LOG.info("Initialized device list and number of devices");
		
		//initialize shell process variables
		String line = "";
		String command = "adb devices";		
		Runtime run = Runtime.getRuntime();
		LOG.info("Running command = " + command);
		
		//--------------------------
		//	infinite loop
		//--------------------------
		while (true) {
			try {
				Process process = run.exec(command);//execute command
				process.waitFor();//wait for process to finish and terminate
				
				currentStatuses = new ArrayList<DeviceStatus>();//refresh current device list				
				BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
				
				//--------------------------
				//	save current output
				//--------------------------
				while ((line=bf.readLine()) != null) {
					LOG.info(line);

					DeviceStatus currentDevice = parseAdbOutput(line);					
					if (currentDevice != null) {
						currentStatuses.add(currentDevice);
					}
				}//end while
				
				//save total number of devices connected
				numOfDevices = currentStatuses.size();
				LOG.info("Number of devices connected = " + numOfDevices);
				
				//--------------------------
				//	if no device is physically connected
				//--------------------------
				if (currentStatuses.size() == 0) {
					//and there were devices connected before
					if (previousStatuses != null) {
						//update all those device statuses to "Disconnected"
						for (int i = 0; i < previousStatuses.size(); i++) {
							previousStatuses.get(i).setStatus("Disconnected");
							String udid = previousStatuses.get(i).getUdid();
							String status = previousStatuses.get(i).getStatus();
							
							LOG.info("Updating device = " + udid + " | status = " + status);								
						}
					} else {
						LOG.info("No devices are connected! Waiting 30 seconds...");
						Thread.sleep(5000);//wait 30 seconds
						continue;//skip loop iteration and check adb again
					}
				}
				
				//--------------------------
				//	if this is the first time getting adb output, then update database for all devices connected
				//--------------------------
				if (previousStatuses == null) {
					previousStatuses = currentStatuses;//initialize previousStatuses list

					for (int i = 0; i < previousStatuses.size(); i++) {
						String status = previousStatuses.get(i).getStatus();
						String udid = previousStatuses.get(i).getUdid();

						if (status.equalsIgnoreCase("offline")) {//if status = offline then update database as "Offline"
							previousStatuses.get(i).setStatus("Offline");
							status = previousStatuses.get(i).getStatus();
							
							LOG.info("Updating device = " + udid + " | status = " + status);
						} else if (status.equalsIgnoreCase("device")) {//if status = device then update database as "Available"
							previousStatuses.get(i).setStatus("Available");
							status = previousStatuses.get(i).getStatus();
							
							LOG.info("Updating device = " + udid + " | status = " + status);								
						}
					}
				}
				
				//--------------------------
				//	else update device status if current output status is different from before
				//--------------------------
				else {
					//check if devices connected are still the same				
					if (previousStatuses.size() == currentStatuses.size()) {
						int size = previousStatuses.size();
						
						for (int i = 0; i < size; i++) {
							String currentUdid = currentStatuses.get(i).getUdid();
							String currentStatus = currentStatuses.get(i).getStatus();

							String previousUdid = previousStatuses.get(i).getUdid();
							String previousStatus = previousStatuses.get(i).getStatus();
							
							//if current device is same as previous device udid
							if (currentUdid.equalsIgnoreCase(previousUdid)) {
								
								//and if device status changed then update database for this device
								if (!currentStatus.equalsIgnoreCase(previousStatus)) {
									if (currentStatus.equalsIgnoreCase("device")) {
										previousStatuses.get(i).setStatus("Available");
										previousStatus = previousStatuses.get(i).getStatus(); 
									} else if (currentStatus.equalsIgnoreCase("offline")) {
										previousStatuses.get(i).setStatus("Offline");
										previousStatus = previousStatuses.get(i).getStatus();
									}
									LOG.info("Status changed for udid = " + currentUdid + "! Updating status = " + previousStatus);
								}
							}
						}						
					}
				}
				
				//--------------------------
				//	if # of devices changed
				//--------------------------
				if (numOfDevices != previousStatuses.size()) {
					//update the status of the disconnected device(s)
					
					LOG.info("Number of devices changed!");
				}


				
				bf.close();							
				LOG.info("Waiting 30 seconds...");
				Thread.sleep(5000);//wait 30 seconds
			} catch (IOException e) {
				LOG.error("Unable to execute command: " + command, e);			
			} catch (InterruptedException e) {
				LOG.error("Could not wait for process to terminate.", e);
//				e.printStackTrace();
			}						
		}//end outer while
		
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
				String udid = matcher.group(1);
				String status = matcher.group(3);
				
				if (status.equalsIgnoreCase("offline")) {
					status = "Offline";
				} else if (status.equalsIgnoreCase("device")) {
					status = "Available";
				}
//				LOG.info("udid = " + matcher.group(1) + " | status = " + matcher.group(3));				
				return new DeviceStatus(matcher.group(1), matcher.group(3));
			}//end if
		}//end if
		
		return null;
	}//end method
}//end class
