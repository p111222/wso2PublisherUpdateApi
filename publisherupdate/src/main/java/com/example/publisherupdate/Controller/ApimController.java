package com.example.publisherupdate.Controller;

import org.springframework.web.bind.annotation.*;
import com.example.publisherupdate.Service.ApiService;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api")
public class ApimController {

    @Autowired
    private ApiService apiService;


    // New endpoint for updating API
    @PutMapping("/{apiId}")
    public ResponseEntity<Map<String, Object>> updateApi(
            @PathVariable String apiId,
            @RequestBody Map<String, Object> apiUpdateData) {
        return apiService.updateApi(apiId, apiUpdateData);
    }
}
