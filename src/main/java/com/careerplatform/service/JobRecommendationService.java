
package com.careerplatform.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.careerplatform.entity.Job;
import com.careerplatform.repository.JobRepository;

@Service
public class JobRecommendationService {

    private final JobRepository jobRepository;

    public JobRecommendationService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Map<String, Object>> recommendJobs(
            List<String> resumeSkills) {

        List<Map<String, Object>> recommendations = new ArrayList<>();

        if (resumeSkills == null || resumeSkills.isEmpty()) {
            return recommendations;
        }

        List<String> normalizedSkills = resumeSkills.stream()
                .map(skill -> skill.toLowerCase(Locale.ROOT).trim())
                .toList();

        List<Job> jobs = jobRepository.findAll();

        for (Job job : jobs) {

            List<String> requiredSkills = new ArrayList<>();
            List<String> matchedSkills = new ArrayList<>();
            List<String> missingSkills = new ArrayList<>();

            if (job.getRequiredSkills() != null
                    && !job.getRequiredSkills().isBlank()) {

                for (String skill : job.getRequiredSkills().split(",")) {
                    requiredSkills.add(skill.trim());
                }
            }

            for (String requiredSkill : requiredSkills) {

                boolean matched = normalizedSkills.contains(
                        requiredSkill.toLowerCase(Locale.ROOT));

                if (matched) {
                    matchedSkills.add(requiredSkill);
                } else {
                    missingSkills.add(requiredSkill);
                }
            }

            double percentage = requiredSkills.isEmpty()
                    ? 0.0
                    : (matchedSkills.size() * 100.0)
                            / requiredSkills.size();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("jobId", job.getId());
            result.put("title", job.getTitle());
            result.put("company", job.getCompany());
            result.put("location", job.getLocation());
            result.put("type", job.getType());
            result.put("applicationUrl", job.getApplicationUrl());
            result.put("matchPercentage",
                    Math.round(percentage * 100.0) / 100.0);
            result.put("matchedSkills", matchedSkills);
            result.put("missingSkills", missingSkills);

            recommendations.add(result);
        }

        recommendations.sort((a, b) ->
                Double.compare(
                        (Double) b.get("matchPercentage"),
                        (Double) a.get("matchPercentage")));

        return recommendations;
    }
}
