package com.upgradingdave.prefs;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;

public class InitServlet extends HttpServlet {

	private static final long serialVersionUID = 2177268415732295833L;

	/*
	 * Be sure to set app-name inside web.xml for all webapps. The value of
	 * app-name is appended to the value of the OS Environment variable
	 * to determine the directory path for where to find log4j.properties as
	 * well as applicaiton level properties files
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() {
		String appName = getInitParameter("app-name");
		Configuration prefs = PrefsHelper.getConfig(appName);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) {
	}

	public String getRelativePath() {
		return getServletContext().getRealPath("/");
	}
}
