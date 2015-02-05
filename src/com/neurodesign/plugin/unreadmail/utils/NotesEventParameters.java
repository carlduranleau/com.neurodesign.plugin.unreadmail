package com.neurodesign.plugin.unreadmail.utils;

public class NotesEventParameters {
	private Object obj = null;
	private String id = null;
	private String query = null;
	private String server = null;
	private String path = null;
	private int refreshspeed = 10000;

	public int getRefreshspeed() {
		return refreshspeed;
	}
	public void setRefreshspeed(int refreshspeed) {
		this.refreshspeed = refreshspeed;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
}
