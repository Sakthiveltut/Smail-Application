package com.smail;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.smail.custom_exception.AuthenticationFailedException;


@WebServlet("/")
public class Smail extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		String action = request.getServletPath();
		
		String splitUrl[] = action.split("/");
				
		if(("/"+Folder.getInboxName()).equals(action)) {
			displayMessages(request,response,Folder.getInboxName());
		}else if(("/"+Folder.getSentName()).equals(action)) {
			displayMessages(request,response,Folder.getSentName());
		}else if(("/"+Folder.getDraftName()).equals(action)) {
			displayMessages(request,response,Folder.getDraftName());
		}else if(("/"+Folder.getSpamName()).equals(action)) {
			displayMessages(request,response,Folder.getSpamName());
		}else if(("/"+Folder.getBinName()).equals(action)) {
			displayMessages(request,response,Folder.getBinName());
		}else if(("/"+Folder.getStarredName()).equals(action)) {
			displayMessages(request,response,Folder.getStarredName());
			
		}else if((("/"+Folder.getInboxName())+"/messageDetails").equals(action)) {
			displayMessageDetails(request,response,Folder.getInboxName());
		}else if((("/"+Folder.getSentName())+"/messageDetails").equals(action)) {
			displayMessageDetails(request,response,Folder.getSentName());
		}else if((("/"+Folder.getDraftName())+"/messageDetails").equals(action)) {
			displayMessageDetails(request,response,Folder.getDraftName());
		}else if((("/"+Folder.getSpamName())+"/messageDetails").equals(action)) {
			displayMessageDetails(request,response,Folder.getSpamName());
		}else if((("/"+Folder.getBinName())+"/messageDetails").equals(action)) {
			displayMessageDetails(request,response,Folder.getBinName());
		}else if((("/"+Folder.getStarredName())+"/messageDetails").equals(action)) {
			displayMessageDetails(request,response,Folder.getStarredName());
			
		}else if("/signout".equals(action)) {
			signOut(request,response);
		}
	}
	
	/*@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) {
		doPost(request,response);
	}*/

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		String action = request.getServletPath();
		
		if("/signup".equals(action)) {
			signUp(request,response);
		}else if("/signin".equals(action)){
			signIn(request,response);
		}
	}
	
	private void displayMessageDetails(HttpServletRequest request, HttpServletResponse response,String folderName) {
		Long messageId = null;
		try {
			String messageIdStr = request.getParameter("id");
			if(messageIdStr!=null) {
				messageId = Long.parseLong(messageIdStr);
			}
			Message message = MessageOperation.getMessage(folderName,messageId);
			request.setAttribute("message", message);
			request.getRequestDispatcher("messageDetails.jsp").forward(request, response);
		}catch(NumberFormatException e) {
			request.setAttribute("error","Please check the URL and try again."+ e.getMessage());
		}catch(Exception e) {
			request.setAttribute("error","An unexpected error occurred. Please try again later. Error details: "+ e.getMessage());
		}
	}

	private void signUp(HttpServletRequest request, HttpServletResponse response){
		String name = request.getParameter("name");
		String smail = request.getParameter("smail");
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("confirmPassword");
				
		if(password.equals(confirmPassword)) {
			try {
				if(Authentication.signUp(name,smail,password)) {
					try {
						response.sendRedirect("signin.jsp");
					} catch (IOException e) {
						request.setAttribute("error","An unexpected error occurred. Please try again later. Error details: "+ e.getMessage());
					}
				}else {
					request.setAttribute("error","That email id is taken.Try another.");;
					try {
						request.getRequestDispatcher("signup.jsp").forward(request, response);
					} catch (Exception e) {
						request.setAttribute("error","An unexpected error occurred. Please try again later. Error details: "+ e.getMessage());
					}
				}
			} catch (Exception e) {
				request.setAttribute("error",e.getMessage());
			}
		}else {
			request.setAttribute("error","Password mismatch.");
			try {
				request.getRequestDispatcher("signup.jsp").forward(request, response);
			} catch (Exception e) {
				request.setAttribute("error","An unexpected error occurred. Please try again later. Error details: "+ e.getMessage());
			} 
		}
	}
	
	private void signIn(HttpServletRequest request, HttpServletResponse response) {
		String smail = request.getParameter("smail");
		String password = request.getParameter("password");
		try {
			User user = Authentication.signIn(smail,password);
			HttpSession session = request.getSession();
			session.setAttribute("user", user);
			response.sendRedirect("home.jsp");
		}catch (AuthenticationFailedException e) {
			request.setAttribute("error",e.getMessage());
			try {
				request.getRequestDispatcher("signin.jsp").forward(request, response);
			} catch (Exception e1) {
				request.setAttribute("error","An unexpected error occurred. Please try again later. Error details: "+ e1.getMessage());
			}
		}  catch (Exception e) {
			request.setAttribute("error","An unexpected error occurred. Please try again later. Error details: "+ e.getMessage());
		}
	}
	
	private void displayMessages(HttpServletRequest request, HttpServletResponse response, String folderName) {
		HttpSession session = request.getSession(false);
		try {
			if(session!=null && session.getAttribute("user")!=null) {
				request.setAttribute("folderName", folderName);
				List<Message> messages = MessageOperation.getMessages(folderName);
				request.setAttribute("messages", messages);
				
				request.getRequestDispatcher("home.jsp").forward(request, response);
			}else {
				response.sendRedirect("signin.jsp");
			}
		} catch (Exception e) {
				request.setAttribute("error","An unexpected error occurred. Please try again later. Error details: "+ e.getMessage());
				e.printStackTrace();
		}
	}
	
	private void signOut(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		session.invalidate();
	}
}