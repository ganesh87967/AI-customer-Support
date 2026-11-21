package com.vcube.support.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcube.support.dto.AiAnalysisResult;
import com.vcube.support.entity.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiTicketService {

    private static final Logger log = LoggerFactory.getLogger(AiTicketService.class);
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiTicketService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public AiAnalysisResult analyzeComplaint(String title, String description, List<Category> categories) {
        String categoryNames = categories.stream()
                .map(Category::getName)
                .collect(Collectors.joining(", "));

        String prompt = """
                You are an expert AI Customer Support Triage Assistant.
                Analyze the following customer complaint and return ONLY a valid JSON object (no markdown fences, no extra text) with these exact 5 keys:
                - "categoryName": Choose the single best match from [%s].
                - "sentiment": One of "POSITIVE", "NEUTRAL", "NEGATIVE", "ANGRY".
                - "priority": One of "LOW", "MEDIUM", "HIGH", "CRITICAL".
                - "suggestedResolution": Concise 2-3 step internal troubleshooting action plan for the support agent.
                - "customerResponse": Polite, empathetic 2-3 sentence draft response ready to send to the customer.

                Complaint Title: %s
                Complaint Description: %s
                """.formatted(categoryNames, title, description);

        try {
            String raw = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (raw != null) {
                String cleaned = raw.trim()
                        .replaceAll("^```json\\s*", "")
                        .replaceAll("^```\\s*", "")
                        .replaceAll("\\s*```$", "")
                        .trim();
                return objectMapper.readValue(cleaned, AiAnalysisResult.class);
            }
        } catch (Exception ex) {
            log.warn("Spring AI call failed or API key not configured, using rule-based fallback: {}", ex.getMessage());
        }

        return fallbackAnalysis(title, description, categories);
    }

    private AiAnalysisResult fallbackAnalysis(String title, String description, List<Category> categories) {
        String text = (title + " " + description).toLowerCase();
        String category = categories.isEmpty() ? "General Inquiry" : categories.get(0).getName();

        if (text.contains("refund") || text.contains("charge") || text.contains("bill") || text.contains("payment")) {
            category = "Billing & Payments";
        } else if (text.contains("crash") || text.contains("error") || text.contains("bug") || text.contains("down")) {
            category = "Technical Issue";
        } else if (text.contains("login") || text.contains("password") || text.contains("account") || text.contains("locked")) {
            category = "Account & Login";
        } else if (text.contains("deliver") || text.contains("ship") || text.contains("package") || text.contains("late")) {
            category = "Delivery & Shipping";
        } else {
            category = "General Inquiry";
        }

        String sentiment = (text.contains("worst") || text.contains("angry") || text.contains("furious") || text.contains("unacceptable"))
                ? "ANGRY"
                : (text.contains("fail") || text.contains("broken") || text.contains("issue") || text.contains("delay"))
                ? "NEGATIVE" : "NEUTRAL";

        String priority = sentiment.equals("ANGRY") || text.contains("urgent") || text.contains("immediately")
                ? "CRITICAL"
                : sentiment.equals("NEGATIVE") ? "HIGH" : "MEDIUM";

        return new AiAnalysisResult(
                category,
                sentiment,
                priority,
                "1. Verify customer account and transaction/system logs for '" + title + "'.\n2. Reproduce or confirm the reported issue under " + category + ".\n3. Apply standard remediation and notify the customer.",
                "Thank you for reaching out to us regarding \"" + title + "\". We understand how frustrating this is and have prioritized your ticket under " + category + ". Our support specialist is reviewing the details and will update you shortly."
        );
    }
}
