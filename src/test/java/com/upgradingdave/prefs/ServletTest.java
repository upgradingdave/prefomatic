package com.upgradingdave.prefs;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import com.upgradingdave.prefs.InitServlet;

public class ServletTest {
	protected void setUp() {
	}

	protected void tearDown() {
	}

	/*
	 * Just a convenient way to figure out how to find path relative to servlet
	 */
	@Test
	public void testRelativeFilePaths() {
		ServletRunner sr = new ServletRunner();
		sr.registerServlet("initServlet", InitServlet.class.getName());
		
		ServletUnitClient sc = sr.newClient();
	    WebRequest request = new PostMethodWebRequest( "http://localhost:8080/initServlet" );
	    
	    try {
			InvocationContext ic = sc.newInvocation( request );
			InitServlet is = (InitServlet) ic.getServlet();
			assertNotNull(is);
		} catch (Exception e) {
			throw new AssertionError(e);
		}	    
	}
}
