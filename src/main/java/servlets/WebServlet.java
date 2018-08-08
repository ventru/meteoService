package servlets;

import dbService.DBService;
import templater.PageGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class WebServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DBService dbService = null;
        try {
            dbService = new DBService();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        dbService.printConnectInfo();
        Map<String, String> pageVariables = null;
        try {
            pageVariables = dbService.getDataToday();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String createStatus = dbService.statusForReport[0] + ";" + dbService.statusForReport[1] + ";" + dbService.statusForReport[2];
        pageVariables.put("status", createStatus);

        response.getWriter().println(PageGenerator.instance().getPage("page.html", pageVariables));

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}