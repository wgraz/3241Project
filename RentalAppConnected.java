import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class RentalAppConnected {
    private static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Manage Members");
            System.out.println("2. Manage Equipment");
            System.out.println("3. Rentals & Deliveries");
            System.out.println("4. Reports");
            System.out.println("5. Exit");
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
                    rentalsMenu();
                    break;
                case 4:
                    reportsMenu();
                    break;
                case 5:
                    running = false;
                    System.out.println("Exiting. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }

        input.close();
    }

    // Members Full Implementation
    private static void membersMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- MEMBERS MENU ---");
            System.out.println("1. Add Member");
            System.out.println("2. Edit Member");
            System.out.println("3. Delete Member");
            System.out.println("4. Search Member");
            System.out.println("5. View All Members");
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
        System.out.print("Enter userID (unique): ");
        String userID = input.nextLine().trim();
        System.out.print("Enter first name: ");
        String fname = input.nextLine().trim();
        System.out.print("Enter last name: ");
        String lname = input.nextLine().trim();
        System.out.print("Enter address: ");
        String addr = input.nextLine().trim();
        System.out.print("Enter phone: ");
        String phone = input.nextLine().trim();
        System.out.print("Enter email: ");
        String email = input.nextLine().trim();

        // Auto-fill startDate with today's date
        String startDate = LocalDate.now().toString();

        String sql = "INSERT INTO members(userID, fname, lname, address, phone, email, startDate) " +
                "VALUES(?,?,?,?,?,?,?);";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            ps.setString(2, fname);
            ps.setString(3, lname);
            ps.setString(4, addr);
            ps.setString(5, phone);
            ps.setString(6, email);
            ps.setString(7, startDate);
            ps.executeUpdate();
            System.out.println("Member added. startDate set to " + startDate);
        } catch (SQLException e) {
            System.err.println("Error adding member: " + e.getMessage());
        }
    }

    private static void editMember() {
        System.out.print("Enter userID of member to edit: ");
        String userID = input.nextLine().trim();

        // Check exists
        String check = "SELECT userID FROM members WHERE userID = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ch = conn.prepareStatement(check)) {
            ch.setString(1, userID);
            try (ResultSet rs = ch.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Member not found.");
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        System.out.println("Enter new values (leave blank to keep current):");
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
            System.err.println("Delete error: " + e.getMessage());
        }
    }

    private static void searchMember() {
        System.out.println("Search by: 1) userID  2) last name");
        int choice = getIntInput();
        if (choice == 1) {
            System.out.print("Enter userID: ");
            String userID = input.nextLine().trim();
            String sql = "SELECT * FROM members WHERE userID = ?;";
            try (Connection conn = Database.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        printMemberRow(rs);
                    else
                        System.out.println("No member found.");
                }
            } catch (SQLException e) {
                System.err.println("Search error: " + e.getMessage());
            }
        } else if (choice == 2) {
            System.out.print("Enter last name (partial allowed): ");
            String lname = input.nextLine().trim();
            String sql = "SELECT * FROM members WHERE lname LIKE ?;";
            try (Connection conn = Database.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + lname + "%");
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
        } else {
            System.out.println("Invalid.");
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
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void printMemberRow(ResultSet rs) throws SQLException {
        System.out.println(
                "userID=" + rs.getString("userID") +
                        " | Name=" + rs.getString("fname") + " " + rs.getString("lname") +
                        " | addr=" + rs.getString("address") +
                        " | phone=" + rs.getString("phone") +
                        " | email=" + rs.getString("email") +
                        " | startDate=" + rs.getString("startDate"));
    }

    // Basic Equpiment Menu
    private static void equipmentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- EQUIPMENT MENU ---");
            System.out.println("1. Add Equipment (basic)");
            System.out.println("2. View All Equipment");
            System.out.println("3. Back");
            System.out.print("Choose: ");
            int c = getIntInput();
            switch (c) {
                case 1:
                    addEquipment();
                    break;
                case 2:
                    viewAllEquipment();
                    break;
                case 3:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid.");
            }
        }
    }

    private static void addEquipment() {
        System.out.print("Enter serialNum (unique): ");
        String serial = input.nextLine().trim();
        System.out.print("Description: ");
        String desc = input.nextLine().trim();
        System.out.print("Type: ");
        String type = input.nextLine().trim();
        System.out.print("Model: ");
        String model = input.nextLine().trim();
        System.out.print("Year (numeric or blank): ");
        String yearS = input.nextLine().trim();
        Integer year = null;
        if (!yearS.isEmpty()) {
            try {
                year = Integer.parseInt(yearS);
            } catch (NumberFormatException e) {
                year = null;
            }
        }
        String sql = "INSERT INTO equipment(serialNum, description, type, model, year, status) VALUES(?,?,?,?,?,?);";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serial);
            ps.setString(2, desc);
            ps.setString(3, type);
            ps.setString(4, model);
            if (year != null)
                ps.setInt(5, year);
            else
                ps.setNull(5, Types.INTEGER);
            ps.setString(6, "AVAILABLE");
            ps.executeUpdate();
            System.out.println("Equipment added.");
        } catch (SQLException e) {
            System.err.println("Add equipment error: " + e.getMessage());
        }
    }

    private static void viewAllEquipment() {
        String sql = "SELECT * FROM equipment ORDER BY type, description;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            boolean any = false;
            while (rs.next()) {
                System.out.println("serial=" + rs.getString("serialNum") +
                        " | desc=" + rs.getString("description") +
                        " | type=" + rs.getString("type") +
                        " | model=" + rs.getString("model") +
                        " | year=" + rs.getInt("year") +
                        " | status=" + rs.getString("status"));
                any = true;
            }
            if (!any)
                System.out.println("No equipment.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Basic Rentals and Deliveries Menu
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

    private static void rentEquipment() {
        System.out.print("Enter checkout ID (unique): ");
        String checkOutID = input.nextLine().trim();
        System.out.print("Enter equipment serialNum: ");
        String serial = input.nextLine().trim();
        System.out.print("Enter userID renting: ");
        String userID = input.nextLine().trim();
        String today = LocalDate.now().toString();
        System.out.print("Enter dueDate (YYYY-MM-DD) or blank: ");
        String due = input.nextLine().trim();
        System.out.print("Enter rental fee (numeric) or blank: ");
        String feeS = input.nextLine().trim();
        Double fee = null;
        if (!feeS.isEmpty()) {
            try {
                fee = Double.parseDouble(feeS);
            } catch (NumberFormatException e) {
                fee = null;
            }
        }

        String sql = "INSERT INTO rentals(checkOutID, serialNum, userID, checkOutDate, dueDate, rentalFees, Returns) " +
                "VALUES(?,?,?,?,?,?,?);";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, checkOutID);
            ps.setString(2, serial);
            ps.setString(3, userID);
            ps.setString(4, today);
            if (!due.isEmpty())
                ps.setString(5, due);
            else
                ps.setNull(5, Types.VARCHAR);
            if (fee != null)
                ps.setDouble(6, fee);
            else
                ps.setNull(6, Types.REAL);
            ps.setString(7, "NO");
            ps.executeUpdate();

            String eqUpd = "UPDATE equipment SET renterID = ?, status = ? WHERE serialNum = ?;";
            try (PreparedStatement ps2 = conn.prepareStatement(eqUpd)) {
                ps2.setString(1, userID);
                ps2.setString(2, "RENTED");
                ps2.setString(3, serial);
                ps2.executeUpdate();
            }
            System.out.println("Equipment rented successfully (DB updated).");
        } catch (SQLException e) {
            System.err.println("Rent error: " + e.getMessage());
        }
    }

    private static void returnEquipment() {
        System.out.print("Enter checkout ID to return: ");
        String checkOutID = input.nextLine().trim();
        String sql = "UPDATE rentals SET Returns = ? WHERE checkOutID = ?;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "YES");
            ps.setString(2, checkOutID);
            int u = ps.executeUpdate();
            System.out.println("Return registered, rows updated: " + u);

            String findSerial = "SELECT serialNum FROM rentals WHERE checkOutID = ?;";
            try (PreparedStatement ps2 = conn.prepareStatement(findSerial)) {
                ps2.setString(1, checkOutID);
                try (ResultSet rs = ps2.executeQuery()) {
                    if (rs.next()) {
                        String serial = rs.getString("serialNum");
                        String eqUpd = "UPDATE equipment SET status = ?, renterID = NULL WHERE serialNum = ?;";
                        try (PreparedStatement ps3 = conn.prepareStatement(eqUpd)) {
                            ps3.setString(1, "AVAILABLE");
                            ps3.setString(2, serial);
                            ps3.executeUpdate();
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Return error: " + e.getMessage());
        }
    }

    private static void scheduleDelivery() {
        System.out.print("Enter equipment serialNum for delivery: ");
        String serial = input.nextLine().trim();
        System.out.print("Enter userID: ");
        String userID = input.nextLine().trim();
        System.out.print("Enter drone serialNum to assign: ");
        String drone = input.nextLine().trim();

        String sql = "INSERT INTO transports(dSerialNum, eSerialNum) VALUES(?,?);";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, drone);
            ps.setString(2, serial);
            ps.executeUpdate();
            System.out.println("Delivery scheduled: equipment " + serial + " -> user " + userID +
                    " via drone " + drone + ". (Stored in transports)");
        } catch (SQLException e) {
            System.err.println("Schedule Delivery error: " + e.getMessage());
        }
    }

    private static void schedulePickup() {
        System.out.print("Enter equipment serialNum for pickup: ");
        String serial = input.nextLine().trim();
        System.out.print("Enter userID: ");
        String userID = input.nextLine().trim();
        System.out.print("Enter drone serialNum to assign: ");
        String drone = input.nextLine().trim();

        String sql = "INSERT INTO transports(dSerialNum, eSerialNum) VALUES(?,?);";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, drone);
            ps.setString(2, serial);
            ps.executeUpdate();
            System.out.println("Pickup scheduled: equipment " + serial + " collected from user " + userID +
                    " via drone " + drone + ". (Stored in transports)");
        } catch (SQLException e) {
            System.err.println("Schedule Pickup error: " + e.getMessage());
        }
    }

    // Reports asked for
    private static void reportsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- REPORTS ---");
            System.out.println("1. Renting checkouts by member (total items rented)");
            System.out.println("2. Popular item (most rented)");
            System.out.println("3. Popular manufacturer");
            System.out.println("4. Popular drone (most transports)");
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

    // 1. Renting checkouts: total number equipment items rented by a single member
    private static void reportRentingCheckoutsByMember() {
        System.out.print("Enter userID to count rentals: ");
        String userID = input.nextLine().trim();
        String sql = "SELECT COUNT(*) AS cnt FROM rentals WHERE userID = ?;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("User " + userID + " has rented " + rs.getInt("cnt") + " items.");
                } else
                    System.out.println("No data.");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 2. Popular item: find most popular equipment by number of rentals
    private static void reportPopularItem() {
        String sql = "SELECT r.serialNum, e.description, COUNT(r.checkOutID) AS timesRented, " +
                "       IFNULL(SUM(r.rentalFees),0) AS totalFees " +
                "FROM rentals r LEFT JOIN equipment e ON r.serialNum = e.serialNum " +
                "GROUP BY r.serialNum " +
                "ORDER BY timesRented DESC, totalFees DESC LIMIT 1;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("Popular item: serial=" + rs.getString("serialNum") +
                        " desc=" + rs.getString("description") +
                        " timesRented=" + rs.getInt("timesRented") +
                        " totalFees=" + rs.getDouble("totalFees"));
            } else {
                System.out.println("No rentals data available.");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 3. Popular Manufacturer: most frequent equipment manufacturer
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
                System.out.println("Popular manufacturer: " + rs.getString("manufacturer") +
                        " | rented units=" + rs.getInt("rentedCount"));
            } else {
                System.out
                        .println("No data for manufacturer popularity (ensure equip_model & equipment are populated).");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 4. Popular Drone: most used drone
    private static void reportPopularDrone() {
        String sql = "SELECT t.dSerialNum, d.name, COUNT(t.id) AS uses " +
                "FROM transports t LEFT JOIN drones d ON t.dSerialNum = d.serialNum " +
                "GROUP BY t.dSerialNum " +
                "ORDER BY uses DESC LIMIT 1;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("Popular drone: serial=" + rs.getString("dSerialNum") +
                        " name=" + rs.getString("name") +
                        " uses=" + rs.getInt("uses"));
            } else {
                System.out.println("No transport data yet.");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 5. Items checked out: member who has rented the most items
    private static void reportMemberWithMostItems() {
        String sql = "SELECT r.userID, COUNT(r.checkOutID) AS totalRented " +
                "FROM rentals r " +
                "GROUP BY r.userID " +
                "ORDER BY totalRented DESC LIMIT 1;";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("Top renter: userID=" + rs.getString("userID") +
                        " | total items rented=" + rs.getInt("totalRented"));
            } else {
                System.out.println("No rental records yet.");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 6. Equipment by Type released before YEAR
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
                while (rs.next()) {
                    System.out.println("serial=" + rs.getString("serialNum") +
                            " desc=" + rs.getString("description") +
                            " year=" + rs.getInt("year"));
                    any = true;
                }
                if (!any)
                    System.out.println("No equipment matched.");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Helper methods
    private static int getIntInput() {
        while (true) {
            try {
                String line = input.nextLine();
                return Integer.parseInt(line.trim());
            } catch (Exception e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}
