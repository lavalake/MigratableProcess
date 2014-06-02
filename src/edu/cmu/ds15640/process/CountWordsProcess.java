package edu.cmu.ds15640.process;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

import edu.cmu.ds15640.transanctionalIO.TransactionalFileInputStream;
import edu.cmu.ds15640.transanctionalIO.TransactionalFileOutputStream;

public class CountWordsProcess extends MigratableProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6941851757285650439L;

	private TransactionalFileInputStream inFile;
	private TransactionalFileOutputStream outFile;
	private int count;
	
	private volatile boolean suspending;
	
	public CountWordsProcess(String args[]) throws Exception {
		super();
		if (args.length != 2) {
			System.out.println("Please enter input file and output file");
			throw new Exception("Invalid Arguments");
		}
		
		count = 0;
		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOutputStream(args[1]);
	}
	
	@Override
	public void run() {
		DataInputStream in = new DataInputStream(inFile);
		PrintStream out = new PrintStream(outFile);
		
		try {
			while(!suspending) {
				@SuppressWarnings("deprecation")
				String line = in.readLine();
				
				if (line == null) {
					stop();
					break;
				}
				
				String[] words = line.split(" ");
				count += words.length;
				out.println("Number of words in current line: " + words.length + " Total words: "+ count);
				
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
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

	@Override
	public void suspend() {
		suspending = true;
		while (suspending) ;
	}

	@Override
	public void stop() {
		complete = true;
	}
	
}
