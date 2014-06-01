package edu.cmu.ds15640.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import edu.cmu.ds15640.command.CommandType;
import edu.cmu.ds15640.command.MasterCommand;
import edu.cmu.ds15640.command.WorkerCommand;
import edu.cmu.ds15640.process.MigratableProcess;

/**
 * Service for each connection - handle all sending and reading for connection
 * 
 * @author Xincheng Liu
 * @author Hao Ge
 */
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
			MigratableProcess process = workerCommand.getMigratableProcess();
			int sourceWorkerID = workerCommand.getSourceWorkerID();
			int targetWorkerID = workerCommand.getTargetWorkerID();
			try {
				int processID = process.getProcessID();
				MasterCommand sc = new MasterCommand(CommandType.MIGRATESTART,
						process);
				ArrayList<ProcessInfoWrapper> arr = ProcessManager
						.getInstance().getWorkerToProcesses()
						.get(sourceWorkerID);
				String processName = "";
				for (int i = 0; i < arr.size(); i++) {
					if (arr.get(i).getProcessID() == processID) {
						processName = arr.get(i).getProcessName();
						arr.remove(i);
						break;
					}
				}
				ProcessInfoWrapper wrapper = new ProcessInfoWrapper(
						process.getProcessID(), processName, StatusType.RUNNING);
				ProcessManager.getInstance().getWorkerToProcesses()
						.get(targetWorkerID).add(wrapper);
				ProcessManager.getInstance().getWorkerToWorkerInfo()
						.get(targetWorkerID).getWorkerService()
						.writeToWorker(sc);
			} catch (IOException e) {
				System.out.println("Worker " + targetWorkerID
						+ "is failed: Cannot migrate process +"
						+ process.getProcessID());
				ProcessManager.getInstance().getWorkerToWorkerInfo()
						.get(targetWorkerID).getWorkerService()
						.stopWorker(targetWorkerID);
			}
			break;
		case "returninfo":
			ArrayList<Integer> processes = workerCommand.getProcessID();
			ArrayList<StatusType> statuses = workerCommand.getStatusList();
			for (int i = 0; i < processes.size(); i++) {
				if (statuses.get(i) == StatusType.COMPLETED) {
					int processID = processes.get(i);
					ProcessManager.getInstance().updateProcessStatus(processID,
							StatusType.COMPLETED);
				} else if (statuses.get(i) == StatusType.FAIL) {
					int processID = processes.get(i);
					ProcessManager.getInstance().updateProcessStatus(processID,
							StatusType.FAIL);
				}
			}
			break;
		case "startreturn":
			int processID = workerCommand.getProcessId();
			StatusType st = workerCommand.getStatus();
			if (st == StatusType.FAIL) {
				ProcessManager.getInstance().updateProcessStatus(processID,
						StatusType.FAIL);
				System.out.println("The process: " + processID
						+ " fails to start");
			}
			break;
		default:
			System.out.println("Unexpected Command: "
					+ workerCommand.getType().name());
			break;
		}
	}

	public void run() {
		WorkerCommand workerCommand;
		MasterCommand masterCommand = new MasterCommand(CommandType.ASSIGNID,
				workerID);
		try {
			writeToWorker(masterCommand);
		} catch (IOException e1) {
			e1.printStackTrace();
			stopWorker(workerID);
		}
		while (!stop) {
			try {
				workerCommand = (WorkerCommand) ois.readObject();
				handleReply(workerCommand);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("The workerID: " + workerID + " is failed");
				e.printStackTrace();
				stopWorker(workerID);
			}
		}
	}

	public void writeToWorker(MasterCommand mc) throws IOException {
		oos.writeObject(mc);
	}

	public void stopWorker(int workerID) {
		stop = true;
		for (int i = 0; i < ProcessManager.getInstance().getWorkerToProcesses()
				.get(workerID).size(); i++) {
			ProcessManager.getInstance().updateProcessStatus(
					ProcessManager.getInstance().getWorkerToProcesses()
							.get(workerID).get(i).getProcessID(),
					StatusType.FAIL);
		}
		ProcessManager.getInstance().getWorkerToWorkerInfo().get(workerID)
				.setStatus(StatusType.MACHINEFAIL);
		;
	}
}
