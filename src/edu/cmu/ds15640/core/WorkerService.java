package edu.cmu.ds15640.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class WorkerService extends Thread {
	Socket s;
	int workerID;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	boolean stop = false;
	
	public WorkerService(Socket socket, int workerID) {
		s = socket;
		this.workerID = workerID;
		try {
			ois = new ObjectInputStream(s.getInputStream());
			oos = new ObjectOutputStream(s.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void handleReply(WorkerCommand workerCommand) {
		switch (workerCommand.getType().name().toLowerCase()) {
		case "migrateto":
			System.out.println("accept migrateto command");
			MigratableProcess process = workerCommand.getMigratableProcess();
			int sourceWorkerID = workerCommand.getSourceWorkerID();
			int workerID = workerCommand.getTargetWorkerID();
			try {
				MasterCommand sc = new MasterCommand(CommandType.MIGRATESTART, process);
				//Update map
				ProcessManager.getInstance().getWorkerToProcesses().remove(sourceWorkerID);
				ProcessInfoWrapper wrapper = new ProcessInfoWrapper(process.getProcessID(), StatusType.RUNNING);
				ProcessManager.getInstance().getWorkerToProcesses().get(workerID).add(wrapper);
				oos.writeObject(sc);
			} catch (IOException e) {
				System.out.println("Worker "+ workerID + "is failed: Cannot migrate process +" + process.getProcessID());
				ProcessManager.getInstance().getWorkerToWorkerInfo().get(workerID).getWorkerService().stopWorker(workerID);
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
	
	public void run(){
		WorkerCommand workerCommand;
		MasterCommand masterCommand = new MasterCommand(CommandType.ASSIGNID, workerID);
		try {
			writeToWorker(masterCommand);
		} catch (IOException e1) {
			e1.printStackTrace();
			stopWorker(workerID);
		}
		while(!stop){
			try {
				workerCommand = (WorkerCommand) ois.readObject();
				handleReply(workerCommand);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				stopWorker(workerID);
			}
		}
	}
	
	public void writeToWorker(MasterCommand mc) throws IOException{
		oos.writeObject(mc);
	}
	
	public void stopWorker(int workerID){
		stop = true;
		ProcessManager.getInstance().getWorkerToProcesses().remove(workerID);
		ProcessManager.getInstance().getWorkerToWorkerInfo().remove(workerID);
	}
}
