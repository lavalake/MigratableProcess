package edu.cmu.ds15640.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ProcessWorker {

	private String host;
	private int port;
	private Socket socket;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	private volatile boolean stop = false;
	
	public ProcessWorker(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void sendToManager(SendCommand sc) {
		try {
			oos.writeObject(sc);
		} catch (IOException e){
			
		}
	}

	public static void main(String[] args) {
		/*
		processClass = ProcessManager.class.getClassLoader().loadClass(processName);
		Constructor constructor = processClass.getConstructor(new Class[]{String[].class});
		MigratableProcess process = (MigratableProcess) constructor.newInstance(args);
		process.setProcessID(processIDCounter++);*/
		
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
				worker.ois = new ObjectInputStream(worker.socket.getInputStream());
				worker.oos = new ObjectOutputStream(worker.socket.getOutputStream());
			} catch (IOException e) {
				System.err.println("cannot create stream");
				e.printStackTrace();
			}
			

			SendCommand joinCommand = new SendCommand(CommandType.JOIN);
			worker.sendToManager(joinCommand);
			
			System.out.println("Successfully connect to the server");
			
			while (!worker.stop) {
				
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
