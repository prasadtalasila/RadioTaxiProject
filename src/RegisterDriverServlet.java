
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class RegisterDriverServlet
 */
@WebServlet("/registerdriver")
public class RegisterDriverServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public RegisterDriverServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)  
            throws ServletException, IOException {

		HttpSession session = request.getSession(false);

		if(session == null) {
			response.setContentType("text/html");
			PrintWriter out=response.getWriter();

			String username=request.getParameter("username");
			String firstName=request.getParameter("firstname");
			String lastName=request.getParameter("lastname");
			String mobile=request.getParameter("mobile");
			String password=request.getParameter("password");
			String licence=request.getParameter("licence");
			String carNo=request.getParameter("car_no");

			Register register = new Register();
			register.registerDriver(new Driver(username, firstName, lastName, mobile, licence, carNo),password);

			out.println("<p align=\"center\"><font color=green>Successfully registered! Pending verification</font></p>");
			request.getRequestDispatcher("index.html").include(request, response);
		} else {
			request.getRequestDispatcher("profile").forward(request, response);
		}
    }
}

