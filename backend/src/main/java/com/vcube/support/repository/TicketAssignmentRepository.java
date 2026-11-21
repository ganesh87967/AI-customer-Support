package com.vcube.support.repository;

import com.vcube.support.entity.TicketAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TicketAssignmentRepository extends JpaRepository<TicketAssignment, Long> {
    Optional<TicketAssignment> findTopByTicketIdOrderByAssignedAtDesc(Long ticketId);
}
