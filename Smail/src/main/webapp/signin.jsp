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
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            background-color: #f0f0f0;
        }
        .content-wrapper {
            display: flex;
            flex-direction: column;
            align-items: center;
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
        input, button {
            display: block;
            width: 20rem;
            padding: 0.5rem;
            margin: 0.5rem;
            font-size: 1rem;
            border: 1px solid #ccc;
            border-radius: 3px;
            box-sizing: border-box;
        }
        button {
            background-color: #007bff;
            color: white;
            border: none;
            cursor: pointer;
            text-align: center;
        }
        button:hover {
            background-color: #0056b3;
        }
        #signInMessage {
            text-align: center;
            font-size: 0.9rem;
            color: #900; 
            margin-top: 1rem; 
            align-self: flex-end; 
            position: absolute;
            bottom: 20px;
            width: calc(100% - 40px);
        }
    </style>
</head>
<body>
    <div class="content-wrapper">
        <h2 style="text-align: center;">Sign In</h2>
        <form id="signInForm">
            <input type="email" id="smail" name="smail" placeholder="Email" pattern="^[a-z0-9]+(\\.[a-z0-9]+)*@smail.com" required>
            <input type="password" id="password" name="password" placeholder="Password" pattern="^(?=.*[A-Z])(?=.*[0-9])(?=.*[\W_]).{8,100}$" required>
            <button type="submit">Sign In</button>
        </form>
    </div>

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
                        if (response.response_status.status === 'success') {
                            $('#signInMessage').html('<p style="color: green;">' + response.response_status.message + '</p>');
                            setTimeout(function() {
                                window.location.href = '/Smail/home.jsp';
                            }, 2000);
                        } else {
                            $('#signInMessage').html('<p style="color: red;">' + response.response_status.message + '</p>');
                        }
                    },
                    error: function(error) {
                        $('#signInMessage').html('<p style="color: red;">' + error + '</p>');
                    }
                });
            });
        });
    </script>
</body>
</html>
