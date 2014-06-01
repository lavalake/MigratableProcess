package edu.cmu.ds15640.process;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;

import edu.cmu.ds15640.transanctionalIO.TransactionalFileInputStream;
import edu.cmu.ds15640.transanctionalIO.TransactionalFileOutputStream;

public class ReverseProcess extends MigratableProcess {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4688247597642266416L;
	
	private TransactionalFileInputStream inFile;
	private TransactionalFileOutputStream outFile;
	
	private volatile boolean suspending;
	
	public ReverseProcess(String args[]) throws Exception {
		super();
		if (args.length != 2) {
			System.out.println("Please enter input file and output file");
			throw new Exception("Invalid Arguments");
		}
		
		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOutputStream(args[1]);
	}
	

	@Override
	public void run() {
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);
		
		try {
			while(!suspending) {
				String line = in.readLine();
				
				if (line == null) {
					stop();
					break;
				}
				
				out.println(reverse(line));
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {

				}				
			}
			
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ReverseProcess: Error: " + e);
		}
		suspending = false;
		try {
			out.flush();
			inFile.close();
			outFile.close();
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String reverse(String input) {
		StringBuilder sb = new StringBuilder(input);
		return sb.reverse().toString();
	}

	@Override
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
