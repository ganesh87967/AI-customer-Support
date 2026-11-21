package com.vcube.support.controller;

import com.vcube.support.entity.Ticket;
import com.vcube.support.repository.TicketRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final TicketRepository ticketRepo;

    public AnalyticsController(TicketRepository ticketRepo) {
        this.ticketRepo = ticketRepo;
    }

    @GetMapping
    public Map<String, Object> getAnalytics() {
        List<Ticket> all = ticketRepo.findAll();

        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));
        Map<String, Long> byPriority = all.stream()
                .collect(Collectors.groupingBy(Ticket::getPriority, Collectors.counting()));
        Map<String, Long> bySentiment = all.stream()
                .collect(Collectors.groupingBy(Ticket::getSentiment, Collectors.counting()));
        Map<String, Long> byCategory = all.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().getName() : "Uncategorized",
                        Collectors.counting()));

        double avgRating = all.stream()
                .filter(t -> t.getCustomerFeedbackRating() != null)
                .mapToInt(Ticket::getCustomerFeedbackRating)
                .average()
                .orElse(0.0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTickets", all.size());
        result.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
        result.put("byStatus", byStatus);
        result.put("byPriority", byPriority);
        result.put("bySentiment", bySentiment);
        result.put("byCategory", byCategory);
        return result;
    }
}
