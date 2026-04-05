<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%  String ctx = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>500 — JavaMail</title>
  <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body style="display:flex;align-items:center;justify-content:center;min-height:100vh;flex-direction:column;gap:20px;">
  <div style="font-size:80px;">⚠</div>
  <div style="font-family:'Syne',sans-serif;font-size:32px;font-weight:800;color:var(--text-primary);">
    500 — Server Error
  </div>
  <div style="color:var(--text-secondary);font-size:15px;">
    Something went wrong on our end. Please try again.
  </div>
  <% if (exception != null) { %>
    <div style="background:var(--bg-2);border:1px solid var(--border);border-radius:var(--radius);
                padding:16px 20px;max-width:600px;width:90%;font-size:12px;color:var(--red);font-family:monospace;">
      <%= exception.getMessage() %>
    </div>
  <% } %>
  <a href="<%= ctx %>/mail/inbox" class="btn btn-primary">← Back to Inbox</a>
</body>
</html>
