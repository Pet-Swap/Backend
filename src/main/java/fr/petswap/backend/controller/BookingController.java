package fr.petswap.backend.controller;

import fr.petswap.backend.dao.jpa.Booking;
import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dto.BookingDto;
import fr.petswap.backend.dto.CreateBookingRequest;
import fr.petswap.backend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(
            @RequestBody CreateBookingRequest request,
            Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        BookingDto booking = bookingService.createBooking(currentUser.getId(), request);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingDto> confirmBooking(
            @PathVariable Integer bookingId,
            Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        BookingDto booking = bookingService.confirmBooking(currentUser.getId(), bookingId);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/reject")
    public ResponseEntity<BookingDto> rejectBooking(
            @PathVariable Integer bookingId,
            Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        BookingDto booking = bookingService.rejectBooking(currentUser.getId(), bookingId);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/complete")
    public ResponseEntity<BookingDto> completeBooking(
            @PathVariable Integer bookingId,
            Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        BookingDto booking = bookingService.completeBooking(currentUser.getId(), bookingId);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingDto> cancelBooking(
            @PathVariable Integer bookingId,
            Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        BookingDto booking = bookingService.cancelBooking(currentUser.getId(), bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Integer bookingId) {
        BookingDto booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getMyBookings(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<BookingDto> bookings = bookingService.getBookingsForUser(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<BookingDto>> getPendingBookings(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<BookingDto> bookings = bookingService.getBookingsForUserByStatus(
            currentUser.getId(), Booking.BookingStatus.PENDING);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/confirmed")
    public ResponseEntity<List<BookingDto>> getConfirmedBookings(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<BookingDto> bookings = bookingService.getBookingsForUserByStatus(
            currentUser.getId(), Booking.BookingStatus.CONFIRMED);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<BookingDto>> getCompletedBookings(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<BookingDto> bookings = bookingService.getBookingsForUserByStatus(
            currentUser.getId(), Booking.BookingStatus.COMPLETED);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/as-pet-sitter")
    public ResponseEntity<List<BookingDto>> getBookingsAsPetSitter(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<BookingDto> bookings = bookingService.getBookingsAsPetSitter(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/as-owner")
    public ResponseEntity<List<BookingDto>> getBookingsAsOwner(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<BookingDto> bookings = bookingService.getBookingsAsOwner(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/rebook")
    public ResponseEntity<BookingDto> createRebooking(
            @RequestBody fr.petswap.backend.dto.RebookingRequest request,
            Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        BookingDto booking = bookingService.createRebooking(currentUser.getId(), request);
        return ResponseEntity.ok(booking);
    }
}
