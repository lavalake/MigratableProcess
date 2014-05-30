package edu.cmu.ds15640.core;

import java.io.Serializable;
import java.util.ArrayList;

public class WorkerCommand implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2215252029261060551L;
	private CommandType type;
	private MigratableProcess migratableProcess;
	//The targetWorkerID is for the target worker the process migrate to
	private int targetWorkerID;
	private StatusType status;
	private int processId;
	private ArrayList<StatusType> statusList;
	//The processID is for the process that has already finished
	private ArrayList<Integer> pidList;
	
	public WorkerCommand(CommandType ct) {
		this.type = ct;
		this.migratableProcess = null;
	}
	
	public WorkerCommand(CommandType ct, StatusType status, int pid) {
		this.type = ct;
		this.status = status;
		this.processId = pid;
	}

	public WorkerCommand (CommandType ct, ArrayList<StatusType> status, ArrayList<Integer> pID) {
		this.type = ct;
		this.statusList = status;
		this.pidList = pID;
	}
	
	public WorkerCommand(CommandType type, MigratableProcess mp, int targetWorkerID) {
		this.type = type;
		this.targetWorkerID = targetWorkerID;
		this.migratableProcess = mp;
	}

	public CommandType getType() {
		return type;
	}
	
	public MigratableProcess getMigratableProcess(){
		return migratableProcess;
	}
	
	public ArrayList<StatusType> getStatusList(){
		return statusList;
	}
	
	public StatusType getStatus(){
		return status;
	}
	
	public ArrayList<Integer> getProcessID(){
		return pidList;
	}
	
	public void setTargetWorkerID(int id){
		targetWorkerID = id;
	}
	
	public int getTargetWorkerID(){
		return targetWorkerID;
	}

	public int getProcessId() {
		return processId;
	}
}
