package com.javamail.servlet;

import com.javamail.dao.UserDAO;
import com.javamail.model.User;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * UserServlet - Handles user registration, login, logout, and profile management.
 * Maps all /user/* URLs.
 */
@WebServlet("/user/*")
public class UserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo(); // e.g. "/login", "/register"
        if (path == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            switch (path) {
                case "/login"    -> handleLogin(req, resp);
                case "/register" -> handleRegister(req, resp);
                case "/logout"   -> handleLogout(req, resp);
                case "/profile"  -> handleUpdateProfile(req, resp);
                case "/password" -> handleChangePassword(req, resp);
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        try {
            if ("/logout".equals(path)) {
                handleLogout(req, resp);
            } else if ("/profile".equals(path)) {
                showProfilePage(req, resp);
            } else {
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ── LOGIN ────────────────────────────────────────────
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        User user = userDAO.login(email, password);
        if (user != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("userEmail", user.getEmail());
            session.setMaxInactiveInterval(30 * 60); // 30 min
            resp.sendRedirect(req.getContextPath() + "/mail/inbox");
        } else {
            req.getSession().setAttribute("error", "Invalid email or password.");
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }

    // ── REGISTER ─────────────────────────────────────────
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        String firstname = req.getParameter("firstname");
        String lastname  = req.getParameter("lastname");
        String email     = req.getParameter("email");
        String password  = req.getParameter("password");
        String dob       = req.getParameter("dob");
        String contact   = req.getParameter("contact");

        if (userDAO.emailExists(email)) {
            req.getSession().setAttribute("error", "Email already registered.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
            return;
        }

        User user = new User(firstname + " " + lastname, email, password, dob, contact);
        if (userDAO.register(user)) {
            req.getSession().setAttribute("success", "Account created! Please login.");
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        } else {
            req.getSession().setAttribute("error", "Registration failed. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
        }
    }

    // ── LOGOUT ───────────────────────────────────────────
    private void handleLogout(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    // ── UPDATE PROFILE ───────────────────────────────────
    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException, ServletException {
        HttpSession session = req.getSession(false);
        if (session == null) { resp.sendRedirect(req.getContextPath() + "/login.jsp"); return; }

        User user = (User) session.getAttribute("user");
        user.setUsername(req.getParameter("username"));
        user.setDob(req.getParameter("dob"));
        user.setContact(req.getParameter("contact"));

        if (userDAO.updateProfile(user)) {
            session.setAttribute("user", user);
            session.setAttribute("success", "Profile updated successfully.");
        } else {
            session.setAttribute("error", "Profile update failed.");
        }
        resp.sendRedirect(req.getContextPath() + "/user/profile");
    }

    // ── CHANGE PASSWORD ──────────────────────────────────
    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) { resp.sendRedirect(req.getContextPath() + "/login.jsp"); return; }

        User user       = (User) session.getAttribute("user");
        String current  = req.getParameter("currentPassword");
        String newPass  = req.getParameter("newPassword");
        String confirm  = req.getParameter("confirmPassword");

        // Verify current password
        User verified = userDAO.login(user.getEmail(), current);
        if (verified == null) {
            session.setAttribute("error", "Current password is incorrect.");
        } else if (!newPass.equals(confirm)) {
            session.setAttribute("error", "New passwords do not match.");
        } else if (userDAO.changePassword(user.getEmail(), newPass)) {
            session.setAttribute("success", "Password changed successfully.");
        } else {
            session.setAttribute("error", "Password change failed.");
        }
        resp.sendRedirect(req.getContextPath() + "/user/profile");
    }

    // ── SHOW PROFILE PAGE ────────────────────────────────
    private void showProfilePage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        HttpSession session = req.getSession(false);
        if (session == null) { resp.sendRedirect(req.getContextPath() + "/login.jsp"); return; }
        req.getRequestDispatcher("/WEB-INF/profile.jsp").forward(req, resp);
    }
}
