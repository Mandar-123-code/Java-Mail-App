<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.javamail.model.Mail, com.javamail.model.User" %>
<%
    User currUser = (User) session.getAttribute("user");
    if (currUser == null) { response.sendRedirect(request.getContextPath() + "/login.jsp"); return; }

    Mail mail = (Mail) request.getAttribute("mail");
    if (mail == null) { response.sendError(404); return; }

    String ctx = request.getContextPath();
    String senderDisplay = (mail.getFromUsername() != null && !mail.getFromUsername().isEmpty())
                           ? mail.getFromUsername() : mail.getFromEmail();
    String firstLetter   = senderDisplay.substring(0, 1).toUpperCase();
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><%= mail.getSubject() %> — JavaMail</title>
  <link rel="stylesheet" href="<%= ctx %>/css/style.css">
  <script>const contextPath = "<%= ctx %>";</script>
</head>
<body>

<div class="app-shell">
  <jsp:include page="sidebar.jsp"/>

  <div class="main-content">
    <!-- Topbar -->
    <div class="topbar">
      <a href="javascript:history.back()" class="btn btn-ghost btn-sm">← Back</a>
      <div class="topbar-actions">
        <a href="<%= ctx %>/mail/reply?id=<%= mail.getId() %>" class="btn btn-secondary btn-sm">↩ Reply</a>
        <a href="<%= ctx %>/mail/forward?id=<%= mail.getId() %>" class="btn btn-secondary btn-sm">↪ Forward</a>
        <button class="btn btn-ghost btn-sm" onclick="confirmDelete(<%= mail.getId() %>, 'inbox')">🗑 Delete</button>
      </div>
    </div>

    <!-- Mail view -->
    <div class="view-area">

      <!-- Subject -->
      <div class="view-header">
        <div class="view-subject"><%= mail.getSubject() %></div>

        <!-- Tag badges -->
        <div style="display:flex;gap:8px;margin-bottom:16px;">
          <% if (mail.isStarred()) { %>
            <span style="background:rgba(245,197,24,.12);color:var(--star-color);padding:3px 10px;border-radius:20px;font-size:11px;font-weight:600;">⭐ Starred</span>
          <% } %>
          <% if (mail.isImportant()) { %>
            <span style="background:rgba(245,166,35,.12);color:var(--amber);padding:3px 10px;border-radius:20px;font-size:11px;font-weight:600;">🔖 Important</span>
          <% } %>
          <span style="background:var(--bg-3);color:var(--text-muted);padding:3px 10px;border-radius:20px;font-size:11px;">
            <%= mail.getStatus() %>
          </span>
        </div>

        <!-- Sender card -->
        <div class="view-meta-card">
          <div class="view-avatar"
               data-email-avatar="<%= mail.getFromEmail() %>"
               data-name="<%= senderDisplay %>">
            <%= firstLetter %>
          </div>
          <div>
            <div class="view-sender-name"><%= senderDisplay %></div>
            <div class="view-sender-email">
              &lt;<%= mail.getFromEmail() %>&gt;
              → &lt;<%= mail.getToEmail() %>&gt;
              <% if (mail.getCcEmail() != null && !mail.getCcEmail().isEmpty()) { %>
                | CC: <%= mail.getCcEmail() %>
              <% } %>
            </div>
          </div>
          <div class="view-timestamp"><%= mail.getFormattedDate() %></div>
        </div>
      </div>

      <!-- Action bar -->
      <div class="view-actions">
        <a href="<%= ctx %>/mail/reply?id=<%= mail.getId() %>" class="btn btn-primary btn-sm">↩ Reply</a>
        <a href="<%= ctx %>/mail/forward?id=<%= mail.getId() %>" class="btn btn-secondary btn-sm">↪ Forward</a>

        <!-- Star toggle -->
        <form method="POST" action="<%= ctx %>/mail/star" style="display:inline;">
          <input type="hidden" name="mailId" value="<%= mail.getId() %>">
          <input type="hidden" name="redirect" value="inbox">
          <button type="submit" class="btn btn-secondary btn-sm">
            <%= mail.isStarred() ? "★ Unstar" : "☆ Star" %>
          </button>
        </form>

        <!-- Important toggle -->
        <form method="POST" action="<%= ctx %>/mail/important" style="display:inline;">
          <input type="hidden" name="mailId" value="<%= mail.getId() %>">
          <input type="hidden" name="redirect" value="inbox">
          <button type="submit" class="btn btn-secondary btn-sm">
            <%= mail.isImportant() ? "🔖 Unmark" : "🔖 Mark Important" %>
          </button>
        </form>

        <!-- Mark as Spam -->
        <form method="POST" action="<%= ctx %>/mail/spam" style="display:inline;">
          <input type="hidden" name="mailId" value="<%= mail.getId() %>">
          <input type="hidden" name="redirect" value="inbox">
          <button type="submit" class="btn btn-ghost btn-sm">🚫 Spam</button>
        </form>

        <!-- Delete -->
        <button class="btn btn-ghost btn-sm" onclick="confirmDelete(<%= mail.getId() %>, 'inbox')">🗑 Delete</button>
      </div>

      <!-- Mail body -->
      <div class="view-body"><%= mail.getBody() != null ? mail.getBody() : "(No content)" %></div>

    </div><!-- /view-area -->
  </div><!-- /main-content -->
</div>

<script src="<%= ctx %>/js/app.js"></script>
</body>
</html>
