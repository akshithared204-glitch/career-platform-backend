
package com.careerplatform.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.careerplatform.entity.JobApplication;
import com.careerplatform.repository.JobApplicationRepository;

@Service
public class JobApplicationService {

    private final JobApplicationRepository applicationRepository;

    public JobApplicationService(
            JobApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    // Get all applications
    public List<JobApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    // Get applications belonging to one user
    public List<JobApplication> getApplicationsByUser(Long userId) {
        return applicationRepository.findByUserId(userId);
    }

    // Save a new application
    public JobApplication saveApplication(JobApplication application) {

        if (application.getStatus() == null ||
                application.getStatus().isBlank()) {
            application.setStatus("Applied");
        } else {
            validateStatus(application.getStatus());
        }

        if (application.getAppliedDate() == null ||
                application.getAppliedDate().isBlank()) {
            application.setAppliedDate(LocalDate.now().toString());
        }

        return applicationRepository.save(application);
    }

    // Update the status of one specific application
    @Transactional
    public JobApplication updateStatus(Long id, String status) {

        validateStatus(status);

        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Application not found with ID: " + id));

        application.setStatus(status);

        return applicationRepository.save(application);
    }

    // Validate application status
    private void validateStatus(String status) {

        if (status == null ||
                !List.of(
                        "Applied",
                        "Interview",
                        "Selected",
                        "Rejected"
                ).contains(status)) {

            throw new IllegalArgumentException(
                    "Invalid status. Allowed: Applied, Interview, Selected, Rejected");
        }
    }

    // Delete one application
    public void deleteApplication(Long id) {

        if (!applicationRepository.existsById(id)) {
            throw new RuntimeException(
                    "Application not found with ID: " + id);
        }

        applicationRepository.deleteById(id);
    }
}
