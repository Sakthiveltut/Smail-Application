<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Sign Up</title>
</head>
<body>
	<form action="signup" method="Post">
		<label for="name">Name: </label>
		<input type="text" name="name" required><br>
		<label for="smail">Smail: </label>
		<input type="text" name="smail" required><br>
		<label for="password">Password: </label>
		<input type="password" name="password" required><br>
		<label for="confirmPassword">Confirm Password: </label>
		<input type="password" name="confirmPassword" required><br>
		<input type="submit">
	</form>
	<a href="signin.jsp">Login</a>
	<% if (request.getAttribute("error")!=null) { %>
		<p style="color:red;"><%= request.getAttribute("error") %></p>
	<%} %>
		
</body>
</html>