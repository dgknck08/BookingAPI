package com.example.BookingApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BookingApp.dto.event.PaymentDto;
import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.entity.Booking;
import com.example.BookingApp.entity.Payment;
import com.example.BookingApp.entityenums.BookingStatus;
import com.example.BookingApp.entityenums.PaymentStatus;
import com.example.BookingApp.repository.BookingRepository;
import com.example.BookingApp.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private BookingService bookingService;
    
    @Transactional
    public PaymentDto processPayment(PaymentDto paymentDto, UserDto currentUser) {
        // Validate booking
        Optional<Booking> bookingOpt = bookingRepository.findById(paymentDto.getBookingId());
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }
        
        Booking booking = bookingOpt.get();
        
        // Validate user owns the booking
        if (!booking.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new RuntimeException("Unauthorized access to booking");
        }
        
        // Check if booking is in correct status
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Booking is not in a payable state");
        }
        
        // Check if payment already exists
        if (paymentRepository.existsByBookingId(booking.getId())) {
            throw new RuntimeException("Payment already processed for this booking");
        }
        
        // Validate amount
        if (!paymentDto.getAmount().equals(booking.getTotalAmount())) {
            throw new RuntimeException("Payment amount does not match booking total");
        }
        
        // Create payment record
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentReference(generatePaymentReference());
        payment.setAmount(paymentDto.getAmount());
        payment.setCurrency("USD");
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        
        // Process payment (mock implementation)
        boolean paymentSuccessful = processPaymentWithGateway(paymentDto);
        
        if (paymentSuccessful) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(UUID.randomUUID().toString());
            payment.setProcessedAt(LocalDateTime.now());
            payment.setGatewayResponse("SUCCESS");
            
            // Confirm the booking
            bookingService.confirmBooking(booking.getId(), currentUser);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setGatewayResponse("PAYMENT_DECLINED");
        }
        
        payment = paymentRepository.save(payment);
        
        return convertToDto(payment);
    }
    
    public Optional<PaymentDto> getPaymentByBookingId(Long bookingId) {
        Optional<Payment> paymentOpt = paymentRepository.findByBookingId(bookingId);
        return paymentOpt.map(this::convertToDto);
    }
    
    public Optional<PaymentDto> getPaymentByReference(String paymentReference) {
        Optional<Payment> paymentOpt = paymentRepository.findByPaymentReference(paymentReference);
        return paymentOpt.map(this::convertToDto);
    }
    
    public List<PaymentDto> getUserPayments(UserDto currentUser) {
        List<Payment> payments = paymentRepository.findByBooking_User_UsernameOrderByCreatedAtDesc(currentUser.getUsername());
        return payments.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Transactional
    public PaymentDto refundPayment(Long paymentId, UserDto currentUser) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }
        
        Payment payment = paymentOpt.get();
        
        // Validate user owns the payment
        if (!payment.getBooking().getUser().getUsername().equals(currentUser.getUsername())) {
            throw new RuntimeException("Unauthorized access to payment");
        }
        
        // Check if payment can be refunded
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Only completed payments can be refunded");
        }
        
        // Process refund (mock implementation)
        boolean refundSuccessful = processRefundWithGateway(payment);
        
        if (refundSuccessful) {
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setGatewayResponse("REFUND_SUCCESS");
            
            // Cancel the associated booking
            bookingService.cancelBooking(payment.getBooking().getId(), currentUser);
        } else {
            throw new RuntimeException("Refund processing failed");
        }
        
        payment = paymentRepository.save(payment);
        
        return convertToDto(payment);
    }
    
    private boolean processPaymentWithGateway(PaymentDto paymentDto) {
        // Mock payment gateway integration
        // In real implementation, integrate with payment providers like Stripe, PayPal, etc.
        
        // Simulate payment processing
        try {
            Thread.sleep(1000); // Simulate network delay
            
            // Mock validation rules
            if (paymentDto.getCardNumber() == null || paymentDto.getCardNumber().length() < 10) {
                return false;
            }
            
            if (paymentDto.getCvv() == null || paymentDto.getCvv().length() != 3) {
                return false;
            }
            
            // 90% success rate for demo
            return Math.random() > 0.1;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    private boolean processRefundWithGateway(Payment payment) {
        // Mock refund processing
        try {
            Thread.sleep(500); // Simulate network delay
            return Math.random() > 0.05; // 95% success rate for refunds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    private String generatePaymentReference() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private PaymentDto convertToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setBookingId(payment.getBooking().getId());
        dto.setPaymentReference(payment.getPaymentReference());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setProcessedAt(payment.getProcessedAt());
        return dto;
    }
}