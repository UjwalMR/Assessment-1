// import java.sql.*;
// import java.time.LocalDate;
// import java.util.Scanner;

// public class EnergyTradeApp {
//     // --- EDIT THESE to match your SQL Server ---
//    // private static final String DB_URL = "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=EnergyTradingDB;encrypt=true;trustServerCertificate=true;";
//    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=EnergyTradingDB;encrypt=true;trustServerCertificate=true;";
 
//    //private static final String DB_USER = "sa";       // or "sa"
//     //private static final String DB_PASS = "";  // set the password you created
//     // ---------------------------------------------

//     private static final Scanner scanner = new Scanner(System.in);

//     public static void main(String[] args) {
//         System.out.println("=== Energy Trade Manager ===");
//         try {
//             // Optional explicit driver load (not required on modern JDKs if driver on classpath)
//             Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//         } catch (ClassNotFoundException e) {
//             System.out.println("JDBC Driver not found. Make sure mssql-jdbc jar is in lib/ and on classpath.");
//         }

//         while (true) {
//             printMenu();
//             String choice = readLine("Enter choice: ");
//             switch (choice) {
//                 case "1": addTrade(); break;
//                 case "2": viewAllTrades(); break;
//                 case "3": updateTrade(); break;
//                 case "4": deleteTrade(); break;
//                 case "5": searchTrades(); break;
//                 case "6": System.out.println("Goodbye!"); return;
//                 default: System.out.println("Invalid choice.");
//             }
//         }
//     }

//     private static void printMenu() {
//         System.out.println("\n1) Add a Trade");
//         System.out.println("2) View All Trades");
//         System.out.println("3) Update Trade (Price/Volume)");
//         System.out.println("4) Delete Trade");
//         System.out.println("5) Search Trades (Counterparty / Commodity)");
//         System.out.println("6) Exit");
//     }

//     private static Connection getConnection() throws SQLException {
//         return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
//     }

//     private static void addTrade() {
//         try (Connection conn = getConnection()) {
//             String dateStr = readLine("Trade Date (YYYY-MM-DD): ");
//             java.sql.Date tradeDate = java.sql.Date.valueOf(LocalDate.parse(dateStr));

//             String counterparty = readLine("Counterparty: ");
//             String commodity = readLine("Commodity (e.g., Power): ");
//             double volume = Double.parseDouble(readLine("Volume (numeric, e.g., 100.00): "));
//             double price = Double.parseDouble(readLine("Price per unit (e.g., 45.50): "));
//             String tradeType;
//             while (true) {
//                 tradeType = readLine("Trade Type (BUY or SELL): ").toUpperCase();
//                 if (tradeType.equals("BUY") || tradeType.equals("SELL")) break;
//                 System.out.println("Type must be BUY or SELL.");
//             }

//             String sql = "INSERT INTO Trades (TradeDate, Counterparty, Commodity, Volume, Price, TradeType) VALUES (?, ?, ?, ?, ?, ?)";
//             try (PreparedStatement ps = conn.prepareStatement(sql)) {
//                 ps.setDate(1, tradeDate);
//                 ps.setString(2, counterparty);
//                 ps.setString(3, commodity);
//                 ps.setBigDecimal(4, new java.math.BigDecimal(String.format("%.2f", volume)));
//                 ps.setBigDecimal(5, new java.math.BigDecimal(String.format("%.2f", price)));
//                 ps.setString(6, tradeType);
//                 int rows = ps.executeUpdate();
//                 System.out.println(rows + " trade inserted.");
//             }
//         } catch (Exception e) {
//             System.out.println("Error inserting trade: " + e.getMessage());
//         }
//     }

