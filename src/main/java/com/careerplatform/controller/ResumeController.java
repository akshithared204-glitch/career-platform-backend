
package com.careerplatform.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careerplatform.service.SkillExtractionService;
import com.careerplatform.service.JobRecommendationService;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174}")
public class ResumeController {

    private final SkillExtractionService skillExtractionService;
    private final JobRecommendationService jobRecommendationService;

    public ResumeController(
            SkillExtractionService skillExtractionService,
            JobRecommendationService jobRecommendationService) {

        this.skillExtractionService = skillExtractionService;
        this.jobRecommendationService = jobRecommendationService;
    }

    @PostMapping("/analyze-text")
    public ResponseEntity<Map<String, Object>> analyzeText(
            @RequestBody Map<String, String> request) {

        String resumeText = request.get("resumeText");

        if (resumeText == null || resumeText.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Please provide resume text."
                    ));
        }

        List<String> skills =
                skillExtractionService.extractSkills(resumeText);

        List<Map<String, Object>> recommendedJobs =
                jobRecommendationService.recommendJobs(skills);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("message",
                "Resume analyzed successfully");
        response.put("skills", skills);
        response.put("recommendedJobs", recommendedJobs);

        return ResponseEntity.ok(response);
    }
}
