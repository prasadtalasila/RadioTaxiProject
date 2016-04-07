
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
 * Servlet implementation class RegisterRiderServlet
 */
@WebServlet("/registerrider")
public class RegisterRiderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public RegisterRiderServlet() {
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

			Register register = new Register();
			register.registerRider(new Rider(username,firstName, lastName,mobile),password);

			RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
			out.println("<p align=\"center\"><font color=green>Successfully registered!</font></p>");
			rd.include(request, response);
		} else {
			RequestDispatcher rd = getServletContext().getRequestDispatcher("/profile");
			rd.forward(request, response);
		}
	}
}
