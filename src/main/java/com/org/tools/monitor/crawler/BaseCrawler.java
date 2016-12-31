/*
 * User: eladh
 * Date: 03/10/13 
 *
 * .
 *
 * Created by IntelliJ IDEA. 
 */
package com.org.tools.monitor.crawler;

import com.google.common.collect.Lists;
import com.org.tools.monitor.model.Folder;
import com.org.tools.monitor.model.Mail;
import org.apache.http.annotation.NotThreadSafe;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@NotThreadSafe
public abstract class BaseCrawler {
	private static final Logger log = LoggerFactory.getLogger(BaseCrawler.class.getName());

	private WebDriver driver;

	public List<Mail> fetchEmails() {
		try {
			startCrawling();
			login();
			List<Mail> mailList = getMailList();
			return mailList;
		} finally {
			stopCrawling();
		}
	}

	protected void startCrawling() {
		log.info("{} started crawling", getClass().getSimpleName());
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	protected List<Mail> getEmailsInCurrentFolder(Folder folder) {
		waitForLoad();
		List<Mail> resultEmails = Lists.newArrayList();

		List<WebElement> emailRows = getMailRows();
		for (final WebElement row : emailRows) {
			Mail mail;

			try {
				mail = parseEmailData(row);
			} catch (RuntimeException e) {
				log.error("Exception in reading email", e);
				continue;
			}
			if (mail == null) {
				continue;
			}

			mail.setFolder(folder);
			resultEmails.add(mail);
		}

		return resultEmails;
	}

	protected void waitForLoad() {
		ExpectedCondition<Boolean> pageLoadCondition = new
				ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver driver) {
						return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
					}
				};
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(pageLoadCondition);

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected WebDriver getDriver() {
		return driver;
	}

	protected void stopCrawling() {
		if (driver != null) {
			driver.quit();
		}
		log.info("{} finished crawling", getClass().getSimpleName());
	}

	protected List<Mail> getMailList() {
		List<Mail> mailList = Lists.newArrayList();
		moveToInboxFolder();
		mailList.addAll(getEmailsInCurrentFolder(Folder.INBOX));
		moveToSpamFolder();
		mailList.addAll(getEmailsInCurrentFolder(Folder.SPAM));
		return mailList;
	}

	protected abstract List<WebElement> getMailRows();

	protected abstract void login();

	protected abstract Mail parseEmailData(WebElement row);

	protected abstract void moveToSpamFolder();

	protected abstract void moveToInboxFolder();

	public abstract String getAccountAddress();
}