package com.library.essay;

import javax.servlet.ServletContext;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.library.essay.web.EssayListPage;
import com.library.essay.web.HomePage;


public class MainApplication extends WebApplication {

	private SpringComponentInjector springComponentInjector;
	private ApplicationContext applicationContext;

	public SpringComponentInjector getSpringComponentInjector() {
		return springComponentInjector;
	}

	public void setSpringComponentInjector(
			SpringComponentInjector springComponentInjector) {
		this.springComponentInjector = springComponentInjector;
	}

	public MainApplication() {
	}

	@Override
	public Class<? extends Page> getHomePage() {

		return HomePage.class;
	}

	@Override
	protected void init() {
		super.init();

		// The wicket-spring-annot project ships with a special component
		// instantiation listener
		// that analyzes the components you construct and injects proxies for
		// all the
		// Spring-bean-annotated members it finds.
		if (springComponentInjector == null) {
			this.springComponentInjector = new SpringComponentInjector(this);
		}
		getComponentInstantiationListeners().add(springComponentInjector);

		
		ServletContext servletContext = super.getServletContext();
		applicationContext = WebApplicationContextUtils
				.getWebApplicationContext(servletContext);
		
		//Mount bookmarkable page to a specific url.
		this.mountPage("essayListPage", EssayListPage.class);

	}

	public Object getBean(String name) {
		if (name == null)
			return null;

		return applicationContext.getBean(name);
	}

}

