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
public class CurrentUser implements Filter{
	
	private static ThreadLocal<User> user = new ThreadLocal<User>();
	
	public static User get() {
		return user.get();
	}
	
	public static void set(User currentUser) {
		user.set(currentUser);
	}
	
	public static void clear() {
		user.remove();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpSession session = httpRequest.getSession(false);
			
			if(session!=null && session.getAttribute("user")!=null) {
				User currentUser = (User)session.getAttribute("user");
				user.set(currentUser);
			}
			chain.doFilter(request, response);
		}finally {
			clear();
		}
	}
}
