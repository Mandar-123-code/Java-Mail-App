<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%
    String ctx = request.getContextPath();
    Object statusObj = request.getAttribute("statusCode");
    Object msgObj = request.getAttribute("errorMessage");
    String statusCode = statusObj != null ? statusObj.toString() : "500";
    String errorText = msgObj != null ? msgObj.toString() : "Something went wrong on our end. Please try again.";
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title><%= statusCode %> — JavaMail</title>
  <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/images/favicon.svg">
  <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body style="display:flex;align-items:center;justify-content:center;min-height:100vh;flex-direction:column;gap:20px;text-align:center;padding:20px;">
  <div style="font-size:80px;">⚡</div>
  <div style="font-family:'Syne',sans-serif;font-size:32px;font-weight:800;color:var(--text-primary);">
    <%= statusCode %> — Application Notice
  </div>
  <div style="color:var(--text-secondary);font-size:15px;max-width:500px;">
    <%= errorText %>
  </div>
  <% if (exception != null && exception.getMessage() != null) { %>
    <div style="background:var(--bg-2);border:1px solid var(--border);border-radius:var(--radius);
                padding:16px 20px;max-width:600px;width:90%;font-size:12px;color:var(--red);font-family:monospace;text-align:left;">
      <%= exception.getMessage() %>
    </div>
  <% } %>
  <div style="display:flex;gap:12px;margin-top:10px;">
    <a href="<%= ctx %>/login" class="btn btn-secondary">Sign In</a>
    <a href="<%= ctx %>/mailbox" class="btn btn-primary">← Go to Inbox</a>
  </div>
</body>
</html>

