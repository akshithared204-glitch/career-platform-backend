package com.careerplatform.controller;

import com.careerplatform.service.GeminiInterviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/interview")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174}")
public class GeminiInterviewController {

    private final GeminiInterviewService interviewService;

    public GeminiInterviewController(
            GeminiInterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/questions")
    public ResponseEntity<?> generateQuestions(
            @RequestBody Map<String, Object> request) {

        String role = getString(request, "role");
        String skills = getString(request, "skills");
        String experienceLevel = getString(request, "experienceLevel");
        String interviewType = getString(request, "interviewType");

        if (role.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please select a job role."));
        }

        if (skills.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please enter your skills."));
        }

        if (experienceLevel.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please select your experience level."));
        }

        if (interviewType.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please select an interview type."));
        }

        try {
            List<Map<String, Object>> generatedQuestions =
                    interviewService.generateQuestions(
                            role,
                            skills,
                            experienceLevel,
                            interviewType
                    );

            List<Map<String, Object>> questions =
                    generatedQuestions == null
                            ? new ArrayList<>()
                            : generatedQuestions.stream()
                                    .limit(5)
                                    .toList();

            if (questions.size() < 5) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                        Map.of(
                                "error",
                                "The AI generated fewer than five questions. Please try again."
                        )
                );
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("role", role);
            response.put("skills", skills);
            response.put("experienceLevel", experienceLevel);
            response.put("interviewType", interviewType);
            response.put("totalQuestions", questions.size());
            response.put("questions", questions);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                Map.of(
                    "error",
                    e.getMessage() != null
                        ? e.getMessage()
                        : "Unknown backend error"
                )
            );
        }  
    }

    private String getString(
            Map<String, Object> request,
            String key) {

        Object value = request.get(key);

        return value == null ? "" : value.toString().trim();
    }
}