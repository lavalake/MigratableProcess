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
			System.out.println("TranslateProcess: Error: " + e);
		}
	}
	
	private String reverse(String input) {
		String result = "";
		for (int i = 0; i < input.length(); i++) {
			result = input.substring(i, i+1) + result;
		}
		return result;
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