//     private static void viewAllTrades() {
//         String sql = "SELECT TradeID, TradeDate, Counterparty, Commodity, Volume, Price, TradeType FROM Trades ORDER BY TradeID";
//         try (Connection conn = getConnection();
//              Statement st = conn.createStatement();
//              ResultSet rs = st.executeQuery(sql)) {
//             System.out.println("\n-- All Trades --");
//             System.out.printf("%-6s %-12s %-20s %-10s %-10s %-10s %-6s%n", "ID", "Date", "Counterparty", "Commodity", "Volume", "Price", "Type");
//             while (rs.next()) {
//                 System.out.printf("%-6d %-12s %-20s %-10s %-10s %-10s %-6s%n",
//                         rs.getInt("TradeID"),
//                         rs.getDate("TradeDate").toString(),
//                         rs.getString("Counterparty"),
//                         rs.getString("Commodity"),
//                         rs.getBigDecimal("Volume").toPlainString(),
//                         rs.getBigDecimal("Price").toPlainString(),
//                         rs.getString("TradeType"));
//             }
//         } catch (SQLException e) {
//             System.out.println("Error reading trades: " + e.getMessage());
//         }
//     }

//     private static void updateTrade() {
//         try (Connection conn = getConnection()) {
//             int id = Integer.parseInt(readLine("Enter TradeID to update: "));
//             // Check exists
//             String check = "SELECT COUNT(*) FROM Trades WHERE TradeID = ?";
//             try (PreparedStatement ps = conn.prepareStatement(check)) {
//                 ps.setInt(1, id);
//                 try (ResultSet rs = ps.executeQuery()) {
//                     rs.next();
//                     if (rs.getInt(1) == 0) {
//                         System.out.println("No trade with ID " + id);
//                         return;
//                     }
//                 }
//             }

//             double newVolume = Double.parseDouble(readLine("New Volume: "));
//             double newPrice = Double.parseDouble(readLine("New Price: "));

//             String upd = "UPDATE Trades SET Volume = ?, Price = ? WHERE TradeID = ?";
//             try (PreparedStatement ps2 = conn.prepareStatement(upd)) {
//                 ps2.setBigDecimal(1, new java.math.BigDecimal(String.format("%.2f", newVolume)));
//                 ps2.setBigDecimal(2, new java.math.BigDecimal(String.format("%.2f", newPrice)));
//                 ps2.setInt(3, id);
//                 int rows = ps2.executeUpdate();
//                 System.out.println(rows + " row(s) updated.");
//             }
//         } catch (Exception e) {
//             System.out.println("Error updating trade: " + e.getMessage());
//         }
//     }

//     private static void deleteTrade() {
//         try (Connection conn = getConnection()) {
//             int id = Integer.parseInt(readLine("Enter TradeID to delete: "));
//             String confirm = readLine("Type YES to confirm deletion: ");
//             if (!"YES".equalsIgnoreCase(confirm)) {
//                 System.out.println("Deletion cancelled.");
//                 return;
//             }
//             String del = "DELETE FROM Trades WHERE TradeID = ?";
//             try (PreparedStatement ps = conn.prepareStatement(del)) {
//                 ps.setInt(1, id);
//                 int rows = ps.executeUpdate();
//                 System.out.println(rows + " row(s) deleted.");
//             }
//         } catch (Exception e) {
//             System.out.println("Error deleting trade: " + e.getMessage());
//         }
//     }

