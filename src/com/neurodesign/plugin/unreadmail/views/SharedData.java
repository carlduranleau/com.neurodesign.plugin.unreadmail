package com.neurodesign.plugin.unreadmail.views;

import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;

import com.neurodesign.plugin.unreadmail.Activator;
import com.neurodesign.plugin.unreadmail.utils.DocDataBean;

//This bean is used to share data between UI thread and process thread

public class SharedData {
	static private String mailDBName = "";
	static private String mailDBServer = "";
	static private String mailOwner = "";
	static private int unreadCount = 0;
	static private Vector<DocDataBean> mailsData = null;
	static private boolean refreshing = false;

	static private ISelection selection; 
	
	//Preferences
	static private int refreshSpeed = Integer.parseInt(Activator.PREF_REFRESH_DEFAULT);
	static private String sortType = Activator.PREF_SORT_DEFAULT;

	synchronized public static int getRefreshSpeed() {
		return refreshSpeed;
	}
	synchronized public static void setRefreshSpeed(int refreshSpeed) {
		SharedData.refreshSpeed = refreshSpeed;
	}
	synchronized public static String getSortType() {
		return sortType;
	}
	synchronized public static void setSortType(String sortType) {
		SharedData.sortType = sortType;
	}
	synchronized public static String getMailDBName() {
		return mailDBName;
	}
	synchronized public static void setMailDBName(String mailDBName) {
		SharedData.mailDBName = mailDBName;
	}
	synchronized public static String getMailDBServer() {
		return mailDBServer;
	}
	synchronized public static void setMailDBServer(String mailDBServer) {
		SharedData.mailDBServer = mailDBServer;
	}
	synchronized public static String getMailOwner() {
		return mailOwner;
	}
	synchronized public static void setMailOwner(String mailOwner) {
		SharedData.mailOwner = mailOwner;
	}
	synchronized public static int getUnreadCount() {
		return unreadCount;
	}
	synchronized public static void setUnreadCount(int unreadCount) {
		SharedData.unreadCount = unreadCount;
	}
	synchronized public static Vector<DocDataBean> getMailsData() {
		return mailsData;
	}
	synchronized public static void setMailsData(Vector<DocDataBean> mailsData) {
		SharedData.mailsData = mailsData;
	}
	synchronized public static boolean isRefreshing () {
		return refreshing;
	}
	synchronized public static void setRefreshFlag (boolean v) {
		SharedData.refreshing = v;
	}
	synchronized public static void restoreSelection (TableViewer viewer) {
		if (SharedData.selection == null) return;
		if (viewer == null) return;
		try {
			viewer.setSelection(SharedData.selection);
		} catch (Exception e) {}
	}
	synchronized public static void saveSelection (TableViewer viewer) {
		if (viewer == null) return;
		try {
			ISelection s = viewer.getSelection();
			SharedData.selection = s;
		} catch (Exception e) {}
	}
}
