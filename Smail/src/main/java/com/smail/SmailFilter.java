package com.smail;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

@WebFilter("/*")
public class SmailFilter implements Filter{
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpSession session = httpRequest.getSession(false);
			
			if(session!=null && session.getAttribute("user")!=null) {
				User currentUser = (User)session.getAttribute("user");
				UserDatabase.setCurrentUser(currentUser);
				session.setMaxInactiveInterval(5*60);
			}
			chain.doFilter(request, response);
		}finally {
			UserDatabase.clearCurrentUser();
		}
	}
}
