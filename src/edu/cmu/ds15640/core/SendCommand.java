package edu.cmu.ds15640.core;

public class SendCommand {
	
	private CommandType type;
	private String processName;
	private int processIDCounter;
	private MigratableProcess migratableProcess;
	
	public SendCommand(CommandType ct){
		type = ct;
		migratableProcess = null;
	}
	
	public SendCommand(CommandType ct, MigratableProcess mp){
		type = ct;
		migratableProcess = mp;
	}

	public SendCommand(CommandType ct, String processName,
			int processIDCounter) {
		type = ct;
		this.processName = processName;
		this.processIDCounter = processIDCounter;
	}
}
