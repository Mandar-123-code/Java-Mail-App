<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%
    // Already logged in? Go to inbox
    if (session != null && session.getAttribute("user") != null) {
        response.sendRedirect(request.getContextPath() + "/mail/inbox");
        return;
    }
    String error   = (String) session.getAttribute("error");
    String success = (String) session.getAttribute("success");
    session.removeAttribute("error");
    session.removeAttribute("success");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>JavaMail — Sign In</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
  <link rel="icon" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><text y='.9em' font-size='90'>✉</text></svg>">
</head>
<body>

<div class="auth-root">

  <!-- ── Left: Form ────────────────────────────── -->
  <div class="auth-panel">
    <div class="auth-brand">
      <div class="auth-brand-icon">✉</div>
      <span class="auth-brand-name">JavaMail</span>
    </div>

    <h1 class="auth-heading">Welcome back</h1>
    <p class="auth-sub">Sign in to your account to continue</p>

    <% if (error != null) { %>
      <div class="alert alert-error">⚠ <%= error %></div>
    <% } %>
    <% if (success != null) { %>
      <div class="alert alert-success">✓ <%= success %></div>
    <% } %>

    <form action="<%= request.getContextPath() %>/user/login" method="POST">
      <div class="form-group">
        <label class="form-label">Email address</label>
        <input class="form-input" type="email" name="email" placeholder="you@example.com" required autofocus>
      </div>
      <div class="form-group">
        <label class="form-label">Password</label>
        <input class="form-input" type="password" name="password" placeholder="Your password" required>
      </div>
      <button type="submit" class="btn btn-primary btn-full" style="margin-top:8px;">
        Sign In →
      </button>
    </form>

    <div class="auth-footer">
      Don't have an account? <a href="<%= request.getContextPath() %>/register.jsp">Create one</a>
    </div>
  </div>

  <!-- ── Right: Visual ─────────────────────────── -->
  <div class="auth-visual">
    <div class="auth-visual-glow"></div>
    <div class="auth-visual-title">Your inbox,<br>reimagined.</div>
    <div class="auth-visual-sub">
      A fast, powerful email client built entirely in Java — with a clean interface, full JDBC persistence, and every feature you need.
    </div>
    <div class="auth-features">
      <div class="auth-feature">
        <div class="auth-feature-icon">📥</div>
        <div class="auth-feature-text">
          <strong>Smart Inbox</strong>
          Unread counts, star, and important flags keep you on top of everything
        </div>
      </div>
      <div class="auth-feature">
        <div class="auth-feature-icon">⚡</div>
        <div class="auth-feature-text">
          <strong>JDBC Backed</strong>
          All data persisted to PostgreSQL via PreparedStatements — fully production-ready
        </div>
      </div>
      <div class="auth-feature">
        <div class="auth-feature-icon">🔒</div>
        <div class="auth-feature-text">
          <strong>Secure Auth</strong>
          SHA-256 hashed passwords and session management built in
        </div>
      </div>
    </div>
  </div>

</div>

<script src="<%= request.getContextPath() %>/js/app.js"></script>
</body>
</html>
