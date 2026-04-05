<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.javamail.model.User" %>
<%
    User currUser = (User) session.getAttribute("user");
    if (currUser == null) { response.sendRedirect(request.getContextPath() + "/login.jsp"); return; }

    String ctx     = request.getContextPath();
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
  <title>Profile — JavaMail</title>
  <link rel="stylesheet" href="<%= ctx %>/css/style.css">
  <script>const contextPath = "<%= ctx %>";</script>
</head>
<body>
<div class="app-shell">
  <%-- pass folder="profile" via request attribute for sidebar active state --%>
  <% request.setAttribute("folder","profile"); request.setAttribute("unread",0); %>
  <jsp:include page="sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <span class="topbar-folder">Profile & Settings</span>
    </div>

    <div class="profile-area">
      <div class="profile-heading">👤 My Profile</div>

      <% if (success != null) { %>
        <div class="alert alert-success">✓ <%= success %></div>
      <% } %>
      <% if (error != null) { %>
        <div class="alert alert-error">⚠ <%= error %></div>
      <% } %>

      <!-- ── Avatar + Name card ─────────────────────── -->
      <div class="profile-card" style="display:flex;align-items:center;gap:20px;">
        <div style="width:72px;height:72px;border-radius:50%;background:var(--accent);
                    display:flex;align-items:center;justify-content:center;
                    font-size:28px;font-weight:700;color:#fff;flex-shrink:0;"
             data-email-avatar="<%= currUser.getEmail() %>"
             data-name="<%= currUser.getUsername() %>">
          <%= currUser.getUsername().substring(0,1).toUpperCase() %>
        </div>
        <div>
          <div style="font-family:'Syne',sans-serif;font-size:20px;font-weight:700;margin-bottom:4px;">
            <%= currUser.getUsername() %>
          </div>
          <div style="color:var(--text-secondary);font-size:14px;">
            <%= currUser.getEmail() %>
          </div>
          <div style="color:var(--text-muted);font-size:12px;margin-top:4px;">
            Member since <%= currUser.getCreatedAt() != null
              ? new java.text.SimpleDateFormat("MMMM yyyy").format(currUser.getCreatedAt())
              : "—" %>
          </div>
        </div>
      </div>

      <!-- ── Edit Profile ─────────────────────────────── -->
      <div class="profile-card">
        <div class="profile-card-title">Edit Profile</div>
        <form action="<%= ctx %>/user/profile" method="POST">
          <div class="form-group">
            <label class="form-label">Full Name</label>
            <input class="form-input" type="text" name="username"
                   value="<%= currUser.getUsername() %>" required>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Date of Birth</label>
              <input class="form-input" type="date" name="dob"
                     value="<%= currUser.getDob() != null ? currUser.getDob() : "" %>">
            </div>
            <div class="form-group">
              <label class="form-label">Contact Number</label>
              <input class="form-input" type="tel" name="contact"
                     value="<%= currUser.getContact() != null ? currUser.getContact() : "" %>">
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Email Address</label>
            <input class="form-input" type="email" value="<%= currUser.getEmail() %>" readonly
                   style="color:var(--text-muted);cursor:not-allowed;">
            <small style="color:var(--text-muted);font-size:11px;">Email cannot be changed.</small>
          </div>
          <button type="submit" class="btn btn-primary">Save Changes</button>
        </form>
      </div>

      <!-- ── Change Password ──────────────────────────── -->
      <div class="profile-card">
        <div class="profile-card-title">Change Password</div>
        <form action="<%= ctx %>/user/password" method="POST">
          <div class="form-group">
            <label class="form-label">Current Password</label>
            <input class="form-input" type="password" name="currentPassword" required
                   placeholder="Your current password">
          </div>
          <div class="form-group">
            <label class="form-label">New Password</label>
            <input class="form-input" type="password" name="newPassword" required
                   placeholder="New password (min 6 chars)" minlength="6" id="new-pass">
          </div>
          <div class="form-group">
            <label class="form-label">Confirm New Password</label>
            <input class="form-input" type="password" name="confirmPassword" required
                   placeholder="Repeat new password" id="confirm-pass">
          </div>
          <button type="submit" class="btn btn-primary">Update Password</button>
        </form>
      </div>

      <!-- ── Account Stats ────────────────────────────── -->
      <div class="profile-card">
        <div class="profile-card-title">Account Info</div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">
          <div style="background:var(--bg-2);border:1px solid var(--border);border-radius:var(--radius);padding:16px;text-align:center;">
            <div style="font-family:'Syne',sans-serif;font-size:28px;font-weight:800;color:var(--accent);">∞</div>
            <div style="font-size:12px;color:var(--text-muted);margin-top:4px;">Storage</div>
          </div>
          <div style="background:var(--bg-2);border:1px solid var(--border);border-radius:var(--radius);padding:16px;text-align:center;">
            <div style="font-family:'Syne',sans-serif;font-size:28px;font-weight:800;color:var(--green);">✓</div>
            <div style="font-size:12px;color:var(--text-muted);margin-top:4px;">Account Active</div>
          </div>
        </div>
      </div>

      <!-- ── Danger Zone ──────────────────────────────── -->
      <div class="profile-card" style="border-color:rgba(245,91,91,.25);">
        <div class="profile-card-title" style="color:var(--red);">Danger Zone</div>
        <p style="font-size:13px;color:var(--text-secondary);margin-bottom:16px;">
          Logging out will end your current session.
        </p>
        <a href="<%= ctx %>/user/logout" class="btn btn-danger btn-sm">⏻ Sign Out</a>
      </div>

    </div><!-- /profile-area -->
  </div><!-- /main-content -->
</div>

<script src="<%= ctx %>/js/app.js"></script>
<script>
// Password match validation
document.querySelector('form[action*="password"]').addEventListener('submit', function(e) {
  const np = document.getElementById('new-pass').value;
  const cp = document.getElementById('confirm-pass').value;
  if (np !== cp) {
    e.preventDefault();
    Toast.error('New passwords do not match!');
  }
});
</script>
</body>
</html>
