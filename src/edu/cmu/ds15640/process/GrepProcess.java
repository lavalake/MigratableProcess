package edu.cmu.ds15640.process;

import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

import edu.cmu.ds15640.transanctionalIO.TransactionalFileInputStream;
import edu.cmu.ds15640.transanctionalIO.TransactionalFileOutputStream;

public class GrepProcess extends MigratableProcess {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7803305571297376999L;
	
	private TransactionalFileInputStream inFile;
	private TransactionalFileOutputStream outFile;
	private String query;

	private volatile boolean suspending;

	public GrepProcess(String args[]) throws Exception {
		super();
		if (args.length != 3) {
			System.out
					.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}

		query = args[0];
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2]);
	}

	public void run() {
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);

		try {
			while (!suspending) {
				String line = in.readLine();

				if (line == null){
					stop();
					break;
				}

				if (line.contains(query)) {
					out.println(line);
				}

				// Make grep take longer so that we don't require extremely
				// large files for interesting results
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			e.printStackTrace();
			// End of File
		} catch (IOException e) {
			System.out.println("GrepProcess: Error: " + e);
		}

		suspending = false;
		try {
			out.flush();
			inFile.close();
			outFile.close();
			out.close();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void suspend() {
		suspending = true;
		while (suspending)
			;
	}

	@Override
	public void stop() {
		complete = true;
	}

}