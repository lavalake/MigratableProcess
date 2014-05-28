package edu.cmu.ds15640.core;

import java.util.ArrayList;

public class WorkerCommand {
	private CommandType type;
	private MigratableProcess migratableProcess;
	//The targetWorkerID is for the target worker the process migrate to
	private int targetWorkerID;
	private ArrayList<StatusType> status;
	//The processID is for the process that has already finished
	private ArrayList<Integer> processID;

	public WorkerCommand(CommandType type, MigratableProcess mp, int id, ArrayList<StatusType> status, ArrayList<Integer> pID) {
		this.type = type;
		migratableProcess = mp;
		targetWorkerID = id;
		this.status = status;
		processID = pID;
	}

	public CommandType getType() {
		return type;
	}
	
	public MigratableProcess getMigratableProcess(){
		return migratableProcess;
	}
	
	public ArrayList<StatusType> getStatus(){
		return status;
	}
	
	public ArrayList<Integer> getProcessID(){
		return processID;
	}
	
	public void setTargetWorkerID(int id){
		targetWorkerID = id;
	}
	
	public int getTargetWorkerID(){
		return targetWorkerID;
	}
}
