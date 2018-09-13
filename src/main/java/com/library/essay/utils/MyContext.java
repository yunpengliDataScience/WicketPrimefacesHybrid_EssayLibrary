package com.library.essay.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class MyContext implements AutoCloseable {

	private static ThreadLocal<MyContext> instance = new ThreadLocal<>();

	private HttpServletRequest request;
	private HttpServletResponse response;

	private MyContext(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}

	public static MyContext create(HttpServletRequest request, HttpServletResponse response) {
		MyContext context = new MyContext(request, response);
		instance.set(context);
		return context;
	}

	public static MyContext getCurrentInstance() {
		return instance.get();
	}

	@Override
	public void close() {
		instance.remove();
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpSession getSession() {
		return request.getSession();
	}

}
