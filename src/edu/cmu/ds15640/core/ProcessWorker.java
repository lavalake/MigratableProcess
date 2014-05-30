package edu.cmu.ds15640.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ProcessWorker {

	private String host;
	private int port;
	private Socket socket;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	private ArrayList<StatusType> statusList;
	private ArrayList<Integer> pidList;
	private ArrayList<MigratableProcess> processList;

	private HashMap<Integer, MigratableProcess> map;

	private Class processClass;
	private Thread t;

	private volatile boolean stop = false;

	public ProcessWorker(String host, int port) {
		this.host = host;
		this.port = port;
		this.statusList = new ArrayList<StatusType>();
		this.pidList = new ArrayList<Integer>();
		this.map = new HashMap<Integer, MigratableProcess>();
	}

	private void sendToManager(WorkerCommand sc) {
		try {
			oos.writeObject(sc);
		} catch (IOException e) {
			System.err.println("fail to send manager");
		}
	}
	
	private void runProcess (MigratableProcess mp) {
		System.out.println("start process");
		t = new Thread(mp);
		t.start();
		System.out.println("end process");
	}
	
	private void handleStartCommand(MasterCommand masterCommand) {
		WorkerCommand startCommand;
		try {
			processClass = ProcessWorker.class.getClassLoader().loadClass(
					masterCommand.getProcessName());
			Constructor constructor = processClass.getConstructor(String[].class);
			Object[] passed = {masterCommand.getArgs()};
			MigratableProcess process = (MigratableProcess) constructor.newInstance(passed);
			
			runProcess(process);
			
			process.setProcessID(masterCommand.getProcessID());

			//processList.add(process);
			pidList.add(process.getProcessID());
			map.put(masterCommand.getProcessID(), process);

			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.RUNNING, masterCommand.getProcessID());
		} catch (ClassNotFoundException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			System.err.println("Process class not found");
			e.printStackTrace();
		} catch (SecurityException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			System.err.println("Method not found");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			System.err.println("Illegal Argument");
			e.printStackTrace();
		} catch (InstantiationException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			System.err.println("Illegal Access");
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			e.printStackTrace();
		}

		sendToManager(startCommand);

	}

	private void handleInfoCommand() {
		WorkerCommand infoCommand = new WorkerCommand(CommandType.RETURNINFO,
				statusList, pidList);
		sendToManager(infoCommand);
	}

	private void handleMigrateCommand(MasterCommand masterCommand) {
		MigratableProcess mp = map.get(masterCommand.getProcessID());
		mp.suspend();
		//TODO update list
		WorkerCommand migrateCommand = new WorkerCommand(CommandType.MIGRATETO,
				mp, masterCommand.getTargetWorkerID());
		sendToManager(migrateCommand);
		System.out.println("finnish migrate");
	}
	
	private void handleMigrateStartCommand (MasterCommand masterCommand) {
		MigratableProcess mp = masterCommand.getMigratableProcess();
		runProcess(mp);
		//TODO update list
		System.out.println("accept migration and start to run");
	}

	public static void main(String[] args) {
	/*
		ProcessWorker worker = new ProcessWorker("localhost", 8888);
		String[] arr = {"CHAPTER", "/Users/haoge/git/MigratableProcess/lifeofjesus.txt", "/Users/haoge/git/MigratableProcess/empty.txt"};
		MasterCommand mc = new MasterCommand(CommandType.START, "edu.cmu.ds15640.core.GrepProcess", 1001, arr);
		worker.handleStartCommand(mc);
		
  */
		if (args.length == 2) {
			String host = args[0];
			int port = Integer.parseInt(args[1]);
			ProcessWorker worker = new ProcessWorker(host, port);

			try {
				worker.socket = new Socket(host, port);
			} catch (IOException e) {
				worker.stop = true;
				System.err.println("cannot create socket");
				e.printStackTrace();
			}

			System.out.println("Create socket");

			try {
				worker.oos = new ObjectOutputStream(
						worker.socket.getOutputStream());
				worker.ois = new ObjectInputStream(
						worker.socket.getInputStream());
			} catch (IOException e) {
				worker.stop = true;
				System.err.println("cannot create stream");
				e.printStackTrace();
			}
			
			
			while (!worker.stop) {
				try {
					MasterCommand masterCommand = (MasterCommand) worker.ois.readObject();
					switch (masterCommand.getType().name().toLowerCase()) {
					case "start":
						worker.handleStartCommand(masterCommand);
						break;
					case "migratestart":
						worker.handleMigrateStartCommand(masterCommand);
						break;
					case "getinfo":
						worker.handleInfoCommand();
						break;
					case "migrate":
						worker.handleMigrateCommand(masterCommand);
						break;
					default:
						System.out.println("Wrong command: "
								+ masterCommand.getType().name());
						break;
					}
				} catch (IOException e) {
					System.err.println("Cannot read from master");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					System.err.println("class not found");
				}
			}
			try {
				worker.oos.close();
				worker.ois.close();
				worker.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("Please enter the host ip and port number");
		}
    
	}
}
