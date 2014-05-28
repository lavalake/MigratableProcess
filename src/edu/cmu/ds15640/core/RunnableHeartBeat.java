package edu.cmu.ds15640.core;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.management.timer.Timer;

public class RunnableHeartBeat implements Runnable {

	private volatile boolean stop = false;
	
	@Override
	public void run() {
		while(!stop){
			for(int i: ProcessManager.getInstance().getWorkerToWorkerInfo().keySet()){
				Socket socket = ProcessManager.getInstance().getWorkerToWorkerInfo().get(i).getSocket();
				ObjectOutputStream oos = null;
				try {
					oos = new ObjectOutputStream(socket.getOutputStream());
					MasterCommand sc = new MasterCommand(CommandType.GETINFO);
					oos.writeObject(sc);
				} catch (IOException e) {
					System.out.println("Worker: " + ProcessManager.getInstance().getWorkerToWorkerInfo().get(i).getWorkerID() + " is failed");
					System.out.println(e.toString());
					return;
				} finally{
					try {
						oos.close();
					} catch (IOException e) {
						System.out.println(e.toString());
					}
				}
			}
			try {
				Thread.sleep(10* Timer.ONE_SECOND);
			} catch (InterruptedException e) {
				System.out.println(e.toString());
			}
		}
	}
	
	public void setStop(boolean bool){
		stop = bool;
	}
}
