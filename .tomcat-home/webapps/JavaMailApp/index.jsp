<%@ page contentType="text/html;charset=UTF-8" %>
<%
    // Redirect to inbox if logged in, otherwise to login
    if (session != null && session.getAttribute("user") != null) {
        response.sendRedirect(request.getContextPath() + "/mail/inbox");
    } else {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
%>
