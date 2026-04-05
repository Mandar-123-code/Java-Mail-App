<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.javamail.model.Mail, com.javamail.model.User, java.util.List" %>
<%
    User   currUser = (User)   session.getAttribute("user");
    if (currUser == null) { response.sendRedirect(request.getContextPath() + "/login.jsp"); return; }

    List<Mail> mails  = (List<Mail>) request.getAttribute("mails");
    String folder     = (String)     request.getAttribute("folder");
    String query      = (String)     request.getAttribute("query");
    int    unread     = (request.getAttribute("unread") instanceof Integer) ? (Integer) request.getAttribute("unread") : 0;
    String ctx        = request.getContextPath();

    String folderTitle;
    switch (folder != null ? folder : "") {
        case "inbox":
            folderTitle = "Inbox";
            break;
        case "sent":
            folderTitle = "Sent";
            break;
        case "draft":
            folderTitle = "Drafts";
            break;
        case "trash":
            folderTitle = "Trash";
            break;
        case "spam":
            folderTitle = "Spam";
            break;
        case "starred":
            folderTitle = "Starred";
            break;
        case "important":
            folderTitle = "Important";
            break;
        case "search":
            folderTitle = "Search Results";
            break;
        default:
            folderTitle = "Mails";
    }

    String success = (String) session.getAttribute("success");
    String error   = (String) session.getAttribute("error");
    session.removeAttribute("success");
    session.removeAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><%= folderTitle %> — JavaMail</title>
  <link rel="stylesheet" href="<%= ctx %>/css/style.css">
  <script>const contextPath = "<%= ctx %>";</script>
</head>
<body>

