<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign Up</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Roboto', sans-serif;
            margin: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: #333;
        }
        .signup-container {
            background-color: #fff;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
            max-width: 450px;
            width: 100%;
            box-sizing: border-box;
        }
        h2 {
            text-align: center;
            color: #444;
            margin-bottom: 1rem;
        }
        form {
            display: flex;
            flex-direction: column;
        }
        input {
            padding: 0.75rem;
            margin-bottom: 1rem;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 1rem;
            transition: border-color 0.3s;
        }
        input:focus {
            border-color: #667eea;
        }
        button {
            padding: 0.75rem;
            background-color: #667eea;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 1rem;
            transition: background-color 0.3s;
        }
        button:hover {
            background-color: #5a67d8;
        }
        #signupMessage {
            text-align: center;
            margin-top: 1rem;
            font-size: 0.9rem;
        }
        .signin-link {
            text-align: center;
            margin-top: 1rem;
        }
        .signin-link a {
            color: #667eea;
            text-decoration: none;
            transition: color 0.3s;
        }
        .signin-link a:hover {
            color: #5a67d8;
        }
    </style>
</head>
<body>
    <div class="signup-container">
        <h2>Sign Up</h2>
        <form id="signupForm">
            <input type="text" id="name" name="name" placeholder="Name" pattern="^[A-Za-z]+( [A-Za-z]+)*$" required>
            <input type="email" id="smail" name="smail" placeholder="example@smail.com" pattern="^[a-z0-9]+(\.[a-z0-9]+)*@smail.com" required>
            <input type="password" id="password" name="password" placeholder="Password" pattern="^(?=.*[A-Z])(?=.*[0-9])(?=.*[\W_]).{8,100}$" required>
            <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Confirm Password" pattern="^(?=.*[A-Z])(?=.*[0-9])(?=.*[\W_]).{8,100}$" required>
            <button type="submit">Sign Up</button>
        </form>
        <div id="signupMessage"></div>
        <div class="signin-link">
            <a id="signInLink" href="/Smail/signin.jsp">Already have an account? Sign In</a>
        </div>
    </div>
    
    <script>
        $(document).ready(function() {
            $('#signupForm').submit(function(event) {
                event.preventDefault();
                $.ajax({
                    type: 'POST',
                    url: '<%= request.getContextPath() %>/signup',
                    data: $(this).serialize(),
                    dataType: 'json',
                    success: function(response) {
                        if (response.response_status.status === "success") {
                            $('#signupMessage').html('<p style="color: green;">' + response.response_status.message + '</p>');
                            setTimeout(function() {
                                window.location.href = '/Smail/signin.jsp';
                            }, 2000);
                        } else {
                            $('#signupMessage').html('<p style="color: red;">' + response.response_status.message + '</p>');
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
	                    $('#signInMessage').html('<p style="color: red;">' + message + '</p>');
	                }
                });
            });
        });
    </script>
</body>
</html>
