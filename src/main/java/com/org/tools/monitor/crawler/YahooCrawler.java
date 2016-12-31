/*
 * User: eladh
 * Date: 16/09/13
 *
 * .
 *
 * Created by IntelliJ IDEA.
 */
package com.org.tools.monitor.crawler;


import com.org.tools.monitor.model.Mail;
import org.apache.http.annotation.NotThreadSafe;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Date;
import java.util.List;

@NotThreadSafe
public class YahooCrawler extends BaseCrawler {
	private static final String BASE_URL = "http://mail.yahoo.com/";
	private static final String EMAIL_ACCOUNT = "eladhr83@yahoo.com "; // pass ->   Zooloo123

	public YahooCrawler() {
		super();
	}

	protected List<WebElement> getMailRows() {
		return getDriver().findElements(By.xpath("//div[@class='flex']"));
	}

	@Override
	public void moveToSpamFolder() {
		getDriver().findElement(By.xpath("//a/span[text() = 'Spam']")).findElement(By.xpath("..")).click();
		waitForLoad();

	}

	@Override
	public void moveToInboxFolder() {
		getDriver().findElement(By.xpath("//span[text() = 'Inbox']")).findElement(By.xpath("..")).click();
		waitForLoad();
	}


	@Override
	public void login() {
		WebDriver driver = getDriver();
		driver.get(BASE_URL);

		By username = By.id("username");
		driver.findElement(username).clear();
		driver.findElement(username).sendKeys(EMAIL_ACCOUNT);
		driver.findElement(username).clear();
		driver.findElement(username).sendKeys(EMAIL_ACCOUNT);

		By password = By.id("passwd");
		driver.findElement(password).clear();
		driver.findElement(password).sendKeys("Zooloo123");
		driver.findElement(By.id(".save")).click();
	}

	@Override
	public Mail parseEmailData(WebElement row) {
		Mail mail = new Mail();
		mail.setTo(getAccountAddress());
		mail.setFrom(row.findElement(By.tagName("div")).getText());
		mail.setSubject(row.findElement(By.xpath("div[@class='subj']")).findElement(By.tagName("span")).getText());
		mail.setDate(new Date()); // row.findElement(By.xpath(".//div[@class='date']")).getAttribute("title")
		return mail;
	}

	@Override
	public String getAccountAddress() {
		return EMAIL_ACCOUNT;
	}

}
