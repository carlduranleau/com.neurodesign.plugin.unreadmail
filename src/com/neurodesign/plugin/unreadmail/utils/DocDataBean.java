package com.neurodesign.plugin.unreadmail.utils;

import java.util.Date;

import com.neurodesign.plugin.unreadmail.Activator;
import com.neurodesign.plugin.unreadmail.views.SharedData;

//Represents a document list entry

public class DocDataBean implements Comparable<DocDataBean> {
	private int hc = 0;
	private String id = "";
	private String title = "";
	private String description = "";
	private String content = "";
	private String from = "";
	private String to = "";
	private String copyTo = "";
	private String blindCopyTo = "";
	private Date date = null;
	
	public DocDataBean (String code, String pId, String pTitle, String pDescription, Date pDate) {
		hc = Integer.parseInt(code, 16);
		id = pId;
		title = pTitle;
		description = pDescription;
		date = pDate;
	}
	
	public DocDataBean() {
		
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCopyTo() {
		return copyTo;
	}

	public void setCopyTo(String copyTo) {
		this.copyTo = copyTo;
	}

	public String getBlindCopyTo() {
		return blindCopyTo;
	}

	public void setBlindCopyTo(String blindCopyTo) {
		this.blindCopyTo = blindCopyTo;
	}
	
	public int compareTo(DocDataBean o) {
		try {
			int result = getDate().compareTo(o.getDate());
			
			if (SharedData.getSortType().equals(Activator.PREF_SORT_CHOICES[1][1])) return -result;
			return result;

		} catch (Exception e) {}
		return 0;
	}

	@Override
	public boolean equals(Object t) {
		return ((DocDataBean)t).getId().equals(this.getId());
	}

	@Override
	public int hashCode() {
		return hc;
	}
}
