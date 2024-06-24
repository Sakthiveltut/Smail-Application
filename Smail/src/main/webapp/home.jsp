<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.smail.Message" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Inbox</title>
<style>
    body {
        font-family: Arial, sans-serif;
        margin: 0;
        padding: 0;
    }
    .vertical-nav {
        background-color: #f0f0f0;
        width: 200px; 
        height: 100%;
        position: fixed; 
        overflow: auto; 
    }
    .vertical-nav a {
        display: block;
        padding: 12px;
        text-decoration: none;
        color: #333;
        transition: background-color 0.3s;
    }
    .vertical-nav a:hover {
        background-color: #ddd;
    }
    .vertical-nav a.active {
        background-color: #4CAF50;
        color: white;
    }
    .message-list {
        list-style-type: none;
        padding: 0;
    }
    .message-list-item {
        border-bottom: 1px solid #ddd;
        padding: 10px;
    }
    .message-list-item a {
        text-decoration: none;
        color: #000;
    }
    .message-list-item a h3 {
        margin: 0;
        font-size: 1.2em;
        color: #4CAF50;
    }
    .message-list-item a p {
        margin: 0;
        font-size: 0.9em;
        color: #555;
    }
    .message-list-item a p + p {
        margin-top: 5px;
    }
</style>
</head>
<body>

<div class="vertical-nav">
    <a href="<%= request.getContextPath() %>/profile">View Profile</a>
    <a href="<%= request.getContextPath() %>/inbox" class="active">Inbox</a>
    <a href="<%= request.getContextPath() %>/starred">Starred</a>
    <a href="<%= request.getContextPath() %>/compose">Compose</a>
    <a href="<%= request.getContextPath() %>/sent">Sent</a>
    <a href="<%= request.getContextPath() %>/unread">Unread</a>
    <a href="<%= request.getContextPath() %>/draft">Draft</a>
    <a href="<%= request.getContextPath() %>/spam">Spam</a>
    <a href="<%= request.getContextPath() %>/bin">Bin</a>
    <a href="<%= request.getContextPath() %>/logout">Log Out</a>
</div>

<div style="margin-left: 220px; padding: 20px;">
    <ul id="messageList" class="message-list">
        <% 
           List<Message> messages = (List<Message>) request.getAttribute("messages");
           if (messages != null && !messages.isEmpty()) {
               for (Message message : messages) {
        %>
                   <li class="message-list-item">
						<a href="<%= request.getContextPath() + "/inbox/messageDetails?id=" + message.getMessageId() %>">
                           <h3><%= message.getSubject() %></h3>
                           <p><%= message.getDescription() %></p>
                           <p><em><%= message.getCreatedTime() %></em></p>
                       </a>
                   </li>
        <% 
               }
           } else { 
        %>
               <li>No messages found.</li>
        <% } %>
    </ul>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    $(document).ready(function() {
        // Function to load messages via AJAX
        function loadMessages() {
            $.ajax({
                type: 'GET',
                url: '<%= request.getContextPath() %>/inboxMessages', // Endpoint to fetch inbox messages
                dataType: 'json',
                success: function(response) {
                    if (response.success) {
                        $('#messageList').empty(); // Clear existing list
                        if (response.data.messages.length > 0) {
                            response.data.messages.forEach(function(message) {
                                $('#messageList').append(
                                    '<li class="message-list-item">' +
                                    '<a href="<%= request.getContextPath() + "/inbox/messageDetails?id=" %>' + message.messageId + '">' +
                                    '<h3>' + message.subject + '</h3>' +
                                    '<p>' + message.description + '</p>' +
                                    '<p><em>' + message.createdTime + '</em></p>' +
                                    '</a>' +
                                    '</li>'
                                );
                            });
                        } else {
                            $('#messageList').append('<li>No messages found.</li>');
                        }
                    } else {
                        console.error('Failed to load messages:', response.message);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('Error loading messages:', error);
                }
            });
        }

        loadMessages();

        setInterval(function() {
            loadMessages();
        }, 30000); 
    });
</script>

</body>
</html>
