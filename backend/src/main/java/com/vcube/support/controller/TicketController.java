package com.vcube.support.controller;

import com.vcube.support.dto.AiAnalysisResult;
import com.vcube.support.entity.*;
import com.vcube.support.repository.*;
import com.vcube.support.service.AiTicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final TicketAssignmentRepository assignmentRepo;
    private final TicketCommentRepository commentRepo;
    private final AiTicketService aiTicketService;

    public TicketController(
            TicketRepository ticketRepo,
            UserRepository userRepo,
            CategoryRepository categoryRepo,
            TicketAssignmentRepository assignmentRepo,
            TicketCommentRepository commentRepo,
            AiTicketService aiTicketService) {
        this.ticketRepo = ticketRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.assignmentRepo = assignmentRepo;
        this.commentRepo = commentRepo;
        this.aiTicketService = aiTicketService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "Smart Customer Support API");
    }

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return categoryRepo.findAll();
    }

    @GetMapping("/agents")
    public List<?> getAgents() {
        return userRepo.findByRole("AGENT").stream()
                .map(a -> Map.of("id", a.getId(), "name", a.getName(), "email", a.getEmail()))
                .toList();
    }

    @PostMapping("/tickets")
    public ResponseEntity<?> createTicket(@RequestBody Map<String, String> body, Authentication auth) {
        User customer = userRepo.findByEmail(auth.getName()).orElseThrow();
        String title = body.get("title");
        String description = body.get("description");
        if (title == null || title.isBlank() || description == null || description.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title and description are required"));
        }

        List<Category> categories = categoryRepo.findAll();
        AiAnalysisResult ai = aiTicketService.analyzeComplaint(title, description, categories);

        Category matchedCategory = categoryRepo.findByNameIgnoreCase(ai.categoryName())
                .orElseGet(() -> categories.isEmpty() ? null : categories.get(0));

        Ticket ticket = new Ticket();
        ticket.setTicketNumber("TKT-" + (1000 + new Random().nextInt(9000)));
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus("OPEN");
        ticket.setPriority(ai.priority());
        ticket.setSentiment(ai.sentiment());
        ticket.setAiSuggestedResolution(ai.suggestedResolution());
        ticket.setAiCustomerResponse(ai.customerResponse());
        ticket.setCustomer(customer);
        ticket.setCategory(matchedCategory);
        ticket = ticketRepo.save(ticket);

        List<User> agents = userRepo.findByRole("AGENT");
        if (!agents.isEmpty()) {
            User assignedAgent = agents.get((int) (ticket.getId() % agents.size()));
            TicketAssignment assignment = new TicketAssignment();
            assignment.setTicket(ticket);
            assignment.setAgent(assignedAgent);
            assignment.setNotes("Auto-assigned by Smart Support triage");
            assignmentRepo.save(assignment);

            TicketComment aiReply = new TicketComment();
            aiReply.setTicket(ticket);
            aiReply.setAuthor(assignedAgent);
            aiReply.setMessage(ai.customerResponse());
            aiReply.setInternal(false);
            commentRepo.save(aiReply);
        }

        return ResponseEntity.ok(enrichTicket(ticket));
    }

    @GetMapping("/tickets")
    public List<?> listTickets(Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        List<Ticket> tickets = user.getRole().equals("CUSTOMER")
                ? ticketRepo.findByCustomerIdOrderByCreatedAtDesc(user.getId())
                : ticketRepo.findAllByOrderByCreatedAtDesc();
        return tickets.stream().map(this::enrichTicket).toList();
    }

    @PatchMapping("/tickets/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ticketRepo.findById(id).map(t -> {
            if (body.containsKey("status")) t.setStatus(body.get("status").toUpperCase());
            if (body.containsKey("priority")) t.setPriority(body.get("priority").toUpperCase());
            ticketRepo.save(t);
            return ResponseEntity.ok((Object) enrichTicket(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tickets/{id}/assign")
    public ResponseEntity<?> assignTicket(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Optional<Ticket> ticketOpt = ticketRepo.findById(id);
        Optional<User> agentOpt = userRepo.findById(body.get("agentId"));
        if (ticketOpt.isEmpty() || agentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid ticket or agent ID"));
        }
        TicketAssignment assignment = new TicketAssignment();
        assignment.setTicket(ticketOpt.get());
        assignment.setAgent(agentOpt.get());
        assignment.setNotes("Manually reassigned");
        assignmentRepo.save(assignment);
        return ResponseEntity.ok(enrichTicket(ticketOpt.get()));
    }

    @PostMapping("/tickets/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        User author = userRepo.findByEmail(auth.getName()).orElseThrow();
        return ticketRepo.findById(id).map(ticket -> {
            TicketComment comment = new TicketComment();
            comment.setTicket(ticket);
            comment.setAuthor(author);
            comment.setMessage(String.valueOf(body.get("message")));
            comment.setInternal(Boolean.TRUE.equals(body.get("isInternal")));
            commentRepo.save(comment);
            return ResponseEntity.ok((Object) Map.of(
                    "id", comment.getId(),
                    "message", comment.getMessage(),
                    "authorName", author.getName(),
                    "authorRole", author.getRole(),
                    "isInternal", comment.isInternal(),
                    "createdAt", comment.getCreatedAt()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tickets/{id}/comments")
    public List<?> getComments(@PathVariable Long id) {
        return commentRepo.findByTicketIdOrderByCreatedAtAsc(id).stream()
                .map(c -> Map.of(
                        "id", c.getId(),
                        "message", c.getMessage(),
                        "authorName", c.getAuthor().getName(),
                        "authorRole", c.getAuthor().getRole(),
                        "isInternal", c.isInternal(),
                        "createdAt", c.getCreatedAt()
                )).toList();
    }

    @PostMapping("/tickets/{id}/feedback")
    public ResponseEntity<?> submitFeedback(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ticketRepo.findById(id).map(t -> {
            t.setCustomerFeedbackRating(Integer.parseInt(String.valueOf(body.get("rating"))));
            t.setCustomerFeedbackComment(String.valueOf(body.getOrDefault("comment", "")));
            ticketRepo.save(t);
            return ResponseEntity.ok((Object) enrichTicket(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> enrichTicket(Ticket t) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", t.getId());
        map.put("ticketNumber", t.getTicketNumber());
        map.put("title", t.getTitle());
        map.put("description", t.getDescription());
        map.put("status", t.getStatus());
        map.put("priority", t.getPriority());
        map.put("sentiment", t.getSentiment());
        map.put("aiSuggestedResolution", t.getAiSuggestedResolution());
        map.put("aiCustomerResponse", t.getAiCustomerResponse());
        map.put("customerFeedbackRating", t.getCustomerFeedbackRating());
        map.put("customerFeedbackComment", t.getCustomerFeedbackComment());
        map.put("customerName", t.getCustomer().getName());
        map.put("categoryName", t.getCategory() != null ? t.getCategory().getName() : "Uncategorized");
        map.put("createdAt", t.getCreatedAt());

        assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc(t.getId()).ifPresentOrElse(
                a -> map.put("assignedAgentName", a.getAgent().getName()),
                () -> map.put("assignedAgentName", "Unassigned")
        );
        return map;
    }
}
