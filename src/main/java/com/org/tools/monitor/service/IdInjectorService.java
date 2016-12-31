package com.org.tools.monitor.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.annotation.ThreadSafe;
import org.springframework.stereotype.Service;

/**
 * @author Elad Hirsch
 */
@Service
@ThreadSafe
public class IdInjectorService {
	private static final String SPLITTER = "~";

	public String injectId(String string, String id) {
		if (StringUtils.isEmpty(string)) {
			return string;
		}
		return id + SPLITTER + string;
	}

	public String injectUniqueId(String string) {
		long uniqueId = System.currentTimeMillis();
		return injectId(string, String.valueOf(uniqueId));
	}

	public String extractId(String string) {
		if (StringUtils.isEmpty(string)) {
			return "";
		}
		int index = string.indexOf(SPLITTER);
		return (index < 0) ? "" :  string.substring(0, index);
	}
}