package com.disp.celesma.service;

import com.disp.celesma.service.interfaces.IAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Сервис для взаимодействия с DeepSeek API.
 * Генерирует короткое название задачи на основе её описания.
 *
 * DeepSeek API полностью совместим с форматом OpenAI Chat Completions,
 * поэтому используется стандартный REST-клиент.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService implements IAiService {

    private final RestTemplate restTemplate;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    private static final String SYSTEM_PROMPT =
            "Ты — ассистент проектного менеджера. " +
            "Сгенерируй короткое, ёмкое название задачи (не более 10 слов) " +
            "на русском языке на основе предоставленного описания. " +
            "Верни ТОЛЬКО название, без кавычек, пояснений и лишнего текста.";

    private static final String FALLBACK_TITLE = "Новая задача";

    @Override
    public String generateAiTitle(String description) {
        if (description == null || description.isBlank()) {
            log.warn("generateAiTitle вызван с пустым описанием");
            return FALLBACK_TITLE;
        }

        Map<String, Object> requestBody = Map.of(
                "model", "deepseek-chat",
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", description)
                ),
                "max_tokens", 30,
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, Map.class);

            Map responseBody = response.getBody();
            if (responseBody == null) {
                log.error("DeepSeek API вернул пустое тело ответа");
                return FALLBACK_TITLE;
            }

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) responseBody.get("choices");

            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.getFirst();
                Map<String, Object> message =
                        (Map<String, Object>) choice.get("message");
                if (message != null) {
                    String title = (String) message.get("content");
                    if (title != null) {
                        return title.trim()
                                .replaceAll("^[\"'«]|[\"'»]$", "")
                                .replaceAll("^Название:\\s*", "");
                    }
                }
            }

            log.warn("DeepSeek API ответил, но choices пуст: {}", responseBody);
            return FALLBACK_TITLE;

        } catch (RestClientException e) {
            log.error("Ошибка при обращении к DeepSeek API: {}", e.getMessage(), e);
            return FALLBACK_TITLE;
        }
    }
}