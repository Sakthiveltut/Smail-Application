<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.smail.Message" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Message Details</title>
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
    .message-details {
        margin-left: 220px;
        padding: 20px;
    }
    .message-details h6 {
        margin: 0;
        font-size: 0.9em;
        color: #555;
    }
    .message-details h1 {
        margin: 10px 0;
        font-size: 1.8em;
        color: #4CAF50;
    }
    .message-details p {
        margin: 10px 0;
        font-size: 1.1em;
        color: #333;
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

<div class="message-details" id="messageDetailsContainer">
    <% 
        Message message = (Message) request.getAttribute("message");
        if (message != null) {
    %>
        <h6><strong>From:</strong> <%= message.getFrom() %></h6>
        <h6><strong>To:</strong> <%= message.getTo() %></h6>
        <% if(message.getCc() != null && !message.getCc().isEmpty()) { %>
            <h6><strong>Cc:</strong> <%= message.getCc() %></h6>
        <% } %>
        <h1><strong>Subject:</strong> <%= message.getSubject() %></h1>
        <p><%= message.getDescription() %></p>
    <% } else { %>
        <h3>Message not found.</h3>
    <% } %>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    $(document).ready(function() {
        function loadMessageDetails(messageId) {
            $.ajax({
                type: 'GET',
                url: '<%= request.getContextPath() %>/messageDetails?id=' + messageId, // Endpoint to fetch message details
                dataType: 'json',
                success: function(response) {
                    if (response.success) {
                        var message = response.data.message;
                        var messageDetailsHtml = '';
                        if (message) {
                            messageDetailsHtml += '<h6><strong>From:</strong> ' + message.from + '</h6>';
                            messageDetailsHtml += '<h6><strong>To:</strong> ' + message.to + '</h6>';
                            if (message.cc && message.cc.length > 0) {
                                messageDetailsHtml += '<h6><strong>Cc:</strong> ' + message.cc.join(', ') + '</h6>';
                            }
                            messageDetailsHtml += '<h1><strong>Subject:</strong> ' + message.subject + '</h1>';
                            messageDetailsHtml += '<p>' + message.description + '</p>';
                        } else {
                            messageDetailsHtml += '<h3>Message not found.</h3>';
                        }
                        $('#messageDetailsContainer').html(messageDetailsHtml);
                    } else {
                        console.error('Failed to load message details:', response.message);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('Error loading message details:', error);
                }
            });
        }

        var messageId = '<%= request.getParameter("id") %>';
        if (messageId) {
            loadMessageDetails(messageId);
        } else {
            console.error('Message ID not provided.');
        }
    });
</script>

</body>
</html>