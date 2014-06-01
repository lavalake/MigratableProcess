package edu.cmu.ds15640.core;

/**
 * Worker information for WorkerServer
 * 
 * @author Xincheng Liu
 * @author Hao Ge
 */
public class WorkerInfo {
	private int workerID;
	private String IPAddress;
	private int port;
	private WorkerService workerService;
	private StatusType status;

	public WorkerInfo(int id, String ip, int p, WorkerService ws) {
		workerID = id;
		IPAddress = ip;
		port = p;
		workerService = ws;
		status = StatusType.RUNNING;
	}

	public int getWorkerID() {
		return workerID;
	}

	public String getIPAddress() {
		return IPAddress;
	}

	public WorkerService getWorkerService() {
		return workerService;
	}

	public String toString() {
		return new String("workerID: " + workerID + "	IPAddress: " + IPAddress
				+ "	Port: " + port + " Status: " + status.toString());
	}

	public StatusType getStatus() {
		return status;
	}

	public void setStatus(StatusType status) {
		this.status = status;
	}
}
