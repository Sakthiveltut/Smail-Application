<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Sign In</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
        }
        form {
            max-width: 400px;
            margin: auto;
            padding: 1rem;
            border: 1px solid #ccc;
            border-radius: 5px;
        }
        input, button {
            display: block;
            width: 100%;
            padding: 0.5rem;
            margin-bottom: 1rem;
        }
        button {
            background-color: #007bff;
            color: white;
            border: none;
            cursor: pointer;
        }
        button:hover {
            background-color: #0056b3;
        }
        #signInMessage {
            text-align: center;
            margin-top: 1rem;
        }
    </style>
</head>
<body>
    <h2>Sign In</h2>
    <form id="signInForm">
        <input type="email" id="smail" name="smail" placeholder="Email" required><br><br>
        <input type="password" id="password" name="password" placeholder="Password" required><br><br>
        <button type="submit">Sign In</button>
    </form>
    <div id="signInMessage"></div>
    <script>
        $(document).ready(function() {
            $('#signInForm').submit(function(event) {
                event.preventDefault();
                $.ajax({
                    type: 'POST',
                    url: '<%= request.getContextPath() %>/signin',
                    data: $(this).serialize(),
                    dataType: 'json',
                    success: function(response) {
                    	if(response.response_status.status_code==200){
                            $('#signInMessage').html('<p style="color: green;">' + response.response_status.message + '</p>');
                            setTimeout(function() {
                                window.location.href = '/Smail/home.jsp';
                            }, 2000);
                    	}else{
                            $('#signInMessage').html('<p style="color: red;">' + response.response_status.message + '</p>');
                    	}
                    },
                    error: function(xhr) {
                        try {
                            var responseJson = JSON.parse(xhr.responseText); 
                            var errorMessage = responseJson.data.message;
                            if (errorMessage) {
                                $('#signInMessage').html('<p style="color: red;">' + errorMessage + '</p>');
                            }
                        } catch (e) {
                            console.error('Error parsing JSON:', e);
                            $('#signInMessage').html('<p style="color: red;">An unexpected error occurred. Please try again later.</p>');
                        }
                    }
                });
            });
        });
    </script>
</body>
</html>