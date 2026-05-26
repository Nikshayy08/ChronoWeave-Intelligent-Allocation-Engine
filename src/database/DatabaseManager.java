package database;

import models.Student;
import models.Resource;
import models.Request;

import java.sql.*;
import java.util.*;

/*
 * DatabaseManager.java
 *
 * Handles all SQLite database operations for the Smart Campus Resource Exchange System.
 *
 * Tables:
 *  students  → stores all registered students
 *  resources → stores all resource listings
 *  requests  → stores all buy/rent requests
 *
 * Usage:
 *  DatabaseManager db = new DatabaseManager();
 *  db.saveStudent(student);
 *  List<Student> students = db.loadAllStudents();
 *
 * The database file (campus_exchange.db) is created automatically
 * in the project root folder on first run.
 */

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:campus_exchange.db";
    private Connection conn;

    // ── Constructor ───────────────────────────────────────────────────────────

    public DatabaseManager() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(true);
            createTables();
            System.out.println("Database connected: campus_exchange.db");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found. Add sqlite-jdbc.jar to lib/");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    // ── Table Creation ────────────────────────────────────────────────────────

    private void createTables() throws SQLException {

        String createStudents =
            "CREATE TABLE IF NOT EXISTS students (" +
            "  student_id      TEXT PRIMARY KEY," +
            "  name            TEXT NOT NULL," +
            "  contact_number  TEXT," +
            "  whatsapp_number TEXT," +
            "  total_listings  INTEGER DEFAULT 0," +
            "  total_requests  INTEGER DEFAULT 0" +
            ");";

        String createResources =
            "CREATE TABLE IF NOT EXISTS resources (" +
            "  resource_id   TEXT PRIMARY KEY," +
            "  resource_name TEXT NOT NULL," +
            "  resource_type TEXT NOT NULL," +
            "  listing_type  TEXT NOT NULL," +
            "  owned_by      TEXT NOT NULL," +
            "  available     INTEGER DEFAULT 1," +
            "  asking_price  REAL DEFAULT 0.0," +
            "  download_link TEXT" +
            ");";

        String createRequests =
            "CREATE TABLE IF NOT EXISTS requests (" +
            "  id             INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  student_name   TEXT NOT NULL," +
            "  resource_name  TEXT NOT NULL," +
            "  request_type   TEXT NOT NULL," +
            "  offering_price REAL DEFAULT 0.0," +
            "  start_time     INTEGER DEFAULT 0," +
            "  end_time       INTEGER DEFAULT 0," +
            "  status         TEXT DEFAULT 'PENDING'," +
            "  created_at     INTEGER" +
            ");";

        Statement stmt = conn.createStatement();
        stmt.execute(createStudents);
        stmt.execute(createResources);
        stmt.execute(createRequests);
        stmt.close();
    }

    // ── Student Operations ────────────────────────────────────────────────────

    /*
     * saveStudent(student)
     * Inserts or replaces student record.
     */
    public void saveStudent(Student student) {
        if (conn == null) return;
        String sql = "INSERT OR REPLACE INTO students " +
                     "(student_id, name, contact_number, whatsapp_number, total_listings, total_requests) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getStudentId());
            ps.setString(2, student.getName());
            ps.setString(3, student.getContactNumber());
            ps.setString(4, student.getWhatsappNumber());
            ps.setInt(5, student.getTotalListings());
            ps.setInt(6, student.getTotalRequests());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving student: " + e.getMessage());
        }
    }

    /*
     * loadAllStudents()
     * Loads all students from DB and returns as list.
     */
    public List<Student> loadAllStudents() {
        List<Student> students = new ArrayList<>();
        if (conn == null) return students;

        String sql = "SELECT * FROM students";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student s = new Student(
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("contact_number"),
                    rs.getString("whatsapp_number")
                );
                // Restore counters
                for (int i = 0; i < rs.getInt("total_listings"); i++) s.incrementListings();
                for (int i = 0; i < rs.getInt("total_requests"); i++) s.incrementRequests();
                students.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Error loading students: " + e.getMessage());
        }
        return students;
    }

    // ── Resource Operations ───────────────────────────────────────────────────

    /*
     * saveResource(resource)
     * Inserts or replaces resource record.
     */
    public void saveResource(Resource resource) {
        if (conn == null) return;
        String sql = "INSERT OR REPLACE INTO resources " +
                     "(resource_id, resource_name, resource_type, listing_type, owned_by, available, asking_price, download_link) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resource.getResourceId());
            ps.setString(2, resource.getResourceName());
            ps.setString(3, resource.getResourceType().name());
            ps.setString(4, resource.getListingType().name());
            ps.setString(5, resource.getOwnedBy());
            ps.setInt(6, resource.isAvailable() ? 1 : 0);
            ps.setDouble(7, resource.getAskingPrice());
            ps.setString(8, resource.getDownloadLink());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving resource: " + e.getMessage());
        }
    }

    /*
     * loadAllResources()
     * Loads all resources from DB.
     */
    public List<Resource> loadAllResources() {
        List<Resource> resources = new ArrayList<>();
        if (conn == null) return resources;

        String sql = "SELECT * FROM resources";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Resource.ResourceType rType = Resource.ResourceType.valueOf(rs.getString("resource_type"));
                Resource.ListingType  lType = Resource.ListingType.valueOf(rs.getString("listing_type"));

                Resource r = new Resource(
                    rs.getString("resource_id"),
                    rs.getString("resource_name"),
                    rType, lType,
                    rs.getString("owned_by")
                );
                r.setAvailable(rs.getInt("available") == 1);
                r.setAskingPrice(rs.getDouble("asking_price"));
                r.setDownloadLink(rs.getString("download_link"));
                resources.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error loading resources: " + e.getMessage());
        }
        return resources;
    }

    // ── Request Operations ────────────────────────────────────────────────────

    /*
     * saveRequest(request)
     * Inserts a new request record.
     */
    public void saveRequest(Request request) {
        if (conn == null) return;
        String sql = "INSERT INTO requests " +
                     "(student_name, resource_name, request_type, offering_price, start_time, end_time, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, request.getStudentName());
            ps.setString(2, request.getResourceName());
            ps.setString(3, request.getRequestType().name());
            ps.setDouble(4, request.getOfferingPrice());
            ps.setInt(5, request.getStartTime());
            ps.setInt(6, request.getEndTime());
            ps.setString(7, request.getStatus().name());
            ps.setLong(8, request.getCreatedAt());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving request: " + e.getMessage());
        }
    }

    /*
     * loadAllRequests()
     * Loads all requests from DB.
     */
    public List<Request> loadAllRequests() {
        List<Request> requests = new ArrayList<>();
        if (conn == null) return requests;

        String sql = "SELECT * FROM requests ORDER BY created_at ASC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Request.RequestType rType = Request.RequestType.valueOf(rs.getString("request_type"));
                Request r;

                if (rType == Request.RequestType.NEED_TO_BUY) {
                    r = new Request(
                        rs.getString("student_name"),
                        rs.getString("resource_name"),
                        rs.getDouble("offering_price")
                    );
                } else {
                    r = new Request(
                        rs.getString("student_name"),
                        rs.getString("resource_name"),
                        rType,
                        rs.getDouble("offering_price"),
                        rs.getInt("start_time"),
                        rs.getInt("end_time")
                    );
                }

                r.setStatus(Request.Status.valueOf(rs.getString("status")));
                requests.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error loading requests: " + e.getMessage());
        }
        return requests;
    }

    /*
     * updateRequestStatus(studentName, resourceName, status)
     * Updates status of a specific request.
     */
    public void updateRequestStatus(String studentName, String resourceName, Request.Status status) {
        if (conn == null) return;
        String sql = "UPDATE requests SET status = ? WHERE student_name = ? AND resource_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, studentName);
            ps.setString(3, resourceName);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating request status: " + e.getMessage());
        }
    }

    /*
     * updateResourceAvailability(resourceId, available)
     * Updates availability of a resource after it's sold/rented.
     */
    public void updateResourceAvailability(String resourceId, boolean available) {
        if (conn == null) return;
        String sql = "UPDATE resources SET available = ? WHERE resource_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, available ? 1 : 0);
            ps.setString(2, resourceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating resource: " + e.getMessage());
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /*
     * clearAllData()
     * Wipes all tables — useful for demo reset.
     */
    public void clearAllData() {
        if (conn == null) return;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM requests");
            stmt.execute("DELETE FROM resources");
            stmt.execute("DELETE FROM students");
            System.out.println("All data cleared.");
        } catch (SQLException e) {
            System.err.println("Error clearing data: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return conn != null;
    }

    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing DB: " + e.getMessage());
        }
    }
}