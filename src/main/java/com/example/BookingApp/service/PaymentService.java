package com.example.BookingApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BookingApp.dto.event.PaymentDto;
import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.entity.Booking;
import com.example.BookingApp.entity.Payment;
import com.example.BookingApp.entityenums.BookingStatus;
import com.example.BookingApp.entityenums.PaymentStatus;
import com.example.BookingApp.repository.BookingRepository;
import com.example.BookingApp.repository.PaymentRepository;
import com.example.BookingApp.exception.BookingException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private BookingService bookingService;
    
    @Transactional
    public PaymentDto processPayment(PaymentDto paymentDto, UserResponse currentUser) {
        Optional<Booking> bookingOpt = bookingRepository.findById(paymentDto.getBookingId());
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }
        
        Booking booking = bookingOpt.get();
        
        if (!booking.getUser().getId().equals(currentUser.id())) {
            throw new RuntimeException("Unauthorized access to booking");
        }
        
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Booking is not in a payable state");
        }
        
        if (paymentRepository.existsByBookingId(booking.getId())) {
            throw new RuntimeException("Payment already processed for this booking");
        }
        
        if (!paymentDto.getAmount().equals(booking.getTotalAmount())) {
            throw new RuntimeException("Payment amount does not match booking total");
        }
        
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentReference(generatePaymentReference());
        payment.setAmount(paymentDto.getAmount());
        payment.setCurrency("USD");
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        
        boolean paymentSuccessful = processPaymentWithGateway(paymentDto);
        
        if (paymentSuccessful) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(UUID.randomUUID().toString());
            payment.setProcessedAt(LocalDateTime.now());
            payment.setGatewayResponse("SUCCESS");
            
            try {
                bookingService.confirmBooking(booking.getId(), currentUser.id());
            } catch (BookingException e) {
                throw new RuntimeException("Failed to confirm booking: " + e.getMessage());
            }
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
    
    @Transactional
    public PaymentDto refundPayment(Long paymentId, UserResponse currentUser) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }
        
        Payment payment = paymentOpt.get();
        
        if (!payment.getBooking().getUser().getId().equals(currentUser.id())) {
            throw new RuntimeException("Unauthorized access to payment");
        }
        
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Only completed payments can be refunded");
        }
        
        boolean refundSuccessful = processRefundWithGateway(payment);
        
        if (refundSuccessful) {
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setGatewayResponse("REFUND_SUCCESS");
            
            try {
                bookingService.cancelBooking(payment.getBooking().getId(), currentUser.id());
            } catch (BookingException e) {
                throw new RuntimeException("Failed to cancel booking: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Refund processing failed");
        }
        
        payment = paymentRepository.save(payment);
        
        return convertToDto(payment);
    }
    
    private boolean processPaymentWithGateway(PaymentDto paymentDto) {
        try {
            Thread.sleep(1000); 
            
            if (paymentDto.getCardNumber() == null || paymentDto.getCardNumber().length() < 10) {
                return false;
            }
            
            if (paymentDto.getCvv() == null || paymentDto.getCvv().length() != 3) {
                return false;
            }
            
            return Math.random() > 0.1;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    private boolean processRefundWithGateway(Payment payment) {
        try {
            Thread.sleep(500); 
            return Math.random() > 0.05; 
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