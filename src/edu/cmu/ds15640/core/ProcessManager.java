package edu.cmu.ds15640.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.cmu.ds15640.command.CommandType;
import edu.cmu.ds15640.command.MasterCommand;

public class ProcessManager {
	private static int port;
	private static ProcessManager processManager;
	private final static Lock mutex = new ReentrantLock();
	private ConcurrentHashMap<Integer, WorkerInfo> workerToWorkerInfo;
	private ConcurrentHashMap<Integer, ArrayList<ProcessInfoWrapper>> workerToProcesses;
	private int processIDCounter = 10000;
	private RunnableServer runnableServer;
	private RunnableHeartBeat runnableHeartBeat;

	private ProcessManager() {
		workerToProcesses = new ConcurrentHashMap<Integer, ArrayList<ProcessInfoWrapper>>();
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
		case "kill":
			handleKillCommand(strs);
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

	private void handleKillCommand(String[] strs) {
		if(strs == null || strs.length != 2){
			System.out.println("Wrong arguments, type 'help' for more information");
			return;
		}
		int processID;
		try {
			processID = Integer.parseInt(strs[1]);
		} catch (Exception e) {
			System.out.println("workerID or processID is not an integer");
			return;
		}
		if(getProcessWithID(processID) == null){
			System.out.println("The process: " + processID + " is not exist");
			return;
		}
		int workerID = -1;
		ProcessInfoWrapper wrapper = null;
		for(int id: workerToProcesses.keySet()){
			if((wrapper = getProcessOnMachineWithID(processID, id)) != null){
				workerID = id;
				break;
			}
		}
		if(workerID == -1 || wrapper == null){
			System.out.println("The process: " + processID + " is not exist");
			return;
		}
		if(wrapper.getStatus() == StatusType.COMPLETED || wrapper.getStatus() == StatusType.FAIL 
				|| workerToWorkerInfo.get(workerID).getStatus() == StatusType.MACHINEFAIL){
			System.out.println("Process: " + processID + " is removed");
			removeProcess(workerID, processID);
			return;
		}
		try {
			MasterCommand sc = new MasterCommand(CommandType.KILL, processID);
			workerToWorkerInfo.get(workerID).getWorkerService().writeToWorker(sc);
		} catch (IOException e) {
			System.out.println("Worker: " + workerID + " is failed");
			System.out.println(e.toString());
			removeWorker(workerID);
			return;
		}
		System.out.println("Process: " + processID + " is removed");
		removeProcess(workerID, processID);
	}

	private void handleMigrateCommand(String[] strs) {
		if(strs == null || strs.length != 4){
			System.out.println("Wrong arguments, type 'help' for more information");
			return;
		}
		int sourceWorkerID, targetWorkerID, processID;
		try {
			processID = Integer.parseInt(strs[1]);
			sourceWorkerID = Integer.parseInt(strs[2]);
			targetWorkerID = Integer.parseInt(strs[3]);
		} catch (Exception e) {
			System.out.println("workerID or processID is not an integer");
			return;
		}
		if(!workerToWorkerInfo.containsKey(sourceWorkerID)){
			System.out.println("The source worker: " + sourceWorkerID + " is not exist");
			return;
		}
		if(!workerToWorkerInfo.containsKey(targetWorkerID)){
			System.out.println("The target worker: " + targetWorkerID + " is not exist");
			return;
		}
		if(getProcessWithID(processID) == null){
			System.out.println("The process: " + processID + " is not exist");
			return;
		}
		if(getProcessOnMachineWithID(processID, sourceWorkerID) == null){
			System.out.println("The process: " + processID + " is not exist on machine: " + sourceWorkerID);
			return;
		}
		try {
			MasterCommand migrateCommand = new MasterCommand(CommandType.MIGRATE, targetWorkerID, processID);
			workerToWorkerInfo.get(sourceWorkerID).getWorkerService().writeToWorker(migrateCommand);
		} catch (IOException e) {
			System.out.println("Worker: " + sourceWorkerID + " is failed");
			System.out.println(e.toString());
			removeWorker(sourceWorkerID);
			return;
		}
	}

	private void handleStartCommand(String[] strs) {
		if(strs == null || strs.length < 3){
			System.out.println("Wrong arguments, type 'help' for more information");
		}
		int workerID = -1;
		try {
			workerID = Integer.parseInt(strs[1]);
		} catch (Exception e) {
			System.out.println("The workerID is not an integer");
			return;
		}
		if(!workerToWorkerInfo.containsKey(workerID)){
			System.out.println("The worker: " + workerID + " is not exist");
			return;
		}
		String processName = strs[2];
		String[] args = new String[strs.length - 3];
		for(int i = 3; i < strs.length; i++){
			args[i - 3] = strs[i];
		}
		try {
			Class processClass = ProcessManager.class.getClassLoader().loadClass(processName);
		} catch (ClassNotFoundException e) {
			System.out.println("Process: " + processName + " is not exist");
			return;
		}
		try {
			MasterCommand sc = new MasterCommand(CommandType.START, processName, processIDCounter, args);
			ProcessInfoWrapper wrapper = new ProcessInfoWrapper(processIDCounter, processName, StatusType.RUNNING);
			workerToProcesses.get(workerID).add(wrapper);
			processIDCounter++;
			workerToWorkerInfo.get(workerID).getWorkerService().writeToWorker(sc);
		} catch (IOException e) {
			System.out.println("Worker: " + workerID + " is failed");
			System.out.println(e.toString());
			removeWorker(workerID);
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
		sb.append("kill:    PROCESSID     --remove the process\n");
		sb.append("start:   WORKERID PROCESSNAME ARG...     --start the process with args...\n");
		sb.append("migrate: PROCESSID WORKERID1 WORKERID2     --migrate the process between workers");
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
		ProcessInfoWrapper wrap = getProcessWithID(processID);
		if(wrap == null){
			System.out.println("fail to update status with processID: " + processID);
			return;
		}
		wrap.setStatus(status);
	}
	
	private ProcessInfoWrapper getProcessOnMachineWithID(int processID, int sourceWorkerID) {
		ArrayList<ProcessInfoWrapper> list = workerToProcesses.get(sourceWorkerID);
		if(list == null){
			return null;
		}
		for(ProcessInfoWrapper wrap: list){
			if(wrap.getProcessID() == processID){
				return wrap;
			}
		}
		return null;
	}

	private void removeWorker(int workerID) {
		workerToWorkerInfo.get(workerID).getWorkerService().stopWorker(workerID);
	}

	private void removeProcess(int workerID, int processID) {
		int index = -1;
		for(int i = 0; i < workerToProcesses.get(workerID).size(); i++){
			if(workerToProcesses.get(workerID).get(i).getProcessID() == processID){
				index = i;
				break;
			}
		}
		if(index != -1){
			workerToProcesses.get(workerID).remove(index);
		}
	}

	private ProcessInfoWrapper getProcessWithID(int processID) {
		for(int i : workerToProcesses.keySet()){
			ArrayList<ProcessInfoWrapper> list = workerToProcesses.get(i);
			for(ProcessInfoWrapper wrap: list){
				if(wrap.getProcessID() == processID){
					return wrap;
				}
			}
		}
		return null;
	}
	
	public void closeManager(){
		for(int id: workerToWorkerInfo.keySet()){
			removeWorker(id);
		}
		runnableServer.stopServer();
		runnableHeartBeat.stopHeartBeat();
		System.exit(0);
	}

	public ConcurrentHashMap<Integer, WorkerInfo> getWorkerToWorkerInfo() {
		return workerToWorkerInfo;
	}

	public ConcurrentHashMap<Integer, ArrayList<ProcessInfoWrapper>> getWorkerToProcesses() {
		return workerToProcesses;
	}
}
