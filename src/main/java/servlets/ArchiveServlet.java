package servlets;

import dbService.DBService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class ArchiveServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String from = request.getParameter("from");
        String to = request.getParameter("to");

        System.out.println("Incoming Data: " + from + ", " + to);

        DBService dbService = null;
        try {
            dbService = new DBService();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String result = null;
        try {
            result = dbService.getDataFromRange(from, to);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.setContentType("text/html;charset=utf-8");
        response.getWriter().println(result);
        response.setStatus(HttpServletResponse.SC_OK);

    }
}
