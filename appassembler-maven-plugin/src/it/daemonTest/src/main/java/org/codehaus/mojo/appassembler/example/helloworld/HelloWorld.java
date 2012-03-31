/**
 *
 * The MIT License
 *
 * Copyright 2006-2012 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.codehaus.mojo.appassembler.example.helloworld;

import org.apache.log4j.Logger;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

public class HelloWorld implements WrapperListener {
    private static Logger LOGGER = Logger.getLogger(HelloWorld.class);

    private MyApplication myApplication;

    private HelloWorld() {
    }

    public void controlEvent(int event) {
	if ((event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT)
		&& (WrapperManager.isLaunchedAsService() || WrapperManager.isLaunchedAsService())) {
	    // Ignore
	} else {
	    WrapperManager.stop(0);
	    // Will not get here.
	}
    }

    public Integer start(String[] args) {
	LOGGER.info("Starting application.");
	myApplication = new MyApplication(args);
	myApplication.start();
	return null;
    }

    public int stop(int exitCode) {
	LOGGER.info("Stopping application.");
	myApplication.stopWorking();

	return exitCode;
    }

    public static void main(String[] args) {
	WrapperManager.start(new HelloWorld(), args);
    }
}
