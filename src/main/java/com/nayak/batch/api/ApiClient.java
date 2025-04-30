package com.nayak.batch.api;

import com.nayak.batch.model.Comments;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiClient {

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
}
