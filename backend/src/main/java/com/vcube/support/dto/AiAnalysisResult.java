package com.vcube.support.dto;

public record AiAnalysisResult(
        String categoryName,
        String sentiment,
        String priority,
        String suggestedResolution,
        String customerResponse
) {}
