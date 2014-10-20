package com.pfe.rollingbridge.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ReadTask implements Callable<Integer> {

	private DataInputStream mInput;
	private ExecutorService executor;
	private int timeout = -1;
	
	public ReadTask(DataInputStream in) {
		executor = Executors.newSingleThreadExecutor();

		mInput = in;
	}
	
	public ReadTask(InputStream in) {
		executor = Executors.newSingleThreadExecutor();
		
		mInput = new DataInputStream(in);
	}
	
	public void close() throws IOException {
		if(mInput != null)
			mInput.close();
	}
	
	public int readInFlux() throws IOException, TimeoutException, InterruptedException, ExecutionException, NullPointerException {
		int readInt = -1;
		
		Future<Integer> future = executor.submit(this);
		
		if(mInput != null)
			if(timeout == -1)
				readInt = future.get();
			else
				readInt = future.get(timeout, TimeUnit.MILLISECONDS);
		else 
			throw new NullPointerException("InStream isn't initialized");

		return readInt;
	}
	
	@Override
	public Integer call() throws Exception {
		return mInput.readInt();
	}
	
	/**
	 * 
	 * @param time duration in milliseconds before cancel operation of read. -1 = unlimited operation
	 */
	public void setTimeout(int time) {
		timeout = time;
	}
}
