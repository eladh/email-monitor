/*
 * User: eladh
 * Date: 02/10/13 
 *
 * .
 *
 * Created by IntelliJ IDEA. 
 */
package com.org.tools.monitor.model;

import java.util.Date;

public class Mail {
	private String subject;
	private String from;
	private String to;
	private Date date;
	private Folder folder;

	public Mail() {
	}

	public Mail(Mail mail) {
		this.subject = mail.subject;
		this.from = mail.from;
		this.to = mail.to;
		this.date = mail.date;
		this.folder = mail.folder;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Folder getFolder() {
		return folder;
	}

	public void setFolder(Folder folder) {
		this.folder = folder;
	}
}