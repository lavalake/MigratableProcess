package edu.cmu.ds15640.core;

import java.io.Serializable;

public abstract class MigratableProcess implements Serializable, Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8679277965235631138L;

	private int processID;
	private StatusType status;

	public MigratableProcess() {
		status = StatusType.RUNNING;
	}

	@Override
	public abstract void run();

	public abstract void suspend();

	@Override
	public String toString() {
		return new String("ProcessID: " + processID + "		Status: " + status);
	}

	public void setProcessID(int id) {
		processID = id;
	}

	public int getProcessID() {
		return processID;
	}

	public StatusType getStatus() {
		return status;
	}

	public void setStatus(StatusType stype) {
		status = stype;
	}

}
