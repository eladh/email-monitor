/*
 * User: eldad+.dor
 * Date: 15/07/13
 
 *
 * .
 *
 * Created by IntelliJ IDEA. 
 */
package com.org.tools.monitor;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author Elad Hirsch
 * @date 15/07/13
 */
public class StartServer {
	public static void main(String[] args) throws Exception {
		Server server = new Server(8085);
		ContextHandler handler = new WebAppContext("src/main/webapp", "/");
		server.setHandler(handler);
		server.start();
	}
}