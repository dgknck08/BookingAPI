package com.example.BookingApp.dto;
import com.booking.entity.PaymentMethod;
import com.booking.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
public class PaymentDto {
    private Long id;
    
    @NotNull(message = "Booking ID is required")
    private Long bookingId;
    
    private String paymentReference;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String currency;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime processedAt;
    
    // Card details for payment processing (should be encrypted in real app)
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
}
