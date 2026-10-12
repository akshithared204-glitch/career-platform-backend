package com.careerplatform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiInterviewService {

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GeminiInterviewService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public List<Map<String, Object>> generateQuestions(
            String role,
            String skills,
            String experienceLevel,
            String interviewType) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured. Please set the GEMINI_API_KEY environment variable.");
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Job role is required.");
        }

        if (skills == null || skills.isBlank()) {
            throw new IllegalArgumentException("Skills are required.");
        }

        if (experienceLevel == null || experienceLevel.isBlank()) {
            throw new IllegalArgumentException(
                    "Experience level is required.");
        }

        if (interviewType == null || interviewType.isBlank()) {
            throw new IllegalArgumentException(
                    "Interview type is required.");
        }

        String prompt = """
                You are an experienced technical interviewer and career coach.

                Generate exactly FIVE distinct interview questions for the
                candidate using the following information.

                Job role: %s
                Candidate skills: %s
                Experience level: %s
                Interview type: %s

                Requirements:
                1. Every question must be relevant to the selected job role.
                2. Match the difficulty to the candidate's experience level.
                3. For Technical interviews, ask role-specific technical questions.
                4. For HR interviews, ask behavioral, motivation, teamwork,
                   communication, and career-goal questions.
                5. For Mixed interviews, include both technical and HR questions.
                6. Use the candidate's listed skills when relevant.
                7. Provide a helpful sample answer for every question.
                8. Provide one relevant follow-up question for every question.
                9. Keep the questions clear and avoid duplicates.

                Return ONLY a valid JSON array with exactly five objects.
                Do not include Markdown, code fences, or explanations outside JSON.

                Each object must follow this structure:
                {
                  "question": "Interview question",
                  "sampleAnswer": "Suggested answer",
                  "followUp": "Relevant follow-up question"
                }
                """.formatted(
                role.trim(),
                skills.trim(),
                experienceLevel.trim(),
                interviewType.trim()
        );

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent";

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "responseMimeType", "application/json"
                )
        );

        try {
            Map<?, ?> response = restClient.post()
                    .uri(url)
                    .header("x-goog-api-key", apiKey)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, clientResponse) -> {
                                throw new IllegalStateException(
                                        "Gemini API returned HTTP "
                                                + clientResponse.getStatusCode()
                                );
                            }
                    )
                    .body(Map.class);

            if (response == null) {
                throw new IllegalStateException(
                        "Gemini returned an empty response.");
            }

            Object candidatesObject = response.get("candidates");

            if (!(candidatesObject instanceof List<?> candidates)
                    || candidates.isEmpty()) {
                throw new IllegalStateException(
                        "Gemini did not return any questions.");
            }

            Object firstCandidate = candidates.get(0);

            if (!(firstCandidate instanceof Map<?, ?> candidate)) {
                throw new IllegalStateException(
                        "Invalid Gemini candidate response.");
            }

            Object contentObject = candidate.get("content");

            if (!(contentObject instanceof Map<?, ?> content)) {
                throw new IllegalStateException(
                        "Gemini response content is missing.");
            }

            Object partsObject = content.get("parts");

            if (!(partsObject instanceof List<?> parts)
                    || parts.isEmpty()) {
                throw new IllegalStateException(
                        "Gemini response text is missing.");
            }

            Object firstPart = parts.get(0);

            if (!(firstPart instanceof Map<?, ?> part)
                    || !(part.get("text") instanceof String generatedText)) {
                throw new IllegalStateException(
                        "Gemini returned an invalid text response.");
            }

            List<Map<String, Object>> questions =
                    objectMapper.readValue(
                            generatedText,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );

            if (questions.size() != 5) {
                throw new IllegalStateException(
                        "Gemini generated " + questions.size()
                                + " questions instead of five.");
            }

            for (Map<String, Object> question : questions) {
                if (!(question.get("question") instanceof String)
                        || !(question.get("sampleAnswer") instanceof String)
                        || !(question.get("followUp") instanceof String)) {
                    throw new IllegalStateException(
                            "A generated question is missing required fields.");
                }
            }

            return questions;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to generate interview questions: "
                            + e.getMessage(),
                    e
            );
        }
    }
}