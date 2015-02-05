package com.neurodesign.plugin.unreadmail.utils;

import java.util.Vector;
import java.util.UUID;

public class NotesEventHandler {
	
	public final static int EVENT_TYPE_DOCHASBEENCHANGED = 1;
	public final static int EVENT_TYPE_DOCHASBEENDELETED = 2;
	public final static int EVENT_TYPE_VIEWCOUNTHASCHANGED = 6;
	public final static int EVENT_TYPE_VIEWUNREADCOUNTHASCHANGED = 7;
	public final static int EVENT_TYPE_SELECTCOUNTHASCHANGED = 8;
	public final static int EVENT_TYPE_VALUEHASCHANGED = 8;
	public final static int EVENT_TYPE_TIMERTICK = 1000;
	
	private Vector<NotesEventThread> threadPool;

	public NotesEventHandler () {
		threadPool = new Vector<NotesEventThread>();
	}
	
	protected void finalize () {
		if (threadPool == null || threadPool.size() == 0) return;
		NotesEventThread evt;
		for (int i = 0; i < threadPool.size(); i++) {
			evt = threadPool.get(i);
			if (evt.isAlive()) evt.stopThread();
			threadPool.remove(evt);
		}
	}
	
	public String registerEvent(NotesEventListener listener, int eventType, NotesEventParameters eventParms) throws NotesEventUnknownEventType {
		switch (eventType) {
		case EVENT_TYPE_DOCHASBEENCHANGED:
		case EVENT_TYPE_DOCHASBEENDELETED:
		case EVENT_TYPE_VIEWCOUNTHASCHANGED:
		case EVENT_TYPE_VIEWUNREADCOUNTHASCHANGED:
		case EVENT_TYPE_SELECTCOUNTHASCHANGED:
		case EVENT_TYPE_TIMERTICK:
			String uuid = UUID.randomUUID().toString();
			NotesEventThread thread = new NotesEventThread (uuid, listener, eventType, eventParms); 
			threadPool.add(thread);
			thread.start();
			return uuid;
		default:
			throw new NotesEventUnknownEventType (eventType);
		}
	}

	public void reset(String id) {
		NotesEventThread evt = getEventThread(id);
		if (evt == null) return;
		evt.reset();
	}
	
	public void unregisterEvent (String id) {
		NotesEventThread evt = getEventThread(id);
		if (evt == null) return;
		
		if (evt.isAlive()) evt.stopThread();
		threadPool.remove(evt);
	}
	
	private NotesEventThread getEventThread (String id) {
		
		NotesEventThread evt;
		
		if (threadPool == null) return null;
		for (int i = 0; i < threadPool.size(); i++) {
			evt = threadPool.get(i);
			if (evt != null && evt.getThreadId().equals(id)) {
				return evt;
			}
		}
		return null;
	}
	
}
