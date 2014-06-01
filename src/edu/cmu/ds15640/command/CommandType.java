package edu.cmu.ds15640.command;

/**
 * START, STARTRETURN are for starting process
 * MIGRATE, MIGRATETO, MIGRATESTART are for migrating process between different workers
 * GETINFO, RETURNINFO are for heartBeat information
 * ASSIGNID is to assignID for each workerServer
 * KILL is for kill process
 * 
 * @author Xincheng Liu
 * @author Hao Ge
 */
public enum CommandType {
	START, STARTRETURN,
	MIGRATE, MIGRATETO, MIGRATESTART, 
	GETINFO, RETURNINFO,
	ASSIGNID,
	KILL
}
