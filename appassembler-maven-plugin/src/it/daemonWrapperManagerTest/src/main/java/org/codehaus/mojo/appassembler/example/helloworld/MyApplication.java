package org.codehaus.mojo.appassembler.example.helloworld;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

public class MyApplication extends Thread {

    private static Logger LOGGER = Logger.getLogger(MyApplication.class);

    private AtomicBoolean stopping;

    private String[] arguments;

    public MyApplication(String[] args) {
	this.arguments = args;
	this.stopping = new AtomicBoolean(false);
    }

    @Override
    public void start() {
	LOGGER.info("Hello World!");
	LOGGER.info("Number of command line arguments: " + arguments.length);

	for (int i = 0; i < arguments.length; i++) {
	    LOGGER.info("Argument #" + i + ":" + arguments[i]);
	}

	LOGGER.info("basedir: " + System.getProperty("basedir"));

	long start = System.currentTimeMillis();
	while (true && !stopping.get()) {
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		LOGGER.error("InterruptedException:", e);
	    }
	    long current = System.currentTimeMillis();
	    LOGGER.info("Vergangene Zeit seit dem Start:" + (current - start)
		    + " ms");
	}

	LOGGER.info("Ended Loop.");
    }


    public void stopWorking() {
	this.stopping.getAndSet(true);
    }
}