<div class="app-shell">

  <!-- Sidebar -->
  <jsp:include page="sidebar.jsp"/>

  <!-- Main -->
  <div class="main-content">

    <!-- Topbar -->
    <div class="topbar">
      <span class="topbar-folder"><%= folderTitle %></span>

      <form class="search-bar" action="<%= ctx %>/mail/search" method="GET">
        <span class="search-icon">🔍</span>
        <input type="text" name="q" placeholder="Search mails…"
               value="<%= query != null ? query : "" %>"
               onkeypress="submitSearch(event)">
      </form>

      <div class="topbar-actions">
        <a href="<%= ctx %>/mail/compose" class="btn btn-primary btn-sm">✏ Compose</a>
      </div>
    </div>

    <!-- Flash messages -->
    <% if (success != null) { %>
      <div style="padding:0 24px;margin-top:12px;">
        <div class="alert alert-success">✓ <%= success %></div>
      </div>
    <% } %>
    <% if (error != null) { %>
      <div style="padding:0 24px;margin-top:12px;">
        <div class="alert alert-error">⚠ <%= error %></div>
      </div>
    <% } %>

    <!-- Mail list -->
    <div class="mail-list-area">

      <!-- List header with bulk actions -->
      <div class="mail-list-header">
        <input type="checkbox" class="checkbox-custom" id="select-all" onchange="toggleSelectAll(this)" title="Select all">
        <span class="mail-count">
          <% if (mails != null) { %>
            <%= mails.size() %> mail<%= mails.size() != 1 ? "s" : "" %>
          <% } %>
        </span>

        <!-- Bulk actions (hidden until selection) -->
        <div id="bulk-actions" style="display:none; align-items:center; gap:8px;">
          <span id="selected-count" style="font-size:12px;color:var(--text-secondary);"></span>
          <% if (!"trash".equals(folder)) { %>
          <button class="btn btn-ghost btn-sm" onclick="bulkAction('delete')">🗑 Delete</button>
          <% } %>
          <% if ("trash".equals(folder)) { %>
          <button class="btn btn-ghost btn-sm" onclick="bulkAction('restore')">↩ Restore</button>
          <% } %>
          <button class="btn btn-ghost btn-sm" onclick="bulkAction('star')">⭐ Star</button>
          <button class="btn btn-ghost btn-sm" onclick="bulkAction('spam')">🚫 Spam</button>
        </div>

        <div class="mail-list-header-actions" style="margin-left:auto;">
          <% if ("trash".equals(folder) && mails != null && !mails.isEmpty()) { %>
            <button class="btn btn-danger btn-sm" onclick="confirmEmptyTrash()">🗑 Empty Trash</button>
          <% } %>
          <button class="btn btn-ghost btn-sm" onclick="location.reload()" title="Refresh">↻</button>
        </div>
      </div>

      <!-- Search label -->
      <% if ("search".equals(folder) && query != null) { %>
        <div style="padding:12px 24px;font-size:13px;color:var(--text-secondary);border-bottom:1px solid var(--border-soft);">
          Results for <strong style="color:var(--text-primary);">"<%= query %>"</strong>
        </div>
      <% } %>

      <!-- Mail rows -->
      <% if (mails == null || mails.isEmpty()) { %>
        <div class="empty-state">
          <div class="empty-icon">
            <%= "inbox".equals(folder) ? "📭" : "trash".equals(folder) ? "🗑" : "draft".equals(folder) ? "📝" : "📭" %>
          </div>
          <div class="empty-title">
            <%= "search".equals(folder) ? "No results found" : "Nothing here" %>
          </div>
          <div class="empty-sub">
            <%= "inbox".equals(folder) ? "Your inbox is all clear!" :
                "search".equals(folder) ? "Try a different search term." :
                "This folder is empty." %>
          </div>
          <% if ("inbox".equals(folder)) { %>
            <a href="<%= ctx %>/mail/compose" class="btn btn-primary btn-sm" style="margin-top:8px;">✏ Compose a mail</a>
          <% } %>
        </div>
      <% } else { %>
        <% for (Mail mail : mails) {
            boolean isUnread = !mail.isRead();
            String senderDisplay = (mail.getFromUsername() != null && !mail.getFromUsername().isEmpty())
                                   ? mail.getFromUsername() : mail.getFromEmail();
            boolean isMine = currUser.getEmail().equals(mail.getFromEmail());
            String displayName = isMine ? ("To: " + mail.getToEmail()) : senderDisplay;
        %>
        <div class="mail-item <%= isUnread ? "unread" : "" %>">
          <!-- Checkbox -->
          <input type="checkbox" class="checkbox-custom mail-checkbox"
                 data-mail-id="<%= mail.getId() %>"
                 onchange="toggleMailSelect(this, <%= mail.getId() %>)">

          <!-- Avatar -->
          <div class="mail-item-avatar"
               data-email-avatar="<%= mail.getFromEmail() %>"
               data-name="<%= senderDisplay %>">
            <%= senderDisplay.substring(0,1).toUpperCase() %>
          </div>

          <!-- Main content (clickable) -->
          <a href="<%= ctx %>/mail/view?id=<%= mail.getId() %>" class="mail-item-main" style="display:block;flex:1;min-width:0;">
            <div class="mail-item-top">
              <span class="mail-item-sender"><%= displayName %></span>
              <span class="mail-item-subject">— <%= mail.getSubject() %></span>
            </div>
            <div class="mail-item-preview"><%= mail.getPreview() %></div>
          </a>

          <!-- Meta -->
          <div class="mail-item-meta">
            <span class="mail-item-date"><%= mail.getFormattedDate() %></span>
            <div class="mail-item-icons">
              <!-- Star -->
              <button class="icon-btn <%= mail.isStarred() ? "starred" : "" %>"
                      title="<%= mail.isStarred() ? "Unstar" : "Star" %>"
                      onclick="toggleStar(this, <%= mail.getId() %>)">⭐</button>
              <!-- Important -->
              <button class="icon-btn <%= mail.isImportant() ? "important" : "" %>"
                      title="<%= mail.isImportant() ? "Unmark" : "Mark important" %>"
                      onclick="toggleImportant(this, <%= mail.getId() %>)">🔖</button>
              <!-- Delete / Restore -->
              <% if ("trash".equals(folder)) { %>
                <form method="POST" action="<%= ctx %>/mail/restore" style="display:inline;">
                  <input type="hidden" name="mailId" value="<%= mail.getId() %>">
                  <input type="hidden" name="redirect" value="trash">
                  <button type="submit" class="icon-btn" title="Restore">↩</button>
                </form>
              <% } else { %>
                <button class="icon-btn" title="Delete"
                        onclick="confirmDelete(<%= mail.getId() %>, '<%= folder %>')">🗑</button>
              <% } %>
            </div>
          </div>
        </div>
        <% } %>
      <% } %>
    </div><!-- /mail-list-area -->

  </div><!-- /main-content -->
</div><!-- /app-shell -->

<script src="<%= ctx %>/js/app.js"></script>
<script>
// Bulk action helper
function bulkAction(action) {
  if (selectedMails.size === 0) return;
  if (!confirm(`Apply "${action}" to ${selectedMails.size} mail(s)?`)) return;
  const ids = Array.from(selectedMails);
  ids.forEach(id => {
    fetch(`${contextPath}/mail/${action}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: `mailId=${id}&redirect=<%= folder %>`
    });
  });
  setTimeout(() => location.reload(), 600);
}

// Show flash toasts
<% if (success != null) { %>
  document.addEventListener('DOMContentLoaded', () => Toast.success("<%= success %>"));
<% } %>
<% if (error != null) { %>
  document.addEventListener('DOMContentLoaded', () => Toast.error("<%= error %>"));
<% } %>
</script>

</body>
</html>
