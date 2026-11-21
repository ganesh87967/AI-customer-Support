package com.vcube.support.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", nullable = false, unique = true)
    private String ticketNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    @Column(nullable = false)
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(nullable = false)
    private String sentiment = "NEUTRAL"; // POSITIVE, NEUTRAL, NEGATIVE, ANGRY

    @Column(name = "ai_suggested_resolution", columnDefinition = "TEXT")
    private String aiSuggestedResolution;

    @Column(name = "ai_customer_response", columnDefinition = "TEXT")
    private String aiCustomerResponse;

    @Column(name = "customer_feedback_rating")
    private Integer customerFeedbackRating;

    @Column(name = "customer_feedback_comment", length = 1000)
    private String customerFeedbackComment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public String getAiSuggestedResolution() { return aiSuggestedResolution; }
    public void setAiSuggestedResolution(String aiSuggestedResolution) { this.aiSuggestedResolution = aiSuggestedResolution; }
    public String getAiCustomerResponse() { return aiCustomerResponse; }
    public void setAiCustomerResponse(String aiCustomerResponse) { this.aiCustomerResponse = aiCustomerResponse; }
    public Integer getCustomerFeedbackRating() { return customerFeedbackRating; }
    public void setCustomerFeedbackRating(Integer customerFeedbackRating) { this.customerFeedbackRating = customerFeedbackRating; }
    public String getCustomerFeedbackComment() { return customerFeedbackComment; }
    public void setCustomerFeedbackComment(String customerFeedbackComment) { this.customerFeedbackComment = customerFeedbackComment; }
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
