package com.example.BookingApp.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @GetMapping
    public String getBookings() {
        return "List of bookings (only for authenticated users)";
    }

    @PostMapping
    public String createBooking() {
        return "Booking created!";
    }
}
