package edu.cmu.ds15640.transanctionalIO;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileInputStream extends InputStream implements
		Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1922874444819579116L;

	private int offset;
	private transient RandomAccessFile randomAccessFile;
	private Boolean migrated;
	private String sourceFile;

	public TransactionalFileInputStream(String source)
			throws FileNotFoundException {
		offset = 0;
		randomAccessFile = new RandomAccessFile(source, "r");
		migrated = false;
		sourceFile = source;
	}

	@Override
	public int read() throws IOException {
		if (migrated) {
			randomAccessFile = new RandomAccessFile(sourceFile, "r");
			randomAccessFile.seek(offset);
			migrated = false;
		}
		int aByte = randomAccessFile.read();
		offset++;
		return aByte;
	}

	public void setMigrated(Boolean bool) {
		migrated = bool;
	}

	@Override
	public void close() throws IOException {
		if (randomAccessFile != null) {
			randomAccessFile.close();
		}
	}
}
