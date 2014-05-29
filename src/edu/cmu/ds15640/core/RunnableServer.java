package edu.cmu.ds15640.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class RunnableServer implements Runnable {

	public static int port;
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
				String IPAddress = socket.getInetAddress().toString();
				int port = socket.getPort();
				WorkerInfo newWorker = new WorkerInfo(workerID++, IPAddress, port, socket);
				ProcessManager.getInstance().getWorkerToWorkerInfo().put(newWorker.getWorkerID(), newWorker);
				ProcessManager.getInstance().getWorkerToProcesses().put(newWorker.getWorkerID(), new ArrayList<MigratableProcess>());
				workerServiceThread thread = new workerServiceThread(socket);
				thread.start();
			}
		} catch (IOException e) {
			System.out.println("Unexpected IOException");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void handleReply(WorkerCommand workerCommand, Socket socket) {
		switch (workerCommand.getType().name().toLowerCase()) {
		case "join":
			System.out.println("one worker join");
			String IPAddress = socket.getInetAddress().toString();
			int port = socket.getPort();
			WorkerInfo newWorker = new WorkerInfo(workerID++, IPAddress, port, socket);
			ProcessManager.getInstance().getWorkerToWorkerInfo().put(newWorker.getWorkerID(), newWorker);
			ProcessManager.getInstance().getWorkerToProcesses().put(newWorker.getWorkerID(), new ArrayList<MigratableProcess>());
			break;
		case "migrateto":
			MigratableProcess process = workerCommand.getMigratableProcess();
			int workerID = workerCommand.getTargetWorkerID();
			Socket s = ProcessManager.getInstance().getWorkerToWorkerInfo().get(workerID).getSocket();
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(s.getOutputStream());
				MasterCommand sc = new MasterCommand(CommandType.MIGRATESTART, process);
				oos.writeObject(sc);
			} catch (IOException e) {
				System.out.println("Worker "+ workerID + "is failed: Cannot migrate process +" + process.getProcessID());
			} finally{
				try {
					oos.close();
				} catch (IOException e) {
				}
			}
			break;
		case "returninfo":
			ArrayList<Integer> processes = workerCommand.getProcessID();
			ArrayList<StatusType> statuses = workerCommand.getStatusList();
			for(int i = 0; i < processes.size(); i++){
				if(statuses.get(i) == StatusType.FINISHED){
					int processID = processes.get(i);
					ProcessManager.getInstance().updateProcessStatus(processID, StatusType.FINISHED);
				}else if(statuses.get(i) == StatusType.FAIL){
					int processID = processes.get(i);
					ProcessManager.getInstance().updateProcessStatus(processID, StatusType.FAIL);
				}
			}
			break;
		case "startreturn":
			int processID = workerCommand.getProcessId();
			StatusType st = workerCommand.getStatus();
			if(st == StatusType.FAIL){
				ProcessManager.getInstance().updateProcessStatus(processID, StatusType.FAIL);
				System.out.println("The process: " + processID + " fails to start");
			}
			break;
		default:
			System.out.println("Unexpected Command: " + workerCommand.getType().name());
			break;
		}
	}

	public void setStop(boolean s){
		stop = s;
	}
	
	public class workerServiceThread extends Thread{
		Socket s;
		ObjectInputStream ois;
		ObjectOutputStream oos;
		
		public workerServiceThread(Socket socket) {
			s = socket;
			try {
				ois = new ObjectInputStream(s.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run(){
			while(true){
				WorkerCommand workerCommand;
				try {
					workerCommand = (WorkerCommand) ois.readObject();
					System.out.println("read something");
					handleReply(workerCommand, s);
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
