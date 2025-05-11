package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class GitHubOAuthController {

  @Value("${github.client-id}")
  private String clientId;

  @Value("${github.client-secret}")
  private String clientSecret;

  @Value("${github.redirect-uri}")
  private String redirectUri;

  private final RestTemplate restTemplate = new RestTemplate();

  @GetMapping("/")
  public String index() {
    return "index";
  }

  @GetMapping("/callback")
  @ResponseBody
  public String callback(@RequestParam String code) {
    // Step 1. Access Token 요청
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    body.add("code", code);
    body.add("redirect_uri", redirectUri);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
    ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
        "https://github.com/login/oauth/access_token",
        request,
        Map.class
    );

    String accessToken = (String) tokenResponse.getBody().get("access_token");

    // Step 2. 사용자 정보 요청
    HttpHeaders userHeaders = new HttpHeaders();
    userHeaders.setBearerAuth(accessToken);
    userHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
    HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

    ResponseEntity<Map> userResponse = restTemplate.exchange(
        "https://api.github.com/user",
        HttpMethod.GET,
        userRequest,
        Map.class
    );

    Map userInfo = userResponse.getBody();
    return "Hello, " + userInfo.get("login") + "!";  // 간단 출력
  }
}

