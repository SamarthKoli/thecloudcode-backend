package com.thecloudcode.cc.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.thecloudcode.cc.models.Subscriber;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    Optional<Subscriber> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Subscriber> findByActiveTrue();
    
    
    @Query("SELECT COUNT(s) FROM Subscriber s WHERE s.active = true")
    long countActiveSubscribers();
}
