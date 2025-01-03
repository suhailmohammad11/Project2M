package taxCalciWithDB;
import java.sql.*;
import java.util.*;

// Custom Exceptions
class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}

// Base Tax Class
abstract class Tax {
    protected static int idCounter = 1;
    protected int id;
    protected double tax;

    public Tax() {
        this.id = idCounter++;
        this.tax = 0.0; // Initially, the tax is not calculated
    }

    public abstract void calculateTax();

    public abstract void displayDetails();

    public double getTax() {
        return tax;
    }
}

// PropertyTax Class
class PropertyTax extends Tax {
    private int baseValue;
    private int builtUpArea;
    private int age;
    private char location;

    public PropertyTax(int baseValue, int builtUpArea, int age, char location) {
        super();
        this.baseValue = baseValue;
        this.builtUpArea = builtUpArea;
        this.age = age;
        this.location = location;
    }

    @Override
    public void calculateTax() {
        if (location == 'Y' || location == 'y') {
            this.tax = (baseValue * builtUpArea * age) + (0.5 * builtUpArea);
        } else if (location == 'N' || location == 'n') {
            this.tax = baseValue * builtUpArea * age;
        }
    }

    @Override
    public void displayDetails() {
        System.out.printf("%5d %15d %10d %10c %10.2f\n", id, builtUpArea, baseValue, location, tax);
    }

    public int getBaseValue() {
        return baseValue;
    }

    public int getBuiltUpArea() {
        return builtUpArea;
    }

    public int getAge() {
        return age;
    }

    public char getLocation() {
        return location;
    }
}

// VehicleTax Class
class VehicleTax extends Tax {
    private int registrNo;
    private String brand;
    private int velocity;
    private int seatCapacity;
    private int type; // 1 for Petrol, 2 for Diesel, 3 for CNG
    private int price; // Purchase cost

    public VehicleTax(int registrNo, String brand, int velocity, int seatCapacity) {
        super();
        this.registrNo = registrNo;
        this.brand = brand;
        this.velocity = velocity;
        this.seatCapacity = seatCapacity;
    }

    public void setTypeAndPrice(int type, int price) {
        this.type = type;
        this.price = price;
    }

    @Override
    public void calculateTax() {
        switch (type) {
            case 1 -> this.tax = velocity + seatCapacity + (0.01 * price); // Petrol
            case 2 -> this.tax = velocity + seatCapacity + (0.11 * price); // Diesel
            case 3 -> this.tax = velocity + seatCapacity + (0.12 * price); // CNG
            default -> throw new IllegalArgumentException("Invalid fuel type!");
        }
    }

    @Override
    public void displayDetails() {
        String fuelType = switch (type) {
            case 1 -> "Petrol";
            case 2 -> "Diesel";
            case 3 -> "CNG";
            default -> "Unknown";
        };

        System.out.printf(
            "%5d %15s %10d %10d %10s %10d %10.2f\n", 
            registrNo, brand, velocity, seatCapacity, fuelType, price, tax
        );
    }

    public int getRegistrationNumber() {
        return registrNo;
    }

    public String getBrand() {
        return brand;
    }

    public int getVelocity() {
        return velocity;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }

    public int getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }
}

// Welcome Class
class Welcome {
    public static boolean input() {
        Scanner sc = new Scanner(System.in);
        System.out.println("+-------------------------------------+");
        System.out.println("|   WELCOME TO TAX CALCULATION APP    |");
        System.out.println("+-------------------------------------+");
        System.out.print("USERNAME: ");
        String username = sc.nextLine();
        System.out.print("PASSWORD: ");
        String password = sc.nextLine();

        if (username.equals("admin") && password.equals("admin123")) {
            System.out.println("Authentication Successful\n");
            return true;
        } else {
            System.out.println("Invalid Credentials. Exiting.");
            return false;
        }
    }
}

