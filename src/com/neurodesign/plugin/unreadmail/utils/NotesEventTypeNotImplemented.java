package com.neurodesign.plugin.unreadmail.utils;

public class NotesEventTypeNotImplemented extends Exception {
	private static final long serialVersionUID = 5407620810100434711L;
	
	private int eventType;

	public NotesEventTypeNotImplemented(int eventType) {
		this.eventType = eventType;
	}
	
	public String getMessage () {
		return "Event type not implemented (" + Integer.toString(eventType) + ")";
	}
	
	public int getEventType() {
		return eventType;
	}
}
