import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.InputMismatchException;

public class RentalAppConnected {
    // --- REPORT REQUIREMENT (SQL Injection Explanation) ---
    /*
     * This code strictly adheres to best practices to prevent SQL Injection
     * attacks.
     * Approach: Use of Prepared Statements (java.sql.PreparedStatement) for ALL
     * database operations.
     * * Example Demonstration (from editMember method):
     * String sql = "UPDATE members SET fname = ? WHERE userID = ?;"; // SAFE
     * try (PreparedStatement ps = conn.prepareStatement(sql)) {
     * ps.setString(1, fname); // Input is treated only as a value, never executable
     * code.
     * ps.setString(2, userID);
     * ps.executeUpdate();
     * }
     * This ensures that malicious input is safely escaped and treated as literal
     * data.
     */
    private static Scanner input = new Scanner(System.in);

    // NOTE: This assumes a working static method Database.getConnection() exists
    // to establish a JDBC connection to the SQLite database.

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Manage Members");
            System.out.println("2. Manage Equipment");
            System.out.println("3. Manage Drones");
            System.out.println("4. Rentals & Deliveries");
            System.out.println("5. Reports");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");
            int choice = getIntInput();

            switch (choice) {
                case 1:
                    membersMenu();
                    break;
                case 2:
                    equipmentMenu();
                    break;
                case 3:
                    dronesMenu();
                    break;
                case 4:
                    rentalsMenu();
                    break;
                case 5:
                    reportsMenu();
                    break;
                case 6:
                    running = false;
                    System.out.println("Exiting. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }

        input.close();
    }

    // ==========================================================
    // --- CORE HELPER METHODS (Argument-less versions for basic input) ---
    // ==========================================================

    /**
     * Forces input until a valid integer is provided (No prompt argument).
     */
    private static int getIntInput() {
        while (true) {
            try {
                String line = input.nextLine();
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    /**
     * Forces input until a non-empty, trimmed string is provided.
     */
    private static String getStringInput(String prompt) {
        String inputStr = "";
        while (inputStr.trim().isEmpty()) {
            System.out.print(prompt);
            inputStr = input.nextLine().trim();
            if (inputStr.isEmpty()) {
                System.out.println("Input cannot be empty. Please enter a value.");
            }
        }
        return inputStr;
    }

    /**
     * Forces input until a valid double is provided.
     */
    private static double getDoubleInput() {
        while (true) {
            try {
                String line = input.nextLine();
                return Double.parseDouble(line.trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid numeric value: ");
            }
        }
    }

    /**
     * Forces input until a valid integer year is provided.
     */
    private static int getYearInput() {
        while (true) {
            try {
                String line = input.nextLine();
                int year = Integer.parseInt(line.trim());
                if (year > 1900 && year <= LocalDate.now().getYear() + 5) { // Simple validation
                    return year;
                } else {
                    System.out.print("Please enter a realistic year (e.g., 2024): ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid numeric year: ");
            }
        }
    }

    // --- Generic Existence Check Helper ---
    private static boolean checkExistence(String table, String column, String value) {
        String sql = "SELECT " + column + " FROM " + table + " WHERE " + column + " = ?;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Database check error: " + e.getMessage());
            return false;
        }
    }

    // ==========================================================
    // --- 1. MEMBER MANAGEMENT (Add/Modify/Remove/Retrieve) ---
    // ==========================================================

    private static void membersMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- MEMBERS MENU ---");
            System.out.println("1. Add Member");
            System.out.println("2. Edit Member");
            System.out.println("3. Delete Member");
            System.out.println("4. Search Member (Retrieve)");
            System.out.println("5. View All Members (Retrieve)");
            System.out.println("6. Back");
            System.out.print("Choose: ");
            int c = getIntInput();
            switch (c) {
                case 1:
                    addMember();
                    break;
                case 2:
                    editMember();
                    break;
                case 3:
                    deleteMember();
                    break;
                case 4:
                    searchMember();
                    break;
                case 5:
                    viewAllMembers();
                    break;
                case 6:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid.");
            }
        }
    }

    private static void addMember() {
        System.out.println("--- Adding New Member (All fields mandatory) ---");
        String userID = getStringInput("Enter userID (unique): ");
        String fname = getStringInput("Enter first name: ");
        String lname = getStringInput("Enter last name: ");
        String addr = getStringInput("Enter address: ");
        String phone = getStringInput("Enter phone: ");
        String email = getStringInput("Enter email: ");

        // CORRECTION: Print prompt, then call argument-less method
        System.out.print("Enter warehouse distance (numeric, mandatory): ");
        double dist = getDoubleInput();

        String startDate = LocalDate.now().toString();

        // All 8 columns are non-null and explicitly bound
        String sql = "INSERT INTO members(userID, fname, lname, address, phone, email, startDate, warehouseDistance) " +
                "VALUES(?,?,?,?,?,?,?,?);";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            ps.setString(2, fname);
            ps.setString(3, lname);
            ps.setString(4, addr);
            ps.setString(5, phone);
            ps.setString(6, email);
            ps.setString(7, startDate);
            ps.setDouble(8, dist); // Set mandatory double

            ps.executeUpdate();
            System.out.println("Member added. startDate set to " + startDate);
        } catch (SQLException e) {
            System.err.println("Error adding member: " + e.getMessage());
        }
    }

    private static void editMember() {
        System.out.print("Enter userID of member to edit: ");
        String userID = input.nextLine().trim();

        // Safety check (retrieve)
        if (!checkExistence("members", "userID", userID)) {
            System.out.println("Member not found.");
            return;
        }

        System.out.println("Enter new values (leave blank to keep current, you can't unset a value):");
        System.out.print("New first name: ");
        String fname = input.nextLine();
        System.out.print("New last name: ");
        String lname = input.nextLine();
        System.out.print("New address: ");
        String addr = input.nextLine();
        System.out.print("New phone: ");
        String phone = input.nextLine();
        System.out.print("New email: ");
        String email = input.nextLine();

        StringBuilder sb = new StringBuilder("UPDATE members SET ");
        boolean first = true;
        if (!fname.isEmpty()) {
            sb.append("fname = ?");
            first = false;
        }
        if (!lname.isEmpty()) {
            if (!first)
                sb.append(", ");
            sb.append("lname = ?");
            first = false;
        }
        if (!addr.isEmpty()) {
            if (!first)
                sb.append(", ");
            sb.append("address = ?");
            first = false;
        }
        if (!phone.isEmpty()) {
            if (!first)
                sb.append(", ");
            sb.append("phone = ?");
            first = false;
        }
        if (!email.isEmpty()) {
            if (!first)
                sb.append(", ");
            sb.append("email = ?");
            first = false;
        }

        if (first) {
            System.out.println("No changes entered.");
            return;
        }
        sb.append(" WHERE userID = ?;");

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (!fname.isEmpty())
                ps.setString(idx++, fname);
            if (!lname.isEmpty())
                ps.setString(idx++, lname);
            if (!addr.isEmpty())
                ps.setString(idx++, addr);
            if (!phone.isEmpty())
                ps.setString(idx++, phone);
            if (!email.isEmpty())
                ps.setString(idx++, email);
            ps.setString(idx, userID);
            int updated = ps.executeUpdate();
            System.out.println("Updated rows: " + updated);
        } catch (SQLException e) {
            System.err.println("Update error: " + e.getMessage());
        }
    }

