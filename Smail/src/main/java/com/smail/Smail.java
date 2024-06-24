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

import org.json.simple.JSONObject;

import com.smail.custom_exception.AuthenticationFailedException;
import com.smail.custom_exception.EmailAlreadyExistsException;
import com.smail.custom_exception.InvalidInputException;

@WebServlet("/")
public class Smail extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            Message message = MessageOperation.getMessage(folderName, messageId);
            sendJsonResponse(response, HttpServletResponse.SC_OK, message);
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Please check the URL and try again. " + e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later. Error details: " + e.getMessage());
        }
    }

    private void signUp(HttpServletRequest request, HttpServletResponse response) {
        String name = request.getParameter("name");
        String smail = request.getParameter("smail");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!password.equals(confirmPassword)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Password mismatch.");
            return;
        }
            try {
				if (Authentication.signUp(name, smail, password)) {
				    sendSuccessResponse(response, "Sign up successful. Please sign in.");
				} else {
				    sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, "That email id is taken. Try another.");
				}
			} catch (InvalidInputException e) {
	            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "An unexpected error occurred. Please try again later. Error details: " + e.getMessage());
			} catch (EmailAlreadyExistsException e) {
	            sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, "An unexpected error occurred. Please try again later. Error details: " + e.getMessage());
			} catch (Exception e) {
	            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later. Error details: " + e.getMessage());
			}
    }

    private void signIn(HttpServletRequest request, HttpServletResponse response) {
        String smail = request.getParameter("smail");
        String password = request.getParameter("password");

        try {
            User user = Authentication.signIn(smail, password);
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            sendSuccessResponse(response, "Sign in successful.");
        } catch (AuthenticationFailedException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later. Error details: " + e.getMessage());
        }
    }

    private void displayMessages(HttpServletRequest request, HttpServletResponse response, String folderName) {
        HttpSession session = request.getSession(false);
        try {
            if (session != null && session.getAttribute("user") != null) {
                List<Message> messages = MessageOperation.getMessages(folderName);
                sendJsonResponse(response, HttpServletResponse.SC_OK, messages);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "User session expired. Please sign in again.");
            }
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later. Error details: " + e.getMessage());
        }
    }

    private void signOut(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        session.invalidate();
        sendSuccessResponse(response, "Sign out successful.");
    }

    private void sendJsonResponse(HttpServletResponse response, int statusCode, Object responseObject) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("data", responseObject);
        out.print(jsonResponse.toJSONString());
        out.flush();
    }

    private void sendSuccessResponse(HttpServletResponse response, String message) {
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", true);
            jsonResponse.put("message", message);
            sendJsonResponse(response, HttpServletResponse.SC_OK, jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String error) {
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", false);
            jsonResponse.put("message", error);
            sendJsonResponse(response, statusCode, jsonResponse);
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
