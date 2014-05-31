package edu.cmu.ds15640.core;

import java.io.Serializable;

public class ProcessInfoWrapper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4221885566660617590L;

	int processID;
	String processName;
	StatusType status;

	public ProcessInfoWrapper(int id, String name, StatusType type) {
		processID = id;
		processName = name;
		status = type;
	}

	public StatusType getStatus() {
		return status;
	}

	public void setStatus(StatusType status) {
		this.status = status;
	}

	public int getProcessID() {
		return processID;
	}

	public void setProcessID(int processID) {
		this.processID = processID;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	@Override
	public String toString() {
		return new String("ProcessID: " + processID + " ProcessName: " + processName + " Status: " + status);
	}

}
