<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Sign Up</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            background-color: #f0f0f0;
        }
        .signup-container {
            width: 100%;
            max-width: 400px;
            background-color: #fff;
            padding: 1rem;
            border: 1px solid #ccc;
            border-radius: 5px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }
        form {
            margin-bottom: 1rem;
        }
        input, button, a {
            display: block;
            width: calc(100% - 2rem);
            padding: 0.5rem;
            margin: 0.5rem;
            font-size: 1rem;
            border: 1px solid #ccc;
            border-radius: 3px;
            box-sizing: border-box;
        }
        button, a {
            background-color: #007bff;
            color: white;
            border: none;
            cursor: pointer;
            text-align: center;
            text-decoration: none;
        }
        button:hover, a:hover {
            background-color: #0056b3;
        }
        #signupMessage {
            text-align: center;
            margin-top: 1rem;
            font-size: 0.9rem;
        }
    </style>
</head>
<body>
    <div class="signup-container">
        <h2 style="text-align: center;">Sign Up</h2>
        <form id="signupForm">
            <input type="text" id="name" name="name" placeholder="Name" pattern="^[A-Za-z]+( [A-Za-z]+)*$" required>
            <input type="email" id="smail" name="smail" placeholder="example@smail.com" pattern="^[a-z0-9]+(\\.[a-z0-9]+)*@smail.com" required>
            <input type="password" id="password" name="password" placeholder="Password" pattern="^(?=.*[A-Z])(?=.*[0-9])(?=.*[\W_]).{8,100}$" required>
            <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Confirm Password" pattern="^(?=.*[A-Z])(?=.*[0-9])(?=.*[\W_]).{8,100}$" required>
            <button type="submit">Sign Up</button>
        </form>
        <div id="signupMessage"></div>
        <a id="signInLink" href="/Smail/signin.jsp">Sign In</a>
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
                    error: function(error) {
                        $('#signupMessage').html('<p style="color: red;">' + error + '</p>');
                    }
                });
            });
        });
    </script>
</body>
</html>
