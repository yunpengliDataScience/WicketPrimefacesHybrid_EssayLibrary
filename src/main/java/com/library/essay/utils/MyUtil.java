package com.library.essay.utils;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;

public class MyUtil {

	public static void exploreHttpServeletRequest(Logger logger) {
		MyContext context = MyContext.getCurrentInstance();

		HttpServletRequest request = context.getRequest();

		String ip = request.getRemoteHost();

		logger.info("----------------------------------");
		logger.info("IP: " + ip);
		logger.info("----------------------------------");

		System.out.print("----------------------------------");
		System.out.print("IP: " + ip);
		System.out.print("----------------------------------");

		HttpSession httpSession = request.getSession(false);

		Enumeration sessionEnumeration = httpSession.getAttributeNames();

		logger.info("----------------------------------");
		logger.info("Objects in Session:");
		while (sessionEnumeration.hasMoreElements()) {
			Object key = sessionEnumeration.nextElement();

			Object value = httpSession.getAttribute((String) key);

			logger.info("Object: " + key.toString() + " Type: " + value.getClass());
		}

		logger.info("----------------------------------");
	}
}
