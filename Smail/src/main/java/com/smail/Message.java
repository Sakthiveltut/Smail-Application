package com.smail;

import java.sql.Timestamp;

public class Message {

	private String from,to,cc,subject,description;
	private long messageId;
	private boolean isRead,isStarred,hasAttachment;
	private Timestamp createdTime;
	
	public Message(long messageId,String from,String to,String cc,String subject,String description,boolean isRead,boolean isStarred,boolean hasAttachment,Timestamp createdTime) {
		this.messageId = messageId;
		this.from = from;
		this.to = to;
		this.cc = cc;
		this.subject = subject;
		this.description = description;
		this.isRead = isRead;
		this.isStarred = isStarred;
		this.hasAttachment = hasAttachment;
		this.createdTime = createdTime;
	}
	
	public String getTo() {
		return to;
	}
	public String getCc() {
		return cc;
	}
	public String getSubject() {
		return subject;
	}
	public String getDescription() {
		return description;
	}
	public long getMessageId() {
		return messageId;
	}
	public String getFrom() {
		return from;
	}
	public boolean isRead() {
		return isRead;
	}
	public boolean isStarred() {
		return isStarred;
	}
	public boolean hasAttachment() {
		return hasAttachment;
	}
	public Timestamp getCreatedTime() {
		return createdTime;
	}
	
	
}