    private static void deleteMember() {
        System.out.print("Enter userID to delete: ");
        String userID = input.nextLine().trim();

        String sql = "DELETE FROM members WHERE userID = ?;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            int del = ps.executeUpdate();
            System.out.println("Deleted rows: " + del);
        } catch (SQLException e) {
            System.err.println("Delete error (Check for foreign key constraints): " + e.getMessage());
        }
    }

    private static void searchMember() {
        System.out.println("Search by: 1) userID  2) last name");
        int choice = getIntInput();
        String sql = "";
        String param = "";

        if (choice == 1) {
            System.out.print("Enter userID: ");
            param = input.nextLine().trim();
            sql = "SELECT * FROM members WHERE userID = ?;";
        } else if (choice == 2) {
            System.out.print("Enter last name (partial allowed): ");
            param = "%" + input.nextLine().trim() + "%";
            sql = "SELECT * FROM members WHERE lname LIKE ?;";
        } else {
            System.out.println("Invalid.");
            return;
        }

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    printMemberRow(rs);
                    found = true;
                }
                if (!found)
                    System.out.println("No members found.");
            }
        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
    }

    private static void viewAllMembers() {
        String sql = "SELECT * FROM members ORDER BY lname, fname;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            boolean any = false;
            while (rs.next()) {
                printMemberRow(rs);
                any = true;
            }
            if (!any)
                System.out.println("No members.");
        } catch (SQLException e) {
            System.err.println("Error viewing all members: " + e.getMessage());
        }
    }

    private static void printMemberRow(ResultSet rs) throws SQLException {
        System.out.println(
                "userID=" + rs.getString("userID") +
                        " | Name=" + rs.getString("fname") + " " + rs.getString("lname") +
                        " | addr=" + rs.getString("address") +
                        " | email=" + rs.getString("email") +
                        " | Dist=" + rs.getDouble("warehouseDistance"));
    }

    // ==========================================================
    // --- 2. EQUIPMENT MANAGEMENT (Add/Modify/Remove/Retrieve) ---
    // ==========================================================

    private static void equipmentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- EQUIPMENT MENU ---");
            System.out.println("1. Add Equipment");
            System.out.println("2. Edit Equipment");
            System.out.println("3. Delete Equipment");
            System.out.println("4. Search Equipment (Retrieve)");
            System.out.println("5. View All Equipment (Retrieve)");
            System.out.println("6. Back");
            System.out.print("Choose: ");
            int c = getIntInput();
            switch (c) {
                case 1:
                    addEquipment();
                    break;
                case 2:
                    editEquipment();
                    break;
                case 3:
                    deleteEquipment();
                    break;
                case 4:
                    searchEquipment();
                    break;
                case 5:
                    viewAllEquipment();
                    break;
                case 6:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid.");
            }
        }
    }

    private static void addEquipment() {
        System.out.println("--- Adding New Equipment (All fields mandatory) ---");

        // String inputs, guaranteed non-empty
        String serial = getStringInput("Enter serialNum (unique): ");
        String desc = getStringInput("Description: ");
        String type = getStringInput("Type: ");
        String model = getStringInput("Model: ");

        // CORRECTION: Print prompt, then call argument-less method
        System.out.print("Year (numeric): ");
        int year = getYearInput();

        // CORRECTION: Print prompt, then call argument-less method
        System.out.print("Enter warehouseID (numeric): ");
        int wid = getIntInput(); // FIX APPLIED HERE

        // String inputs, guaranteed non-empty
        String wAddr = getStringInput("Enter warehouse address: ");
        String orderNum = getStringInput("Enter Purchase Order Number (orderNum - string): ");
        String location = getStringInput("Enter Equipment Location: ");
        String warExp = getStringInput("Enter Warranty Expiration Date (YYYY-MM-DD): ");

        // Fields: serialNum, description, type, model, year, status, warehouseID,
        // warehouseAddress, orderNum, location, warExp, renterID
        String sql = "INSERT INTO equipment(serialNum, description, type, model, year, status, warehouseID, warehouseAddress, orderNum, location, warExp, renterID) VALUES(?,?,?,?,?,?,?,?,?,?,?,?);";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serial);
            ps.setString(2, desc);
            ps.setString(3, type);
            ps.setString(4, model);
            ps.setInt(5, year);
            ps.setString(6, "AVAILABLE"); // Default status
            ps.setInt(7, wid); // Mandatory int
            ps.setString(8, wAddr); // Mandatory string
            ps.setString(9, orderNum); // Mandatory string/numeric
            ps.setString(10, location); // Mandatory string
            ps.setString(11, warExp); // Mandatory string
            ps.setString(12, "0"); // Placeholder non-null value for renterID (unrented)

            ps.executeUpdate();
            System.out.println("Equipment added.");
        } catch (SQLException e) {
            System.err.println("Add equipment error: " + e.getMessage());
        }
    }

    private static void editEquipment() {
        System.out.print("Enter serialNum of equipment to edit: ");
        String serial = input.nextLine().trim();

        if (!checkExistence("equipment", "serialNum", serial)) {
            System.out.println("Equipment not found.");
            return;
        }

        System.out.println("Enter new values (leave blank to keep current):");
        System.out.print("New description: ");
        String desc = input.nextLine();
        System.out.print("New type: ");
        String type = input.nextLine();
        System.out.print("New model: ");
        String model = input.nextLine();
        System.out.print("New status (e.g., AVAILABLE, RENTED, LOST): ");
        String status = input.nextLine();

        StringBuilder sb = new StringBuilder("UPDATE equipment SET ");
        boolean first = true;

        if (!desc.isEmpty()) {
            sb.append("description = ?");
            first = false;
        }
        if (!type.isEmpty()) {
            if (!first)
                sb.append(", ");
            sb.append("type = ?");
            first = false;
        }
        if (!model.isEmpty()) {
            if (!first)
                sb.append(", ");
            sb.append("model = ?");
            first = false;
        }
        if (!status.isEmpty()) {
            if (!first)
                sb.append(", ");
            sb.append("status = ?");
            first = false;
        }

        if (first) {
            System.out.println("No changes entered.");
            return;
        }
        sb.append(" WHERE serialNum = ?;");

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (!desc.isEmpty())
                ps.setString(idx++, desc);
            if (!type.isEmpty())
                ps.setString(idx++, type);
            if (!model.isEmpty())
                ps.setString(idx++, model);
            if (!status.isEmpty())
                ps.setString(idx++, status);
            ps.setString(idx, serial);
            int updated = ps.executeUpdate();
            System.out.println("Updated rows: " + updated);
        } catch (SQLException e) {
            System.err.println("Update error: " + e.getMessage());
        }
    }

    private static void deleteEquipment() {
        System.out.print("Enter serialNum of equipment to delete: ");
        String serial = input.nextLine().trim();

        String sql = "DELETE FROM equipment WHERE serialNum = ?;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serial);
            int del = ps.executeUpdate();
            System.out.println("Deleted rows: " + del);
        } catch (SQLException e) {
            System.err.println("Delete error (Check for foreign key constraints): " + e.getMessage());
        }
    }

    private static void searchEquipment() {
        System.out.println("Search by: 1) SerialNum  2) Type");
        int choice = getIntInput();
        String sql = "";
        String param = "";

        if (choice == 1) {
            System.out.print("Enter SerialNum: ");
            param = input.nextLine().trim();
            sql = "SELECT * FROM equipment WHERE serialNum = ?;";
        } else if (choice == 2) {
            System.out.print("Enter Type (partial allowed): ");
            param = "%" + input.nextLine().trim() + "%";
            sql = "SELECT * FROM equipment WHERE type LIKE ?;";
        } else {
            System.out.println("Invalid.");
            return;
        }

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    printEquipmentRow(rs);
                    found = true;
                }
                if (!found)
                    System.out.println("No equipment found.");
            }
        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
    }

    private static void viewAllEquipment() {
        String sql = "SELECT * FROM equipment ORDER BY type, description;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            boolean any = false;
            while (rs.next()) {
                printEquipmentRow(rs);
                any = true;
            }
            if (!any)
                System.out.println("No equipment.");
        } catch (SQLException e) {
            System.err.println("Error viewing all equipment: " + e.getMessage());
        }
    }

    private static void printEquipmentRow(ResultSet rs) throws SQLException {
        System.out.println("serial=" + rs.getString("serialNum") +
                " | desc=" + rs.getString("description") +
                " | type=" + rs.getString("type") +
                " | model=" + rs.getString("model") +
                " | status=" + rs.getString("status") +
                " | RenterID=" + rs.getString("renterID"));
    }

    // ==========================================================
    // --- 3. DRONE MANAGEMENT (Add/Modify/Remove/Retrieve) ---
    // ==========================================================

    private static void dronesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- DRONES MENU ---");
            System.out.println("1. Add Drone");
            System.out.println("2. Edit Drone");
            System.out.println("3. Delete Drone");
            System.out.println("4. Search Drone (Retrieve)");
            System.out.println("5. View All Drones (Retrieve)");
            System.out.println("6. Back");
            System.out.print("Choose: ");
            int c = getIntInput();
            switch (c) {
                case 1:
                    addDrone();
                    break;
                case 2:
                    editDrone();
                    break;
                case 3:
                    deleteDrone();
                    break;
                case 4:
                    searchDrone();
                    break;
                case 5:
                    viewAllDrones();
                    break;
                case 6:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid.");
            }
        }
    }

    private static void addDrone() {
        System.out.println("--- Adding New Drone (All fields mandatory) ---");

        // String inputs, guaranteed non-empty
        String serial = getStringInput("Enter serialNum (unique): ");
        String name = getStringInput("Name: ");
        String model = getStringInput("Model: ");

        // String input, guaranteed non-empty
        String location = getStringInput("Enter Location: ");

        // CORRECTION: Print prompt, then call argument-less method
        System.out.print("Enter Year (numeric): ");
        int year = getYearInput();

        // String inputs, guaranteed non-empty
        String wAddr = getStringInput("Enter Warehouse Address: ");
        String batteryID = getStringInput("Enter Battery ID: ");

        // CORRECTION: Print prompt, then call argument-less method
        System.out.print("Enter Total Miles (numeric): ");
        double totalMiles = getDoubleInput();

        // Fields: serialNum, name, model, status, location, year,
        // WarehouseAddress, BatteryID, totalMiles (9 COLUMNS TOTAL)
        String sql = "INSERT INTO drones(serialNum, name, model, status, location, year, WarehouseAddress, BatteryID, totalMiles) VALUES(?,?,?,?,?,?,?,?,?);";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serial);
            ps.setString(2, name);
            ps.setString(3, model);
            ps.setString(4, "AVAILABLE"); // Default status

            // Correctly indexed parameters
            ps.setString(5, location);
            ps.setInt(6, year);
            ps.setString(7, wAddr);
            ps.setString(8, batteryID);
            ps.setDouble(9, totalMiles);

            ps.executeUpdate();
            System.out.println("Drone added.");
        } catch (SQLException e) {
            System.err.println("Add drone error: " + e.getMessage());
        }
    }

    private static void editDrone() {
        System.out.print("Enter serialNum of drone to edit: ");
        String serial = input.nextLine().trim();

        if (!checkExistence("drones", "serialNum", serial)) {
            System.out.println("Drone not found.");
            return;
        }

        System.out.println("Enter new values (leave blank to keep current):");
        System.out.print("New name: ");
        String name = input.nextLine();
        System.out.print("New model: ");
        String model = input.nextLine();
        System.out.print("New status (e.g., AVAILABLE, IN_TRANSIT, INACTIVE): ");
        String status = input.nextLine();

        StringBuilder sb = new StringBuilder("UPDATE drones SET ");
        boolean first = true;

        if (!name.isEmpty()) {
            sb.append("name = ?");
            first = false;
        }
        if (!model.isEmpty()) {
            if (!first)
                sb.append(", ");
            sb.append("model = ?");
            first = false;
        }
        if (!status.isEmpty()) {
            if (!first)
                sb.append(", ");
            sb.append("status = ?");
            first = false;
        }

        if (first) {
            System.out.println("No changes entered.");
            return;
        }
        sb.append(" WHERE serialNum = ?;");

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (!name.isEmpty())
                ps.setString(idx++, name);
            if (!model.isEmpty())
                ps.setString(idx++, model);
            if (!status.isEmpty())
                ps.setString(idx++, status);
            ps.setString(idx, serial);
            int updated = ps.executeUpdate();
            System.out.println("Updated rows: " + updated);
        } catch (SQLException e) {
            System.err.println("Update error: " + e.getMessage());
        }
    }

    private static void deleteDrone() {
        System.out.print("Enter serialNum of drone to delete: ");
        String serial = input.nextLine().trim();

        String sql = "DELETE FROM drones WHERE serialNum = ?;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serial);
            int del = ps.executeUpdate();
            System.out.println("Deleted rows: " + del);
        } catch (SQLException e) {
            System.err.println("Delete error (Check for foreign key constraints): " + e.getMessage());
        }
    }

    private static void searchDrone() {
        System.out.println("Search by: 1) SerialNum  2) Model");
        int choice = getIntInput();
        String sql = "";
        String param = "";

        if (choice == 1) {
            System.out.print("Enter SerialNum: ");
            param = input.nextLine().trim();
            sql = "SELECT * FROM drones WHERE serialNum = ?;";
        } else if (choice == 2) {
            System.out.print("Enter Model (partial allowed): ");
            param = "%" + input.nextLine().trim() + "%";
            sql = "SELECT * FROM drones WHERE model LIKE ?;";
        } else {
            System.out.println("Invalid.");
            return;
        }

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    printDroneRow(rs);
                    found = true;
                }
                if (!found)
                    System.out.println("No drone found.");
            }
        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
    }

    private static void viewAllDrones() {
        String sql = "SELECT * FROM drones ORDER BY name, model;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            boolean any = false;
            while (rs.next()) {
                printDroneRow(rs);
                any = true;
            }
            if (!any)
                System.out.println("No drones.");
        } catch (SQLException e) {
            System.err.println("Error viewing all drones: " + e.getMessage());
        }
    }

    private static void printDroneRow(ResultSet rs) throws SQLException {
        System.out.println("serial=" + rs.getString("serialNum") +
                " | name=" + rs.getString("name") +
                " | model=" + rs.getString("model") +
                " | status=" + rs.getString("status") +
                " | miles=" + rs.getDouble("totalMiles"));
    }

    // ==========================================================
    // --- 4. RENTALS & DELIVERIES (Transactional Operations) ---
    // ==========================================================

    private static void rentalsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- RENTALS & DELIVERIES ---");
            System.out.println("1. Rent Equipment");
            System.out.println("2. Return Equipment");
            System.out.println("3. Schedule Delivery (assign drone)");
            System.out.println("4. Schedule Pickup (assign drone)");
            System.out.println("5. Back");
            System.out.print("Choose: ");
            int c = getIntInput();
            switch (c) {
                case 1:
                    rentEquipment();
                    break;
                case 2:
                    returnEquipment();
                    break;
                case 3:
                    scheduleDelivery();
                    break;
                case 4:
                    schedulePickup();
                    break;
                case 5:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid.");
            }
        }
    }

    // RENT EQUIPMENT: Inserts new rental record, updates equipment status.
    private static void rentEquipment() {
        System.out.println("--- Rent Equipment (All fields mandatory) ---");
        String checkOutID = getStringInput("Enter checkout ID (unique): ");
        String serial = getStringInput("Enter equipment serialNum: ");
        String userID = getStringInput("Enter userID renting: ");

        // Due date is mandatory
        String due = getStringInput("Enter dueDate (YYYY-MM-DD): ");

        // Print prompt, then call argument-less method
        System.out.print("Enter rental fee (numeric): ");
        double fee = getDoubleInput();

        if (!checkExistence("equipment", "serialNum", serial)) {
            System.out.println("Error: Equipment not found.");
            return;
        }

        // 1. INSERT into rentals
        // Fields: checkOutID, serialNum, userID, dueDate, rentalFees, Returns (6
        // columns)
        String sql = "INSERT INTO rentals(checkOutID, serialNum, userID, dueDate, rentalFees, Returns) " +
                "VALUES(?,?,?,?,?,?);";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, checkOutID);
                ps.setString(2, serial);
                ps.setString(3, userID);
                // Index 4 corresponds to dueDate (provided by the user)
                ps.setString(4, due);
                // Index 5 corresponds to rentalFees
                ps.setDouble(5, fee);
                // Index 6 corresponds to Returns
                ps.setString(6, "NO");
                ps.executeUpdate();
            }

            // 2. UPDATE equipment status
            // Sets renterID to the renting userID and status to RENTED.
            String eqUpd = "UPDATE equipment SET renterID = ?, status = ? WHERE serialNum = ? AND status = 'AVAILABLE';";
            try (PreparedStatement ps2 = conn.prepareStatement(eqUpd)) {
                ps2.setString(1, userID);
                ps2.setString(2, "RENTED");
                ps2.setString(3, serial);
                int updated = ps2.executeUpdate();
                if (updated == 0) {
                    // Equipment was not available, abort transaction
                    conn.rollback();
                    System.out
                            .println("RENT FAILED: Equipment is not AVAILABLE or SerialNum is invalid (Rolling back).");
                    return;
                }
            }
            conn.commit(); // Commit transaction
            System.out.println("Equipment rented successfully (ID: " + checkOutID + ").");
        } catch (SQLException e) {
            System.err.println("Rent error: " + e.getMessage());
        }
    }

    // RETURN EQUIPMENT: Updates rental record, updates equipment status.
    private static void returnEquipment() {
        System.out.print("Enter checkout ID to return: ");
        String checkOutID = input.nextLine().trim();

        // returnDate is intentionally omitted from variable and SQL UPDATE since the
        // schema lacks it.

        String findSerial = "SELECT serialNum FROM rentals WHERE checkOutID = ? AND Returns = 'NO';";

        // CORRECTED SQL: Only updates the 'Returns' column to 'YES' (1 parameter).
        String updateRental = "UPDATE rentals SET Returns = ? WHERE checkOutID = ?;";

        // Sets renterID back to the placeholder '0' (per 'no NULL' requirement).
        String updateEquipment = "UPDATE equipment SET status = ?, renterID = '0' WHERE serialNum = ? AND status = 'RENTED';";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            String serial = null;

            // Step 1: Find the serial number and check if it's currently not returned
            try (PreparedStatement ps1 = conn.prepareStatement(findSerial)) {
                ps1.setString(1, checkOutID);
                try (ResultSet rs = ps1.executeQuery()) {
                    if (rs.next()) {
                        serial = rs.getString("serialNum");
                    } else {
                        System.out.println("Error: Rental ID not found or already returned.");
                        return;
                    }
                }
            }

            // Step 2: Mark the rental record as returned
            try (PreparedStatement ps2 = conn.prepareStatement(updateRental)) {
                ps2.setString(1, "YES");
                ps2.setString(2, checkOutID); // The second parameter is now the WHERE clause value
                ps2.executeUpdate();
            }

            // Step 3: Update equipment status (only if currently RENTED)
            try (PreparedStatement ps3 = conn.prepareStatement(updateEquipment)) {
                ps3.setString(1, "AVAILABLE");
                ps3.setString(2, serial);
                ps3.executeUpdate();
            }

            conn.commit(); // Commit transaction
            System.out.println("Equipment " + serial + " returned successfully.");

        } catch (SQLException e) {
            System.err.println("Return error: " + e.getMessage());
        }
    }

    // DELIVERY: Assigns a drone to transport equipment
    private static void scheduleDelivery() {
        System.out.print("Enter equipment serialNum for delivery: ");
        String serial = input.nextLine().trim();
        System.out.print("Enter drone serialNum to assign: ");
        String drone = input.nextLine().trim();

        // Inserts dSerialNum and eSerialNum into transports.
        String sql = "INSERT INTO transports(dSerialNum, eSerialNum) VALUES(?,?);";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, drone);
                ps.setString(2, serial);
                ps.executeUpdate();
            }

            // Update drone status to IN_TRANSIT
            String droneUpd = "UPDATE drones SET status = 'IN_TRANSIT' WHERE serialNum = ?;";
            try (PreparedStatement ps2 = conn.prepareStatement(droneUpd)) {
                ps2.setString(1, drone);
                ps2.executeUpdate();
            }

            conn.commit(); // Commit transaction
            System.out.println("Delivery scheduled: equipment " + serial + " via drone " + drone + ".");
        } catch (SQLException e) {
            System.err.println("Schedule Delivery error: " + e.getMessage());
        }
    }

    // PICKUP: Assigns a drone to retrieve equipment
    private static void schedulePickup() {
        System.out.print("Enter equipment serialNum for pickup: ");
        String serial = input.nextLine().trim();
        System.out.print("Enter drone serialNum to assign: ");
        String drone = input.nextLine().trim();

        // Inserts dSerialNum and eSerialNum into transports.
        String sql = "INSERT INTO transports(dSerialNum, eSerialNum) VALUES(?,?);";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, drone);
                ps.setString(2, serial);
                ps.executeUpdate();
            }

            // Update drone status to IN_TRANSIT
            String droneUpd = "UPDATE drones SET status = 'IN_TRANSIT' WHERE serialNum = ?;";
            try (PreparedStatement ps2 = conn.prepareStatement(droneUpd)) {
                ps2.setString(1, drone);
                ps2.executeUpdate();
            }

            conn.commit(); // Commit transaction
            System.out.println("Pickup scheduled: equipment " + serial + " via drone " + drone + ".");
        } catch (SQLException e) {
            System.err.println("Schedule Pickup error: " + e.getMessage());
        }
    }

    // ==========================================================
    // --- 5. REPORTS ---
    // ==========================================================

    private static void reportsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- REPORTS ---");
            System.out.println("1. Renting checkouts by member (Total rented per UserID)");
            System.out.println("2. Popular item (Most rented equipment)");
            System.out.println("3. Popular manufacturer");
            System.out.println("4. Popular drone (Most transports)");
            System.out.println("5. Member who rented most items");
            System.out.println("6. Equipment by type released before YEAR");
            System.out.println("7. Back");
            System.out.print("Choose: ");
            int c = getIntInput();
            switch (c) {
                case 1:
                    reportRentingCheckoutsByMember();
                    break;
                case 2:
                    reportPopularItem();
                    break;
                case 3:
                    reportPopularManufacturer();
                    break;
                case 4:
                    reportPopularDrone();
                    break;
                case 5:
                    reportMemberWithMostItems();
                    break;
                case 6:
                    reportEquipmentByTypeBeforeYear();
                    break;
                case 7:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid.");
            }
        }
    }

    // Report 1: Total number equipment items rented by a single member
    private static void reportRentingCheckoutsByMember() {
        System.out.print("Enter userID to count rentals: ");
        String userID = input.nextLine().trim();
        String sql = "SELECT COUNT(checkOutID) AS cnt FROM rentals WHERE userID = ?;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("User " + userID + " has a total of " + rs.getInt("cnt") + " rental checkouts.");
                } else {
                    System.out.println("No data found for user: " + userID);
                }
            }
        } catch (SQLException e) {
            System.err.println("Report error: " + e.getMessage());
        }
    }

    // Report 2: Find most popular equipment by number of rentals
    private static void reportPopularItem() {
        String sql = "SELECT r.serialNum, e.description, COUNT(r.checkOutID) AS timesRented " +
                "FROM rentals r JOIN equipment e ON r.serialNum = e.serialNum " +
                "GROUP BY r.serialNum, e.description ORDER BY timesRented DESC LIMIT 1;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("Most Popular Item:");
                System.out.println("   Serial: " + rs.getString("serialNum"));
                System.out.println("   Desc: " + rs.getString("description"));
                System.out.println("   Times Rented: " + rs.getInt("timesRented"));
            } else {
                System.out.println("No rental data available.");
            }
        } catch (SQLException e) {
            System.err.println("Report error: " + e.getMessage());
        }
    }

    // Report 3: Most frequent equipment manufacturer (Requires 'equip_model' table
    // with 'manufacturer' column)
    private static void reportPopularManufacturer() {
        String sql = "SELECT em.manufacturer, COUNT(r.checkOutID) AS rentedCount " +
                "FROM rentals r " +
                "JOIN equipment e ON r.serialNum = e.serialNum " +
                "JOIN equip_model em ON e.model = em.model " +
                "GROUP BY em.manufacturer " +
                "ORDER BY rentedCount DESC LIMIT 1;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("Most Popular Manufacturer: " + rs.getString("manufacturer") +
                        " | Units Rented: " + rs.getInt("rentedCount"));
            } else {
                System.out.println("No manufacturer data available (ensure equip_model table exists).");
            }
        } catch (SQLException e) {
            System.err.println("Report error: " + e.getMessage());
        }
    }

    // Report 4: Most used drone
    private static void reportPopularDrone() {
        String sql = "SELECT t.dSerialNum, d.name, COUNT(t.dSerialNum) AS uses " +
                "FROM transports t JOIN drones d ON t.dSerialNum = d.serialNum " +
                "GROUP BY t.dSerialNum, d.name " +
                "ORDER BY uses DESC LIMIT 1;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("Most Popular Drone:");
                System.out.println("   Serial: " + rs.getString("dSerialNum"));
                System.out.println("   Name: " + rs.getString("name"));
                System.out.println("   Transports: " + rs.getInt("uses"));
            } else {
                System.out.println("No transport data available.");
            }
        } catch (SQLException e) {
            System.err.println("Report error: " + e.getMessage());
        }
    }

    // Report 5: Member who has rented the most items
    private static void reportMemberWithMostItems() {
        String sql = "SELECT r.userID, m.fname, m.lname, COUNT(r.checkOutID) AS totalRented " +
                "FROM rentals r JOIN members m ON r.userID = m.userID " +
                "GROUP BY r.userID, m.fname, m.lname " +
                "ORDER BY totalRented DESC LIMIT 1;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("Top Renter:");
                System.out.println("   UserID: " + rs.getString("userID"));
                System.out.println("   Name: " + rs.getString("fname") + " " + rs.getString("lname"));
                System.out.println("   Total Items Rented: " + rs.getInt("totalRented"));
            } else {
                System.out.println("No rental records yet.");
            }
        } catch (SQLException e) {
            System.err.println("Report error: " + e.getMessage());
        }
    }

    // Report 6: Equipment by Type released before YEAR
    private static void reportEquipmentByTypeBeforeYear() {
        System.out.print("Enter equipment type (exact match): ");
        String type = input.nextLine().trim();
        System.out.print("Enter YEAR (e.g., 2018): ");
        int year = getIntInput();

        String sql = "SELECT serialNum, description, year FROM equipment WHERE type = ? AND year < ? ORDER BY year DESC;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                boolean any = false;
                System.out.println("--- Equipment of type '" + type + "' made before " + year + " ---");
                while (rs.next()) {
                    System.out.println("   serial=" + rs.getString("serialNum") +
                            " | desc=" + rs.getString("description") +
                            " | year=" + rs.getInt("year"));
                    any = true;
                }
                if (!any)
                    System.out.println("No equipment matched.");
            }
        } catch (SQLException e) {
            System.err.println("Report error: " + e.getMessage());
        }
    }
}