package edu.cmu.ds15640.core;

import java.lang.reflect.Constructor;
import java.net.Socket;

public class ProcessWorker {
	private String host;
	private int port;

	private Socket socket;


	public ProcessWorker(String host, int port){

	}

	
	public static void main(String[] args) {
		/*
		processClass = ProcessManager.class.getClassLoader().loadClass(processName);
		Constructor constructor = processClass.getConstructor(new Class[]{String[].class});
		MigratableProcess process = (MigratableProcess) constructor.newInstance(args);
		process.setProcessID(processIDCounter++);*/
	}

}
