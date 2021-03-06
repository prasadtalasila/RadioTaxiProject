package in.ac.bits_pilani.radiotaxi.db;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import in.ac.bits_pilani.radiotaxi.roles.User;
import in.ac.bits_pilani.radiotaxi.roles.admin.Admin;
import in.ac.bits_pilani.radiotaxi.roles.driver.Driver;
import in.ac.bits_pilani.radiotaxi.roles.rider.Rider;

/**
 * all queries to database routed via this class
 */

public class AccessDB {

	static Cluster cluster;
	static Session session;

	private static Logger logger = Logger.getLogger("AccessDB.class");

	public static void initialise() throws Exception {
		cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		try {
			session = cluster.connect();
			useKeyspace();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error initialising db", e);
			throw e;
		}
	}
	
	public static void setSession(Session s) {
		session = s;
	}

	public static void useKeyspace() throws Exception {
		String query = "use radiotaxi";
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error using keyspace", e);
			throw e;
		}
	}

	public static void shutDown() {
		session.close();
		cluster.close();
	}

	public static String hashPassword(String password) {
		return DigestUtils.sha1Hex(password);
	}

	public static void registerRider(Rider rider, String password)
			throws Exception {
		String query = "INSERT INTO rider_info (username,first_name,last_name,mobile_no,balance,password) VALUES ('"
				+ rider.getUsername()
				+ "','"
				+ rider.getFirstname()
				+ "','"
				+ rider.getLastname()
				+ "','"
				+ rider.getMobileNo()
				+ "',0,'"
				+ hashPassword(password) + "');";
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error registerRider", e);
			throw e;
		}
	}
	
	public static void updateRiderDetails(String rider, String password)
			throws Exception {
		String query = "UPDATE rider_info SET password = '" + hashPassword(password) + "' WHERE username = '" + rider + "'"; 
		
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error registerDriver", e);
			throw e;
		}
	}

	public static List<Row> getUnregisteredDrivers() throws Exception {
		String query = "SELECT * FROM unregistered_drivers;";
		List<Row> results = null;
		try {
			ResultSet rs = session.execute(query);
			results = rs.all();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error getUnregisteredDrivers", e);
			throw e;
		}
		return results;
	}
	
	public static boolean approveDriver(String username) throws Exception {

		String query = "SELECT * FROM unregistered_drivers WHERE username = '"
				+ username + "';";

		List<Row> results = null;
		try {
		    ResultSet rs = session.execute(query);
			results = rs.all();
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error approveDriver", e);
			throw e;
		}
		if (results == null) {
			return false;
		}
		Row r = results.get(0);
		String firstName = r.getString("first_name");
		String lastName = r.getString("last_name");
		String carNo = r.getString("car_no");
		String licenseNo = r.getString("license_no");
		String mobileNo = r.getString("mobile_no");
		String password = r.getString("password");

		query = "INSERT INTO driver_info (username,first_name,last_name,mobile_no,password,car_no,license_no,balance) VALUES ('"
				+ username
				+ "','"
				+ firstName
				+ "','"
				+ lastName
				+ "','"
				+ mobileNo
				+ "','"
				+ password
				+ "','"
				+ carNo
				+ "','"
				+ licenseNo + "',0);";
		
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error approveDriver", e);
			throw e;
		}

		query = "DELETE FROM unregistered_drivers WHERE username = '"
				+ username + "';";
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error approveDriver", e);
			throw e;
		}

		return true;

	}

	public static void registerDriver(Driver driver, String password)
			throws Exception {
		String query = " INSERT INTO unregistered_drivers (username,first_name,last_name,mobile_no,car_no,license_no,balance,password) VALUES ('"
				+ driver.getUsername()
				+ "','"
				+ driver.getFirstname()
				+ "','"
				+ driver.getLastname()
				+ "','"
				+ driver.getMobileNo()
				+ "','"
				+ driver.getCarNo()
				+ "','"
				+ driver.getLicenseNo()
				+ "',0,'"
				+ hashPassword(password) + "');";
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error registerDriver", e);
			throw e;
		}
	}
	
	public static void updateDriverDetails(String driver, String password)
			throws Exception {
		String query = "UPDATE driver_info SET password = '" + 
			hashPassword(password) + "' WHERE username = '" + driver + "'"; 
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error registerDriver", e);
			throw e;
		}
	}

	public static void registerAdmin() throws Exception {
		String query = " INSERT INTO admin_list (username,first_name,last_name,password) VALUES ('"
				+ "admin"
				+ "','"
				+ "Admin"
				+ "','"
				+ "Admin"
				+ "','"
				+ hashPassword("admin") + "');";
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error registerAdmin", e);
			throw e;
		}
	}

	/*
	 * command to create bookings table - CREATE TABLE bookings (rider
	 * text,driver text,time timestamp,origin text,destination text,PRIMARY
	 * KEY(rider, time));
	 */
	public static void bookARide(String rider, String time, String origin,
			String destination, int fare, float[] originCoord, float[] destCoord)
			        throws Exception {
		
	    int randBookingId = (((17*37 + rider.hashCode())* 37 + time.hashCode())
	            * 37 + origin.hashCode())* 37 +
	            destination.hashCode();
		String query = "INSERT INTO unmatched_bookings (booking_id,"
		        + "rider,time,origin,destination,fare,orig_coord,"
		        + "dest_coord) VALUES ("
				+ randBookingId	+ ",'" + rider + "','" + time + "','"
				+ origin + "','" + destination + "'," + fare	+ "," 
				+ "[" + originCoord[0] + "," + originCoord[1] + "]"	+ ","
				+ "[" + destCoord[0] + "," + destCoord[1] + "]"
				+ ");";
		// System.out.println("bookARide query\n"+query);
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error bookARide", e);
			throw e;
		}
	}

	public static List<Row> getUnmatchedRides() throws Exception {
		String query = "SELECT * FROM unmatched_bookings;";
		List<Row> results = null;
		try {
			ResultSet rs = session.execute(query);
			results = rs.all();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error getUnmatchedRides", e);
			throw e;
		}
		return results;
	}

	public static void confirmMatch(int bookingId, String driver)
			throws Exception {
		List<Row> results = null;
		String query = "SELECT * FROM unmatched_bookings WHERE booking_id = "
				+ bookingId + ";";
		try {
			ResultSet rs = session.execute(query);
			results = rs.all();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error confirmMatch", e);
			throw e;
		}
		if (results == null) {
			return; 
		}
		Row r = results.get(0);
		String rider = r.getString("rider");
		Date time = r.getTimestamp("time");
		LocalDateTime localTime = LocalDateTime.ofInstant(time.toInstant(),
				ZoneId.of("Asia/Kolkata"));
		String time_str = localTime.toString().replace('T', ' ').split("\\.")[0];

		String origin = r.getString("origin");
		String destination = r.getString("destination");
		int fare = r.getInt("fare");

		query ="select * from rider_info where username='"+rider+"';";
		
		try {
			ResultSet rs = session.execute(query);
			results = rs.all();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error confirmMatch", e);
			throw e;
		}
		Row rn = results.get(0);
		int fr=rn.getInt("balance");
	//	System.out.println(fr);
		
		fr=fr-fare;
		query ="update rider_info set balance="+fr+" where username='"+rider+"';";
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error confirmMatch", e);
			throw e;
		}

		query ="select * from driver_info where username='"+driver+"';";
	//	System.out.println("\n\n\n\n"+query+"\n\n\n\n");
		try {
			ResultSet rs = session.execute(query);
			results = rs.all();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error confirmMatch", e);
			throw e;
		}
		Row rd = results.get(0);
		int fd=rd.getInt("balance");
	//	System.out.println(fr);
		
		fd+=fare;
		query ="update driver_info set balance="+fd+" where username='"+driver+"';";
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error confirmMatch", e);
			throw e;
		}

		
		query = "INSERT INTO matched_bookings (booking_id,rider,driver,time,origin,destination,fare) VALUES ("
				+ bookingId
				+ ",'"
				+ rider
				+ "','"
				+ driver
				+ "','"
				+ time_str
				+ "','" + origin + "','" + destination + "'," + fare + ");";
		//System.out.println("\n\n\n\n"+query+"\n\n\n\n");
		
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error confirmMatch", e);
			throw e;
		}

		query = "DELETE FROM unmatched_bookings WHERE booking_id = "
				+ bookingId + ";";
		try {
			session.execute(query);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error confirmMatch", e);
			throw e;
		}

		return;
	}

	public static int bookedCabBalance(){
		List<Row> results = null;
		String query = "SELECT SUM(FARE) from matched_bookings;";
		try {
			ResultSet rs = session.execute(query);
			results = rs.all();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error confirmMatch", e);
			throw e;
		}
		if (results == null) {
			return 0;
		}
		Row r = results.get(0);
		return r.getInt("system.sum(fare)");
		//int balance= Integer.parseInt(r.getString("system.sum(fare)"));
		
	}
	
	public static User login(String username, String password, String type)
			throws Exception {
	    String table = null;
        switch (type) {
        case "rider":
            table = "rider_info";
            break;
        case "driver":
            table = "driver_info";
            break;
        case "admin":
            table = "admin_list";
        }
        String query = "SELECT * FROM " + table + " WHERE username = '"
                + username + "' AND password = '" + hashPassword(password)
                + "' ALLOW FILTERING;";
        
		List<Row> results = null;
		try {
			ResultSet rs = session.execute(query); 
		    results = rs.all();
			//System.out.println(rs);
			//System.out.println(results);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error login", e);
			throw e;
		}
		if (results == null) {
			return null;
		}
		Row r = results.get(0);
		String firstname, lastname, mobile;
		int balance;

		switch(type) {
		case "rider":
			firstname = r.getString("first_name");
			lastname = r.getString("last_name");
			mobile = r.getString("mobile_no");
			balance = r.getInt("balance");
			return new Rider(username, firstname, lastname, mobile, balance);

		case "admin":
			firstname = r.getString("first_name");
			lastname = r.getString("last_name");
			// TODO: add mobile no field to admin table
			mobile = "9999999999";
			balance = 0;
			return new Admin(username, firstname, lastname, mobile, balance);

		case "driver":
			firstname = r.getString("first_name");
			lastname = r.getString("last_name");
			mobile = r.getString("mobile_no");
			String license = r.getString("license_no");
			String car = r.getString("car_no");
			balance = r.getInt("balance");
			return new Driver(username, firstname, lastname, mobile, license, car, balance);

		default:
			return null;
		}
	}
	
	public static List<Row> getRiderTravelHistory(Rider rider) throws Exception{
	    
	    String query = "SELECT * FROM matched_bookings WHERE rider ='" + rider.getUsername() + "' ALLOW FILTERING;";
	    List<Row> results = null;
	    try {
            ResultSet rs = session.execute(query);
            results = rs.all();
        } catch(Exception e) {
            logger.log(Level.SEVERE, "error getTravelHistory", e);
            throw e;
        }
	    return results;
	}

    public static List<Row> getDriverTravelHistory(Driver driver) throws Exception{
        String query = "SELECT * FROM matched_bookings WHERE driver ='" + driver.getUsername() +
                "' ALLOW FILTERING;";
        List<Row> results = null;
        try {
            ResultSet rs = session.execute(query);
            results = rs.all();
        } catch(Exception e) {
            logger.log(Level.SEVERE, "error getTravelHistory", e);
            throw e;
        }
        return results;
        
    }
}
