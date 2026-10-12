
package com.careerplatform.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.careerplatform.entity.JobApplication;
import com.careerplatform.service.JobApplicationService;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174}")
public class JobApplicationController {

    private final JobApplicationService applicationService;

    public JobApplicationController(
            JobApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<JobApplication> getAllApplications() {
        return applicationService.getAllApplications();
    }

    @GetMapping("/user/{userId}")
    public List<JobApplication> getApplicationsByUser(
            @PathVariable Long userId) {
        return applicationService.getApplicationsByUser(userId);
    }

    @PostMapping
    public JobApplication createApplication(
            @RequestBody JobApplication application) {
        return applicationService.saveApplication(application);
    }

    @PutMapping("/{id}/status")
    public JobApplication updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");

        if (status == null || !List.of(
                "Applied", "Interview", "Selected", "Rejected"
        ).contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid application status");
        }

        return applicationService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public String deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return "Application deleted successfully";
    }
}
