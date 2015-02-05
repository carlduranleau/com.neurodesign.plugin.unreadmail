package com.neurodesign.plugin.unreadmail.utils;

import org.eclipse.swt.widgets.Display;

import lotus.domino.*;

public class NotesEventThread extends Thread {

	private String id;
	private int eventType;
	private NotesEventListener listener;
	private NotesEventParameters eventParms;
	
	private int sleepTime = 10000;
	private boolean shouldStop;
	
	private String oldCount = "";
	
	public NotesEventThread (String id, NotesEventListener listener, int eventType, NotesEventParameters eventParms) {
		this.id = id;
		this.shouldStop = false;
		this.eventType = eventType;
		this.eventParms = eventParms;
		this.listener = listener;
		if (eventParms != null) {
			this.sleepTime = eventParms.getRefreshspeed() * 1000;
			if (this.sleepTime < 10000) this.sleepTime = 10000; 
		}
	}

	//Reset 'memory' variables
	public void reset() {
		oldCount = "";
	}
	
	public String getThreadId() {
		return id;
	}

	public void stopThread () {
		shouldStop = true;
	}
	
	public void run() {
		while (!shouldStop) {
			switch(eventType) {
				case NotesEventHandler.EVENT_TYPE_DOCHASBEENCHANGED:
					runDocChangedEvent();
					break;
				case NotesEventHandler.EVENT_TYPE_DOCHASBEENDELETED:
					runDocDeletededEvent();
					break;
				case NotesEventHandler.EVENT_TYPE_VIEWCOUNTHASCHANGED:
					runViewChangedEvent();
					break;
				case NotesEventHandler.EVENT_TYPE_VIEWUNREADCOUNTHASCHANGED:
					runViewUnreadChangedEvent();
					break;
				case NotesEventHandler.EVENT_TYPE_SELECTCOUNTHASCHANGED:
					runSelectionChangedEvent();
					break;
				case NotesEventHandler.EVENT_TYPE_TIMERTICK:
					runTimedTickEvent();
					break;
				default:
					shouldStop = true;
			}

			if (!shouldStop) {
				try {
					sleep(sleepTime);
				} catch (InterruptedException e) {
					shouldStop = true;
				}
			}
		}
	}
	
	public void runSelectionChangedEvent () {
		//Call the listener synchronously.
		
	}
	public void runViewChangedEvent () {
		//Call the listener synchronously.
		if (listener == null || eventParms == null) return;
		try {
			listener.eventReceived(eventType, eventParms);
		} catch (Exception e) {}
		return;
	}
	public void runViewUnreadChangedEvent () {
		//Call the listener synchronously.
		if (listener == null || eventParms == null) return;
		try {
			listener.eventReceived(eventType, eventParms);
		} catch (Exception e) {}
		return;
	}
	public void runDocDeletededEvent () {
		//Call the listener synchronously.
		
	}
	public void runDocChangedEvent () {
		//Call the listener synchronously.
		
	}
	public void runTimedTickEvent () {
		//Call the listener synchronously.
		if (listener == null || eventParms == null) return;
		try {
			Display.getDefault().asyncExec(new Runnable() {
				public void run () {
					listener.eventReceived(eventType, eventParms);
				}
			});
		} catch (Exception e) {}
		return;
	}
}
