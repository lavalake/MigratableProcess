package edu.cmu.ds15640.core;

public class MasterCommand {
	
	private CommandType type;
	private String processName;
	private int processID;
	private MigratableProcess migratableProcess;
	private String[] args;
	
	public MasterCommand(CommandType ct){
		type = ct;
		migratableProcess = null;
	}
	
	public MasterCommand(CommandType ct, MigratableProcess mp){
		type = ct;
		migratableProcess = mp;
	}

	public MasterCommand(CommandType ct, String processName,
			int processIDCounter, String[] args) {
		type = ct;
		this.processName = processName;
		this.processID = processIDCounter;
		this.args = args;
	}
	
	public CommandType getType() {
		return type;
	}

	public String getProcessName() {
		return processName;
	}

	public int getProcessID() {
		return processID;
	}

	public MigratableProcess getMigratableProcess() {
		return migratableProcess;
	}

	public String[] getArgs() {
		return args;
	}
	
	
}