// Main Application Class
public class TaxApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        if (!Welcome.input()) {
            System.exit(0); // Exit if authentication fails
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/tax_db", "root", "root")) {
            while (true) {
                try {
                    System.out.println("\n+-------------------------------------+");
                    System.out.println("|   MAIN MENU                         |");
                    System.out.println("+-------------------------------------+");
                    System.out.println("1. Property Tax Menu");
                    System.out.println("2. Vehicle Tax Menu");
                    System.out.println("3. Total Tax");
                    System.out.println("4. Exit");
                    System.out.print("Select an option: ");
                    int mainChoice = sc.nextInt();

                    switch (mainChoice) {
                        case 1 -> propertyTaxMenu(sc, conn);
                        case 2 -> vehicleTaxMenu(sc, conn);
                        case 3 -> total(conn);
                        case 4 -> {
                            System.out.println("Exiting the application. Thank you!");
                            System.exit(0);
                        }
                        default -> throw new InvalidInputException("Invalid choice! Please select a valid option.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Error: Invalid input type. Please enter a valid number.");
                    sc.nextLine(); // Clear invalid input
                } catch (InvalidInputException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void propertyTaxMenu(Scanner sc, Connection conn) throws InvalidInputException {
        while (true) {
            System.out.println("\n+-------------------------------------+");
            System.out.println("|   PROPERTY TAX MENU                 |");
            System.out.println("+-------------------------------------+");
            System.out.println("1. Add Property Details");
            System.out.println("2. Calculate Property Tax");
            System.out.println("3. Display All Properties");
            System.out.println("4. Back to Main Menu");
            System.out.print("Select an option: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter Base Value: ");
                    int baseValue = sc.nextInt();
                    System.out.print("Enter Built-Up Area: ");
                    int builtUpArea = sc.nextInt();
                    System.out.print("Enter Age: ");
                    int age = sc.nextInt();
                    System.out.print("Is Located in City? (Y/N): ");
                    char location = sc.next().charAt(0);

                    try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO property_tax (base_value, built_up_area, age, location) VALUES (?, ?, ?, ?)");) {
                        stmt.setInt(1, baseValue);
                        stmt.setInt(2, builtUpArea);
                        stmt.setInt(3, age);
                        stmt.setString(4, String.valueOf(location));
                        stmt.executeUpdate();
                        System.out.println("Property added successfully!");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case 2 -> {
                    System.out.print("Enter Property ID to calculate tax: ");
                    int id = sc.nextInt();
                    try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM property_tax WHERE id = ?")) {
                        stmt.setInt(1, id);
                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {
                            int baseValue = rs.getInt("base_value");
                            int builtUpArea = rs.getInt("built_up_area");
                            int age = rs.getInt("age");
                            char location = rs.getString("location").charAt(0);

                            PropertyTax property = new PropertyTax(baseValue, builtUpArea, age, location);
                            property.calculateTax();
                            double calculatedTax = property.getTax();

                            try (PreparedStatement updateStmt = conn.prepareStatement(
                                "UPDATE property_tax SET tax = ? WHERE id = ?")) {
                                updateStmt.setDouble(1, calculatedTax);
                                updateStmt.setInt(2, id);
                                updateStmt.executeUpdate();
                                System.out.println("Property tax calculated successfully!");
                            }
                        } else {
                            System.out.println("Property ID not found.");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case 3 -> {
                    System.out.println("\n+--------------------------------------------------+");
                    System.out.printf("%5s %15s %10s %10s %10s\n", "ID", "BuiltupArea", "Baseprice", "In City", "Property Tax");
                    System.out.println("+--------------------------------------------------+");

                    try (Statement stmt = conn.createStatement()) {
                        ResultSet rs = stmt.executeQuery("SELECT * FROM property_tax");

                        while (rs.next()) {
                            int id = rs.getInt("id");
                            int builtUpArea = rs.getInt("built_up_area");
                            int baseValue = rs.getInt("base_value");
                            char location = rs.getString("location").charAt(0);
                            double tax = rs.getDouble("tax");

                            System.out.printf("%5d %15d %10d %10c %10.2f\n", id, builtUpArea, baseValue, location, tax);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case 4 -> {
                    return;
                }
                default -> throw new InvalidInputException("Invalid choice! Please select a valid option.");
            }
        }
    }

    private static void vehicleTaxMenu(Scanner sc, Connection conn) throws InvalidInputException {
        while (true) {
            System.out.println("\n+-------------------------------------+");
            System.out.println("|   VEHICLE TAX MENU                  |");
            System.out.println("+-------------------------------------+");
            System.out.println("1. Add Vehicle Details");
            System.out.println("2. Calculate Vehicle Tax");
            System.out.println("3. Display All Vehicles");
            System.out.println("4. Back to Main Menu");
            System.out.print("Select an option: ");

            int choice = sc.nextInt();
            switch (choice) {
                case 1 -> {
                    System.out.println("Enter Registration Number: ");
                    int registrNo = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter Brand: ");
                    String brand = sc.nextLine();
                    System.out.print("Enter Velocity: ");
                    int velocity = sc.nextInt();
                    System.out.print("Enter Seat Capacity: ");
                    int seatCapacity = sc.nextInt();

                    try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO vehicle_tax (registration_number, brand, velocity, seat_capacity) VALUES (?, ?, ?, ?)");) {
                        stmt.setInt(1, registrNo);
                        stmt.setString(2, brand);
                        stmt.setInt(3, velocity);
                        stmt.setInt(4, seatCapacity);
                        stmt.executeUpdate();
                        System.out.println("Vehicle added successfully!");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case 2 -> {
                    System.out.print("Enter Registration Number to calculate tax: ");
                    int registrNo1 = sc.nextInt();

                    try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM vehicle_tax WHERE registration_number = ?")) {
                        stmt.setInt(1, registrNo1);
                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {
                            int velocity = rs.getInt("velocity");
                            int seatCapacity = rs.getInt("seat_capacity");
                           
                            System.out.print("Enter Type of Vehicle (1 - Petrol, 2 - Diesel, 3 - CNG): ");
                            int type = sc.nextInt();
                            System.out.print("Enter Purchase Cost: ");
                            int price = sc.nextInt();

                            VehicleTax vehicle = new VehicleTax(registrNo1, rs.getString("brand"), velocity, seatCapacity);
                            vehicle.setTypeAndPrice(type, price);
                            vehicle.calculateTax();
                            double calculatedTax = vehicle.getTax();

                            try (PreparedStatement updateStmt = conn.prepareStatement(
                                "UPDATE vehicle_tax SET type = ?, price = ?, tax = ? WHERE registration_number = ?")) {
                                updateStmt.setInt(1, type);
                                updateStmt.setInt(2, price);
                                updateStmt.setDouble(3, calculatedTax);
                                updateStmt.setInt(4, registrNo1);
                                updateStmt.executeUpdate();
                                System.out.println("Vehicle tax calculated successfully!");
                            }
                        } else {
                            System.out.println("Registration Number not found.");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case 3 -> {
                    System.out.println("\n+---------------------------------------------------------------------+");
                    System.out.printf("%5s %15s %10s %10s %10s %10s %10s\n", 
                                      "RegNo", "Brand", "Velocity", "Seats", "Fuel Type", "Price", "Tax");
                    System.out.println("+---------------------------------------------------------------------+");

                    try (Statement stmt = conn.createStatement()) {
                        ResultSet rs = stmt.executeQuery("SELECT * FROM vehicle_tax");

                        while (rs.next()) {
                            int registrNo = rs.getInt("registration_number");
                            String brand = rs.getString("brand");
                            int velocity = rs.getInt("velocity");
                            int seatCapacity = rs.getInt("seat_capacity");
                            int type = rs.getInt("type");
                            int price = rs.getInt("price");
                            double tax = rs.getDouble("tax");

                            String fuelType = switch (type) {
                                case 1 -> "Petrol";
                                case 2 -> "Diesel";
                                case 3 -> "CNG";
                                default -> "Unknown";
                            };

                            System.out.printf("%5d %15s %10d %10d %10s %10d %10.2f\n", 
                                              registrNo, brand, velocity, seatCapacity, fuelType, price, tax);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case 4 -> {
                    return;
                }
                default -> throw new InvalidInputException("Invalid choice! Please select a valid option.");
            }
        }
    }

    private static void total(Connection conn) {
        try {
            double totalPropertyTax = 0, totalVehicleTax = 0;

            // Calculate total property tax
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT SUM(tax) AS total FROM property_tax");
                if (rs.next()) {
                    totalPropertyTax = rs.getDouble("total");
                }
            }

            // Calculate total vehicle tax
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT SUM(tax) AS total FROM vehicle_tax");
                if (rs.next()) {
                    totalVehicleTax = rs.getDouble("total");
                }
            }

            System.out.println("\n+-------------------------------------+");
            System.out.printf("Total Property Tax: %.2f\n", totalPropertyTax);
            System.out.printf("Total Vehicle Tax: %.2f\n", totalVehicleTax);
            System.out.printf("Total Tax Payable: %.2f\n", totalPropertyTax + totalVehicleTax);
            System.out.println("+-------------------------------------+");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
