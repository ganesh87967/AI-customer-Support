package com.vcube.support.repository;

import com.vcube.support.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Ticket> findAllByOrderByCreatedAtDesc();
}
