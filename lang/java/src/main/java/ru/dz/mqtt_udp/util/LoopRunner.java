package ru.dz.mqtt_udp.util;

import java.io.IOException;
import java.net.DatagramSocket;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.MqttProtocolException;

/**
 * Set up thread, run user's code in a loop. Provide start and stop controls.
 * 
 * @author dz
 *
 */
public abstract class LoopRunner {

	// ------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------

	/**
	 * 
	 * @param name Thread name
	 */
	public LoopRunner(String name) {
		threadName = name;
	}
	
	// ------------------------------------------------------------
	// For user to override
	// ------------------------------------------------------------

	/**
	 * To be overridden in subclass. On start do preparations needed.
	 */
	protected abstract void onStart() throws IOException, MqttProtocolException;

	/**
	 * To be overridden in subclass. Called in loop to do actual work.
	 */
	protected abstract void step() throws IOException, MqttProtocolException;

	/**
	 * To be overridden in subclass. On stop do cleanup needed.
	 */
	protected abstract void onStop() throws IOException, MqttProtocolException;




	// ------------------------------------------------------------
	// Incoming data process thread
	// ------------------------------------------------------------

	volatile private boolean run = false;
	private final String threadName;

	public boolean isRunning() { return run; }


	/**
	 * Request to start reception loop thread.
	 */
	public void requestStart()
	{
		if(isRunning()) return;
		start();
	}

	/**
	 * Request to stop reception loop thread.
	 */
	public void requestStop() { run = false; }

	/**
	 * Worker: start loop thread.
	 */
	protected void start() {
		Runnable target = makeLoopRunnable();
		Thread t = new Thread( target, threadName );
		t.start();
	}


	private void loop() throws IOException, MqttProtocolException 
	{
		
		onStart();

		run = true;

		while(run)
		{
			step();
		}

		onStop();
	}


	private Runnable makeLoopRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				try {
					loop();
				} catch (IOException e) {
					GlobalErrorHandler.handleError(ErrorType.IO, e);
				} catch (MqttProtocolException e) {
					GlobalErrorHandler.handleError(ErrorType.Protocol, e);
				}				
			}
		};
	}


}