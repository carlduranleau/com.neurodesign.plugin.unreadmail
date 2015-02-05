package com.neurodesign.plugin.unreadmail.utils;

import java.util.Collections;
import java.util.Vector;

import org.eclipse.swt.widgets.Display;

import lotus.domino.*;

import com.neurodesign.plugin.unreadmail.views.*;

//Process thread. This process computes the content of the document list.

public class RebuildThread extends Thread {

	private SampleView parent;
	
	public RebuildThread (SampleView obj) {
		super();
		parent = obj;
	}
	
	public void run () {
		try {
			System.out.println ("Rebuild thread start");
			if (parent == null) return;
			System.out.println ("Calling rebuild function");
			rebuild();
		} catch (Exception e) {
			System.out.println ("Failed to call rebuild function");
			e.printStackTrace();
		}
	}
	
	synchronized private void initNotesParameters () {
		try {
			NotesThread.sinitThread();
	
			Session notesSession = NotesFactory.createSession();
			SharedData.setMailOwner(notesSession.createName(notesSession.getEffectiveUserName()).getCommon());
			SharedData.setMailDBServer(notesSession.getEnvironmentString("MailServer", true));
			SharedData.setMailDBName(notesSession.getEnvironmentString("MailFile", true));
			if (notesSession != null) notesSession.recycle();
		} catch (Exception e) {
			System.out.println("Unable to initialize mail file");
			SharedData.setMailDBServer("");
			SharedData.setMailDBName("");
			SharedData.setMailOwner("");
		} finally {
			NotesThread.stermThread();
		}
	}

	synchronized private boolean isMailDBChanged () {
		boolean hasChanged = false;
		try {
			NotesThread.sinitThread();
			
			Session notesSession = NotesFactory.createSession();
			hasChanged = !((notesSession.getEnvironmentString("MailServer", true) + notesSession.getEnvironmentString("MailFile", true)).equals(SharedData.getMailDBServer() + SharedData.getMailDBName()));
			if (notesSession != null) notesSession.recycle();
		} catch (Exception e) {
			System.out.println("isMailDBChanged: " + e.toString());
		} finally {
			NotesThread.stermThread();
		}
		return hasChanged;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void rebuild() {
		try {
			Database mailDB = null;
			DocumentCollection mails = null;

			if (isMailDBChanged()) initNotesParameters();
			
			if (SharedData.getMailDBName().equals("")) return;
			
			NotesThread.sinitThread();

			Session notesSession = NotesFactory.createSession();
			mailDB = notesSession.getDatabase(SharedData.getMailDBServer(), SharedData.getMailDBName());
			SharedData.setMailsData(new Vector<DocDataBean>());
			if (mailDB.isOpen()) {
				mails = mailDB.getAllUnreadDocuments();
				
				Document doc;
				DocDataBean bean;
				for (int i = 1; i <= mails.getCount(); i++) {
					Thread.yield();
					doc = mails.getNthDocument(i);
					if (!(doc.isDeleted()) && doc.isValid()) {
						try {
							bean = new DocDataBean(doc.getNoteID(), doc.getUniversalID(), "", (String)doc.getCreated().getDateOnly(), doc.getCreated().toJavaDate());
							bean.setFrom(doc.getItemValueString("From"));
							bean.setTo(getCommonNames(notesSession, doc.getItemValue("SendTo")));
							bean.setCopyTo(getCommonNames(notesSession, doc.getItemValue("CopyTo")));
							bean.setBlindCopyTo(getCommonNames(notesSession, doc.getItemValue("BlindCopyTo")));
							bean.setContent((String)doc.getItemValueString("Body"));
							if (doc.hasItem("Subject") && !(doc.getItemValueString("Subject").equals(""))) {
								bean.setTitle(doc.getItemValueString("Subject"));
								SharedData.getMailsData().addElement(bean);
							} else {
								bean.setTitle("Sans sujet");
								SharedData.getMailsData().addElement(bean);
							}
							Collections.sort(SharedData.getMailsData()); 
						} catch (Exception e) {}
					}
				}
				SharedData.setUnreadCount(SharedData.getMailsData().size());
			}
			if (mails != null) mails.recycle();
			if (mailDB != null) mailDB.recycle();
			if (notesSession != null) notesSession.recycle();
			notifyParent();
		} catch (Exception e) {
			System.out.println("rebuild: " + e.toString());
		} finally {
			NotesThread.stermThread();
			SharedData.setRefreshFlag(false);
		}
	}
	
	private String getCommonNames (Session s, Vector<String> notesname) {
		try {
			String nameList = "";
			Name cnvName;
			for (int i = 0; i < notesname.size(); i++) {
				cnvName = s.createName(notesname.elementAt(i));
				if (i == 0)
					nameList = cnvName.getCommon();
				else
					nameList += ", " + cnvName.getCommon();
				cnvName.recycle();
			}
			 
			return nameList;
		} catch (Exception e) {}
		return "";
	}

	private void notifyParent() {
		if (parent == null) return;
		try {
			Display.getDefault().asyncExec(new Runnable() {
				public void run () {
					parent.rebuildFinished();
				}
			});
		} catch (Exception e) {}
		return;
	}
}
