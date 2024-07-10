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
        .star-button {
            background: none;
            border: none;
            cursor: pointer;
            color: #ccc;
            font-size: 16px;
        }
        .star-button:hover {
            color: gold;
        }
        .starred {
            color: gold;
        }
        .message-list-item.read {
            background-color: #ffffff; 
        }
        .message-list-item.unread {
            background-color: #e0e0e0; 
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
        <button id="deleteMessages" onclick="deleteSelectedMessages()" disabled>Delete Selected</button>
        <div id="backButton"><button onclick="showMessageList()">Back to Messages</button></div>
        <div id="messageList"></div>
        <div id="messageDetails"></div>
        
        <div id="composeMessage">
            <span class="close-icon" onclick="closeCompose()">×</span> 
            <h2 id="composeHeading">Compose Message</h2>
            <form id="messageForm">
                <input type="hidden" id="id" name="id"><br><br>
                <input type="text" id="to" name="to" placeholder="To" required><br><br>
                <input type="text" id="cc" name="cc" placeholder="CC"><br><br>
                <input type="text" id="subject" name="subject" placeholder="Subject" required><br><br>
                <textarea id="description" placeholder="Message Description" name="description" rows="6" required></textarea><br><br>
                <button type="button" id="sendMessage" value="sendMessage">Send</button>
                <button type="button" id="saveDraft" value="draftMessage">Save as Draft</button>
            </form>
            <p id="composeError"></p>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        var selectedMessages = [];

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
                        html += "<p><strong>Starred:</strong> " + message.is_starred + "</p>";
                        html += "<p><strong>Unread:</strong> " + message.is_read + "</p>";
                        html += "<button onclick='starMessage(" + messageId + ")' class='star-button " + (message.is_starred ? 'starred' : '') + "'>";
                        html += "<i class='fa fa-star'></i> Star</button>";
                        html += "<button onclick='markAsRead(" + messageId + ")'>";
                        html += "<i class='fa fa-envelope-open'></i> Mark as Read</button>";
                        html += "<button onclick='deleteMessage(" + messageId + ")'>";
                        html += "<i class='fa fa-trash'></i> Delete</button>";
                        html += "</div>";
                        html += "<div>" + message.description + "</div>";
                        html += "</div>";
                        $("#messageDetails").html(html);
                        $("#messageList").hide();
                        $("#messageDetails").show();
                        $("#backButton").show();
                        $("h1").text("Message Details");
                    } else {
                        alert("Failed to retrieve message details.");
                    }
                },
                error: function(xhr, status, error) {
                    console.error("Error fetching message details:", error);
                    alert("An error occurred while fetching message details.");
                }
            });
        }

        function displayMessages(option) {
            $.ajax({
                url: option + "/messages",
                type: "GET",
                dataType: "json",
                success: function(response) {
                    if (response.response_status.status === "success") {
                        var messages = response.data;
                        var html = "<ul class='message-list'>";
                        messages.forEach(function(message) {
                            var readClass = message.is_read ? 'read' : 'unread';
                            html += "<li class='message-list-item " + readClass + "'>";
                            html += "<input type='checkbox' class='message-checkbox' data-message-id='" + message.id + "'>";
                            html += "<a href='javascript:void(0)' onclick='showMessageDetails(" + message.id + ")'>";
                            html += "<div class='message-subject'>" + message.subject + "</div>";
                            html += "<div class='message-attachment'>" + (message.has_attachment ? "<i class='fa fa-paperclip'></i>" : "") + "</div>";
                            html += "<div class='message-created-time'>" + message.created_time + "</div>";
                            html += "</a>";
                            html += "</li>";
                        });
                        html += "</ul>";
                        $("#messageList").html(html);
                    } else {
                        alert("Failed to retrieve messages.");
                    }
                },
                error: function(xhr, status, error) {
                    console.error("Error fetching messages:", error);
                    alert("An error occurred while fetching messages.");
                }
            });
        }

        function deleteSelectedMessages() {
            if (selectedMessages.length === 0) {
                alert("Select messages to delete.");
                return;
            }

            var option = $(".vertical-nav button.active").attr("id");
            $.ajax({
                url: option + "/deleteMessages",
                type: "POST",
                contentType: 'application/json',
                dataType: 'json',
                data: JSON.stringify({ ids: selectedMessages }),
                success: function(response) {
                    if (response.response_status.status === "success") {
                        alert("Messages deleted successfully!");
                        displayMessages(option);
                        selectedMessages = [];
                        $("#deleteMessages").prop('disabled', true);
                    } else {
                        alert("Failed to delete messages.");
                    }
                },
                error: function(xhr, status, error) {
                    console.error("Error deleting messages:", error);
                    alert("An error occurred while deleting messages.");
                }
            });
        }

        $(document).ready(function() {
            displayMessages("inbox");

            $(".vertical-nav button").click(function() {
                $(".vertical-nav button").removeClass("active");
                $(this).addClass("active");
                displayMessages($(this).attr("id"));
            });

            $("#compose").click(function() {
                $("#composeMessage").show();
            });

            $("#sendMessage").click(function() {
                // Handle send message functionality
            });

            $("#saveDraft").click(function() {
                // Handle save draft functionality
            });

            $("#signout").click(function() {
                // Handle signout functionality
            });

            $(document).on('change', '.message-checkbox', function() {
                var messageId = $(this).data('message-id');
                if ($(this).is(':checked')) {
                    selectedMessages.push(messageId);
                } else {
                    var index = selectedMessages.indexOf(messageId);
                    if (index !== -1) {
                        selectedMessages.splice(index, 1);
                    }
                }
                toggleDeleteButton();
            });
        });

        function toggleDeleteButton() {
            if (selectedMessages.length > 0) {
                $('#deleteMessages').prop('disabled', false);
            } else {
                $('#deleteMessages').prop('disabled', true);
            }
        }

        function closeCompose() {
            $("#composeMessage").hide();
        }

        function starMessage(messageId) {
            // Implement star message functionality
        }

        function markAsRead(messageId) {
            // Implement mark as read functionality
        }

        function deleteMessage(messageId) {
            // Implement delete message functionality
        }
    </script>
</body>
</html>
