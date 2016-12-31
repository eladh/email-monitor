/*
 * User: eladh
 * Date: 16/09/13 
 *
 * .
 *
 * Created by IntelliJ IDEA. 
 */
package com.org.tools.monitor.service.ws;

import com.org.tools.monitor.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Service
@Path("/monitor")
public class MonitorRestService {
	private static final Logger log = LoggerFactory.getLogger(MonitorRestService.class.getName());

	private static final Response OK = Response.ok().entity("ok").build();
	private static final Response ERROR = Response.status(500).entity("error").build();

	@Inject
	private ValidationService validationService;

	@Scheduled(cron = "0 0 9 * * *")
	private void scheduledSend() {
		sendEmails();
	}

	@Scheduled(cron = "0 0 10 * * *")
	private void scheduledValidation() {
		validateEmails();
	}

	@GET
	@Path("/emails/send")
	public Response sendEmails() {
		try {
			validationService.sendEmails();
		} catch (Exception e) {
			log.error("Error while sending mails", e);
			return ERROR;
		}
		return OK;
	}

	@GET
	@Path("/emails/validate")
	public Response validateEmails() {
		try {
			validationService.validateEmails();
		} catch (Exception e) {
			log.error("Error while validating mails", e);
			return ERROR;
		}
		return OK;
	}

	@GET
	@Path("/emails/sendAndValidate/{delay}")
	public Response sendAndValidate(@PathParam("delay") final double delaySeconds) {
		try {
			validationService.sendEmails();
			Thread.sleep((long) (delaySeconds * 1000));
			validationService.validateEmails();
		} catch (Exception e) {
			log.error("Error while sending and validating mails", e);
			return ERROR;
		}
		return OK;
	}

	@GET
	@Path("/emails/sendAndValidate/async/{delay}")
	public Response asyncSendAndValidate(@PathParam("delay") final double delaySeconds) throws InterruptedException {
		Response response = sendEmails();

		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep((long) (delaySeconds * 1000));
					validationService.validateEmails();
				} catch (Exception e) {
					log.error("Error while sending and validating mails", e);
				}
			}
		}.start();

		return response;
	}

	@GET
	@Path("/status")
	public Response sendAndValidate() {
		return OK;
	}
}


