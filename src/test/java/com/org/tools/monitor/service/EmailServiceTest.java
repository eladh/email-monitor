package com.org.tools.monitor.service;

import com.org.tools.monitor.model.Mail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

/**
 * @author Elad Hirsch
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class EmailServiceTest {

	@Inject
	private EmailService emailService;

	@Test
	public void testSendEmails() throws Exception {
		Mail mail = emailService.sendEmail(39447232, "hirsch.elad@gmail.com");

	}
}
