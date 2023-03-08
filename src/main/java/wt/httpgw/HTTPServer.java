package wt.httpgw;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class HTTPServer extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req,
            HttpServletResponse response) throws ServletException, IOException {
        doExecute(req, response);
    }

    @Override
    public void doPost(HttpServletRequest req,
            HttpServletResponse response) throws ServletException, IOException {
        doExecute(req, response);
    }

    private void doExecute(HttpServletRequest req,
            HttpServletResponse response) throws ServletException, IOException {
        System.out.println("HTTPServer.doExecute(req, response)");
        try {
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}
