<html lang="en">
<head>
	<title>Library - Books</title>
</head>
<body>
<h2>This is cool library!</h2>

Books available:

<ul>
<#list books as book>
	<li>${book_index + 1}. ${book.name}, <em>${book.author}</em></li>
</#list>
</ul>
</body>
</html>