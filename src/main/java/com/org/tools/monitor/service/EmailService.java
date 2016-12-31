/*
 * User: paz
 * Date: 03/10/13 
 *
 * .
 *
 * Created by IntelliJ IDEA. 
 */
package com.org.tools.monitor.service;

import com.google.common.collect.Maps;
import com.org.tools.monitor.model.Folder;
import com.org.tools.monitor.model.Mail;
import com.org.tools.monitor.model.MonitorException;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.util.*;

@Service
@ThreadSafe
public class EmailService {
	private static final Logger log = LoggerFactory.getLogger(EmailService.class.getName());

	@Inject
	private IdInjectorService idInjectorService;

	@Value("${email.template.folder}")
	private String templatesFolder;

	private Map<Integer, MimeMessage> emails;

	@SuppressWarnings("ConstantConditions")
	@PostConstruct
	private void reademails() throws IOException, MessagingException {
		emails = Maps.newHashMap();
		File emailsLib = ResourceUtils.getFile(templatesFolder);
		for (final File emailFile : emailsLib.listFiles()) {
			emails.put(Integer.valueOf(FilenameUtils.removeExtension(emailFile.getName())), getemailFromFile(emailFile));

		}
	}

	public Mail sendEmail(Integer emailNr, String recipientAddress) {
		MimeMessage email = emails.get(emailNr);
		return sendemail(email, recipientAddress);
	}

	public Collection<MimeMessage> getemails() {
		return emails.values();
	}

	public Collection<Mail> sendAllEmails(String recipientAddress) {
		List<Mail> mails = new ArrayList<Mail>();
		Collection<MimeMessage> emailCollection = getemails();
		for (final MimeMessage email : emailCollection) {
			Mail mail = sendemail(email, recipientAddress);
			mails.add(mail);
		}
		return mails;
	}

	private Mail sendemail(MimeMessage message, String recipientAddress) {
		try {
			// set recipient
			message = new MimeMessage(message);
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientAddress));

			// inject id to title
			String subject = MimeUtility.decodeText(message.getSubject());
			subject = idInjectorService.injectUniqueId(subject);
			message.setSubject(MimeUtility.encodeText(subject, "ISO-8859-8", null));

			Transport.send(message);
		} catch (AddressException e) {
			log.error("AddressException!", e);
		} catch (MessagingException e) {
			log.error("MessagingException!", e);
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException!", e);
		}
		return mimeMessageToMail(message);
	}

	private MimeMessage getemailFromFile(final File emailFile) throws IOException, MessagingException {
		InputStream is = new FileInputStream(emailFile);
		return new MimeMessage(null, is);
	}

	private Mail mimeMessageToMail(MimeMessage message) {
		Mail mail = new Mail();
		try {
			mail.setSubject(message.getSubject());
			mail.setFrom(extractEmail(message.getFrom()[0].toString()));
			mail.setTo(extractEmail(message.getRecipients(Message.RecipientType.TO)[0].toString()));
			mail.setDate(new Date());
			mail.setFolder(Folder.INBOX);
		} catch (MessagingException e) {
			throw new MonitorException("Invalid Message", e);
		}

		return mail;
	}

	private String extractEmail(String address) {
		int startIndex = address.indexOf("<");
		if (startIndex < 0) {
			return address;
		}
		int endAddress = address.indexOf(">", startIndex);
		return address.substring(startIndex + 1, endAddress);
	}


}

