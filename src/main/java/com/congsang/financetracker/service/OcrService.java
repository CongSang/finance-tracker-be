package com.congsang.financetracker.service;

import com.congsang.financetracker.dto.response.ScanResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OcrService {

    @Value("${ollama.api-url}")
    private String ollamaUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScanResponseDTO scanInvoice(MultipartFile file) throws Exception {
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llava");
        requestBody.put("stream", false);
        requestBody.put("prompt", """
                            Identify the text in this receipt image and extract the following information into a JSON format.\s
                            Follow these specific instructions for each field:
            
                            1. "amount": Look for keywords like "Tổng cộng", "Tổng", "Thành tiền" or "Khách đã trả". Check the very last line of the table for the total amount, Choose the largest numerical value associated with the final payment. Remove all dots, commas, and currency symbols. It must be a pure number.
                            2. "transactionDate": Find the transaction date (e.g., DD/MM/YYYY). Convert it to ISO format (YYYY-MM-DD). If you cannot find the year, assume 2026.
                            3. "note": List the main items or the store name (e.g., "Sữa bịch, Bánh Sandwich").
                            4. "category": Based on the items, choose one from: Food, Shopping, Transport, Utilities, Others.
            
                            Return ONLY the JSON object. Do not include any explanation or markdown.
                            Example: {"amount": 260000, "transactionDate": "2026-04-08", "note": "Sua bich, Banh Sandwich", "category": "Food"}
                            """);
        requestBody.put("images", List.of(base64Image));

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.0);
        requestBody.put("options", options);

        String rawResponse = restTemplate.postForObject(ollamaUrl, requestBody, String.class);
        System.out.println(rawResponse);
        JsonNode root = objectMapper.readTree(rawResponse);
        String aiText = root.has("response") ? root.get("response").asText() : "";

        try {
            Pattern pattern = Pattern.compile("\\{.*}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(aiText);

            String jsonClean;
            if (matcher.find()) {
                jsonClean = matcher.group();
            } else {
                throw new RuntimeException("AI_FAILED_TO_PARSE");
            }

            return objectMapper.readValue(jsonClean, ScanResponseDTO.class);
        } catch (Exception e) {
            System.err.println("Nội dung AI trả về lỗi: " + aiText);
            throw new RuntimeException("AI_FAILED_TO_PARSE");
        }
    }
}