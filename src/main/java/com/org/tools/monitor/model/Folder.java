/*
 * User: eladh
 * Date: 03/10/13 
 *
 * .
 *
 * Created by IntelliJ IDEA. 
 */
package com.org.tools.monitor.model;

/**
 *
 */
public enum Folder {
	INBOX(true),
	SPAM(false),
	PROMOTIONAL(false),
	NONE(false);

	private boolean valid;


	Folder(boolean valid) {

		this.valid = valid;
	}

	public boolean isValid() {
		return valid;
	}
}