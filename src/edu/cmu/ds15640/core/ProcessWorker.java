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

	private volatile boolean stop = false;

	public ProcessWorker(String host, int port) {
		this.host = host;
		this.port = port;
	}

	private void sendToManager(WorkerCommand sc) {
		try {
			oos.writeObject(sc);
		} catch (IOException e) {

		}
	}

	private MasterCommand receiveFromManager() {
		MasterCommand masterCommand = null;
		try {
			masterCommand = (MasterCommand) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Cannot read from master");
			e.printStackTrace();
		}
		return masterCommand;
	}

	private void handleStartCommand(MasterCommand masterCommand) {
		WorkerCommand startCommand;
		try {
			processClass = ProcessWorker.class.getClassLoader().loadClass(
					masterCommand.getProcessName());
			Constructor constructor = processClass
					.getConstructor(new Class[] { String[].class });
			MigratableProcess process = (MigratableProcess) constructor
					.newInstance(masterCommand.getArgs());
			process.setProcessID(masterCommand.getProcessIDCounter());

			processList.add(process);
			pidList.add(process.getProcessID());
			map.put(masterCommand.getProcessIDCounter(), process);

			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.RUNNING);
		} catch (ClassNotFoundException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL);
			System.err.println("Process class not found");
			e.printStackTrace();
		} catch (SecurityException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL);
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL);
			System.err.println("Method not found");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL);
			System.err.println("Illegal Argument");
			e.printStackTrace();
		} catch (InstantiationException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL);
			System.err.println("Illegal Access");
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL);
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
		 MigratableProcess mp = map.get(masterCommand.getProcessIDCounter());
		 WorkerCommand migrateCommand = new WorkerCommand (CommandType.MIGRATETO, mp);
		 sendToManager(migrateCommand);
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			String host = args[0];
			int port = Integer.parseInt(args[1]);
			ProcessWorker worker = new ProcessWorker(host, port);

			try {
				worker.socket = new Socket(host, port);
			} catch (IOException e) {
				System.err.println("cannot create socket");
				e.printStackTrace();
			}

			try {
				worker.ois = new ObjectInputStream(
						worker.socket.getInputStream());
				worker.oos = new ObjectOutputStream(
						worker.socket.getOutputStream());
			} catch (IOException e) {
				System.err.println("cannot create stream");
				e.printStackTrace();
			}

			WorkerCommand joinCommand = new WorkerCommand(CommandType.JOIN);
			worker.sendToManager(joinCommand);

			System.out.println("Successfully connect to the server");

			while (!worker.stop) {
				MasterCommand masterCommand = worker.receiveFromManager();
				switch (masterCommand.getType().name().toLowerCase()) {
				case "start":
					worker.handleStartCommand(masterCommand);
					break;
				case "info":
					worker.handleInfoCommand();
					break;
				case "migrate":
					worker.handleMigrateCommand(masterCommand);
					break;
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
