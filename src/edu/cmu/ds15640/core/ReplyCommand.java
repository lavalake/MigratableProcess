package edu.cmu.ds15640.core;

public class ReplyCommand {
	private CommandType type;
	private MigratableProcess migratableProcess;
	//The targetWorkerID is for the target worker the process migrate to
	private int targetWorkerID;
	private StatusType status;
	//The processID is for the process that has already finished
	private int processID;

	public ReplyCommand(CommandType type, MigratableProcess mp, int id, StatusType status, int pID) {
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
	
	public StatusType getStatus(){
		return status;
	}
	
	public int getProcessID(){
		return processID;
	}
	
	public void setTargetWorkerID(int id){
		targetWorkerID = id;
	}
	
	public int getTargetWorkerID(){
		return targetWorkerID;
	}
}
