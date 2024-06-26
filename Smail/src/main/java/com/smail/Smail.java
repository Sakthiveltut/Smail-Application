package com.smail;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.smail.custom_exception.AuthenticationFailedException;
import com.smail.custom_exception.EmailAlreadyExistsException;
import com.smail.custom_exception.InvalidInputException;

@WebServlet("/")
public class Smail extends HttpServlet {
	
	private static final String STATUS_SUCCESS = "success", STATUS_FAILED = "failed";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response){
        String action = request.getServletPath();

        if (("/" + Folder.getInboxName()).equals(action)) {
            displayMessages(request, response, Folder.getInboxName());
        } else if (("/" + Folder.getSentName()).equals(action)) {
            displayMessages(request, response, Folder.getSentName());
        } else if (("/" + Folder.getDraftName()).equals(action)) {
            displayMessages(request, response, Folder.getDraftName());
        } else if (("/" + Folder.getSpamName()).equals(action)) {
            displayMessages(request, response, Folder.getSpamName());
        } else if (("/" + Folder.getBinName()).equals(action)) {
            displayMessages(request, response, Folder.getBinName());
        } else if (("/" + Folder.getStarredName()).equals(action)) {
            displayMessages(request, response, Folder.getStarredName());
            
        } else if ((("/" + Folder.getInboxName()) + "/messageDetails").equals(action)) {
            displayMessageDetails(request, response, Folder.getInboxName());
        } else if ((("/" + Folder.getSentName()) + "/messageDetails").equals(action)) {
            displayMessageDetails(request, response, Folder.getSentName());
        } else if ((("/" + Folder.getDraftName()) + "/messageDetails").equals(action)) {
            displayMessageDetails(request, response, Folder.getDraftName());
        } else if ((("/" + Folder.getSpamName()) + "/messageDetails").equals(action)) {
            displayMessageDetails(request, response, Folder.getSpamName());
        } else if ((("/" + Folder.getBinName()) + "/messageDetails").equals(action)) {
            displayMessageDetails(request, response, Folder.getBinName());
        } else if ((("/" + Folder.getStarredName()) + "/messageDetails").equals(action)) {
            displayMessageDetails(request, response, Folder.getStarredName());
            
        } else if ("/signout".equals(action)) {
            signOut(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String action = request.getServletPath();

        if ("/signup".equals(action)) {
            signUp(request, response);
        } else if ("/signin".equals(action)) {
            signIn(request, response);
        }
    }

    private void displayMessageDetails(HttpServletRequest request, HttpServletResponse response, String folderName) {
        Long messageId = null;
        try {
            String messageIdStr = request.getParameter("id");
            if (messageIdStr != null) {
                messageId = Long.parseLong(messageIdStr);
            }
            JSONObject message = MessageOperation.getMessage(folderName, messageId);
            sendResponse(response, HttpServletResponse.SC_OK,STATUS_SUCCESS, null, message);
        } catch (NumberFormatException e) {
        	sendResponse(response, HttpServletResponse.SC_BAD_REQUEST,STATUS_FAILED,"Please check the URL and try again. " + e.getMessage(),null);
        } catch (Exception e) {
        	sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,STATUS_FAILED, "An unexpected error occurred. Please try again later. Error details: " + e.getMessage(),null);
        }
    }

    private void signUp(HttpServletRequest request, HttpServletResponse response) {
        String name = request.getParameter("name");
        String smail = request.getParameter("smail");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        if (!password.equals(confirmPassword)) {
        	sendResponse(response, HttpServletResponse.SC_BAD_REQUEST,STATUS_FAILED ,"Password mismatch.", null);
            return;
        }
            try {
				if (Authentication.signUp(name, smail, password)) {
					sendResponse(response,HttpServletResponse.SC_OK,STATUS_SUCCESS,"Sign up successful. Please sign in.", null);
				}
			} catch (InvalidInputException e) {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST,STATUS_FAILED, e.getMessage(),null);
			} catch (EmailAlreadyExistsException e) {
				sendResponse(response, HttpServletResponse.SC_CONFLICT,STATUS_FAILED, e.getMessage(),null);
			} catch (Exception e) {
				sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,STATUS_FAILED,e.getMessage(),null);
			}
    }

    private void signIn(HttpServletRequest request, HttpServletResponse response) {
        String smail = request.getParameter("smail");
        String password = request.getParameter("password");
        try {
            User user = Authentication.signIn(smail, password);
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            sendResponse(response,HttpServletResponse.SC_OK,STATUS_SUCCESS, "Sign in successful.", null);
        } catch (InvalidInputException e) {
        	sendResponse(response, HttpServletResponse.SC_BAD_REQUEST,STATUS_FAILED, e.getMessage(),null);
        }  catch (AuthenticationFailedException e) {
        	sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED,STATUS_FAILED, e.getMessage(), null);
        } catch (Exception e) {
        	sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,STATUS_FAILED, e.getMessage(),null);
        }
    }

    private void displayMessages(HttpServletRequest request, HttpServletResponse response, String folderName) {
        HttpSession session = request.getSession(false);
        try {
            if (session != null && session.getAttribute("user") != null) {
				request.setAttribute("folderName", folderName);
                JSONArray messages = MessageOperation.getMessages(folderName);
                sendResponse(response, HttpServletResponse.SC_OK,STATUS_SUCCESS,null, messages);
            } else {
            	sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED,STATUS_FAILED, "User session expired. Please sign in again.",null);
            }
        } catch (Exception e) {
        	sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,STATUS_FAILED,e.getMessage(),null);
        }
    }

    private void signOut(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        session.invalidate();
        sendResponse(response,HttpServletResponse.SC_OK,STATUS_SUCCESS, "Sign out successful.", null);
    }

    @SuppressWarnings("unchecked")
	private void sendResponse(HttpServletResponse response, int statusCode,String status,String message, Object jsonData){

        JSONObject response_status = new JSONObject();
        response_status.put("status_code", statusCode);
        response_status.put("status", status);
        response_status.put("message", message);
        
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("response_status", response_status);
        jsonResponse.put("data", jsonData);
        System.out.println(jsonResponse);
        
		try(PrintWriter out = response.getWriter()) {
	        out.print(jsonResponse);
	        out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}


/*
@WebServlet("/")
public class Smail extends HttpServlet {
	
	//private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		String action = request.getServletPath();
		
				
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
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) {
		doPost(request,response);
	}

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
			request.getRequestDispatcher("/messageDetails.jsp").forward(request, response);
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
						request.getRequestDispatcher("signin.jsp").forward(request, response);
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
			request.getRequestDispatcher("home.jsp").forward(request, response);
		}catch (AuthenticationFailedException e) {
			request.setAttribute("error",e.getMessage());
			try {
				response.sendRedirect("signin.jsp");
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
	
	
}*/