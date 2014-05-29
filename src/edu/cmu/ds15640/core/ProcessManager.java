package edu.cmu.ds15640.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProcessManager {

	private static int port;
	private static ProcessManager processManager;
	private final static Lock mutex = new ReentrantLock();
	private ConcurrentHashMap<Integer, WorkerInfo> workerToWorkerInfo;
	private ConcurrentHashMap<Integer, ArrayList<MigratableProcess>> workerToProcesses;
	private int processIDCounter = 10000;
	private RunnableServer runnableServer;
	private RunnableHeartBeat runnableHeartBeat;

	private ProcessManager() {
		workerToProcesses = new ConcurrentHashMap<Integer, ArrayList<MigratableProcess>>();
		workerToWorkerInfo = new ConcurrentHashMap<Integer, WorkerInfo>();
	}

	public static ProcessManager getInstance() {
		synchronized (mutex) {
			if (processManager == null) {
				processManager = new ProcessManager();
			}
			return processManager;
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("usage: java ProcessManager <PORT>");
			System.exit(0);
		}
		port = Integer.parseInt(args[0]);
		ProcessManager.getInstance().startServer(port);
		ProcessManager.getInstance().startConsole();
	}

	private void startConsole() {
		System.out
				.println("Welcome to the processManager, type 'help' for more information");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		while (true) {
			System.out.print("> ");
			try {
				line = br.readLine();
			} catch (IOException e) {
				System.out.println(e.toString());
				closeManager();
			}
			handleCommand(line);
		}
	}

	private void handleCommand(String line) {
		if (line == null) {
			return;
		}
		String[] strs = line.split(" ");
		String command = strs[0];
		switch (command.toLowerCase()) {
		case "help":
			handleHelpCommand();
			break;
		case "ls":
			handleLsCommand();
			break;
		case "ps":
			handlePsCommand();
			break;
		case "start":
			handleStartCommand(strs);
			break;
		case "migrate":
			handleMigrateCommand(strs);
			break;
		default:
			System.out.println("Wrong command, type 'help' for more information");
			break;
		}
	}

	private void handleMigrateCommand(String[] strs) {
		if(strs == null || strs.length != 4){
			System.out.println("Wrong arguments, type 'help' for more information");
		}
		int sourceWorkerID, targetWorkerID, processID;
		try {
			processID = Integer.parseInt(strs[1]);
			sourceWorkerID = Integer.parseInt(strs[2]);
			targetWorkerID = Integer.parseInt(strs[3]);
		} catch (Exception e) {
			System.out.println(e.toString());
			return;
		}
		if(!workerToWorkerInfo.contains(sourceWorkerID)){
			System.out.println("The source worker: " + sourceWorkerID + " is not exist");
			return;
		}
		if(!workerToWorkerInfo.contains(targetWorkerID)){
			System.out.println("The target worker: " + targetWorkerID + " is not exist");
			return;
		}
		if(getProcessWithID(processID) == null){
			System.out.println("The processID: " + processID + " is not exist");
			return;
		}
		try {
			MasterCommand migrateCommand = new MasterCommand(CommandType.MIGRATE, targetWorkerID, processID);
			workerToWorkerInfo.get(sourceWorkerID).getWorkerService().writeToWorker(migrateCommand);
		} catch (IOException e) {
			System.out.println("Worker: " + sourceWorkerID + " is failed");
			System.out.println(e.toString());
			RemoveWorker(sourceWorkerID);
			return;
		}
	}

	private void RemoveWorker(int workerID) {
		workerToWorkerInfo.get(workerID).getWorkerService().stopWorker(workerID);
	}

	private MigratableProcess getProcessWithID(int processID) {
		for(int i : workerToProcesses.keySet()){
			ArrayList<MigratableProcess> list = workerToProcesses.get(i);
			for(MigratableProcess mp: list){
				if(mp.getProcessID() == processID){
					return mp;
				}
			}
		}
		return null;
	}

	private void handleStartCommand(String[] strs) {
		if(strs == null || strs.length < 3){
			System.out.println("Wrong arguments, type 'help' for more information");
		}
		int workerID = -1;
		try {
			workerID = Integer.parseInt(strs[1]);
		} catch (Exception e) {
			System.out.println(e.toString());
			return;
		}
		
		if(!workerToWorkerInfo.keySet().contains(workerID)){
			System.out.println("The worker: " + workerID + " is not exist");
			return;
		}
		String processName = strs[2];
		String[] args = new String[strs.length - 3];
		for(int i = 3; i < strs.length; i++){
			args[i - 3] = strs[i];
		}
		try {
			//Validate the processName here
			MasterCommand sc = new MasterCommand(CommandType.START, processName, processIDCounter, args);
			processIDCounter++;
			workerToWorkerInfo.get(workerID).getWorkerService().writeToWorker(sc);
		} catch (IOException e) {
			System.out.println("Worker: " + workerID + " is failed");
			System.out.println(e.toString());
			RemoveWorker(workerID);
			return;
		}
	}

	private void handlePsCommand() {
		System.out.println("All process information:");
		if(workerToProcesses.size() == 0){
			System.out.println("No process information");
			return;
		}
		for (int i : workerToProcesses.keySet()) {
			System.out.println("Worker " + workerToWorkerInfo.get(i).getWorkerID() + ":");
			for(int j = 0; j < workerToProcesses.get(i).size(); j++){
				System.out.println(workerToProcesses.get(i).get(j));
			}
		}
	}

	private void handleLsCommand() {
		System.out.println("All worker information:");
		if(workerToWorkerInfo.size() == 0){
			System.out.println("No worker information");
			return;
		}
		for (int i : workerToWorkerInfo.keySet()) {
			System.out.println(workerToWorkerInfo.get(i));
		}
	}

	private void handleHelpCommand() {
		StringBuilder sb = new StringBuilder();
		sb.append("help:    list all command information\n");
		sb.append("ls:      list all workers\n");
		sb.append("ps:      list all processes\n");
		sb.append("start:   WORKERID PROCESSNAME ARG... \n        start the process with args...\n");
		sb.append("migrate: PROCESSID WORKERID1 WORKERID2 \n        migrate the process between workers");
		System.out.println(sb);
	}

	private void startServer(int port) {
		runnableServer = new RunnableServer(port);
		Thread t1 = new Thread(runnableServer);
		t1.start();
		runnableHeartBeat = new RunnableHeartBeat();
		Thread t2 = new Thread(runnableHeartBeat);
		t2.start();
	}
	
	public void updateProcessStatus(int processID, StatusType status) {
		MigratableProcess mp = getProcessWithID(processID);
		if(mp == null){
			System.out.println("fail to update status with processID: " + processID);
		}
		mp.setStatus(status);
	}
	
	public void closeManager(){
		runnableServer.stopServer();
		runnableHeartBeat.stopHeartBeat();
		System.exit(0);
	}

	public ConcurrentHashMap<Integer, WorkerInfo> getWorkerToWorkerInfo() {
		return workerToWorkerInfo;
	}

	public ConcurrentHashMap<Integer, ArrayList<MigratableProcess>> getWorkerToProcesses() {
		return workerToProcesses;
	}
}
