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
}
