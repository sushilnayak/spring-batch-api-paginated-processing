package com.nayak.batch.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayak.batch.model.Comments;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiClient {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String COMMENTS_URL = "https://jsonplaceholder.typicode.com/comments";

    public List<Comments> fetchAllRecords() {
        ResponseEntity<List<Comments>> response = restTemplate.exchange(
                COMMENTS_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Comments>>() {
                }
        );
        return response.getBody();
    }

    public <T> List<T> fetchPage(int currentPage, int pageSize, Class<T> clazz) {
        System.out.println("https://jsonplaceholder.typicode.com/comments?_start=" + currentPage + "&_limit=" + pageSize);
        ResponseEntity<List<LinkedHashMap<String, Object>>> response = restTemplate.exchange(
                "https://jsonplaceholder.typicode.com/comments?_start=" + currentPage + "&_limit=" + pageSize,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<LinkedHashMap<String, Object>>>() {
                }
        );
        List<LinkedHashMap<String, Object>> rawList = response.getBody();

        List<T> result = new ArrayList<>();

        for (LinkedHashMap<String, Object> map : rawList) {
            result.add(objectMapper.convertValue(map, clazz));
        }

        return result;
    }
}
