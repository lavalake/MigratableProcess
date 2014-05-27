package edu.cmu.ds15640.transanctionalIO;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements
		Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7895673353100940930L;
	
	private int offset;
	private transient RandomAccessFile randomAccessFile;
	private Boolean migrated;
	private String targetFile;
	
	public TransactionalFileOutputStream(String target) throws FileNotFoundException{
		offset = 0;
		randomAccessFile = new RandomAccessFile(target, "rw");
		migrated = false;
		targetFile = target;
	}
	
	@Override
	public void write(int aByte) throws IOException {
		if(migrated){
			randomAccessFile = new RandomAccessFile(targetFile, "rw");
			randomAccessFile.seek(offset);
			migrated = false;
		}
		randomAccessFile.write(aByte);
		offset++;
	}
	
	public void setMigrated(Boolean bool){
		migrated = bool;
	}
	
	@Override
	public void close() throws IOException {
		if(randomAccessFile != null){
			randomAccessFile.close();
		}
	}
}
