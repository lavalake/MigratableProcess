package edu.cmu.ds15640.command;

import java.io.Serializable;

import edu.cmu.ds15640.process.MigratableProcess;

/**
 * MasterCommand is used for MasterServer
 * 
 * @author Xincheng Liu
 * @author Hao Ge
 */
public class MasterCommand implements Serializable {
	private static final long serialVersionUID = -2245667253840915290L;
	private CommandType type;
	private int processID;

	private MigratableProcess migratableProcess;
	private String processName;
	private String[] args;

	private int sourceWorkerID;
	private int targetWorkerID;

	public MasterCommand(CommandType ct) {
		type = ct;
		migratableProcess = null;
	}

	public MasterCommand(CommandType ct, int workerID) {
		type = ct;
		this.sourceWorkerID = workerID;
	}

	public MasterCommand(CommandType ct, MigratableProcess mp) {
		type = ct;
		migratableProcess = mp;
	}

	public MasterCommand(CommandType ct, String processName, int processID,
			String[] args) {
		type = ct;
		this.processName = processName;
		this.processID = processID;
		this.args = args;
	}

	public MasterCommand(CommandType ct, int targetWorkerID, int processID) {
		type = ct;
		this.targetWorkerID = targetWorkerID;
		this.processID = processID;
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

	public int getTargetWorkerID() {
		return targetWorkerID;
	}

	public int getWorkerID() {
		return sourceWorkerID;
	}

}
