package com.library.essay.tinymce.spellchecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * JSpellCheckerContextListener.java
 * Created on 5/13/13 by Andrey Chorniy
 */
public class JSpellCheckerContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(JSpellCheckerContextListener.class);
    @Override
    public void contextInitialized(ServletContextEvent sce) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.debug("started contextDestroyed()");

        try {

        } catch (Exception e) {

        }
        logger.debug("finished contextDestroyed()");
    }
}