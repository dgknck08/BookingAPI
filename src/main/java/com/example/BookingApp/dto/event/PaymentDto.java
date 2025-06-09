package com.example.BookingApp.dto.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.BookingApp.entityenums.PaymentMethod;
import com.example.BookingApp.entityenums.PaymentStatus;


@Getter
@Setter
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
