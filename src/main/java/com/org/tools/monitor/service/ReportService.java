package com.org.tools.monitor.service;

import com.org.tools.monitor.model.Folder;
import com.org.tools.monitor.model.Mail;
import com.org.tools.monitor.model.MonitorException;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.*;

/**
 * @author Elad Hirsch
 */
@Service
public class ReportService {
	private static final Logger log = LoggerFactory.getLogger(ReportService.class.getName());

	@Value("${report.recipients}")
	private String reportRecipients;

	private Session mailSession;

	@PostConstruct
	private void initSessionProperties() {
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", "smtp.click21.com");
		mailSession = Session.getDefaultInstance(props);
	}

	public void reportMailStatus(List<Mail> mails) {
		// log mail status (to  be monitored by Splunk)
		boolean statusOk = true;
		for (Mail mail : mails) {
			String status = Folder.INBOX.equals(mail.getFolder()) ? "OK" : "ERROR";
			String emailMessage = String.format("Email Status: folder=%s, subject=%s, from=%s, to=%s, date=%s, status=%s", mail.getFolder(), mail.getSubject(), mail.getFrom(), mail.getTo(), mail.getDate(), status);
			log.info(emailMessage);

			statusOk = statusOk && mail.getFolder().isValid();
		}

		// sort by folder, to, from, subject
		Collections.sort(mails, new Comparator<Mail>() {
			@Override
			public int compare(Mail mail1, Mail mail2) {
				int result = -mail1.getFolder().compareTo(mail2.getFolder());
				if (result == 0) {
					result = mail1.getTo().compareTo(mail2.getTo());
				}
				if (result == 0) {
					result = mail1.getFrom().compareTo(mail2.getFrom());
				}
				if (result == 0) {
					result = mail1.getSubject().compareTo(mail2.getSubject());
				}
				return result;
			}
		});

		// build FreeMarker configuration
		try {
			Configuration cfg = new Configuration();
			cfg.setTemplateLoader(new ClassTemplateLoader(ReportService.class, "/template"));
			cfg.setDefaultEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			cfg.setIncompatibleImprovements(new Version(2, 3, 20)); // sync with jar version

			// build template model
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("mails", mails);
			model.put("status", statusOk);

			// build & process template
			Template template = cfg.getTemplate("report.ftl");
			Writer report = new StringWriter();
			template.process(model, report);

			// send mail
			String subject = "Email Monitor Report: " + (statusOk ? "Ok" : "Error");
			sendMessage(reportRecipients, subject, report.toString());

		} catch (Exception e) {
			throw new MonitorException("IOException", e);
		}
	}

	private void sendMessage(String recipientAddress, String subject, String messageText) {
		MimeMessage message = new MimeMessage(mailSession);
		try {
			String encoding = "text/html; charset=UTF-8";
			message.setHeader("Content-Type", encoding);
			message.setRecipients(Message.RecipientType.TO, getAddresses(recipientAddress));
			message.setSubject(MimeUtility.encodeText(subject, "ISO-8859-8", null));
			message.setContent(messageText, encoding);
			Transport.send(message);
		} catch (AddressException e) {
			log.error("AddressException!", e);
		} catch (MessagingException e) {
			log.error("MessagingException!", e);
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException!", e);
		}
	}

	private Address[] getAddresses(String recipientsString) throws AddressException {
		// build addresses array
		String[] recipients = recipientsString.split(";");
		Address[] addresses = new Address[recipients.length];
		for (int i = 0; i < recipients.length; i++) {
			addresses[i] = new InternetAddress(recipients[i]);
		}
		return addresses;
	}
}
