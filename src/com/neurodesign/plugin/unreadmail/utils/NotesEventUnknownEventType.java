package com.neurodesign.plugin.unreadmail.utils;

public class NotesEventUnknownEventType extends Exception {
	private static final long serialVersionUID = -2202080832885613480L;
	
	private int eventType;

	public NotesEventUnknownEventType(int eventType) {
		this.eventType = eventType;
	}

	public String getMessage () {
		return "Unknown event type (" + Integer.toString(eventType) + ")";
	}
	
	public int getEventType() {
		return eventType;
	}
}
