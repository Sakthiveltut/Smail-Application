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
            background-color: #f9f9f9;
        }
        .vertical-nav {
            background-color: #343a40;
            width: 250px;
            height: 100vh;
            position: fixed;
            padding-top: 20px;
            box-shadow: 2px 0 10px rgba(0, 0, 0, 0.2);
            transition: width 0.3s;
        }
        .vertical-nav button {
            display: flex;
            align-items: center;
            width: 100%;
            padding: 12px 20px;
            background: none;
            border: none;
            text-align: left;
            color: #fff;
            cursor: pointer;
            font-size: 16px;
            transition: background-color 0.3s;
        }
        .vertical-nav button .fa {
            margin-right: 15px;
        }
        .vertical-nav button:hover, .vertical-nav button.active {
            background-color: #4CAF50;
        }
        .content {
            margin-left: 250px;
            padding: 20px;
            width: calc(100% - 250px);
            transition: margin-left 0.3s, width 0.3s;
        }
        .message-list {
            list-style-type: none;
            padding: 0;
            margin: 0;
            border-top: 1px solid #ddd;
        }
        .message-list-item {
            display: flex;
            align-items: center;
            border-bottom: 1px solid #ddd;
            padding: 15px;
            background-color: #fff;
            transition: background-color 0.3s;
        }
        .message-list-item a {
            text-decoration: none;
            color: #000;
            display: flex;
            align-items: center;
            width: 100%;
        }
        .message-list-item:hover {
            background-color: #f1f1f1;
        }
        .message-subject {
            flex: 3;
            margin: 0;
            font-size: 1.1em;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .message-attachment {
            flex: 1;
            text-align: center;
            font-size: 0.9em;
            color: #888;
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
            background-color: #fff;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
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
        .hidden {
            display: none;
        }
        #composeMessage {
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background-color: #fff;
            padding: 20px;
            border: 1px solid #ddd;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            z-index: 1000;
            max-width: 80%;
            width: 700px;
            box-sizing: border-box;
            display:none;
        }
        #composeMessage input[type="text"], #composeMessage textarea {
            width: calc(100% - 20px);
            padding: 10px;
            margin-bottom: 10px;
            border: 1px solid #ccc;
            border-radius: 4px;
            resize: vertical;
        }
        #composeMessage button {
            background-color: #007bff;
            color: white;
            border: none;
            padding: 10px 20px;
            cursor: pointer;
        }
        #composeMessage button:hover {
            background-color: #0056b3;
        }
        .close-icon {
            position: absolute;
            top: 10px;
            right: 10px;
            cursor: pointer;
            font-size: 30px;
            color: #666;
        }
        .close-icon:hover {
            color: #333;
        }
    </style>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <div class="vertical-nav">
        <button id="compose"><i class="fa fa-pencil-alt"></i> Compose</button>
        <button id="inbox" class="active"><i class="fa fa-inbox"></i> Inbox</button>
        <button id="starred"><i class="fa fa-star"></i> Starred</button>
        <button id="sent"><i class="fa fa-paper-plane"></i> Sent</button>
        <button id="unread"><i class="fa fa-envelope-open"></i> Unread</button>
        <button id="draft"><i class="fa fa-file-alt"></i> Draft</button>
        <button id="spam"><i class="fa fa-exclamation-circle"></i> Spam</button>
        <button id="bin"><i class="fa fa-trash"></i> Bin</button>
        <button id="signout"><i class="fa fa-sign-out-alt"></i> Sign Out</button>
    </div>

    <div class="content">
        <h1>Messages</h1>
        <div id="backButton"><button onclick="showMessageList()">Back to Messages</button></div>
        <div id="messageList"></div>
        <div id="messageDetails"></div>
        
        <div id="composeMessage">
            <span class="close-icon" onclick="closeCompose()">�</span> 
            <h2>Compose Message</h2>
            <form id="messageForm">
                <input type="text" id="to" name="to" placeholder="To" required><br><br>
                <input type="text" id="cc" name="cc" placeholder="CC"><br><br>
                <input type="text" id="subject" name="subject" placeholder="Subject" required><br><br>
                <textarea id="description" placeholder="Message Description" name="description" rows="6" required></textarea><br><br>
                <button type="button" id="sendMessage" value="newMessage">Send</button>
                <button type="button" id="saveDraft" value="draftMessage">Save as Draft</button>
            </form>
            <p id="composeError"></p>
        </div>
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
            var option = $(".vertical-nav button.active").attr("id");
            $.ajax({
                url: option + "/messageDetails?id=" + messageId,
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

        function closeCompose() {
            $("#composeMessage").hide();
        }

        function showCompose() {
            $("#composeMessage").show();
        }

        $(document).ready(function() {
            function displayMessages(option) {
                $.ajax({
                    url: option,
                    type: "GET",
                    dataType: "json",
                    success: function(response) {
                        if (response.response_status.status_code == 401 || option === "signout") {
                            window.location.href = "/Smail/signin.jsp";
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
                var option = $(this).attr("id");
                $(".vertical-nav button").removeClass("active");
                $(this).addClass("active");
                if (option !== "compose") {
                    displayMessages(option);
                    showMessageList();
                }
            });

            $("#compose").click(function(e) {
                e.preventDefault();
                showCompose();
            });

            $('#sendMessage').click(function(e) {
                e.preventDefault();
                submitForm('newMessage');
            });

            $('#saveDraft').click(function(e) {
                e.preventDefault();
                submitForm('saveDraft');
            });

            function submitForm(action) {
                var formData = {
                    to: $('#to').val().trim(),
                    cc: $('#cc').val().trim(),
                    subject: $('#subject').val().trim(),
                    description: $('#description').val().trim()
                };

                $.ajax({
                    url: action,
                    method: 'POST',
                    contentType: 'application/json',
                    dataType: 'json',
                    data: JSON.stringify(formData),
                    success: function(response) {
                        if (response.response_status && response.response_status.status === "success") {
                            alert('Message ' + (action === 'newMessage' ? 'sent' : 'saved as draft') + ' successfully!');
                            closeCompose();
                        } else {
                        	console.log("else block");
                            $('#composeError').text(response.response_status.message || 'Unknown error');
                        }
                    },
	                error: function(xhr, status, error) {
	                    let message;
	                    try {
	                        const response = JSON.parse(xhr.responseText);
	                        message = response.response_status.message || "An unknown error occurred.";
	                    } catch (e) {
	                        message = "An error occurred. Please try again.";
	                    }
	                    $('#composeError').html('<p style="color: red;">' + message + '</p>');
	                }
                });
            }
        });
    </script>
</body>
</html>
