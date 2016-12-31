/*
 * User: eladh
 * Date: 17/09/13 
 *
 * .
 *
 * Created by IntelliJ IDEA. 
 */
package com.org.tools.monitor.service;

import com.org.tools.monitor.crawler.*;
import com.org.tools.monitor.model.Folder;
import com.org.tools.monitor.model.Mail;
import com.org.tools.monitor.model.MonitorException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

@Service
@ThreadSafe
public class ValidationService {
	private static final Logger log = LoggerFactory.getLogger(ValidationService.class.getName());

	@Inject
	private EmailService emailService;

	@Inject
	private IdInjectorService idInjectorService;
	@Inject
	private ReportService reportService;

	private List<Mail> sentMails;
	private ReentrantLock lock = new ReentrantLock(true);
	private List<BaseCrawler> crawlers;

	@PostConstruct
	private void loadCrawlers() {
		crawlers = Arrays.asList(
				new WallaCrawler(),
				new YahooCrawler()
		);
	}

	public List<Mail> sendEmails() throws InterruptedException {
		lock.lock();
		try {
			log.info("Start sending emails...");
			List<Mail> sentMails = new ArrayList<Mail>();
			for (BaseCrawler crawler : crawlers) {
				String address = crawler.getAccountAddress();
				Collection<Mail> mails = emailService.sendAllEmails(address);
				sentMails.addAll(mails);
			}
			this.sentMails = sentMails;
			log.info("Finished sending emails. sent={}", sentMails.size());
			return sentMails;
		} finally {
			lock.unlock();
		}
	}

	public void validateEmails() throws InterruptedException {
		lock.lock();
		try {
			log.info("Start validating sent emails");

			// sanity
			if (CollectionUtils.isEmpty(sentMails)) {
				throw new MonitorException("No emails where sent");
			}
			List<Mail> mails = getMailsInFolders();
			reportService.reportMailStatus(mails);
			log.info("Finished validating emails. scanned={}", sentMails.size());
		} finally {
			lock.unlock();
		}
	}

	private List<Mail> getMailsInFolders() throws InterruptedException {
		// find each mail folder
		List<Mail> receivedMails = getAllReceivedEmails();
		List<Mail> result = new ArrayList<Mail>(sentMails.size());
		for (Mail sendMail : sentMails) {
			Mail mail = findReceivedFolder(sendMail, receivedMails);
			result.add(mail);
		}
		return result;
	}

	private Mail findReceivedFolder(Mail sendMail, List<Mail> receivedMails) {
		for (Mail receivedMail : receivedMails) {
			if (match(sendMail, receivedMail)) {
				return receivedMail;
			}
		}
		// mail not found, returning NONE folder
		Mail mail = new Mail(sendMail);
		mail.setFolder(Folder.NONE);
		return mail;
	}

	private boolean match(Mail sendMail, Mail receivedMail) {
		// todo check use date and subject when crawler implemented correctly
		// compare ids instead of full subject and date (subject might be cropped, date not implemented yet)
		String sendId = idInjectorService.extractId(sendMail.getSubject());
		String receivedId = idInjectorService.extractId(receivedMail.getSubject());
		return sendId.equals(receivedId);
	}

	private List<Mail> getAllReceivedEmails() throws InterruptedException {
		// run all crawlers simultaneously
		final CountDownLatch latch = new CountDownLatch(crawlers.size());
		final List<Mail> receivedMails = new CopyOnWriteArrayList<Mail>();
		ExecutorService executor = Executors.newFixedThreadPool(crawlers.size());

		for (final BaseCrawler crawler : crawlers) {
			executor.execute(new Runnable() {
				public void run() {
					try {
						List<Mail> mails = crawler.fetchEmails();
						receivedMails.addAll(mails);
					} catch (Exception e) {
						log.error("Error while crawling for emails using " + crawler.getClass().getSimpleName(), e);
					} finally {
						latch.countDown();
					}
				}
			});
		}
		latch.await();
		return receivedMails;
	}
}