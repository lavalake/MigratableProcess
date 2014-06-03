package edu.cmu.ds15640.process;

import java.io.Serializable;

import edu.cmu.ds15640.core.StatusType;


/**
 * 
 * 
 * */

public abstract class MigratableProcess implements Serializable, Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8679277965235631138L;

	private int processID;
	private StatusType status;
	protected boolean complete;

	public MigratableProcess() {
		status = StatusType.RUNNING;
	}

	@Override
	public abstract void run();

	public abstract void suspend();

	public abstract void stop();
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

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

}
