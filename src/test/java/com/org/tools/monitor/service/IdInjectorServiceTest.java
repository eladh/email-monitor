package com.org.tools.monitor.service;

import org.junit.Assert;
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
public class IdInjectorServiceTest {

	@Inject
	private IdInjectorService idInjectorService;

	@Test
	public void testInjectId() throws Exception {
		Assert.assertEquals("123~string", idInjectorService.injectId("string", "123"));
	}

	@Test
	public void testExtractId() throws Exception {
		String string = "444~string string 123";
		String newString = idInjectorService.injectId(string, "555");
		Assert.assertEquals("555", idInjectorService.extractId(newString));
	}
}
