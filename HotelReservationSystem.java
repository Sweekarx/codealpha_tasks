import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * CodeAlpha Java Internship - Task 4
 * Hotel Reservation System
 *
 * Includes room search, categories, booking/cancellation, payment simulation,
 * booking details and file-based persistence.
 *
 * Compatible with Java 21. No external libraries required.
 */
public class HotelReservationSystem {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Path DATA_FILE = Paths.get("hotel_bookings.txt");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    enum RoomCategory {
        STANDARD(1500.00),
        DELUXE(2500.00),
        SUITE(4000.00);

        private final double pricePerDay;

        RoomCategory(double pricePerDay) {
            this.pricePerDay = pricePerDay;
        }

        double getPricePerDay() {
            return pricePerDay;
        }
    }

    static class Room {
        private final int number;
        private final RoomCategory category;

        Room(int number, RoomCategory category) {
            this.number = number;
            this.category = category;
        }

        int getNumber() {
            return number;
        }

        RoomCategory getCategory() {
            return category;
        }
    }

    static class Reservation {
        private final String bookingId;
        private final String guestName;
        private final int roomNumber;
        private final RoomCategory category;
        private final int days;
        private final String paymentMethod;
        private final String paymentStatus;
        private final LocalDateTime bookedAt;

        Reservation(String bookingId, String guestName, int roomNumber,
                    RoomCategory category, int days, String paymentMethod,
                    String paymentStatus, LocalDateTime bookedAt) {
            this.bookingId = bookingId;
            this.guestName = guestName;
            this.roomNumber = roomNumber;
            this.category = category;
            this.days = days;
            this.paymentMethod = paymentMethod;
            this.paymentStatus = paymentStatus;
            this.bookedAt = bookedAt;
        }

        double getTotalCost() {
            return category.getPricePerDay() * days;
        }

        String getBookingId() {
            return bookingId;
        }

        int getRoomNumber() {
            return roomNumber;
        }

        String toFileLine() {
            return String.join("|",
                    bookingId,
                    guestName.replace("|", " "),
                    String.valueOf(roomNumber),
                    category.name(),
                    String.valueOf(days),
                    paymentMethod,
                    paymentStatus,
                    bookedAt.toString());
        }

        void display() {
            System.out.println("----------------------------------------");
            System.out.println("Booking ID     : " + bookingId);
            System.out.println("Guest          : " + guestName);
            System.out.println("Room           : " + roomNumber);
            System.out.println("Category       : " + category);
            System.out.println("Days           : " + days);
            System.out.printf("Price / Day    : Rs. %.2f%n", category.getPricePerDay());
            System.out.printf("Total Bill     : Rs. %.2f%n", getTotalCost());
            System.out.println("Payment Method : " + paymentMethod);
            System.out.println("Payment Status : " + paymentStatus);
            System.out.println("Booked At      : " + bookedAt.format(TIME_FORMAT));
        }
    }

    private static final Map<Integer, Room> ROOMS = new LinkedHashMap<>();
    private static final Map<Integer, Reservation> RESERVATIONS = new LinkedHashMap<>();

