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
        #signupMessage {
            text-align: center;
            margin-top: 1rem;
        }
    </style>
</head>
<body>
    <h2>Sign Up</h2>
    <form id="signupForm">
        <input type="text" id="name" name="name" placeholder="Name" required><br><br>
        <input type="email" id="smail" name="smail" placeholder="Email" required><br><br>
        <input type="password" id="password" name="password" placeholder="Password" required><br><br>
        <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Confirm Password" required><br><br>
        <button type="submit">Sign Up</button>
    </form>
    <div id="signupMessage"></div>
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
                        if (response.success) {
                            $('#signupMessage').html('<p style="color: green;">' + response.data.message + '</p>');
                            setTimeout(function() {
                                window.location.href = '<%= request.getContextPath() %>/signin.jsp';
                            }, 2000);
                        } else {
                            $('#signupMessage').html('<p style="color: red;">' + response.data.message + '</p>');
                        }
                    },
                    error: function(xhr, status, error) {
                        $('#signupMessage').html('<p style="color: red;">An unexpected error occurred. Please try again later.</p>');
                    }
                });
            });
        });
    </script>
</body>
</html>
