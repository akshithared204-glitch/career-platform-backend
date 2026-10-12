package com.careerplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /**
     * Generates five interview questions. If Gemini is unavailable after retries,
     * returns five clearly marked built-in questions so the feature still works.
     */
    public Map<String, Object> generateInterviewQuestions(String role, String skills) {
        String safeRole = (role == null || role.isBlank()) ? "Entry-level software developer" : role.trim();
        String safeSkills = (skills == null || skills.isBlank()) ? "Problem solving, communication" : skills.trim();

        if (apiKey == null || apiKey.isBlank()) {
            return fallbackResponse(safeRole, safeSkills, "Gemini API key is missing; showing standard questions.");
        }

        try {
            String prompt = "Create exactly 5 concise interview questions for the role: " + safeRole
                    + ". Candidate skills: " + safeSkills + ". "
                    + "Return ONLY valid JSON in this exact shape: {\"questions\":["
                    + "{\"question\":\"...\",\"sampleAnswer\":\"...\",\"followUpQuestions\":[\"...\"]}]}. "
                    + "Include exactly 5 objects. Keep each sample answer to 2-3 sentences and each object to 1-2 follow-up questions. Do not use Markdown.";

            ObjectNode requestBody = mapper.createObjectNode();
            ArrayNode contents = requestBody.putArray("contents");
            ObjectNode content = contents.addObject();
            content.put("role", "user");
            content.putArray("parts").addObject().put("text", prompt);
            ObjectNode generationConfig = requestBody.putObject("generationConfig");
            generationConfig.put("temperature", 0.4);
            generationConfig.put("maxOutputTokens", 1800);
            generationConfig.put("responseMimeType", "application/json");

            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model.trim() + ":generateContent?key=" + apiKey.trim();
            String requestJson = mapper.writeValueAsString(requestBody);
            String responseText = callGeminiWithRetry(endpoint, requestJson);

            JsonNode root = mapper.readTree(responseText);
            String generatedText = root.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text").asText("").trim();
            if (generatedText.isBlank()) {
                return fallbackResponse(safeRole, safeSkills, "Gemini returned an empty response; showing standard questions.");
            }

            JsonNode generatedJson = mapper.readTree(stripMarkdownFences(generatedText));
            JsonNode questionsNode = generatedJson.path("questions");
            List<Map<String, Object>> questions = new ArrayList<>();
            if (questionsNode.isArray()) {
                for (JsonNode item : questionsNode) {
                    if (questions.size() == 5) break;
                    Map<String, Object> question = new LinkedHashMap<>();
                    question.put("question", item.path("question").asText("Tell me about a project related to your skills."));
                    question.put("sampleAnswer", item.path("sampleAnswer").asText("Explain your approach, the tools you used, and the result."));
                    List<String> followUps = new ArrayList<>();
                    JsonNode followNode = item.path("followUpQuestions");
                    if (followNode.isArray()) {
                        for (JsonNode follow : followNode) followUps.add(follow.asText());
                    }
                    question.put("followUpQuestions", followUps);
                    questions.add(question);
                }
            }
            if (questions.isEmpty()) {
                return fallbackResponse(safeRole, safeSkills, "Gemini response could not be read; showing standard questions.");
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("questions", questions);
            result.put("source", "gemini");
            result.put("message", "Interview questions generated successfully.");
            return result;

        } catch (Exception ex) {
            return fallbackResponse(safeRole, safeSkills,
                    "Gemini is temporarily unavailable; showing standard interview questions instead.");
        }
    }

    private String callGeminiWithRetry(String endpoint, String requestJson)
            throws IOException, InterruptedException {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(35))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            if (code >= 200 && code < 300) return response.body();

            boolean temporary = code == 429 || code == 500 || code == 502 || code == 503 || code == 504;
            if (temporary && attempt < maxAttempts) {
                Thread.sleep(attempt * 1000L); // 1 second, then 2 seconds
                continue;
            }
            throw new IOException("Gemini request failed with HTTP " + code + ": " + response.body());
        }
        throw new IOException("Gemini request failed after retries.");
    }

    private Map<String, Object> fallbackResponse(String role, String skills, String message) {
        List<Map<String, Object>> questions = new ArrayList<>();
        addQuestion(questions, "Tell me about yourself and why you are interested in the " + role + " role.",
                "I am building my skills in " + skills + ". I enjoy solving problems, learning new tools, and applying what I learn in projects.",
                List.of("What is one strength you would bring to this role?"));
        addQuestion(questions, "Describe a project where you used one or more of these skills: " + skills + ".",
                "Explain the project's goal, your contribution, the technologies used, one challenge, and the result. Use a real project from your experience.",
                List.of("What challenge did you face, and how did you solve it?"));
        addQuestion(questions, "How do you debug a problem when your code does not work as expected?",
                "I reproduce the issue, read the error message, isolate the relevant code, test a possible fix, and verify that the change does not break other features.",
                List.of("Can you give an example of a bug you fixed?"));
        addQuestion(questions, "Which of your technical skills are strongest, and how have you used them?",
                "I would choose the skills I have practised most, describe the concepts I understand, and give a specific example of how I used them in coursework or a project.",
                List.of("Which skill are you currently improving?"));
        addQuestion(questions, "What would you do if you did not know how to solve a task assigned to you?",
                "I would clarify the expected result, break the task into smaller steps, consult reliable documentation, try a small solution, and ask a focused question if I remained blocked.",
                List.of("How do you decide when to ask for help?"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("questions", questions);
        result.put("source", "fallback");
        result.put("message", message);
        return result;
    }

    private void addQuestion(List<Map<String, Object>> list, String questionText,
                             String sampleAnswer, List<String> followUps) {
        Map<String, Object> question = new LinkedHashMap<>();
        question.put("question", questionText);
        question.put("sampleAnswer", sampleAnswer);
        question.put("followUpQuestions", followUps);
        list.add(question);
    }

    private String stripMarkdownFences(String text) {
        String result = text.trim();
        if (result.startsWith("```")) {
            result = result.replaceFirst("^```(?:json)?\\s*", "");
            result = result.replaceFirst("\\s*```$", "");
        }
        return result.trim();
    }
}
