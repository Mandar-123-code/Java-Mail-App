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

// ── Shared modal dialogs ──────────────────────────────────
const Modal = {
  queue: [],
  active: false,

  show(
    message,
    {
      title = "Please confirm",
      type = "confirm",
      confirmText = "Continue",
    } = {},
  ) {
    return new Promise((resolve) => {
      this.queue.push({ message, title, type, confirmText, resolve });
      this.next();
    });
  },

  next() {
    if (this.active || this.queue.length === 0) return;
    this.active = true;
    const { message, title, type, confirmText, resolve } = this.queue.shift();
    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";
    overlay.innerHTML = `
      <section class="modal-dialog" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <button class="modal-close" type="button" aria-label="Close">×</button>
        <div class="modal-icon modal-icon-${type}">${type === "danger" ? "!" : "?"}</div>
        <h2 id="modal-title">${title}</h2>
        <p class="modal-message"></p>
        <div class="modal-actions">
          <button class="btn btn-secondary modal-cancel" type="button">Cancel</button>
          <button class="btn ${type === "danger" ? "btn-danger" : "btn-primary"} modal-confirm" type="button">${confirmText}</button>
        </div>
      </section>`;
    overlay.querySelector(".modal-message").textContent = message;
    document.body.appendChild(overlay);
    requestAnimationFrame(() => overlay.classList.add("is-visible"));

    const finish = (confirmed) => {
      overlay.classList.remove("is-visible");
      setTimeout(() => {
        overlay.remove();
        this.active = false;
        resolve(confirmed);
        this.next();
      }, 180);
    };
    overlay
      .querySelector(".modal-confirm")
      .addEventListener("click", () => finish(true));
    overlay
      .querySelector(".modal-cancel")
      .addEventListener("click", () => finish(false));
    overlay
      .querySelector(".modal-close")
      .addEventListener("click", () => finish(false));
    overlay.addEventListener("click", (event) => {
      if (event.target === overlay) finish(false);
    });
    overlay.addEventListener("keydown", (event) => {
      if (event.key === "Escape") finish(false);
    });
    overlay.querySelector(".modal-confirm").focus();
  },
};

// Convert every server flash alert to the same modal presentation.
document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".alert").forEach((alert) => {
    const isError = alert.classList.contains("alert-error");
    const message = alert.textContent.replace(/^[\s⚠✓]+/, "").trim();
    alert.remove();
    Modal.show(message, {
      title: isError ? "Something went wrong" : "Done",
      type: isError ? "danger" : "success",
      confirmText: "Close",
    });
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
    body: `id=${mailId}`,
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
    body: `id=${mailId}`,
  }).catch(() => {
    btn.classList.toggle("important"); // rollback
  });
}

// ── Confirm delete ────────────────────────────────────────
function confirmDelete(mailId, redirect) {
  Modal.show("This mail will be moved to Trash.", {
    title: "Move to Trash?",
    type: "danger",
    confirmText: "Move mail",
  }).then((confirmed) => {
    if (confirmed) submitAction("/mail/delete", { mailId, redirect });
  });
}

function confirmEmptyTrash() {
  Modal.show(
    "All messages in Trash will be permanently deleted. This cannot be undone.",
    { title: "Empty Trash?", type: "danger", confirmText: "Empty Trash" },
  ).then((confirmed) => {
    if (confirmed) submitAction("/mail/empty-trash", {});
  });
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

// ── Light / Dark Theme Switcher ──────────────────────────
function initTheme() {
  const savedTheme = localStorage.getItem("javamail-theme") || "light";
  applyTheme(savedTheme);

  const toggleBtn = document.getElementById("theme-toggle-btn");
  if (toggleBtn) {
    toggleBtn.addEventListener("click", () => {
      const currentTheme =
        document.documentElement.getAttribute("data-theme") || "light";
      const newTheme = currentTheme === "dark" ? "light" : "dark";
      applyTheme(newTheme);
      localStorage.setItem("javamail-theme", newTheme);
    });
  }
}

function applyTheme(theme) {
  if (theme === "dark") {
    document.documentElement.setAttribute("data-theme", "dark");
  } else {
    document.documentElement.removeAttribute("data-theme");
  }

  const iconEl = document.getElementById("theme-toggle-icon");
  const textEl = document.getElementById("theme-toggle-text");
  if (iconEl && textEl) {
    if (theme === "dark") {
      iconEl.textContent = "☀️";
      textEl.textContent = "Light Mode";
    } else {
      iconEl.textContent = "🌙";
      textEl.textContent = "Dark Mode";
    }
  }
}

document.addEventListener("DOMContentLoaded", () => {
  initTheme();
  document.querySelectorAll("[data-email-avatar]").forEach((el) => {
    const email = el.dataset.emailAvatar;
    const name = el.dataset.name || email;
    el.style.background = colorForEmail(email);
    el.textContent = getInitials(name);
  });
});

// ── contextPath injected by JSP pages ────────────────────
// The JSP sets: const contextPath = "${pageContext.request.contextPath}";
