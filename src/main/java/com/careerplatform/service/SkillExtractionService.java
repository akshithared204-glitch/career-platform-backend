
package com.careerplatform.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class SkillExtractionService {

    private final List<String> knownSkills = Arrays.asList(
            "Java",
            "Python",
            "C",
            "C++",
            "JavaScript",
            "HTML",
            "CSS",
            "React",
            "Node.js",
            "Spring Boot",
            "SQL",
            "MySQL",
            "MongoDB",
            "Machine Learning",
            "Deep Learning",
            "Artificial Intelligence",
            "Data Structures",
            "Data Analysis",
            "Excel",
            "Git",
            "GitHub",
            "REST API",
            "FastAPI",
            "Flask",
            "Django"
    );

    public List<String> extractSkills(String resumeText) {

        List<String> extractedSkills = new ArrayList<>();

        if (resumeText == null || resumeText.isBlank()) {
            return extractedSkills;
        }

        String text = resumeText.toLowerCase(Locale.ROOT);

        for (String skill : knownSkills) {

            String keyword = skill.toLowerCase(Locale.ROOT);

            if (containsSkill(text, keyword)) {
                extractedSkills.add(skill);
            }
        }

        return extractedSkills;
    }

    private boolean containsSkill(String text, String skill) {

        int index = text.indexOf(skill);

        while (index >= 0) {

            int end = index + skill.length();

            boolean leftBoundary = index == 0
                    || !Character.isLetterOrDigit(text.charAt(index - 1));

            boolean rightBoundary = end == text.length()
                    || !Character.isLetterOrDigit(text.charAt(end));

            if (leftBoundary && rightBoundary) {
                return true;
            }

            index = text.indexOf(skill, index + 1);
        }

        return false;
    }
}
