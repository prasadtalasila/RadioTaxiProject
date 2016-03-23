

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
 * Servlet implementation class Book
 */
@WebServlet("/Book")
public class BookingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BookingServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		HttpSession session = request.getSession(false);
		if(session != null) {
			PrintWriter out = response.getWriter();
			String rider = (String) session.getAttribute("user");
			String origin = request.getParameter("origin");
			String dest = request.getParameter("dest");
			
			Booking b = new Booking();
			String query = b.bookTrip(rider, origin, dest); // add driver later when confirmed
			
			RequestDispatcher rd = getServletContext().getRequestDispatcher("/riderBookings.jsp");
			request.setAttribute("success", true);
			rd.forward(request, response);
		}
		else {
			response.sendRedirect("index.html");
		}
	}
}
