package edu.cmu.ds15640.core;

public class WorkerInfo {
	private int workerID;
	private String IPAddress;
	private int port;
	private WorkerService workerService;

	public WorkerInfo(int id, String ip, int p, WorkerService ws) {
		workerID = id;
		IPAddress = ip;
		port = p;
		workerService = ws;
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
				+ "	Port: " + port);
	}
}
