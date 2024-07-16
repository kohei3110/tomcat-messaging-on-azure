<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html>
<head>
    <title>ユーザー情報入力</title>
</head>
<body>
<h2>ユーザー情報を入力してください</h2>
<html:form action="/processServiceBus" method="post">
    <p>* お名前:<br/><html:text property="name" size="40" maxlength="50"/></p>
	<p>
		<html:submit>
			<bean:message key="button.submit" />
		</html:submit>
		<html:cancel/>
	</p>
</html:form>
</body>
</html>