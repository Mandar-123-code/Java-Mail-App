package com.javamail.servlet;

import com.javamail.dao.MailDAO;
import com.javamail.dao.UserDAO;
import com.javamail.model.Mail;
import com.javamail.model.User;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * MailServlet - Central servlet handling all mail operations.
 *  GET  /mail/inbox       -> show inbox
 *  GET  /mail/sent        -> show sent
 *  GET  /mail/draft       -> show drafts
 *  GET  /mail/trash       -> show trash
 *  GET  /mail/spam        -> show spam
 *  GET  /mail/starred     -> show starred
 *  GET  /mail/important   -> show important
 *  GET  /mail/view?id=X   -> view single mail
 *  GET  /mail/compose     -> compose form
 *  GET  /mail/reply?id=X  -> reply form
 *  GET  /mail/forward?id=X-> forward form
 *  GET  /mail/search?q=X  -> search results
 *  POST /mail/send        -> send new mail
 *  POST /mail/draft/save  -> save draft
 *  POST /mail/delete      -> move to trash
 *  POST /mail/star        -> toggle star
 *  POST /mail/important   -> toggle important
 *  POST /mail/spam        -> mark spam
 *  POST /mail/restore     -> restore from trash
 *  POST /mail/empty-trash -> empty trash
 */
@WebServlet("/mail/*")
public class MailServlet extends HttpServlet {

    private final MailDAO mailDAO = new MailDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String path = req.getPathInfo();
        if (path == null) {
            resp.sendRedirect(req.getContextPath() + "/mail/inbox");
            return;
        }
        User user   = (User) session.getAttribute("user");

