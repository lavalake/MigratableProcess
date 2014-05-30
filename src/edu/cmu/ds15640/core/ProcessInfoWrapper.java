package edu.cmu.ds15640.core;

import java.io.Serializable;

public class ProcessInfoWrapper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4221885566660617590L;
	
	int processID;
	StatusType status;

	public ProcessInfoWrapper(int id, StatusType type) {
		processID = id;
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

}
