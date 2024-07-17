package com.smail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.smail.custom_exception.AuthenticationFailedException;
import com.smail.custom_exception.EmailAlreadyExistsException;
import com.smail.custom_exception.InvalidEmailException;
import com.smail.custom_exception.InvalidInputException;

@WebServlet("/")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, // 10 MB
		maxFileSize = 1024 * 1024 * 50, // 50 MB
		maxRequestSize = 1024 * 1024 * 100) // 100 MB
public class Smail extends HttpServlet {

	private static final String SAVE_DIR = "D:\\Sakthi\\Temp\\Attachments";

	@Override
	public void init() {
		try {
			Folder.setFolders();
			RecipientType.setRecipientTypes();
			Attachment.loadFileTypes();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static final String STATUS_SUCCESS = "success", STATUS_FAILED = "failed";
	private MessageOperation messageOperation = new MessageOperation();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		String action = request.getServletPath();
		System.out.println("doGet" + action);

		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute("user") != null) {

			String url[] = action.split("/");
			System.out.println(Arrays.toString(url));
			byte size = (byte) url.length;

			if ("/profile".equals(action)) {
				profile(response);
			} else if (size == 2 && (Folder.getFolders().containsKey(url[1]) || Folder.getStarredName().equals(url[1])
					|| "unread".equals(url[1]))) {
				displayMessages(request, response, url[1]);

			} else if (size == 3 && "messageDetails".equals(url[size - 1])) {
				if (Folder.getFolders().containsKey(url[1]) || Folder.getStarredName().equals(url[1])
						|| "unread".equals(url[1])) {
					displayMessageDetails(request, response, url[1]);
				}
			} else if (size == 3 && "star".equals(url[size - 1])) {
				if (Folder.getFolders().containsKey(url[1]) || Folder.getStarredName().equals(url[1])
						|| "unread".equals(url[1])) {
					starMessage(request, response, url[1]);
				}
			} else if ("/signout".equals(action)) {
				signOut(request, response);
				
			} else if ("/attachment".equals(action)) {
						downloadAttachment(request, response);
			} else {
				System.out.println("Url not found");
				sendResponse(response, HttpServletResponse.SC_NOT_FOUND, STATUS_FAILED, "Page not found.", null);
			}
		} else {
			sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED, STATUS_FAILED,
					"User session expired. Please sign in again.", null);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		String action = request.getServletPath();
		System.out.println("doPost " + action);

		if ("/signup".equals(action)) {
			signUp(request, response);
		} else if ("/signin".equals(action)) {
			signIn(request, response);
		} else if ("/sendMessage".equals(action)) {
			sendMessage(request, response, "sendMessage");
		} else if ("/saveDraft".equals(action)) {
			saveDraftMessage(request, response);
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
		String action = request.getServletPath();
		System.out.println("doDelete " + action);

		String url[] = action.split("/");
		byte size = (byte) url.length;

		if ("deleteMessages".equals(url[size - 1])) {
			if (Folder.getFolders().containsKey(url[1]) || Folder.getStarredName().equals(url[1])
					|| "unread".equals(url[1])) {
				deleteMessages(request, response, url[1]);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void profile(HttpServletResponse response) {
		User user = UserDatabase.getCurrentUser();
		JSONObject profile = new JSONObject();
		profile.put("email", user.getEmail());
		profile.put("name", user.getName());
		profile.put("lastLoginTime", user.getLastLoginTime());

		sendResponse(response, HttpServletResponse.SC_OK, STATUS_SUCCESS, "Messages deleted successfully.", profile);
	}
	
	private void downloadAttachment(HttpServletRequest request, HttpServletResponse response) {
		String attachmentIdStr = request.getParameter("attachmentId").trim();
		String messageIdStr = request.getParameter("messageId").trim();
        Long attachmentId =Long.parseLong(attachmentIdStr);
        Long messageId =Long.parseLong(messageIdStr);
        
		String filePath = null;
		try {
		filePath = messageOperation.fetchAttachmentPath(attachmentId,messageId);
		System.out.println("filePath "+filePath);

		File downloadFile = new File(filePath);
		FileInputStream inStream = null;
		inStream = new FileInputStream(downloadFile);

		String mimeType = getServletContext().getMimeType(filePath);
		System.out.println("mimeType "+mimeType); 
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		response.setContentType(mimeType);
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
		response.setHeader(headerKey, headerValue);
		OutputStream outStream = null;
		outStream = response.getOutputStream();
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		while ((bytesRead = inStream.read(buffer)) != -1) {
			System.out.println("bytesRead "+bytesRead);
			outStream.write(buffer, 0, bytesRead);
		}
		System.out.println("outStream "+outStream);
		inStream.close();
		outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, STATUS_FAILED, e.getMessage(), null);
		}
	}

	private void deleteMessages(HttpServletRequest request, HttpServletResponse response, String option) {
		System.out.println(request.getParameter("ids"));

		StringBuilder stringBuilder = new StringBuilder();
		try (BufferedReader reader = request.getReader()) {
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}
		} catch (IOException e) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, STATUS_FAILED, "Invalid request body.", null);
			return;
		}

		JSONParser parser = new JSONParser();
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) parser.parse(stringBuilder.toString());
			JSONArray messageIds = (JSONArray) jsonObject.get("ids");

