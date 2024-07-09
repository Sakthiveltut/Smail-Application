package com.smail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.smail.custom_exception.AuthenticationFailedException;
import com.smail.custom_exception.EmailAlreadyExistsException;
import com.smail.custom_exception.InvalidEmailException;
import com.smail.custom_exception.InvalidInputException;


@WebServlet("/")
public class Smail extends HttpServlet {
	
	@Override
	public void init() throws ServletException {
		try {
			Folder.setFolders();
			RecipientType.setRecipientTypes();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static final String STATUS_SUCCESS = "success", STATUS_FAILED = "failed";
	private MessageOperation messageOperation = new MessageOperation();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response){
        String action = request.getServletPath();
        System.out.println(action);
        
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
        	        	
        	String url[] = action.split("/");
        	byte size = (byte) url.length;
        	if(size==2 && Folder.getFolders().containsKey(url[1])) {
        		System.out.println("if block "+url[1]);
        		displayMessages(request, response, url[1]);
        		
	        } else if (size==3 && "messageDetails".equals(url[size-1])) {
	        	if(Folder.getFolders().containsKey(url[1])) {
		            displayMessageDetails(request, response, url[1]);
	        	}
	        } else if (size==3 && "star".equals(url[size-1])) {
 	        	if(Folder.getFolders().containsKey(url[1])) {
 		            starMessage(request, response, url[1]);
 	        	} 
	        } else if ("/signout".equals(action)) {
	            signOut(request, response);
	        }else {
	        	System.out.println("url not found");
	        }
     
        } else {
        	sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED,STATUS_FAILED, "User session expired. Please sign in again.",null);
        }
    }
    
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String action = request.getServletPath();

        if ("/signup".equals(action)) {
            signUp(request, response);
        } else if ("/signin".equals(action)) {
            signIn(request, response);
        } else if ("/sendMessage".equals(action)) {
        	sendMessage(request, response,"sendMessage");
        } else if ("/saveDraft".equals(action)) {
        	saveDraftMessage(request, response);        	
        } 
    }

    private void sendMessage(HttpServletRequest request, HttpServletResponse response, String messageType) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader;
		try {
			reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);}
		} catch (IOException e) {
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,STATUS_FAILED,e.getMessage(),null);
			return;
		}
    	System.out.println("Data"+sb.toString());
    	
        if (sb.toString().isEmpty()) {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, STATUS_FAILED, "Empty request body", null);
            return;
        }

		JSONParser parser = new JSONParser();
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) parser.parse(sb.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}    
	    
	    Long messageId = null;
	    if (jsonObject.get("id") != null && !jsonObject.get("id").toString().isEmpty()) {
	        try {
	            messageId = Long.parseLong(jsonObject.get("id").toString());
	        } catch (NumberFormatException e) {
	            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, STATUS_FAILED, "Invalid message ID format", null);
	            return;
	        }
	    }
	    
	    System.out.println(messageId);
    	String to = (String)jsonObject.get("to");
    	String cc = (String)jsonObject.get("cc");
    	String subject = (String)jsonObject.get("subject");
    	String description = (String)jsonObject.get("description");
    	messageOperation.inputMessageDetails(to,cc,subject,description);
		try {
			JSONObject message = null;
			if(messageId==null) {
				 message = messageOperation.createMessage();
			}else {
				message = messageOperation.getMessage(Folder.getDraftName(),messageId);
			}			
			messageOperation.sendMessage(messageId==null?"newMessage":"draftMessage", message);
            sendResponse(response, HttpServletResponse.SC_OK,STATUS_SUCCESS, null, message);
		} catch (InvalidInputException | InvalidEmailException e) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST,STATUS_FAILED, e.getMessage(),null);
		} catch (Exception e) {
			e.printStackTrace();
        	sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,STATUS_FAILED, e.getMessage(),null);
		}	
    }
    
    private void saveDraftMessage(HttpServletRequest request, HttpServletResponse response) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader;
		try {
			reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,STATUS_FAILED,e.getMessage(),null);
			return;
		}
    	
        if (sb.toString().isEmpty()) {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, STATUS_FAILED, "Empty request body", null);
            return;
        }

		JSONParser parser = new JSONParser();
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) parser.parse(sb.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}    
	    
	    Long messageId = null;
	    if (jsonObject.get("id") != null && !jsonObject.get("id").toString().isEmpty()) {
	        try {
	            messageId = Long.parseLong(jsonObject.get("id").toString());
	        } catch (NumberFormatException e) {
	            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, STATUS_FAILED, "Invalid message ID format", null);
	            return;
	        }
	    }

	    String to = (String)jsonObject.get("to");
    	String cc = (String)jsonObject.get("cc");
    	String subject = (String)jsonObject.get("subject");
    	String description = (String)jsonObject.get("description");
    	messageOperation.inputMessageDetails(to,cc,subject,description);
		try {
			JSONObject message = null;
			if(messageId==null) {
				 message = messageOperation.createMessage();
				 messageOperation.setMessageFolder(UserDatabase.getCurrentUser().getUserId(),(long)message.get("id"),Folder.getFolderId(Folder.getDraftName()));
			}else {
				message = messageOperation.getMessage(Folder.getDraftName(),messageId);
				messageOperation.updateDraftMessage(message);
			}
            sendResponse(response, HttpServletResponse.SC_OK,STATUS_SUCCESS, null, message);
		} catch (InvalidInputException | InvalidEmailException e) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST,STATUS_FAILED, e.getMessage(),null);
		} catch (Exception e) {
			e.printStackTrace();
        	sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,STATUS_FAILED, e.getMessage(),null);
		}	
    }
    
    private void starMessage(HttpServletRequest request, HttpServletResponse response,String folderName) {
    	long messageId = Long.parseLong(request.getParameter("id"));
    	try {
			messageOperation.starredMessage(messageId,Folder.getFolderId(folderName));
		} catch (Exception e) {
			e.printStackTrace();
        	sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,STATUS_FAILED, e.getMessage(),null);
		}
    }

    private void displayMessageDetails(HttpServletRequest request, HttpServletResponse response, String folderName) {
        Long messageId = null;
        try {
            String messageIdStr = request.getParameter("id");
            if (messageIdStr != null) {
                messageId = Long.parseLong(messageIdStr);
            }
            JSONObject message = messageOperation.getMessage(folderName, messageId);
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
        if (password!=null && !password.equals(confirmPassword)) {
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
    	System.out.println("displayMessages "+folderName);
        try {
            JSONArray messages = MessageOperation.getMessages(folderName);
            sendResponse(response, HttpServletResponse.SC_OK,STATUS_SUCCESS,null, messages);
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
	        out.print(jsonResponse.toString());
	        out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}