package com.example.BookingApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.BookingApp.entity.Payment;
import com.example.BookingApp.entity.Booking;
import com.example.BookingApp.entityenums.PaymentStatus;
import com.example.BookingApp.entityenums.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	boolean existsByBookingId(Long bookingId);

	List<Payment> findByBooking_User_UsernameOrderByCreatedAtDesc(String username);

	Optional<Payment> findByBookingId(Long bookingId);
    
    Optional<Payment> findByPaymentReference(String paymentReference);
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    Optional<Payment> findByBooking(Booking booking);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);
    
    @Query("SELECT p FROM Payment p WHERE p.booking.user.id = :userId ORDER BY p.processedAt DESC")
    List<Payment> findByUserIdOrderByProcessedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Payment p WHERE p.processedAt >= :startDate AND p.processedAt <= :endDate")
    List<Payment> findByProcessedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt < :expiredTime")
    List<Payment> findExpiredPayments(@Param("status") PaymentStatus status, 
                                    @Param("expiredTime") LocalDateTime expiredTime);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentStatus status);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND p.processedAt >= :startDate AND p.processedAt <= :endDate")
    Double sumAmountByStatusAndDateRange(@Param("status") PaymentStatus status,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}