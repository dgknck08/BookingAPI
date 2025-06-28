package com.example.BookingApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.BookingApp.entity.Booking;
import com.example.BookingApp.entity.User;
import com.example.BookingApp.entityenums.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUserOrderByBookedAtDesc(User user);
    
    List<Booking> findByStatus(BookingStatus status);
    
    Optional<Booking> findByBookingReference(String bookingReference);
    
    @Query("SELECT b FROM Booking b WHERE b.reservedUntil < :now AND b.status = :status")
    List<Booking> findExpiredReservations(@Param("now") LocalDateTime now, @Param("status") BookingStatus status);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.event.id = :eventId AND b.status IN (:statuses)")
    long countByEventIdAndStatusIn(@Param("eventId") Long eventId, @Param("statuses") List<BookingStatus> statuses);
    
    boolean existsBySeatIdAndStatusIn(Long seatId, List<BookingStatus> statuses);
    
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.user.id = :userId AND b.event.id = :eventId AND b.status IN (:statuses)")
    boolean existsByUserIdAndEventIdAndStatusIn(@Param("userId") Long userId, 
                                               @Param("eventId") Long eventId, 
                                               @Param("statuses") List<BookingStatus> statuses);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.event.id = :eventId AND b.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") BookingStatus status);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.status IN (:statuses)")
    long countByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<BookingStatus> statuses);
    
    @Query("SELECT b FROM Booking b WHERE b.bookedAt BETWEEN :startDate AND :endDate ORDER BY b.bookedAt DESC")
    List<Booking> findBookingsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
}