package edu.cmu.ds15640.core;

public class SendCommand {
	
	private CommandType type;
	private MigratableProcess migratableProcess;
	
	public SendCommand(CommandType ct){
		type = ct;
		migratableProcess = null;
	}
	
	public SendCommand(CommandType ct, MigratableProcess mp){
		type = ct;
		migratableProcess = mp;
	}
}
