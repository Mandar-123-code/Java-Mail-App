/* ========================================================
   JavaMail — Main JavaScript
   ======================================================== */

// ── Toast notifications ───────────────────────────────────
const Toast = {
  container: null,

  init() {
    if (!this.container) {
      this.container = document.createElement("div");
      this.container.className = "toast-container";
      document.body.appendChild(this.container);
    }
  },

  show(msg, type = "success", duration = 3500) {
    this.init();
    const icons = { success: "✓", error: "✕", info: "ℹ" };
    const el = document.createElement("div");
    el.className = `toast toast-${type}`;
    el.innerHTML = `
      <span class="toast-icon">${icons[type] || icons.info}</span>
      <span class="toast-msg">${msg}</span>
    `;
    this.container.appendChild(el);
    setTimeout(() => {
      el.style.opacity = "0";
      el.style.transform = "translateX(20px)";
      el.style.transition = "0.25s ease";
      setTimeout(() => el.remove(), 300);
    }, duration);
  },

  success(msg) {
    this.show(msg, "success");
  },
  error(msg) {
    this.show(msg, "error");
  },
};

// ── Auto-dismiss flash alerts ─────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".alert").forEach((alert) => {
    setTimeout(() => {
      alert.style.opacity = "0";
      alert.style.transition = "0.4s ease";
      setTimeout(() => alert.remove(), 400);
    }, 4000);
  });
});

// ── Mail checkbox selection ───────────────────────────────
let selectedMails = new Set();

function toggleMailSelect(checkbox, mailId) {
  if (checkbox.checked) {
    selectedMails.add(mailId);
  } else {
    selectedMails.delete(mailId);
  }
  updateBulkActions();
}

function toggleSelectAll(masterCb) {
  document.querySelectorAll(".mail-checkbox").forEach((cb) => {
    cb.checked = masterCb.checked;
    const id = parseInt(cb.dataset.mailId);
    masterCb.checked ? selectedMails.add(id) : selectedMails.delete(id);
  });
  updateBulkActions();
}

function updateBulkActions() {
  const bar = document.getElementById("bulk-actions");
  if (!bar) return;
  bar.style.display = selectedMails.size > 0 ? "flex" : "none";
  const cnt = document.getElementById("selected-count");
  if (cnt) cnt.textContent = selectedMails.size + " selected";
}

// ── Star toggle (AJAX) ────────────────────────────────────
function toggleStar(btn, mailId) {
  btn.classList.toggle("starred"); // instant UI

  fetch(`${contextPath}/mail/star`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `mailId=${mailId}&redirect=inbox`,
  }).catch(() => {
    btn.classList.toggle("starred"); // rollback if error
  });
}

// ── Mark important (AJAX) ─────────────────────────────────
function toggleImportant(btn, mailId) {
  btn.classList.toggle("important"); // instant UI

  fetch(`${contextPath}/mail/important`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `mailId=${mailId}&redirect=inbox`,
  }).catch(() => {
    btn.classList.toggle("important"); // rollback
  });
}

// ── Confirm delete ────────────────────────────────────────
function confirmDelete(mailId, redirect) {
  if (!confirm("Move this mail to Trash?")) return;
  submitAction("/mail/delete", { mailId, redirect });
}

function confirmEmptyTrash() {
  if (!confirm("Permanently delete all trash? This cannot be undone.")) return;
  submitAction("/mail/empty-trash", {});
}

function submitAction(path, data) {
  const form = document.createElement("form");
  form.method = "POST";
  form.action = contextPath + path;
  Object.entries(data).forEach(([k, v]) => {
    const inp = document.createElement("input");
    inp.type = "hidden";
    inp.name = k;
    inp.value = v;
    form.appendChild(inp);
  });
  document.body.appendChild(form);
  form.submit();
}

// ── Compose: save draft shortcut (Ctrl+S) ─────────────────
document.addEventListener("keydown", (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === "s") {
    const draftBtn = document.getElementById("save-draft-btn");
    if (draftBtn) {
      e.preventDefault();
      draftBtn.click();
    }
  }
});

// ── Search form ───────────────────────────────────────────
function submitSearch(e) {
  if (e.key === "Enter") {
    const q = e.target.value.trim();
    if (q)
      window.location.href = `${contextPath}/mail/search?q=${encodeURIComponent(q)}`;
  }
}

// ── Avatar initials ───────────────────────────────────────
function getInitials(name) {
  if (!name) return "?";
  const parts = name.trim().split(/\s+/);
  return parts.length >= 2
    ? (parts[0][0] + parts[1][0]).toUpperCase()
    : name.substring(0, 2).toUpperCase();
}

// ── Auto-color avatars ────────────────────────────────────
const avatarColors = [
  "#5b6ef5",
  "#3ecf8e",
  "#f5a623",
  "#f55b5b",
  "#a855f7",
  "#06b6d4",
];
function colorForEmail(email) {
  let h = 0;
  for (let i = 0; i < email.length; i++)
    h = (h * 31 + email.charCodeAt(i)) >>> 0;
  return avatarColors[h % avatarColors.length];
}

document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("[data-email-avatar]").forEach((el) => {
    const email = el.dataset.emailAvatar;
    const name = el.dataset.name || email;
    el.style.background = colorForEmail(email);
    el.textContent = getInitials(name);
  });
});

// ── contextPath injected by JSP pages ────────────────────
// The JSP sets: const contextPath = "${pageContext.request.contextPath}";
