<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.javamail.model.Mail, com.javamail.model.User" %>
<%
    User currUser = (User) session.getAttribute("user");
    if (currUser == null) { response.sendRedirect(request.getContextPath() + "/login.jsp"); return; }

    String ctx         = request.getContextPath();
    String composeMode = (String) request.getAttribute("composeMode");
    Mail   original    = (Mail)   request.getAttribute("original");
    Mail   prefill     = (Mail)   request.getAttribute("prefill");
    if (composeMode == null) composeMode = "compose";

    String headingText;
    switch (composeMode) {
        case "reply":
            headingText = "Reply";
            break;
        case "forward":
            headingText = "Forward";
            break;
        default:
            headingText = "New Mail";
    }

    // Pre-fill fields
    String toVal      = "";
    String subjectVal = "";
    String bodyVal    = "";

    if ("reply".equals(composeMode) && original != null) {
        toVal      = original.getFromEmail();
        subjectVal = "Re: " + original.getSubject();
        bodyVal    = "\n\n--- Original message ---\nFrom: " + original.getFromEmail()
                   + "\nDate: " + original.getFormattedDate()
                   + "\n\n" + original.getBody();
    } else if ("forward".equals(composeMode) && original != null) {
        subjectVal = "Fwd: " + original.getSubject();
        bodyVal    = "\n\n--- Forwarded message ---\nFrom: " + original.getFromEmail()
                   + "\nDate: " + original.getFormattedDate()
                   + "\n\n" + original.getBody();
    } else if (prefill != null) {
        toVal      = prefill.getToEmail()  != null ? prefill.getToEmail()  : "";
        subjectVal = prefill.getSubject()  != null ? prefill.getSubject()  : "";
        bodyVal    = prefill.getBody()     != null ? prefill.getBody()     : "";
    }

    String error   = (String) session.getAttribute("error");
    session.removeAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><%= headingText %> — JavaMail</title>
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
    </div>

    <div class="compose-area">
      <div class="compose-heading">✏ <%= headingText %></div>

      <% if (error != null) { %>
        <div class="alert alert-error" style="margin-bottom:16px;">⚠ <%= error %></div>
      <% } %>

      <div class="compose-form-card">

        <!-- From (read-only) -->
        <div class="compose-field">
          <span class="compose-field-label">From</span>
          <input type="text" value="<%= currUser.getEmail() %>" readonly
                 style="color:var(--text-muted);cursor:default;">
        </div>

        <!-- To -->
        <div class="compose-field">
          <span class="compose-field-label">To</span>
          <input type="email" id="to-field" name="to" form="mail-form"
                 value="<%= toVal %>"
                 placeholder="recipient@example.com" required
                 <%= "reply".equals(composeMode) ? "" : "" %>>
        </div>

        <!-- CC (collapsible) -->
        <div class="compose-field" id="cc-row" style="display:none;">
          <span class="compose-field-label">CC</span>
          <input type="email" name="cc" form="mail-form" placeholder="cc@example.com">
        </div>

        <!-- BCC (collapsible) -->
        <div class="compose-field" id="bcc-row" style="display:none;">
          <span class="compose-field-label">BCC</span>
          <input type="email" name="bcc" form="mail-form" placeholder="bcc@example.com">
        </div>

        <!-- Subject -->
        <div class="compose-field">
          <span class="compose-field-label">Subject</span>
          <input type="text" name="subject" form="mail-form"
                 value="<%= subjectVal %>"
                 placeholder="Subject" required>
        </div>

        <!-- Body -->
        <div class="compose-field" style="border-bottom:none;">
          <span class="compose-field-label" style="padding-top:16px;">Body</span>
          <textarea name="body" form="mail-form"
                    placeholder="Write your message here…"
                    id="mail-body"><%= bodyVal %></textarea>
        </div>

        <!-- Toolbar -->
        <div class="compose-toolbar">
          <!-- CC / BCC toggles -->
          <button type="button" class="btn btn-ghost btn-sm" onclick="toggleField('cc-row')">+ CC</button>
          <button type="button" class="btn btn-ghost btn-sm" onclick="toggleField('bcc-row')">+ BCC</button>

          <div style="margin-left:auto;display:flex;gap:8px;">
            <!-- Save Draft -->
            <button type="button" id="save-draft-btn" class="btn btn-secondary btn-sm"
                    onclick="submitForm('draft')">💾 Save Draft</button>
            <!-- Send -->
            <button type="submit" form="mail-form" class="btn btn-primary btn-sm">
              📤 Send
            </button>
          </div>
        </div>
      </div><!-- /compose-form-card -->

      <!-- Actual form (outside card so button can submit it) -->
      <form id="mail-form" action="<%= ctx %>/mail/send" method="POST" style="display:none;">
        <input type="hidden" name="_action" id="form-action" value="send">
      </form>

    </div><!-- /compose-area -->
  </div><!-- /main-content -->
</div>

<script src="<%= ctx %>/js/app.js"></script>
<script>
function toggleField(id) {
  const row = document.getElementById(id);
  row.style.display = row.style.display === 'none' ? 'flex' : 'none';
}

function submitForm(action) {
  const form = document.getElementById('mail-form');
  if (action === 'draft') {
    form.action = contextPath + '/mail/draft/save';
  } else {
    form.action = contextPath + '/mail/send';
  }
  // Copy inline inputs to hidden form
  ['to', 'cc', 'bcc', 'subject', 'body'].forEach(name => {
    let src = document.querySelector(`[name="${name}"]`);
    if (!src) src = document.getElementById(name === 'body' ? 'mail-body' : name + '-field');
    if (src) {
      let inp = form.querySelector(`[name="${name}"]`);
      if (!inp) {
        inp = document.createElement('input');
        inp.type  = 'hidden';
        inp.name  = name;
        form.appendChild(inp);
      }
      inp.value = src.value;
    }
  });
  form.submit();
}

// Override send button to go through submitForm
document.getElementById('mail-form').addEventListener('submit', function(e) {
  e.preventDefault();
  submitForm('send');
});

// Also make inline inputs submit via submitForm on Enter in To/Subject fields
['to', 'subject'].forEach(id => {
  const el = document.querySelector(`[name="${id}"]`);
  if (el) el.addEventListener('keydown', e => {
    if (e.key === 'Enter') { e.preventDefault(); submitForm('send'); }
  });
});
</script>
</body>
</html>
