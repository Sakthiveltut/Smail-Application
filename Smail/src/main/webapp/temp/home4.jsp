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
            display: flex;
            align-items: center;
            border-bottom: 1px solid #ddd;
            padding: 10px;
        }
        .message-list-item a {
            text-decoration: none;
            color: #000;
            display: flex;
            align-items: center;
            width: 100%;
        }
        .message-subject {
            flex: 3;
            margin: 0;
            font-size: 1em;
            color: #4CAF50;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .message-attachment {
            flex: 1;
            text-align: center;
        }
        .message-created-time {
            flex: 1;
            text-align: right;
            font-size: 0.8em;
            color: #777;
        }
        #messageDetails {
            margin-top: 20px;
            padding: 20px;
            border: 1px solid #ddd;
            background-color: #f9f9f9;
            display: none; 
        }
        #messageDetailsContent {
            display: flex;
            flex-direction: row;
            gap: 20px;
        }
        #messageDetailsContent > div {
            flex: 1;
        }
        #backButton {
            display: none; 
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="vertical-nav">
        <button id="inbox" class="active">Inbox</button>
        <button id="starred">Starred</button>
        <button id="compose">Compose</button>
        <button id="sent">Sent</button>
        <button id="unread">Unread</button>
        <button id="draft">Draft</button>
        <button id="spam">Spam</button>
        <button id="bin">Bin</button>
        <button id="signout">Sign Out</button>
    </div>

    <div class="content">
        <h1>Messages</h1>
        <div id="backButton"><button onclick="showMessageList()">Back to Messages</button></div>
        <div id="messageList"></div>
        <div id="messageDetails"></div>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        function showMessageList() {
            $("#messageDetails").hide();
            $("#messageList").show();
            $("#backButton").hide();
            $("h1").text("Messages");
        }

        function showMessageDetails(messageId) {
            var folderName = $(".vertical-nav button.active").attr("id");
            $.ajax({
                url: "<%= request.getContextPath() %>/" + folderName + "/messageDetails?id=" + messageId,
                type: "GET",
                dataType: "json", 
                success: function(response) {
                    if (response.response_status.status === "success") {
                        var message = response.data;
                        var html = "<div id='messageDetailsContent'>";
                        html += "<div>";
                        html += "<h2>" + message.subject + "</h2>";
                        html += "<p><strong>From:</strong> " + message.from + "</p>";
                        html += "<p><strong>To:</strong> " + message.to + "</p>";
                        html += "<p><strong>Created:</strong> " + message.created_time + "</p>";
                        html += "<p><strong>Attachment:</strong> " + (message.has_attachment ? "Yes" : "No") + "</p>";
                        html += "<p><strong>Read:</strong> " + (message.is_read ? "Yes" : "No") + "</p>";
                        html += "<p><strong>Starred:</strong> " + (message.is_starred ? "Yes" : "No") + "</p>";
                        html += "</div>";
                        html += "<div>";
                        html += "<p><strong>Description:</strong><br>" + message.description + "</p>";
                        html += "</div>";
                        html += "</div>";
                        $("#messageDetails").html(html).show();
                        $("#messageList").hide();
                        $("#backButton").show();
                        $("h1").text("Message Details");
                    } else {
                        $("#messageDetails").html("<p>Error: " + response.response_status.message + "</p>").show();
                        $("#backButton").show();
                    }
                },
                error: function(xhr, status, error) {
                    console.error("Error: " + error);
                    $("#messageDetails").html("<p>An error occurred while fetching message details.</p>").show();
                    $("#backButton").show();
                }
            });
        }

        $(document).ready(function() {
            function displayMessages(folderName) {
                $.ajax({
                    url: "<%= request.getContextPath() %>/" + folderName,
                    type: "GET",
                    dataType: "json",
                    success: function(response) {
                    	if(response.response_status.status_code==401){
                    		window.location.href="/Smail/signup.jsp";
                    		return;
                    	}
                        if (response.response_status.status === "success") {
                            var messages = response.data;
                            if (messages && messages.length > 0) {
                                var html = "<ul class='message-list'>";
                                $.each(messages, function(index, message) {
                                    html += "<li class='message-list-item'>";
                                    html += "<a href='#' onclick='showMessageDetails(\"" + message.id + "\")'>";
                                    html += "<h3 class='message-subject'>" + message.subject + "</h3>";
                                    html += "<p class='message-attachment'>" + (message.has_attachment ? "Attachment: Yes" : "Attachment: No") + "</p>";
                                    html += "<em class='message-created-time'>" + message.created_time + "</em>";
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

            displayMessages("inbox");

            $(".vertical-nav button").click(function(e) {
                e.preventDefault();
                var folderName = $(this).attr("id");
                $(".vertical-nav button").removeClass("active");
                $(this).addClass("active");
                displayMessages(folderName);
                showMessageList(); 
            });
        });
    </script>
</body>
</html>