    public static void main(String[] args) {
        initializeRooms();
        loadReservations();

        while (true) {
            System.out.println("\n========== HOTEL RESERVATION SYSTEM ==========");
            System.out.println("1. Search Available Rooms");
            System.out.println("2. Book Room");
            System.out.println("3. View Booking Details");
            System.out.println("4. View All Bookings");
            System.out.println("5. Cancel Booking");
            System.out.println("6. Exit");

            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> searchRooms();
                case 2 -> bookRoom();
                case 3 -> viewBookingDetails();
                case 4 -> viewAllBookings();
                case 5 -> cancelBooking();
                case 6 -> {
                    saveReservations();
                    System.out.println("Thank you for using the Hotel Reservation System.");
                    SCANNER.close();
                    return;
                }
                default -> System.out.println("Invalid choice. Please select 1-6.");
            }
        }
    }

    private static void initializeRooms() {
        // 12 rooms across the required categories.
        for (int room = 101; room <= 104; room++) {
            ROOMS.put(room, new Room(room, RoomCategory.STANDARD));
        }
        for (int room = 201; room <= 204; room++) {
            ROOMS.put(room, new Room(room, RoomCategory.DELUXE));
        }
        for (int room = 301; room <= 304; room++) {
            ROOMS.put(room, new Room(room, RoomCategory.SUITE));
        }
    }

    private static void searchRooms() {
        System.out.println("\nRoom Categories:");
        System.out.println("1. All");
        System.out.println("2. Standard - Rs. 1500/day");
        System.out.println("3. Deluxe   - Rs. 2500/day");
        System.out.println("4. Suite    - Rs. 4000/day");

        int choice = readInt("Select category: ");
        RoomCategory selected = null;

        if (choice >= 2 && choice <= 4) {
            selected = RoomCategory.values()[choice - 2];
        } else if (choice != 1) {
            System.out.println("Invalid category.");
            return;
        }

        System.out.println("\nAvailable Rooms:");
        boolean found = false;

        for (Room room : ROOMS.values()) {
            boolean categoryMatches =
                    selected == null || room.getCategory() == selected;
            boolean available = !RESERVATIONS.containsKey(room.getNumber());

            if (categoryMatches && available) {
                System.out.printf("Room %d | %-8s | Rs. %.2f/day%n",
                        room.getNumber(), room.getCategory(),
                        room.getCategory().getPricePerDay());
                found = true;
            }
        }

        if (!found) {
            System.out.println("No rooms are currently available for that category.");
        }
    }

    private static void bookRoom() {
        searchRooms();

        int roomNumber = readInt("\nEnter room number to book: ");
        Room room = ROOMS.get(roomNumber);

        if (room == null) {
            System.out.println("Invalid room number.");
            return;
        }

        if (RESERVATIONS.containsKey(roomNumber)) {
            System.out.println("This room is already booked.");
            return;
        }

        System.out.print("Enter guest name: ");
        String guestName = SCANNER.nextLine().trim();
        while (guestName.isEmpty()) {
            System.out.print("Guest name cannot be empty. Enter again: ");
            guestName = SCANNER.nextLine().trim();
        }

        int days = readPositiveInt("Enter number of days: ");

        double total = room.getCategory().getPricePerDay() * days;
        System.out.printf("Total amount: Rs. %.2f%n", total);

        String paymentMethod = choosePaymentMethod();
        if (paymentMethod == null) {
            System.out.println("Booking cancelled before payment.");
            return;
        }

        System.out.println("\nProcessing payment...");
        String paymentStatus = simulatePayment(paymentMethod);

        if (!paymentStatus.equals("PAID")) {
            System.out.println("Payment failed. Room was not booked.");
            return;
        }

        String bookingId = "BK" + System.currentTimeMillis();
        Reservation reservation = new Reservation(
                bookingId, guestName, roomNumber, room.getCategory(),
                days, paymentMethod, paymentStatus, LocalDateTime.now());

        RESERVATIONS.put(roomNumber, reservation);
        saveReservations();

        System.out.println("\nBooking successful!");
        reservation.display();
    }

    private static String choosePaymentMethod() {
        System.out.println("\nPayment Methods:");
        System.out.println("1. UPI");
        System.out.println("2. Card");
        System.out.println("3. Cash");

        int choice = readInt("Choose payment method: ");

        return switch (choice) {
            case 1 -> "UPI";
            case 2 -> "CARD";
            case 3 -> "CASH";
            default -> null;
        };
    }

    private static String simulatePayment(String paymentMethod) {
        // This is intentionally a simulation; no real payment is processed.
        System.out.println("Payment method: " + paymentMethod);
        System.out.println("Payment simulation completed successfully.");
        return "PAID";
    }

    private static void viewBookingDetails() {
        System.out.print("Enter booking ID: ");
        String bookingId = SCANNER.nextLine().trim();

        for (Reservation reservation : RESERVATIONS.values()) {
            if (reservation.getBookingId().equalsIgnoreCase(bookingId)) {
                reservation.display();
                return;
            }
        }

        System.out.println("Booking not found.");
    }

    private static void viewAllBookings() {
        if (RESERVATIONS.isEmpty()) {
            System.out.println("No current bookings.");
            return;
        }

        System.out.println("\n========== ALL BOOKINGS ==========");
        List<Reservation> list = new ArrayList<>(RESERVATIONS.values());
        list.sort(Comparator.comparingInt(Reservation::getRoomNumber));

        for (Reservation reservation : list) {
            reservation.display();
        }
        System.out.println("----------------------------------");
    }

    private static void cancelBooking() {
        System.out.print("Enter booking ID to cancel: ");
        String bookingId = SCANNER.nextLine().trim();

        Integer roomToRemove = null;

        for (Reservation reservation : RESERVATIONS.values()) {
            if (reservation.getBookingId().equalsIgnoreCase(bookingId)) {
                roomToRemove = reservation.getRoomNumber();
                break;
            }
        }

        if (roomToRemove == null) {
            System.out.println("Booking not found.");
            return;
        }

        RESERVATIONS.remove(roomToRemove);
        saveReservations();
        System.out.println("Booking " + bookingId + " cancelled successfully.");
        System.out.println("Room " + roomToRemove + " is available again.");
    }

    private static void loadReservations() {
        if (!Files.exists(DATA_FILE)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(DATA_FILE)) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);

                if (parts.length != 8) {
                    continue;
                }

                try {
                    Reservation reservation = new Reservation(
                            parts[0],
                            parts[1],
                            Integer.parseInt(parts[2]),
                            RoomCategory.valueOf(parts[3]),
                            Integer.parseInt(parts[4]),
                            parts[5],
                            parts[6],
                            LocalDateTime.parse(parts[7]));

                    if (ROOMS.containsKey(reservation.getRoomNumber())) {
                        RESERVATIONS.put(reservation.getRoomNumber(), reservation);
                    }
                } catch (IllegalArgumentException ignored) {
                    // Ignore malformed records and continue loading valid ones.
                }
            }

            System.out.println("Saved bookings loaded from " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Could not load saved bookings: " + e.getMessage());
        }
    }

    private static void saveReservations() {
        try (BufferedWriter writer = Files.newBufferedWriter(DATA_FILE)) {
            for (Reservation reservation : RESERVATIONS.values()) {
                writer.write(reservation.toFileLine());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save bookings: " + e.getMessage());
        }
    }

    private static int readInt(String message) {
        while (true) {
            System.out.print(message);
            String input = SCANNER.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ignored) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static int readPositiveInt(String message) {
        while (true) {
            int value = readInt(message);
            if (value > 0) {
                return value;
            }
            System.out.println("Value must be greater than 0.");
        }
    }
}
