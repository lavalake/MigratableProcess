package edu.cmu.ds15640.core;

import java.net.Socket;

public class WorkerInfo {
	private int workerID;
	private String IPAddress;
	private int port;
	private Socket socket;

	public WorkerInfo(int id, String ip, int p, Socket s) {
		workerID = id;
		IPAddress = ip;
		port = p;
		socket = s;
	}

	public int getWorkerID() {
		return workerID;
	}

	public String getIPAddress() {
		return IPAddress;
	}

	public Socket getSocket() {
		return socket;
	}

	public String toString() {
		return new String("workerID: " + workerID + "	IPAddress: " + IPAddress + "	Port: " + port);
	}
}
