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
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				ReplyCommand replyCommand = (ReplyCommand) ois.readObject();
				handleReply(replyCommand, socket);
			}
		} catch (IOException e) {
			System.out.println("Unexpected IOException");
			e.printStackTrace();
			System.exit(0);
		} catch (ClassNotFoundException e) {
			System.out.println("Unexpected Class!");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void handleReply(ReplyCommand replyCommand, Socket socket) {
		switch (replyCommand.getType().name().toLowerCase()) {
		case "join":
			String IPAddress = socket.getInetAddress().toString();
			int port = socket.getPort();
			WorkerInfo newWorker = new WorkerInfo(workerID++, IPAddress, port, socket);
			ProcessManager.getInstance().getWorkerToWorkerInfo().put(newWorker.getWorkerID(), newWorker);
			ProcessManager.getInstance().getWorkerToProcesses().put(newWorker.getWorkerID(), new ArrayList<MigratableProcess>());
			break;
		case "migrateto":
			MigratableProcess process = replyCommand.getMigratableProcess();
			int workerID = replyCommand.getTargetWorkerID();
			Socket s = ProcessManager.getInstance().getWorkerToWorkerInfo().get(workerID).getSocket();
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(s.getOutputStream());
				SendCommand sc = new SendCommand(CommandType.MIGRATESTART, process);
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
		case "info":
			
//			TODO array list
			
//			StatusType status = replyCommand.getStatus();
//			if(status == StatusType.FINISHED){
//				int processID = replyCommand.getProcessID();
//				ProcessManager.getInstance().updateProcessStatus(processID, status);
//			}
//			break;
		default:
			System.out.println("Unexpected Command: " + replyCommand.getType().name());
			break;
		}
	}

	public void setStop(boolean s){
		stop = s;
	}
	
}