        try {
            switch (path) {
                case "/inbox"     -> showFolder(req, resp, user, "inbox",     mailDAO.getInbox(user.getEmail()));
                case "/sent"      -> showFolder(req, resp, user, "sent",      mailDAO.getSentMails(user.getEmail()));
                case "/draft"     -> showFolder(req, resp, user, "draft",     mailDAO.getDrafts(user.getEmail()));
                case "/trash"     -> showFolder(req, resp, user, "trash",     mailDAO.getTrash(user.getEmail()));
                case "/spam"      -> showFolder(req, resp, user, "spam",      mailDAO.getSpam(user.getEmail()));
                case "/starred"   -> showFolder(req, resp, user, "starred",   mailDAO.getStarred(user.getEmail()));
                case "/important" -> showFolder(req, resp, user, "important", mailDAO.getImportant(user.getEmail()));
                case "/view"      -> showMailView(req, resp, user);
                case "/compose"   -> showCompose(req, resp, "compose", null);
                case "/reply"     -> showReplyForward(req, resp, "reply");
                case "/forward"   -> showReplyForward(req, resp, "forward");
                case "/search"    -> showSearch(req, resp, user);
                default           -> resp.sendRedirect(req.getContextPath() + "/mail/inbox");
            }
        } catch (SQLException e) {
            throw new ServletException("DB error: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        User user   = (User) session.getAttribute("user");
        String path = req.getPathInfo();
        if (path == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            switch (path) {
                case "/send"        -> handleSend(req, resp, user);
                case "/draft/save"  -> handleSaveDraft(req, resp, user);
                case "/delete"      -> handleFlag(req, resp, user, "delete");
                case "/star"        -> handleFlag(req, resp, user, "star");
                case "/important"   -> handleFlag(req, resp, user, "important");
                case "/spam"        -> handleFlag(req, resp, user, "spam");
                case "/restore"     -> handleFlag(req, resp, user, "restore");
                case "/empty-trash" -> { mailDAO.emptyTrash(user.getEmail());
                                         resp.sendRedirect(req.getContextPath() + "/mail/trash"); }
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ── Show folder list ─────────────────────────────────
    private void showFolder(HttpServletRequest req, HttpServletResponse resp,
                             User user, String folder, List<Mail> mails)
            throws ServletException, IOException, SQLException {
        req.setAttribute("mails",    mails);
        req.setAttribute("folder",   folder);
        req.setAttribute("unread",   mailDAO.countUnread(user.getEmail()));
        req.getRequestDispatcher("/WEB-INF/mailbox.jsp").forward(req, resp);
    }

    // ── View single mail ─────────────────────────────────
    private void showMailView(HttpServletRequest req, HttpServletResponse resp, User user)
            throws SQLException, ServletException, IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        Mail mail = mailDAO.getMailById(id);
        if (mail == null) { resp.sendError(404); return; }

        // Mark as read
        mailDAO.markRead(id);

        req.setAttribute("mail",   mail);
        req.setAttribute("unread", mailDAO.countUnread(user.getEmail()));
        req.getRequestDispatcher("/WEB-INF/viewmail.jsp").forward(req, resp);
    }

    // ── Compose form ─────────────────────────────────────
    private void showCompose(HttpServletRequest req, HttpServletResponse resp,
                              String mode, Mail prefill)
            throws ServletException, IOException {
        req.setAttribute("composeMode", mode);
        req.setAttribute("prefill", prefill);
        req.getRequestDispatcher("/WEB-INF/compose.jsp").forward(req, resp);
    }

    // ── Reply / Forward form ─────────────────────────────
    private void showReplyForward(HttpServletRequest req, HttpServletResponse resp, String mode)
            throws SQLException, ServletException, IOException {
        int id   = Integer.parseInt(req.getParameter("id"));
        Mail original = mailDAO.getMailById(id);
        req.setAttribute("original",    original);
        req.setAttribute("composeMode", mode);
        req.getRequestDispatcher("/WEB-INF/compose.jsp").forward(req, resp);
    }

    // ── Search ───────────────────────────────────────────
    private void showSearch(HttpServletRequest req, HttpServletResponse resp, User user)
            throws SQLException, ServletException, IOException {
        String query       = req.getParameter("q");
        List<Mail> results = mailDAO.searchMails(user.getEmail(), query);
        req.setAttribute("mails",    results);
        req.setAttribute("folder",   "search");
        req.setAttribute("query",    query);
        req.setAttribute("unread",   mailDAO.countUnread(user.getEmail()));
        req.getRequestDispatcher("/WEB-INF/mailbox.jsp").forward(req, resp);
    }

    // ── Send mail ────────────────────────────────────────
    private void handleSend(HttpServletRequest req, HttpServletResponse resp, User user)
            throws SQLException, IOException {
        String to      = req.getParameter("to");
        String cc      = req.getParameter("cc");
        String bcc     = req.getParameter("bcc");
        String subject = req.getParameter("subject");
        String body    = req.getParameter("body");

        // Validate recipient exists
        if (!userDAO.emailExists(to)) {
            req.getSession().setAttribute("error", "Recipient email not found: " + to);
            resp.sendRedirect(req.getContextPath() + "/mail/compose");
            return;
        }

        Mail mail = new Mail(user.getEmail(), to, subject, body, Mail.Status.SENT);
        mail.setCcEmail(cc);
        mail.setBccEmail(bcc);

        if (mailDAO.sendMail(mail)) {
            req.getSession().setAttribute("success", "Mail sent successfully!");
        } else {
            req.getSession().setAttribute("error", "Failed to send mail.");
        }
        resp.sendRedirect(req.getContextPath() + "/mail/sent");
    }

    // ── Save draft ───────────────────────────────────────
    private void handleSaveDraft(HttpServletRequest req, HttpServletResponse resp, User user)
            throws SQLException, IOException {
        Mail draft = new Mail();
        draft.setFromEmail(user.getEmail());
        draft.setToEmail(req.getParameter("to"));
        draft.setCcEmail(req.getParameter("cc"));
        draft.setSubject(req.getParameter("subject"));
        draft.setBody(req.getParameter("body"));

        mailDAO.saveDraft(draft);
        req.getSession().setAttribute("success", "Draft saved.");
        resp.sendRedirect(req.getContextPath() + "/mail/draft");
    }

    // ── Generic flag handler ─────────────────────────────
    private void handleFlag(HttpServletRequest req, HttpServletResponse resp,
                             User user, String action)
            throws SQLException, IOException {
        int mailId      = Integer.parseInt(req.getParameter("mailId"));
        String redirect = req.getParameter("redirect");
        if (redirect == null || redirect.isEmpty()) redirect = "inbox";

        switch (action) {
            case "delete"    -> mailDAO.moveToTrash(mailId);
            case "star"      -> mailDAO.toggleStar(mailId);
            case "important" -> mailDAO.toggleImportant(mailId);
            case "spam"      -> mailDAO.markAsSpam(mailId);
            case "restore"   -> mailDAO.restoreFromTrash(mailId);
        }

        resp.sendRedirect(req.getContextPath() + "/mail/" + redirect);
    }
}
