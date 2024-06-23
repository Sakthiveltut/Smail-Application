<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Sign In</title>
</head>
<body>
	<form action="signin" method="Post">
		<label for="smail">Smail: </label>
		<input type="text" name="smail" required><br>
		<label for="password">Password: </label>
		<input type="password" name="password" required><br>
		<input type="submit">
	</form>
	<% if (request.getAttribute("error")!=null) { %>
		<p style="color:red;"><%= request.getAttribute("error") %></p>
	<%} %>
</body>
</html>
