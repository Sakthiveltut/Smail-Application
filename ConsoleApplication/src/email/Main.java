package email;

import custom_exception.InvalidInputException;

public class Main {
	
	private static final byte SIGN_UP=1, SIGN_IN=2, EXIT=3; 
	private static final byte VIEW_PROFILE=1, COMPOSE=2, INBOX=3, STARRED=4, UNREAD=5, SENT=6, DRAFT=7, SPAM=8, BIN=9, LOG_OUT=10;
	private static final byte SEND_MESSAGE=1, DRAFT_MESSAGE=2;
	
	private static final String CHOICE = "Enter your choice: ";
	private static final String MAIN_MENU = "\u001B[33m"+"1.Sign up\n2.Sign in\n3.Exit"+"\u001B[0m";
	private static final String OPTIONS = "\u001B[33m"+"1.View Profile\n2.Compose\n3.Inbox\n4.Starred\n5.Unread\n6.Sent\n7.Draft\n8.Spam\n9.Bin\n10.Log out"+"\u001B[0m";
	private static final String INVALID_CHOICE = "\033[31m"+"Invalid choice.Please try again..."+"\033[0m";
	
	private static final String NAME = "Enter a name: ";
	private static final String EMAIL = "Enter a email id: ";
	private static final String PASSWORD_POLICY = "\u001B[33m"+"The password must be at least 8 characters long and contain at least one uppercase letter, one number, and one symbol."+"\u001B[0m";
	private static final String PASSWORD = "Enter a password: ";
	private static final String REPASSWORD = "Re enter password: ";
	
	static{
		try {
			Folder.setFolders();
			RecipientType.setRecipientTypes();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		System.out.println("Email Application"); 

		boolean exit = false;
		while(!exit){
				System.out.println(MAIN_MENU);
				InputHandler inputHandler = new InputHandler();
				Byte authChoice=null;
					try {
						authChoice = inputHandler.readByte(CHOICE);
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				if(authChoice!=null) {
					switch(authChoice){
						case SIGN_UP:
							String name = inputHandler.readString(NAME);
							String email = inputHandler.readString(EMAIL);
							System.out.println(PASSWORD_POLICY);
							String password = inputHandler.readString(PASSWORD);
							String confirmPassword = inputHandler.readString(REPASSWORD);
							if(password.equals(confirmPassword)) {
								try {
									Authentication.signUp(name,email,password);
								} catch (Exception e) {
									System.out.println(e.getMessage());
									//e.printStackTrace();
								}
							}else
								System.out.println("\033[31m"+"Password mismatch."+"\033[0m");
							break;
						case SIGN_IN:
							email = inputHandler.readString(EMAIL);
							password = inputHandler.readString(PASSWORD);
							boolean isValidUser=false;
							try {
								isValidUser = Authentication.signIn(email,password);
							} catch (Exception e) {
								System.out.println(e.getMessage());
								//e.printStackTrace();
							}
							if(isValidUser){
								boolean logout=false;
								while(!logout){
									System.out.println(OPTIONS);
									Byte choice=null;
									try {
										choice = inputHandler.readByte(CHOICE);
									}catch (InvalidInputException e) {
										System.out.println(e.getMessage());
										//e.printStackTrace();
									}
									if(choice!=null) {
										MessageOperations messageOperations = new MessageOperations();
										User currentUser = UserDatabase.getCurrentUser();
										switch(choice){
											case VIEW_PROFILE:	
												System.out.println("Name: "+currentUser.getName());
												System.out.println("Email: "+currentUser.getEmail());
												System.out.println("Last login date and time: "+(currentUser.getLastLoginTime()==null?"No login data available.":currentUser.getLastLoginTime()));
												break;
											case INBOX:
												try {
													messageOperations.viewMessageOptions(Folder.getInboxName());
												} catch (Exception e) {
													System.out.println(e.getMessage());
													e.printStackTrace();
												}
												break;
											case STARRED:
												try {
													messageOperations.viewMessageOptions(Folder.getStarredName());
												} catch (Exception e) {
													System.out.println(e.getMessage());
													e.printStackTrace();
												}
												break;
											case COMPOSE:
												messageOperations.inputMessageDetails();
												Message message=null;
												try {
													message = messageOperations.createMessage();
												} catch (Exception e) {
													System.out.println(e.getMessage());
												}
												if(message!=null) {
													System.out.println("1.Send\n2.Draft");
													Byte messageChoice=null;
													try {
														messageChoice = inputHandler.readByte(CHOICE);
													}catch (Exception e) {
														System.out.println(e.getMessage());
													}
													if(SEND_MESSAGE==messageChoice) {
														try {
															messageOperations.sendMessage(Folder.getSentName(), message);
														} catch (Exception e) {
															System.out.println(e.getMessage());
														}
													}else if(DRAFT_MESSAGE==messageChoice) {
														try {
															messageOperations.setMessageFolder(currentUser.getUserId(),message.getMessageId(),Folder.getFolderId(Folder.getDraftName()));
														} catch (Exception e) {
															System.out.println(e.getMessage());
														}
														System.out.println("\u001B[32m"+"The message was drafted successfully.\n"+"\u001B[0m");
													}else {
														System.out.println(INVALID_CHOICE);
													}
												}
												break;
											case UNREAD:
												
												break;
											case SENT:
												try {
													messageOperations.viewMessageOptions(Folder.getSentName());
												} catch (Exception e) {
													System.out.println(e.getMessage());
												}
												break;
											case DRAFT:
												try {
													messageOperations.viewMessageOptions(Folder.getDraftName());
												} catch (Exception e) {
													System.out.println(e.getMessage());
												}
												break;
											case SPAM:
												try {
													messageOperations.viewMessageOptions(Folder.getSpamName());
												} catch (Exception e) {
													System.out.println(e.getMessage());
												}
												break;
											case BIN:
												try {
													messageOperations.viewMessageOptions(Folder.getBinName());
												} catch (Exception e) {
													System.out.println(e.getMessage());
												}
												break;
											case LOG_OUT:
												logout=true;
												UserDatabase.clearCurrentUser();
												break;
											default:
												System.out.println(INVALID_CHOICE);
										}
									}
								}
							}
							break;
						case EXIT:
							exit = true;
							inputHandler.close();
							break;
						default:
							System.out.println(INVALID_CHOICE);
					}
				}
			}
		System.out.println("Thank you");
	}
}

