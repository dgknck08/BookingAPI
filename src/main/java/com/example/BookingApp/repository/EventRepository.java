package com.example.BookingApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.BookingApp.entity.Event;
import com.example.BookingApp.entityenums.EventStatus;
import com.example.BookingApp.entityenums.EventType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    List<Event> findByEventTypeAndStatusOrderByEventDateAsc(EventType eventType, EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.eventDate >= :startDate AND e.eventDate <= :endDate AND e.status = :status")
    List<Event> findByDateRangeAndStatus(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        @Param("status") EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.venue.city = :city AND e.status = :status ORDER BY e.eventDate ASC")
    List<Event> findByCityAndStatus(@Param("city") String city, @Param("status") EventStatus status);

    // Geliştirilmiş arama - title, description, organizer ve venue arama 
    @Query("SELECT e FROM Event e WHERE " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.organizer) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.venue.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND e.status = :status ORDER BY e.eventDate ASC")
    List<Event> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") EventStatus status);

    // Sadece başlık ve açıklamada arama 
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> findByTitleOrDescriptionContainingIgnoreCase(@Param("keyword") String keyword);

    // Otomatik tamamlama için başlık 
    @Query("SELECT DISTINCT e.title FROM Event e WHERE " +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND e.status = :status ORDER BY e.title")
    List<String> findTitleSuggestions(@Param("keyword") String keyword, @Param("status") EventStatus status);

    // Kategori ve arama kelimesi kombinasyonu
    @Query("SELECT e FROM Event e WHERE " +
           "e.eventType = :eventType AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND e.status = :status ORDER BY e.eventDate ASC")
    List<Event> findByEventTypeAndKeyword(@Param("eventType") EventType eventType, 
                                         @Param("keyword") String keyword, 
                                         @Param("status") EventStatus status);

    // Şehir ve arama kelimesi kombinasyonu
    @Query("SELECT e FROM Event e WHERE " +
           "e.venue.city = :city AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND e.status = :status ORDER BY e.eventDate ASC")
    List<Event> findByCityAndKeyword(@Param("city") String city, 
                                    @Param("keyword") String keyword, 
                                    @Param("status") EventStatus status);
}