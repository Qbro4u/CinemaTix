package cinematix.model;

import cinematix.exception.SeatAlreadyBookedException;

public interface Bookable {
    double calculateTotal();
    boolean reserveSeat(int seatNumber) throws SeatAlreadyBookedException;
    boolean cancelBooking(String bookingCode);
    String generateTicketCode();
}