package model;

public class DeviceStatus {
	private String udid;
	private String status;
	
	public DeviceStatus() {
		
	}
	
	public DeviceStatus(String udid, String status) {
		this.udid = udid;
		this.status = status;
	}
	
	public String getUdid() {
		return udid;
	}
	public void setUdid(String udid) {
		this.udid = udid;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
}
