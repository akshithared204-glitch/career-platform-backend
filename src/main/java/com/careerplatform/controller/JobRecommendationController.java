
package com.careerplatform.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careerplatform.service.JobRecommendationService;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174}")
public class JobRecommendationController {

    private final JobRecommendationService jobRecommendationService;

    public JobRecommendationController(
            JobRecommendationService jobRecommendationService) {
        this.jobRecommendationService = jobRecommendationService;
    }

    @PostMapping
    public List<Map<String, Object>> recommendJobs(
            @RequestBody List<String> skills) {
        return jobRecommendationService.recommendJobs(skills);
    }
}
