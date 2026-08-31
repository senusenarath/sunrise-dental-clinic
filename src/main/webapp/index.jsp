<%--
    index.jsp
    Root application redirector
    Redirects to HomeServlet which handles routing logic
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%
    response.sendRedirect(request.getContextPath() + "/home");
%>