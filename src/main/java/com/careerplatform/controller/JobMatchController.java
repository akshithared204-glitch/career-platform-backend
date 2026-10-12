
package com.careerplatform.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.careerplatform.entity.Job;
import com.careerplatform.service.JobService;

@RestController
@RequestMapping("/api/match")
@CrossOrigin(origins = "http://localhost:5174")
public class JobMatchController {

    private final JobService jobService;

    public JobMatchController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/{jobId}")
    public Map<String, Object> matchJob(
            @PathVariable Long jobId,
            @RequestParam String skills) {

        Job job = jobService.getJobById(jobId);

        Set<String> userSkills = Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<String> requiredSkills =
                Arrays.stream(job.getRequiredSkills().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        List<String> matchedSkills = requiredSkills.stream()
                .filter(s -> userSkills.contains(s.toLowerCase()))
                .toList();

        List<String> missingSkills = requiredSkills.stream()
                .filter(s -> !userSkills.contains(s.toLowerCase()))
                .toList();

        int percentage = requiredSkills.isEmpty() ? 0 :
                (matchedSkills.size() * 100) / requiredSkills.size();

        return Map.of(
                "jobTitle", job.getTitle(),
                "matchPercentage", percentage,
                "matchedSkills", matchedSkills,
                "missingSkills", missingSkills
        );
    }
}
