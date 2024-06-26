<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inbox</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            display: flex;
        }
        .vertical-nav {
            background-color: #f0f0f0;
            width: 200px;
            height: 100vh;
            position: fixed;
            padding-top: 10px;
            box-shadow: 2px 0 5px rgba(0,0,0,0.1);
        }
        .vertical-nav button {
            display: block;
            width: 100%;
            padding: 12px 16px;
            background: none;
            border: none;
            text-align: left;
            color: #333;
            cursor: pointer;
            transition: background-color 0.3s;
        }
        .vertical-nav button:hover, .vertical-nav button.active {
            background-color: #4CAF50;
            color: white;
        }
        .content {
            margin-left: 220px;
            padding: 20px;
            width: calc(100% - 220px);
        }
        .message-list {
            list-style-type: none;
            padding: 0;
            margin: 0;
        }
        .message-list-item {
            border-bottom: 1px solid #ddd;
            padding: 10px;
        }
        .message-list-item a {
            text-decoration: none;
            color: #000;
            display: block;
        }
        .message-list-item a h3 {
            margin: 0;
            font-size: 1.2em;
            color: #4CAF50;
        }
        .message-list-item a p {
            margin: 5px 0;
            font-size: 0.9em;
            color: #555;
        }
        .message-list-item a em {
            font-size: 0.8em;
            color: #777;
        }
    </style>
</head>
<body>
    <div class="vertical-nav">
        <button id="inbox">Inbox</button>
        <button id="starred">Starred</button>
        <button id="compose">Compose</button>
        <button id="sent">Sent</button>
        <button id="unread">Unread</button>
        <button id="draft">Draft</button>
        <button id="spam">Spam</button>
        <button id="bin">Bin</button>
        <button id="logout">Log Out</button>
    </div>

    <div class="content">
        <h1>Messages</h1>
        <div id="messageList"></div>
    </div>

    <div id="messageDetailsContainer" class="content" style="display: none;">
        <h1>Message Details</h1>
        <div id="messageDetails"></div>
        <a href="#" id="backToList">Back to List</a>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function() {
            function displayMessages(folderName) {
                $.ajax({
                    url: "<%= request.getContextPath() %>/" + folderName,
                    type: "GET",
                    dataType: "json",
                    success: function(response) {
                        if (response.response_status.status === "success") {
                            var messages = response.data;
                            if (messages && messages.length > 0) {
                                var html = "<ul class='message-list'>";
                                $.each(messages, function(index, message) {
                                    html += "<li class='message-list-item'>";
                                    html += "<a href='#' class='message-link' data-id='" + message.id + "'>";
                                    html += "<h3>" + message.subject + "</h3>";
                                    html += "<p>" + message.description + "</p>";
                                    html += "<p>" + (message.has_attachment ? "Attachment: Yes" : "Attachment: No") + "</p>";
                                    html += "<em>" + message.created_time + "</em>";
                                    html += "</a>";
                                    html += "</li>";
                                });
                                html += "</ul>";
                                $("#messageList").html(html);
                            } else {
                                $("#messageList").html("<p>No messages found.</p>");
                            }
                        } else {
                            $("#messageList").html("<p>Error: " + response.response_status.message + "</p>");
                        }
                    },
                    error: function(xhr, status, error) {
                        console.error("Error: " + error);
                        $("#messageList").html("<p>An error occurred while fetching messages.</p>");
                    }
                });
            }

            function loadMessageDetails(messageId) {
                $.ajax({
                    url: "<%= request.getContextPath() %>/messageDetails?id=" + messageId,
                    type: "GET",
                    dataType: "json",
                    success: function(response) {
                        if (response.success) {
                            var message = response.data.message;
                            var messageDetailsHtml = '';
                            if (message) {
                                messageDetailsHtml += '<h3>' + message.subject + '</h3>';
                                messageDetailsHtml += '<p><strong>From:</strong> ' + message.from + '</p>';
                                messageDetailsHtml += '<p><strong>To:</strong> ' + message.to + '</p>';
                                messageDetailsHtml += '<p>' + message.description + '</p>';
                                messageDetailsHtml += '<p>' + (message.has_attachment ? "Attachment: Yes" : "Attachment: No") + '</p>';
                                messageDetailsHtml += '<p><em>' + message.created_time + '</em></p>';
                            } else {
                                messageDetailsHtml += '<p>Message not found.</p>';
                            }
                            $("#messageDetails").html(messageDetailsHtml);
                            $("#messageList").hide(); // Hide message list
                            $("#messageDetailsContainer").show(); // Show message details
                        } else {
                            console.error('Failed to load message details:', response.message);
                        }
                    },
                    error: function(xhr, status, error) {
                        console.error('Error loading message details:', error);
                    }
                });
            }

            displayMessages("inbox");

            // Event delegation for message links
            $("#messageList").on("click", ".message-link", function(e) {
                e.preventDefault();
                var messageId = $(this).data("id");
                loadMessageDetails(messageId);
            });

            // Handling back to message list
            $("#messageDetailsContainer").on("click", "#backToList", function(e) {
                e.preventDefault();
                $("#messageDetailsContainer").hide(); // Hide message details
                $("#messageList").show(); // Show message list
            });

            // Navigation click handler
            $(".vertical-nav button").click(function(e) {
                e.preventDefault();
                var folderName = $(this).attr("id");
                $(".vertical-nav button").removeClass("active");
                $(this).addClass("active");
                displayMessages(folderName);
            });
        });
    </script>
</body>
</html>
