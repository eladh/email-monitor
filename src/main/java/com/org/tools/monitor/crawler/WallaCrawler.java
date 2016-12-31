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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.List;

@NotThreadSafe
public class WallaCrawler extends BaseCrawler {
	private static final Logger log = LoggerFactory.getLogger(WallaCrawler.class.getName());

	private static final String BASE_URL = "http://newmail.walla.co.il/";
	private static final String EMAIL_ACCOUNT = "eladhr80@walla.com"; // pass -> zooloo1

	public WallaCrawler() {
		super();
	}

	protected List<WebElement> getMailRows() {
		return getDriver().findElements(By.xpath("//tr[@class='walLine_Unread']"));
	}

	@Override
	public void moveToSpamFolder() {
		getDriver().get(BASE_URL + "#folder:-1001:0:1");
		waitForLoad();
	}

	@Override
	public void moveToInboxFolder() {
		getDriver().get(BASE_URL + "#folder:-1000:0:1");
		waitForLoad();
	}

	@Override
	public void login() {
		WebDriver driver = getDriver();
		driver.get("https://friends.walla.co.il/?w=/@frame&l_count=0&ReturnURL=http://newmail.walla.co.il/ts.cgi&error=&username=&srv=");
		waitForLoad();
		By username = By.name("username");
		driver.findElement(username).clear();
		driver.findElement(username).sendKeys(EMAIL_ACCOUNT);
		By password = By.name("password");
		driver.findElement(password).clear();
		driver.findElement(password).sendKeys("WSD8bdjfei");
		driver.findElement(By.cssSelector("div.subTopCenter")).click();
	}

	public Mail parseEmailData(WebElement row) {
		Mail mail = new Mail();
		List<WebElement> cells = row.findElements(By.tagName("td"));
		mail.setTo(getAccountAddress());
		mail.setFrom(cells.get(2).findElement(By.tagName("a")).getAttribute("title"));
		mail.setSubject(cells.get(3).getText());
		String dateString = cells.get(4).getText();
		try {
			mail.setDate(DateFormat.getDateInstance().parse(dateString));
		} catch (ParseException e) {
			log.warn("Failed to parse date {}", dateString);
		}
		return mail;
	}

	@Override
	public String getAccountAddress() {
		return EMAIL_ACCOUNT;
	}
}