			if (Folder.getBinName().equals(option)) {
				for (Object messageId : messageIds) {
					messageOperation.deleteMessage((long) messageId, Folder.getFolderId(option));
				}
			} else if (!Folder.getBinName().equals(option)) {
				for (Object messageId : messageIds) {
					messageOperation.changeMessageFolderId((long) messageId, Folder.getFolderId(option),
							Folder.getFolderId(Folder.getBinName()));
				}
			}
			sendResponse(response, HttpServletResponse.SC_OK, STATUS_SUCCESS, "Messages deleted successfully.", null);
		} catch (ParseException e) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, STATUS_FAILED, "Invalid JSON format.", null);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, STATUS_FAILED, e.getMessage(), null);
		}
	}

	private void sendMessage(HttpServletRequest request, HttpServletResponse response, String messageType) {
		try {
			String messageIdStr = request.getParameter("id");
			Long messageId = (messageIdStr != null && !messageIdStr.isEmpty()) ? Long.parseLong(messageIdStr) : null;
			String to = request.getParameter("to");
			String cc = request.getParameter("cc");
			String subject = request.getParameter("subject");
			String description = request.getParameter("description");

			messageOperation.inputMessageDetails(to, cc, subject, description);
			JSONObject message = null;
			if (messageId == null) {
				message = messageOperation.createMessage();
			} else {
				message = messageOperation.getMessage(Folder.getDraftName(), messageId);
				messageOperation.updateDraftMessage(message);
			}
			messageOperation.sendMessage(messageId == null ? "newMessage" : "draftMessage", message);
			messageId = (long) message.get("id");
			
			attachmentProcess(request,response, messageId);
			
			sendResponse(response, HttpServletResponse.SC_OK, STATUS_SUCCESS, null, message);
		} catch (InvalidInputException | InvalidEmailException e) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, STATUS_FAILED, e.getMessage(), null);
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, STATUS_FAILED, e.getMessage(), null);
		}
	}
	
	private void attachmentProcess(HttpServletRequest request , HttpServletResponse response, long messageId) throws IOException, ServletException, Exception {
		boolean hasAttachment = false;
		for(Part part :request.getParts()) {
			String fileName = part.getSubmittedFileName();
			
		    if (fileName != null && !fileName.isEmpty()) {
		    	
		    	String fileExtension = "";
		    	int dotIndex = fileName.lastIndexOf('.');
		    	if (dotIndex != -1) {
		    		fileExtension = fileName.substring(dotIndex+1);
		    	}
			    Map<String,Byte> fileTypes = Attachment.getFileTypes();
			    
			    if(fileTypes.containsKey(fileExtension)) {
			    	hasAttachment = true;
			    	System.out.println("File size: "+part.getSize());
			    	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			    	String newFileName = fileName.substring(0,dotIndex)+'_'+timeStamp+"."+fileExtension;
			    	String filePath = SAVE_DIR + File.separator + newFileName;
			    	part.write(filePath);
			    	System.out.println("File size: " + part.getSize());
			    	messageOperation.saveAttachment(messageId, newFileName, fileTypes.get(fileExtension) ,(int)part.getSize(), filePath);
			    }else {
					sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, STATUS_FAILED, "File upload failed: The selected file type is not allowed. Please upload files with the following extensions: .jpg, .png, .pdf, etc.", null);
			    }
			}

		}
		if(hasAttachment) {
			messageOperation.updateMessage(messageId);
		}
	}

	private void saveDraftMessage(HttpServletRequest request, HttpServletResponse response) {
		String messageIdStr = request.getParameter("id");
		Long messageId = (messageIdStr != null && !messageIdStr.isEmpty()) ? Long.parseLong(messageIdStr) : null;
		String to = request.getParameter("to");
		String cc = request.getParameter("cc");
		String subject = request.getParameter("subject");
		String description = request.getParameter("description");
		messageOperation.inputMessageDetails(to, cc, subject, description);
		try {
			JSONObject message = null;
			if (messageId == null) {
				message = messageOperation.createMessage();
				messageOperation.setMessageFolder(UserDatabase.getCurrentUser().getUserId(), (long) message.get("id"),
						Folder.getFolderId(Folder.getDraftName()));
			} else {
				message = messageOperation.getMessage(Folder.getDraftName(), messageId);
				messageOperation.updateDraftMessage(message);
			}
			messageId = (long) message.get("id");
			attachmentProcess(request, response, messageId);
			sendResponse(response, HttpServletResponse.SC_OK, STATUS_SUCCESS, null, message);
		} catch (InvalidInputException | InvalidEmailException e) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, STATUS_FAILED, e.getMessage(), null);
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, STATUS_FAILED, e.getMessage(), null);
		}
	}

	private void starMessage(HttpServletRequest request, HttpServletResponse response, String folderName) {
		long messageId = Long.parseLong(request.getParameter("id"));
		try {
			messageOperation.starredMessage(folderName, messageId);
			sendResponse(response, HttpServletResponse.SC_OK, STATUS_SUCCESS, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, STATUS_FAILED, e.getMessage(), null);
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
			sendResponse(response, HttpServletResponse.SC_OK, STATUS_SUCCESS, null, message);
		} catch (NumberFormatException e) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, STATUS_FAILED,
					"Please check the URL and try again. " + e.getMessage(), null);
		} catch (Exception e) {
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, STATUS_FAILED,
					"An unexpected error occurred. Please try again later. Error details: " + e.getMessage(), null);
		}
	}

	private void signUp(HttpServletRequest request, HttpServletResponse response) {
		String name = request.getParameter("name");
		String smail = request.getParameter("smail");
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("confirmPassword");
		if (password != null && !password.equals(confirmPassword)) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, STATUS_FAILED, "Password mismatch.", null);
			return;
		}
		try {
			if (Authentication.signUp(name, smail, password)) {
				sendResponse(response, HttpServletResponse.SC_OK, STATUS_SUCCESS, "Sign up successful. Please sign in.",
						null);
			}
		} catch (InvalidInputException e) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, STATUS_FAILED, e.getMessage(), null);
		} catch (EmailAlreadyExistsException e) {
			sendResponse(response, HttpServletResponse.SC_CONFLICT, STATUS_FAILED, e.getMessage(), null);
		} catch (Exception e) {
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, STATUS_FAILED, e.getMessage(), null);
		}
	}

	private void signIn(HttpServletRequest request, HttpServletResponse response) {
		String smail = request.getParameter("smail");
		String password = request.getParameter("password");
		try {
			User user = Authentication.signIn(smail, password);
			HttpSession session = request.getSession();
			session.setAttribute("user", user);
			sendResponse(response, HttpServletResponse.SC_OK, STATUS_SUCCESS, "Sign in successful.", null);
		} catch (InvalidInputException e) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, STATUS_FAILED, e.getMessage(), null);
		} catch (AuthenticationFailedException e) {
			sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED, STATUS_FAILED, e.getMessage(), null);
		} catch (Exception e) {
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, STATUS_FAILED, e.getMessage(), null);
		}
	}

	private void displayMessages(HttpServletRequest request, HttpServletResponse response, String folderName) {
		String searchedKeyword = request.getParameter("search");
		System.out.println(searchedKeyword);
		JSONArray messages = null;
		try {
			if (searchedKeyword == null) {
				messages = MessageOperation.getMessages(folderName);
			} else {
				messages = MessageOperation.getSearchedMessages(folderName, searchedKeyword);
			}
			sendResponse(response, HttpServletResponse.SC_OK, STATUS_SUCCESS, null, messages);
		} catch (Exception e) {
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, STATUS_FAILED, e.getMessage(), null);
		}
	}

	private void signOut(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		session.invalidate();
		sendResponse(response, HttpServletResponse.SC_OK, STATUS_SUCCESS, "Sign out successful.", null);
	}

	@SuppressWarnings("unchecked")
	private void sendResponse(HttpServletResponse response, int statusCode, String status, String message,
			Object jsonData) {

		JSONObject response_status = new JSONObject();
		response_status.put("status_code", statusCode);
		response_status.put("status", status);
		response_status.put("message", message);

		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put("response_status", response_status);
		jsonResponse.put("data", jsonData);
		System.out.println(jsonResponse);

		try (PrintWriter out = response.getWriter()) {
			out.print(jsonResponse.toString());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}