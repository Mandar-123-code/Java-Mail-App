<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%  String ctx = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>404 — JavaMail</title>
  <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body style="display:flex;align-items:center;justify-content:center;min-height:100vh;flex-direction:column;gap:20px;">
  <div style="font-size:80px;">📭</div>
  <div style="font-family:'Syne',sans-serif;font-size:32px;font-weight:800;color:var(--text-primary);">
    404 — Page Not Found
  </div>
  <div style="color:var(--text-secondary);font-size:15px;">
    The page you're looking for doesn't exist.
  </div>
  <a href="<%= ctx %>/mail/inbox" class="btn btn-primary">← Back to Inbox</a>
</body>
</html>
