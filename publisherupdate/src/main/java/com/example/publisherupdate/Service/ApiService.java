package com.example.publisherupdate.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class ApiService {

    @Autowired
    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private String token;

    public ApiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public ResponseEntity<Map<String, Object>> updateApi(String apiId, Map<String, Object> apiUpdateData) {
        String apiUrl = "https://api.kriate.co.in:8344/api/am/publisher/v4/apis/" + apiId;

        // Get or refresh token
        token = fetchNewToken();
        System.out.println("updateApi token: " + token);

        // Prepare request with Authorization header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(apiUpdateData, headers);

        // Call API
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl, 
            HttpMethod.PUT, 
            entity, 
            String.class
        );

        // If token is expired (401), fetch a new token and retry
        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            token = fetchNewToken();
            headers.set("Authorization", "Bearer " + token);
            entity = new HttpEntity<>(apiUpdateData, headers);
            response = restTemplate.exchange(apiUrl, HttpMethod.PUT, entity, String.class);
        }

        try {
            Map<String, Object> jsonResponse = objectMapper.readValue(response.getBody(), Map.class);
            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to parse JSON"));
        }
    }

    private String fetchNewToken() {
        String tokenUrl = "https://api.kriate.co.in:8344/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization",
                "Basic YVFDcjR4ajhnVU9WUXBBcTFra3ozbWR5WkZvYTpmbHRZaHFrcG90NEY3R2VXZmp1QVRXU1BjY1lh");

        String requestBody = "grant_type=password&username=admin&password=admin&scope=apim:api_create apim:api_manage";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String str = response.getBody().get("access_token").toString();

            System.out.println("fetchToken" + str);

            return response.getBody().get("access_token").toString();
        } else {
            throw new RuntimeException("Failed to fetch token");
        }
    }
}
