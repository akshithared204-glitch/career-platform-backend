package com.careerplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.careerplatform.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {

}