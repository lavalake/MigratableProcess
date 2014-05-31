package edu.cmu.ds15640.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class RunnableServer implements Runnable {

	private int port;
	private volatile boolean stop;
	private int workerID = 0;
	
	public RunnableServer(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			while(!stop){
				Socket socket = serverSocket.accept();
				System.out.println("one worker joins");
				String IPAddress = socket.getInetAddress().toString();
				int port = socket.getPort();
				WorkerService workerService = new WorkerService(socket, workerID);
				WorkerInfo newWorker = new WorkerInfo(workerID, IPAddress, port, workerService);
				workerID++;
				ProcessManager.getInstance().getWorkerToWorkerInfo().put(newWorker.getWorkerID(), newWorker);
				ProcessManager.getInstance().getWorkerToProcesses().put(newWorker.getWorkerID(), new ArrayList<ProcessInfoWrapper>());
				workerService.start();
			}
		} catch (IOException e) {
			System.out.println("Server Network Exception");
			e.printStackTrace();
			ProcessManager.getInstance().closeManager();
		}
	}

	public void stopServer(){
		stop = true;
	}
}
