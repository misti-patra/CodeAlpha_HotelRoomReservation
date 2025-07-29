import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

// Room Class
class Room implements Serializable {
    private static final long serialVersionUID = 1L; // Added for serialization
    private int roomNumber;
    private String category; // e.g., Standard, Deluxe, Suite
    private boolean isAvailable;
    private double price; // Price per night

    public Room(int roomNumber, String category, double price) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.isAvailable = true; // Rooms are available by default
        this.price = price;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getCategory() {
        return category;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public double getPrice() {
        return price;
    }
}

// Reservation Class
class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    private int reservationId;
    private Room room;
    private String customerName;
    private Date startDate;
    private Date endDate;

    public Reservation(int reservationId, Room room, String customerName, Date startDate, Date endDate) {
        this.reservationId = reservationId;
        this.room = room;
        this.customerName = customerName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getReservationId() {
        return reservationId;
    }

    public Room getRoom() {
        return room;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public long getNumberOfNights() {
        return (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24);
    }
}

// Hotel Class
class Hotel {
    private List<Room> rooms;
    private List<Reservation> reservations;

    public Hotel() {
        rooms = new ArrayList<>();
        reservations = new ArrayList<>();
        initializeRooms();
    }

    private void initializeRooms() {
        // Add rooms to the hotel with prices
        rooms.add(new Room(101, "Standard", 100.0));
        rooms.add(new Room(102, "Deluxe", 150.0));
        rooms.add(new Room(201, "Suite", 200.0));
        // Add more rooms as needed
    }

    public List<Room> searchAvailableRooms() {
        List<Room> availableRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (room.isAvailable()) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    public boolean bookRoom(Room room, String customerName, Date startDate, Date endDate) {
        if (room.isAvailable()) {
            room.setAvailable(false);
            Reservation reservation = new Reservation(reservations.size() + 1, room, customerName, startDate, endDate);
            reservations.add(reservation);
            BookingManager.saveReservations(reservations); // Save to file

            // Process payment
            double totalAmount = room.getPrice() * reservation.getNumberOfNights();
            if (processPayment(totalAmount)) {
                System.out.println("Payment of $" + totalAmount + " processed successfully.");
                return true; // Booking successful
            } else {
                System.out.println("Payment failed. Booking not completed.");
                room.setAvailable(true); // Revert room availability
                reservations.remove(reservation); // Remove reservation
                return false; // Booking failed
            }
        }
        return false; // Room not available
    }

    public void cancelReservation(int reservationId) {
        for (Reservation reservation : reservations) {
            if (reservation.getReservationId() == reservationId) {
                reservation.getRoom().setAvailable(true); // Make room available again
                reservations.remove(reservation);
                BookingManager.saveReservations(reservations); // Save to file
                System.out.println("Reservation cancelled successfully.");
                return;
            }
        }
        System.out.println("Reservation ID not found.");
    }

    public void viewReservations() {
        for (Reservation reservation : reservations) {
            System.out.println("Reservation ID: " + reservation.getReservationId() +
                    ", Room Number: " + reservation.getRoom().getRoomNumber() +
                    ", Customer Name: " + reservation.getCustomerName() +
                    ", Start Date: " + reservation.getStartDate() +
                    ", End Date: " + reservation.getEndDate());
        }
    }

    private boolean processPayment(double amount) {
        // Simulate payment processing
        System.out.println("Processing payment of $" + amount);
        // Here you can add more complex payment logic if needed
        return true; // Assume payment is successful
    }

    public void loadReservations() {
        this.reservations = BookingManager.loadReservations();
    }
}

// BookingManager Class
class BookingManager {
    public static void saveReservations(List<Reservation> reservations) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("reservations.dat"))) {
            oos.writeObject(reservations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Reservation> loadReservations() {
        List<Reservation> reservations = new ArrayList<>();
        File file = new File("reservations.dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                reservations = (List<Reservation>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return reservations;
    }
}

// Main Class
public class HotelReservationSystem {
    public static void main(String[] args) {
        Hotel hotel = new Hotel();
        Scanner scanner = new Scanner(System.in);

        // Load existing reservations
        hotel.loadReservations();

        while (true) {
            System.out.println("1. Search Available Rooms");
            System.out.println("2. Book Room");
            System.out.println("3. Cancel Reservation");
            System.out.println("4. View Reservations");
            System.out.println("5. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    List<Room> availableRooms = hotel.searchAvailableRooms();
                    if (availableRooms.isEmpty()) {
                        System.out.println("No available rooms.");
                    } else {
                        System.out.println("Available Rooms:");
                        for (Room room : availableRooms) {
                            System.out.println("Room Number: " + room.getRoomNumber() + ", Category: " + room.getCategory() + ", Price: $" + room.getPrice());
                        }
                    }
                    break;
                case 2:
                    System.out.print("Enter Room Number: ");
                    int roomNumber = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    System.out.print("Enter Customer Name: ");
                    String customerName = scanner.nextLine();
                    System.out.print("Enter Start Date (yyyy-mm-dd): ");
                    String startDateStr = scanner.nextLine();
                    System.out.print("Enter End Date (yyyy-mm-dd): ");
                    String endDateStr = scanner.nextLine();

                    // Convert string to Date
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date startDate = null;
                    Date endDate = null;
                    try {
                        startDate = dateFormat.parse(startDateStr);
                        endDate = dateFormat.parse(endDateStr);
                    } catch (ParseException e) {
                        System.out.println("Invalid date format. Please use yyyy-mm-dd.");
                        break;
                    }

                    Room roomToBook = null;
                    for (Room room : hotel.searchAvailableRooms()) {
                        if (room.getRoomNumber() == roomNumber) {
                            roomToBook = room;
                            break;
                        }
                    }

                    if (roomToBook != null) {
                        if (hotel.bookRoom(roomToBook, customerName, startDate, endDate)) {
                            System.out.println("Room booked successfully.");
                        } else {
                            System.out.println("Room is not available.");
                        }
                    } else {
                        System.out.println("Room not found.");
                    }
                    break;
                case 3:
                    System.out.print("Enter Reservation ID to cancel: ");
                    int reservationId = scanner.nextInt();
                    hotel.cancelReservation(reservationId);
                    break;
                case 4:
                    hotel.viewReservations();
                    break;
                case 5:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}