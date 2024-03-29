package edu.cmu.ds15640.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import edu.cmu.ds15640.command.CommandType;
import edu.cmu.ds15640.command.MasterCommand;
import edu.cmu.ds15640.command.WorkerCommand;
import edu.cmu.ds15640.process.MigratableProcess;


/**
 * ProcessWorker serves as the slave client. It accepts master's ip and port number in command line and 
 * create socket connection. After that, it accepts the command from master and doing process. It will print
 * some status information during work
 * 
 * @author Xincheng Liu
 * @author Hao Ge
 * 
 * */

public class ProcessWorker {

	private String host;
	private int port;
	private Socket socket;
	private int workID;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	private HashMap<Integer, MigratableProcess> currentMap;

	@SuppressWarnings("rawtypes")
	private Class processClass;
	private Thread t;

	private volatile boolean stop = false;

	public ProcessWorker(String host, int port) {
		this.host = host;
		this.port = port;
		this.currentMap = new HashMap<Integer, MigratableProcess>();
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	/**
	 * send method writes object into output stream
	 * */
	
	private void sendToManager(WorkerCommand sc) {
		try {
			oos.writeObject(sc);
		} catch (IOException e) {
			System.err.println("fail to send manager");
		}
	}

	/**
	 * create a thread to run the process
	 * */
	
	private void runProcess(MigratableProcess mp) {
		System.out.println("start process");
		t = new Thread(mp);
		t.start();
		currentMap.put(mp.getProcessID(), mp);
	}
	
	/**
	 * use reflection to handle start command
	 * Find process based on class name
	 * **/

	private void handleStartCommand(MasterCommand masterCommand) {
		WorkerCommand startCommand;
		try {
			processClass = ProcessWorker.class.getClassLoader().loadClass(
					masterCommand.getProcessName());
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Constructor constructor = processClass
					.getConstructor(String[].class);
			Object[] passed = { masterCommand.getArgs() };
			MigratableProcess process = (MigratableProcess) constructor
					.newInstance(passed);
			process.setProcessID(masterCommand.getProcessID());
			runProcess(process);

			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.RUNNING, masterCommand.getProcessID());
		} catch (ClassNotFoundException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			System.err.println("Process class not found");
		} catch (SecurityException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			System.err.println("Security Exception");
		} catch (NoSuchMethodException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			System.err.println("Method not found");
		} catch (IllegalArgumentException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			System.err.println("Illegal Argument");
		} catch (InstantiationException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			System.err.println("Cannot create instance");
		} catch (IllegalAccessException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			System.err.println("Illegal Access");
		} catch (InvocationTargetException e) {
			startCommand = new WorkerCommand(CommandType.STARTRETURN,
					StatusType.FAIL, masterCommand.getProcessID());
			System.err.println("Invalid Argument");
			//e.printStackTrace();
		}
		sendToManager(startCommand);
	}

	/**
	 * return current running process and the status 
	 * respond to heartbeating
	 * 
	 * **/
	
	private void handleInfoCommand() {
		ArrayList<StatusType> statusList = new ArrayList<StatusType>();
		ArrayList<Integer> pidList = new ArrayList<Integer>();
		for (int i : currentMap.keySet()) {
			MigratableProcess mp = currentMap.get(i);
			if (mp.isComplete()) {
				mp.setStatus(StatusType.COMPLETED);
			}
			pidList.add(i);
			statusList.add(mp.getStatus());
		}
		WorkerCommand infoCommand = new WorkerCommand(CommandType.RETURNINFO,
				statusList, pidList);
		sendToManager(infoCommand);
	}
	
	/**
	 * Package the process into a command object and send back to server
	 * 
	 * **/

	private void handleMigrateCommand(MasterCommand masterCommand) {
		MigratableProcess mp = currentMap.get(masterCommand.getProcessID());
		mp.suspend();
		currentMap.remove(mp.getProcessID());
		WorkerCommand migrateCommand = new WorkerCommand(CommandType.MIGRATETO,
				mp, masterCommand.getTargetWorkerID(), workID);
		sendToManager(migrateCommand);
		System.out.println("finish migrate");
	}

	/**
	 * Accept the migrated process from master and resume the process 
	 * 
	 * */
	
	private void handleMigrateStartCommand(MasterCommand masterCommand) {
		MigratableProcess mp = masterCommand.getMigratableProcess();
		System.out.println("accept migrated process and start to run the process");
		runProcess(mp);
	}
	
	/**
	 * Accept ID from server
	 * */

	private void handleAssignIDCommand(MasterCommand masterCommand) {
		workID = masterCommand.getWorkerID();
	}
	
	/**
	 * Permanently kill a process. Remove from local Hashmap
	 * 
	 * */

	private void handleKillCommand(MasterCommand masterCommand) {
		System.out.println(masterCommand.getProcessID());
		MigratableProcess mp = currentMap.remove(masterCommand.getProcessID());
		mp.suspend();
		System.out.println("Successfully kill the process");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			String host = args[0];
			int port = Integer.parseInt(args[1]);
			ProcessWorker worker = new ProcessWorker(host, port);
			
			/**
			 *  Build the socket connection
			 * */
			
			try {
				worker.socket = new Socket(host, port);
			} catch (IOException e) {
				worker.stop = true;
				System.err.println("cannot create socket");
				System.exit(0);
			}

			System.out.println("Create socket");
			
			/**
			 * Create the input and output stream
			 * */

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

			
			/**
			 * Wait for the command from master and handle each 
			 * command
			 * 
			 * */
			
			while (!worker.stop) {
				try {
					MasterCommand masterCommand = (MasterCommand) worker.ois
							.readObject();
					switch (masterCommand.getType().name().toLowerCase()) {
					case "assignid":
						worker.handleAssignIDCommand(masterCommand);
						break;
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
						System.out
								.println("accept migrate command, target work"
										+ masterCommand.getTargetWorkerID());
						worker.handleMigrateCommand(masterCommand);
						break;
					case "kill":
						worker.handleKillCommand(masterCommand);
						break;
					default:
						System.out.println("Wrong command: "
								+ masterCommand.getType().name());
						break;
					}
				} catch (IOException e) {
					worker.stop = true;
					System.err.println("Cannot read from master");
				} catch (ClassNotFoundException e) {
					System.err.println("class not found");
				}
			}

			try {
				worker.oos.close();
				worker.ois.close();
				worker.socket.close();
				System.out.println("Process Worker closed");
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("Please enter the host ip and port number");
		}

	}
}
