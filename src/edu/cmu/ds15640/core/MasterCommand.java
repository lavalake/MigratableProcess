package edu.cmu.ds15640.core;

public class MasterCommand {
	
	private CommandType type;
	private String processName;
	private int processIDCounter;
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
		this.processIDCounter = processIDCounter;
		this.args = args;
	}
	
	public CommandType getType() {
		return type;
	}

	public String getProcessName() {
		return processName;
	}

	public int getProcessIDCounter() {
		return processIDCounter;
	}

	public MigratableProcess getMigratableProcess() {
		return migratableProcess;
	}

	public String[] getArgs() {
		return args;
	}
	
	
}
