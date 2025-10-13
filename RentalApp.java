import java.util.ArrayList;
import java.util.Scanner;

public class RentalApp {
    private static Scanner input = new Scanner(System.in);

    // Temporary in memory storage
    private static ArrayList<Client> clients = new ArrayList<>();
    private static ArrayList<Equipment> equipmentList = new ArrayList<>();

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Manage Clients");
            System.out.println("2. Manage Equipment");
            System.out.println("3. Rentals & Deliveries");
            System.out.println("4. Reports (not implemented)");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    clientMenu();
                    break;
                case 2:
                    equipmentMenu();
                    break;
                case 3:
                    rentalMenu();
                    break;
                case 4:
                    System.out.println("Reports feature will be available in a later version.");
                    break;
                case 5:
                    System.out.println("Exiting system. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        input.close();
    }

    // Client Menu
    private static void clientMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- CLIENT MENU ---");
            System.out.println("1. Add Client");
            System.out.println("2. Edit Client");
            System.out.println("3. Delete Client");
            System.out.println("4. Search Client");
            System.out.println("5. View All Clients");
            System.out.println("6. Return to Main Menu");
            System.out.print("Choose an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    addClient();
                    break;
                case 2:
                    editClient();
                    break;
                case 3:
                    deleteClient();
                    break;
                case 4:
                    searchClient();
                    break;
                case 5:
                    viewAllClients();
                    break;
                case 6:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void addClient() {
        System.out.print("Enter client name: ");
        String name = input.nextLine();
        System.out.print("Enter contact info: ");
        String contact = input.nextLine();

        clients.add(new Client(name, contact));
        System.out.println("Client added successfully.");
    }

    private static void editClient() {
        System.out.print("Enter client name to edit: ");
        String name = input.nextLine();

        for (Client c : clients) {
            if (c.getName().equalsIgnoreCase(name)) {
                System.out.print("Enter new contact info: ");
                String newContact = input.nextLine();
                c.setContactInfo(newContact);
                System.out.println("Client updated successfully.");
                return;
            }
        }
        System.out.println("Client not found.");
    }

    private static void deleteClient() {
        System.out.print("Enter client name to delete: ");
        String name = input.nextLine();

        clients.removeIf(c -> c.getName().equalsIgnoreCase(name));
        System.out.println("Client removed (if existed).");
    }

    private static void searchClient() {
        System.out.print("Enter client name to search: ");
        String name = input.nextLine();

        for (Client c : clients) {
            if (c.getName().equalsIgnoreCase(name)) {
                System.out.println("Client found: " + c);
                return;
            }
        }
        System.out.println("Client not found.");
    }

    private static void viewAllClients() {
        if (clients.isEmpty()) {
            System.out.println("No clients available.");
            return;
        }
        for (Client c : clients) {
            System.out.println(c);
        }
    }

    // Equipment Menu
    private static void equipmentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- EQUIPMENT MENU ---");
            System.out.println("1. Add Equipment");
            System.out.println("2. Edit Equipment");
            System.out.println("3. Delete Equipment");
            System.out.println("4. Search Equipment");
            System.out.println("5. View All Equipment");
            System.out.println("6. Return to Main Menu");
            System.out.print("Choose an option: ");

            int choice = getIntInput();

            switch (choice) {
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
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void addEquipment() {
        System.out.print("Enter equipment name: ");
        String name = input.nextLine();
        System.out.print("Enter equipment type: ");
        String type = input.nextLine();

        equipmentList.add(new Equipment(name, type));
        System.out.println("Equipment added successfully.");
    }

    private static void editEquipment() {
        System.out.print("Enter equipment name to edit: ");
        String name = input.nextLine();

        for (Equipment e : equipmentList) {
            if (e.getName().equalsIgnoreCase(name)) {
                System.out.print("Enter new equipment type: ");
                String newType = input.nextLine();
                e.setType(newType);
                System.out.println("Equipment updated successfully.");
                return;
            }
        }
        System.out.println("Equipment not found.");
    }

    private static void deleteEquipment() {
        System.out.print("Enter equipment name to delete: ");
        String name = input.nextLine();

        equipmentList.removeIf(e -> e.getName().equalsIgnoreCase(name));
        System.out.println("Equipment removed (if existed).");
    }

    private static void searchEquipment() {
        System.out.print("Enter equipment name to search: ");
        String name = input.nextLine();

        for (Equipment e : equipmentList) {
            if (e.getName().equalsIgnoreCase(name)) {
                System.out.println("Equipment found: " + e);
                return;
            }
        }
        System.out.println("Equipment not found.");
    }

    private static void viewAllEquipment() {
        if (equipmentList.isEmpty()) {
            System.out.println("No equipment available.");
            return;
        }
        for (Equipment e : equipmentList) {
            System.out.println(e);
        }
    }

    // Rentals and Deliveries Menu
    private static void rentalMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- RENTALS & DELIVERIES ---");
            System.out.println("1. Rent Equipment");
            System.out.println("2. Return Equipment");
            System.out.println("3. Schedule Delivery");
            System.out.println("4. Schedule Pickup");
            System.out.println("5. Return to Main Menu");
            System.out.print("Choose an option: ");

            int choice = getIntInput();

            switch (choice) {
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
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void rentEquipment() {
        System.out.print("Enter client name: ");
        String clientName = input.nextLine();
        System.out.print("Enter equipment name: ");
        String equipmentName = input.nextLine();

        System.out.println("Equipment rented successfully to " + clientName + ".");
    }

    private static void returnEquipment() {
        System.out.print("Enter client name: ");
        String clientName = input.nextLine();
        System.out.print("Enter equipment name: ");
        String equipmentName = input.nextLine();

        System.out.println("Equipment returned successfully by " + clientName + ".");
    }

    private static void scheduleDelivery() {
        System.out.print("Enter equipment name for delivery: ");
        String equipmentName = input.nextLine();
        System.out.print("Enter client name: ");
        String clientName = input.nextLine();
        System.out.print("Enter delivery drone ID: ");
        String droneID = input.nextLine();

        System.out
                .println("Delivery scheduled: " + equipmentName + " to " + clientName + " via Drone " + droneID + ".");
    }

    private static void schedulePickup() {
        System.out.print("Enter equipment name for pickup: ");
        String equipmentName = input.nextLine();
        System.out.print("Enter client name: ");
        String clientName = input.nextLine();
        System.out.print("Enter pickup drone ID: ");
        String droneID = input.nextLine();

        System.out
                .println("Pickup scheduled: " + equipmentName + " from " + clientName + " via Drone " + droneID + ".");
    }

    // Utlilties
    private static int getIntInput() {
        while (true) {
            try {
                int value = Integer.parseInt(input.nextLine());
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}

// Enity Classes
class Client {
    private String name;
    private String contactInfo;

    public Client(String name, String contactInfo) {
        this.name = name;
        this.contactInfo = contactInfo;
    }

    public String getName() {
        return name;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    @Override
    public String toString() {
        return "Client{name='" + name + "', contactInfo='" + contactInfo + "'}";
    }
}

class Equipment {
    private String name;
    private String type;

    public Equipment(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Equipment{name='" + name + "', type='" + type + "'}";
    }
}