//     private static void searchTrades() {
//         try (Connection conn = getConnection()) {
//             String term = readLine("Search by Counterparty or Commodity (enter search text): ");
//             String sql = "SELECT TradeID, TradeDate, Counterparty, Commodity, Volume, Price, TradeType FROM Trades WHERE Counterparty LIKE ? OR Commodity LIKE ? ORDER BY TradeID";
//             try (PreparedStatement ps = conn.prepareStatement(sql)) {
//                 String like = "%" + term + "%";
//                 ps.setString(1, like);
//                 ps.setString(2, like);
//                 try (ResultSet rs = ps.executeQuery()) {
//                     System.out.println("\n-- Search Results --");
//                     System.out.printf("%-6s %-12s %-20s %-10s %-10s %-10s %-6s%n", "ID", "Date", "Counterparty", "Commodity", "Volume", "Price", "Type");
//                     boolean found = false;
//                     while (rs.next()) {
//                         found = true;
//                         System.out.printf("%-6d %-12s %-20s %-10s %-10s %-10s %-6s%n",
//                                 rs.getInt("TradeID"),
//                                 rs.getDate("TradeDate").toString(),
//                                 rs.getString("Counterparty"),
//                                 rs.getString("Commodity"),
//                                 rs.getBigDecimal("Volume").toPlainString(),
//                                 rs.getBigDecimal("Price").toPlainString(),
//                                 rs.getString("TradeType"));
//                     }
//                     if (!found) System.out.println("No records found.");
//                 }
//             }
//         } catch (Exception e) {
//             System.out.println("Error searching trades: " + e.getMessage());
//         }
//     }

