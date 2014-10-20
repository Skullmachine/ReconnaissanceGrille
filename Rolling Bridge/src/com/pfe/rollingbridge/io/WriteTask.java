package com.pfe.rollingbridge.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WriteTask implements Callable<Void> {

	private DataOutputStream mOutput;
	private ExecutorService executor;
	
	private ArrayList<Integer> dataToArray;
	private int dataToInt;
	
	public WriteTask(DataOutputStream out) {
		executor = Executors.newSingleThreadExecutor();
		
		mOutput = out;
	}
	
	public WriteTask(OutputStream out) {
		executor = Executors.newSingleThreadExecutor();
		
		mOutput = new DataOutputStream(out);
	}
	
	public void writeInFLux(final ArrayList<Integer> data) throws InterruptedException, ExecutionException, TimeoutException, NullPointerException {
		
		dataToArray = data;
		dataToInt = -1;
		
		Future<Void> future = executor.submit(this);
		
		if(mOutput != null)		
			future.get(2000, TimeUnit.MILLISECONDS);
		else
			throw new NullPointerException("OutStream isn't initialized");
	}
	
	public void writeInFLux(final int data) throws InterruptedException, ExecutionException, TimeoutException, NullPointerException {
		
		dataToInt = data;
		dataToArray = null;
		
		Future<Void> future = executor.submit(this);
		
		if(mOutput != null)
			future.get(2000, TimeUnit.MILLISECONDS);
		else
			throw new NullPointerException("OutStream isn't initialized");
	}
	
	public void close() throws IOException {
		if(mOutput != null)
			mOutput.close();
	}

	@Override
	public Void call() throws Exception {
		
		if(dataToArray == null) {
			mOutput.writeInt(dataToInt);
			mOutput.flush();
		}
		else {
			for(Integer one : dataToArray)
				mOutput.writeInt(one);
		
			mOutput.flush();
		}
		
		return null;
	}
}
