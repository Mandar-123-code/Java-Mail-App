<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.javamail.model.User" %>
<%
    User __sidebarUser  = (User)  session.getAttribute("user");
    int  __unreadCount  = (request.getAttribute("unread") instanceof Integer)
                         ? (Integer) request.getAttribute("unread") : 0;
    String __folder     = (String) request.getAttribute("folder");
    if (__folder == null) __folder = "";
    String __ctx        = request.getContextPath();
%>

<nav class="sidebar">
  <!-- Logo -->
  <div class="sidebar-logo">
    <div class="sidebar-logo-icon">✉</div>
    <span class="sidebar-logo-text">JavaMail</span>
  </div>

  <!-- Compose button -->
  <a href="<%= __ctx %>/mail/compose" class="compose-btn">
    ✏ Compose
  </a>

  <!-- Folders -->
  <span class="nav-section-label">Folders</span>

  <a href="<%= __ctx %>/mail/inbox"
     class="nav-item <%= "inbox".equals(__folder) ? "active" : "" %>">
    <span class="nav-icon">📥</span> Inbox
    <% if (__unreadCount > 0) { %><span class="nav-badge"><%= __unreadCount %></span><% } %>
  </a>

  <a href="<%= __ctx %>/mail/sent"
     class="nav-item <%= "sent".equals(__folder) ? "active" : "" %>">
    <span class="nav-icon">📤</span> Sent
  </a>

  <a href="<%= __ctx %>/mail/draft"
     class="nav-item <%= "draft".equals(__folder) ? "active" : "" %>">
    <span class="nav-icon">📝</span> Drafts
  </a>

  <a href="<%= __ctx %>/mail/starred"
     class="nav-item <%= "starred".equals(__folder) ? "active" : "" %>">
    <span class="nav-icon">⭐</span> Starred
  </a>

  <a href="<%= __ctx %>/mail/important"
     class="nav-item <%= "important".equals(__folder) ? "active" : "" %>">
    <span class="nav-icon">🔖</span> Important
  </a>

  <div class="sidebar-divider"></div>

  <a href="<%= __ctx %>/mail/spam"
     class="nav-item <%= "spam".equals(__folder) ? "active" : "" %>">
    <span class="nav-icon">🚫</span> Spam
  </a>

  <a href="<%= __ctx %>/mail/trash"
     class="nav-item <%= "trash".equals(__folder) ? "active" : "" %>">
    <span class="nav-icon">🗑</span> Trash
  </a>

  <div class="sidebar-divider"></div>

  <!-- Settings -->
  <span class="nav-section-label">Account</span>
  <a href="<%= __ctx %>/user/profile"
     class="nav-item <%= "profile".equals(__folder) ? "active" : "" %>">
    <span class="nav-icon">👤</span> Profile
  </a>

  <!-- User info + logout -->
  <div class="sidebar-user">
    <div class="sidebar-avatar"
         data-email-avatar="<%= __sidebarUser != null ? __sidebarUser.getEmail() : "" %>"
         data-name="<%= __sidebarUser != null ? __sidebarUser.getUsername() : "" %>">
      <%= __sidebarUser != null ? __sidebarUser.getUsername().substring(0,1).toUpperCase() : "?" %>
    </div>
    <div class="sidebar-user-info">
      <div class="sidebar-user-name"><%= __sidebarUser != null ? __sidebarUser.getUsername() : "" %></div>
      <div class="sidebar-user-email"><%= __sidebarUser != null ? __sidebarUser.getEmail() : "" %></div>
    </div>
    <a href="<%= __ctx %>/user/logout" class="sidebar-logout" title="Logout">⏻</a>
  </div>
</nav>