//     private static String readLine(String prompt) {
//         System.out.print(prompt);
//         String s = scanner.nextLine();
//         if (s == null) return "";
//         return s.trim();
//     }
// }
import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class EnergyTradeApp {

    // ✅ Windows Authentication Connection String
    // - Make sure you use the right instance name if not default
    // - Example: localhost\\SQLEXPRESS OR localhost
    private static final String DB_URL =
        "jdbc:sqlserver://localhost:1433;"
        + "databaseName=EnergyTradingDB;"
        + "integratedSecurity=true;"
        + "encrypt=true;"
        + "trustServerCertificate=true;";

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Energy Trade Manager ===");
        try {
            // Load Driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found. Ensure mssql-jdbc jar is in lib/ and classpath.");
            return;
        }

        while (true) {
            printMenu();
            String choice = readLine("Enter choice: ");
            switch (choice) {
                case "1": addTrade(); break;
                case "2": viewAllTrades(); break;
                case "3": updateTrade(); break;
                case "4": deleteTrade(); break;
                case "5": searchTrades(); break;
                case "6": System.out.println("Goodbye!"); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n1) Add a Trade");
        System.out.println("2) View All Trades");
        System.out.println("3) Update Trade (Price/Volume)");
        System.out.println("4) Delete Trade");
        System.out.println("5) Search Trades (Counterparty / Commodity)");
        System.out.println("6) Exit");
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void addTrade() {
        try (Connection conn = getConnection()) {
            String dateStr = readLine("Trade Date (YYYY-MM-DD): ");
            java.sql.Date tradeDate = java.sql.Date.valueOf(LocalDate.parse(dateStr));

            String counterparty = readLine("Counterparty: ");
            String commodity = readLine("Commodity (e.g., Power): ");
            double volume = Double.parseDouble(readLine("Volume (e.g., 100.00): "));
            double price = Double.parseDouble(readLine("Price per unit (e.g., 45.50): "));
            String tradeType;
            while (true) {
                tradeType = readLine("Trade Type (BUY or SELL): ").toUpperCase();
                if (tradeType.equals("BUY") || tradeType.equals("SELL")) break;
                System.out.println("Type must be BUY or SELL.");
            }

            String sql = "INSERT INTO Trades (TradeDate, Counterparty, Commodity, Volume, Price, TradeType) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, tradeDate);
                ps.setString(2, counterparty);
                ps.setString(3, commodity);
                ps.setBigDecimal(4, new java.math.BigDecimal(String.format("%.2f", volume)));
                ps.setBigDecimal(5, new java.math.BigDecimal(String.format("%.2f", price)));
                ps.setString(6, tradeType);
                int rows = ps.executeUpdate();
                System.out.println(rows + " trade inserted.");
            }
        } catch (Exception e) {
            System.out.println("Error inserting trade: " + e.getMessage());
        }
    }

    private static void viewAllTrades() {
        String sql = "SELECT TradeID, TradeDate, Counterparty, Commodity, Volume, Price, TradeType FROM Trades ORDER BY TradeID";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\n-- All Trades --");
            System.out.printf("%-6s %-12s %-20s %-10s %-10s %-10s %-6s%n", "ID", "Date", "Counterparty", "Commodity", "Volume", "Price", "Type");
            while (rs.next()) {
                System.out.printf("%-6d %-12s %-20s %-10s %-10s %-10s %-6s%n",
                        rs.getInt("TradeID"),
                        rs.getDate("TradeDate").toString(),
                        rs.getString("Counterparty"),
                        rs.getString("Commodity"),
                        rs.getBigDecimal("Volume").toPlainString(),
                        rs.getBigDecimal("Price").toPlainString(),
                        rs.getString("TradeType"));
            }
        } catch (SQLException e) {
            System.out.println("Error reading trades: " + e.getMessage());
        }
    }

    private static void updateTrade() {
        try (Connection conn = getConnection()) {
            int id = Integer.parseInt(readLine("Enter TradeID to update: "));
            // Check exists
            String check = "SELECT COUNT(*) FROM Trades WHERE TradeID = ?";
            try (PreparedStatement ps = conn.prepareStatement(check)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        System.out.println("No trade with ID " + id);
                        return;
                    }
                }
            }

            double newVolume = Double.parseDouble(readLine("New Volume: "));
            double newPrice = Double.parseDouble(readLine("New Price: "));

            String upd = "UPDATE Trades SET Volume = ?, Price = ? WHERE TradeID = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(upd)) {
                ps2.setBigDecimal(1, new java.math.BigDecimal(String.format("%.2f", newVolume)));
                ps2.setBigDecimal(2, new java.math.BigDecimal(String.format("%.2f", newPrice)));
                ps2.setInt(3, id);
                int rows = ps2.executeUpdate();
                System.out.println(rows + " row(s) updated.");
            }
        } catch (Exception e) {
            System.out.println("Error updating trade: " + e.getMessage());
        }
    }

    private static void deleteTrade() {
        try (Connection conn = getConnection()) {
            int id = Integer.parseInt(readLine("Enter TradeID to delete: "));
            String confirm = readLine("Type YES to confirm deletion: ");
            if (!"YES".equalsIgnoreCase(confirm)) {
                System.out.println("Deletion cancelled.");
                return;
            }
            String del = "DELETE FROM Trades WHERE TradeID = ?";
            try (PreparedStatement ps = conn.prepareStatement(del)) {
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                System.out.println(rows + " row(s) deleted.");
            }
        } catch (Exception e) {
            System.out.println("Error deleting trade: " + e.getMessage());
        }
    }

    private static void searchTrades() {
        try (Connection conn = getConnection()) {
            String term = readLine("Search by Counterparty or Commodity: ");
            String sql = "SELECT TradeID, TradeDate, Counterparty, Commodity, Volume, Price, TradeType " +
                         "FROM Trades WHERE Counterparty LIKE ? OR Commodity LIKE ? ORDER BY TradeID";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String like = "%" + term + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("\n-- Search Results --");
                    System.out.printf("%-6s %-12s %-20s %-10s %-10s %-10s %-6s%n", "ID", "Date", "Counterparty", "Commodity", "Volume", "Price", "Type");
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        System.out.printf("%-6d %-12s %-20s %-10s %-10s %-10s %-6s%n",
                                rs.getInt("TradeID"),
                                rs.getDate("TradeDate").toString(),
                                rs.getString("Counterparty"),
                                rs.getString("Commodity"),
                                rs.getBigDecimal("Volume").toPlainString(),
                                rs.getBigDecimal("Price").toPlainString(),
                                rs.getString("TradeType"));
                    }
                    if (!found) System.out.println("No records found.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error searching trades: " + e.getMessage());
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        String s = scanner.nextLine();
        if (s == null) return "";
        return s.trim();
    }
}
