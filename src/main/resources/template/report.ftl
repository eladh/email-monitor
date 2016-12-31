<!doctype html>
<html lang="en-US">
<head>
	<meta charset="UTF-8">
	<style>
		html, body, td {
			font-family: arial, sans-serif;
			font-size: 13px;
		}

		td, th {
			border-spacing: 0;

			padding: 2px 5px;
			border-top: solid 1px #000000;
			border-left: solid 1px #000000;
			margin: 0;
		}

		th {
			background: #D5FFFF;
			border-bottom: solid 1px #000000;
		}

		table {
			border-top: solid 1px #000000;
			border-left: solid 1px #000000;
			border-right: solid 2px #000000;
			border-bottom: solid 2px #000000;
		}
	</style>
</head>
<body>

<#-- Status -->
<span style="font-size: 18px;<#if status>border: solid 2px darkgreen;background: #E6FFE6;<#else>border: solid 2px darkred;background: #FFE6E6;</#if>">
	&nbsp;Status:
	<span style="font-weight: bold;">
	<#if status>
		<span style="color: darkgreen;">OK</span>
	<#else>
		<span style="color: red;">Error</span>
	</#if>
	</span>&nbsp;
</span>

<br/>
<br/>

<#-- Mails -->
<table cellspacing="0">
	<tr>
		<th>From</th>
		<th>To</th>
		<th>Subject</th>
		<th>Sent</th>
		<th>Folder</th>
		<th>Status</th>
	</tr>


<#list mails as mail>
	<#if !(lastFolder??) || lastFolder != mail.folder>
		<tr>
			<td style="background: #f6f6f6;font-weight: bold; color:<#if mail.folder.valid>darkgreen<#else>darkred</#if>;"
			    colspan="6">
			${mail.folder}
			</td>
		</tr>
	</#if>
	<#assign lastFolder = mail.folder>
	<tr>
		<td>${mail.from}</td>
		<td>${mail.to}</td>
		<td style="direction: rtl">${mail.subject}</td>
		<td>${mail.date?datetime}</td>
		<td style="text-align: center;">${mail.folder}</td>
		<#if mail.folder.valid>
			<td style="text-align: center;color: darkgreen;background: #E6FFE6;">OK</td>
		<#else>
			<td style="text-align: center;color: red;font-weight: bold;background: #FFE6E6">Error</td>
		</#if>
	</tr>
</#list>
</table>
</body>
</html>