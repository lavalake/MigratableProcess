package edu.cmu.ds15640.core;

import java.io.IOException;
import javax.management.timer.Timer;

public class RunnableHeartBeat implements Runnable {

	private volatile boolean stop = false;
	
	@Override
	public void run() {
		while(!stop){
			//System.out.println("Send heart beat information");
			for(int id: ProcessManager.getInstance().getWorkerToWorkerInfo().keySet()){
				try {
					MasterCommand sc = new MasterCommand(CommandType.GETINFO);
					ProcessManager.getInstance().getWorkerToWorkerInfo().get(id).getWorkerService().writeToWorker(sc);
				} catch (IOException e) {
					System.out.println("Worker: " + ProcessManager.getInstance().getWorkerToWorkerInfo().get(id).getWorkerID() + " is failed");
					System.out.println(e.toString());
					ProcessManager.getInstance().getWorkerToWorkerInfo().get(id).getWorkerService().stopWorker(id);
					return;
				}
			}
			try {
				Thread.sleep(10 * Timer.ONE_SECOND);
			} catch (InterruptedException e) {
				System.out.println(e.toString());
			}
		}
	}
	
	public void stopHeartBeat(){
		stop = true;
	}
}
