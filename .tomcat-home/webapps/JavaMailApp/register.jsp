<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (session != null && session.getAttribute("user") != null) {
        response.sendRedirect(request.getContextPath() + "/mail/inbox");
        return;
    }
    String error = (String) session.getAttribute("error");
    session.removeAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>JavaMail — Create Account</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>

<div class="auth-root">

  <!-- ── Left: Form ────────────────────────────── -->
  <div class="auth-panel">
    <div class="auth-brand">
      <div class="auth-brand-icon">✉</div>
      <span class="auth-brand-name">JavaMail</span>
    </div>

    <h1 class="auth-heading">Create account</h1>
    <p class="auth-sub">Set up your JavaMail inbox in seconds</p>

    <% if (error != null) { %>
      <div class="alert alert-error">⚠ <%= error %></div>
    <% } %>

    <form action="<%= request.getContextPath() %>/user/register" method="POST">
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">First Name</label>
          <input class="form-input" type="text" name="firstname" placeholder="Alice" required autofocus>
        </div>
        <div class="form-group">
          <label class="form-label">Last Name</label>
          <input class="form-input" type="text" name="lastname" placeholder="Johnson" required>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">Email Address</label>
        <input class="form-input" type="email" name="email" placeholder="alice@javamail.com" required>
      </div>

      <div class="form-group">
        <label class="form-label">Password</label>
        <input class="form-input" type="password" name="password" placeholder="At least 6 characters" required minlength="6">
      </div>

      <div class="form-row">
        <div class="form-group">
          <label class="form-label">Date of Birth</label>
          <input class="form-input" type="date" name="dob">
        </div>
        <div class="form-group">
          <label class="form-label">Contact Number</label>
          <input class="form-input" type="tel" name="contact" placeholder="9876543210">
        </div>
      </div>

      <button type="submit" class="btn btn-primary btn-full" style="margin-top:4px;">
        Create Account →
      </button>
    </form>

    <div class="auth-footer">
      Already have an account? <a href="<%= request.getContextPath() %>/login.jsp">Sign in</a>
    </div>
  </div>

  <!-- ── Right: Visual ─────────────────────────── -->
  <div class="auth-visual">
    <div class="auth-visual-glow"></div>
    <div class="auth-visual-title">Join<br>JavaMail.</div>
    <div class="auth-visual-sub">
      A full-featured email platform with compose, inbox, drafts, trash, spam, starred, search and more — all persisted to MySQL.
    </div>
    <div class="auth-features">
      <div class="auth-feature">
        <div class="auth-feature-icon">📝</div>
        <div class="auth-feature-text"><strong>Compose & Draft</strong>Write, save drafts, send when ready</div>
      </div>
      <div class="auth-feature">
        <div class="auth-feature-icon">🗑</div>
        <div class="auth-feature-text"><strong>Trash & Spam</strong>Full lifecycle management for every mail</div>
      </div>
      <div class="auth-feature">
        <div class="auth-feature-icon">🔍</div>
        <div class="auth-feature-text"><strong>Search</strong>Find any mail by subject, sender, or content</div>
      </div>
    </div>
  </div>

</div>

<script src="<%= request.getContextPath() %>/js/app.js"></script>
</body>
</html>
