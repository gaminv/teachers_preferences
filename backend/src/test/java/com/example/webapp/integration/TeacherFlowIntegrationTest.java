package com.example.webapp.integration;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.example.webapp.dto.PreferenceDto;
import com.example.webapp.service.AuthService.AuthResponse;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    // Настройки in-memory H2
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class TeacherFlowIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String loginAndGetToken(String login, String password) {
        Map<String, String> loginReq = Map.of(
            "login",    login,
            "password", password
        );
        ResponseEntity<AuthResponse> loginResp = rest.exchange(
            "/api/auth/login",
            HttpMethod.POST,
            new HttpEntity<>(loginReq, jsonHeaders()),
            AuthResponse.class
        );
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return loginResp.getBody().token;
    }

    @Test
    void teacherCanRegisterLoginAndManagePreferences() {
        // 1) Регистрация
        Map<String, String> regReq = Map.of(
            "fullName", "John Doe",
            "login",    "jdoe",
            "password", "123456"
        );
        ResponseEntity<Map> regResp = rest.postForEntity(
            "/api/auth/register",
            new HttpEntity<>(regReq, jsonHeaders()),
            Map.class
        );
        assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 2) Логин и получение токена
        String token = loginAndGetToken("jdoe", "123456");

        // 3) Готовим заголовки с Bearer
        HttpHeaders bearer = new HttpHeaders();
        bearer.setBearerAuth(token);
        bearer.setContentType(MediaType.APPLICATION_JSON);

        // 4) Сохраняем два предпочтения
        PreferenceDto p1 = new PreferenceDto();
        p1.type = "semester";
        p1.subject = "Math";
        p1.groups = "A1";

        PreferenceDto p2 = new PreferenceDto();
        p2.type = "semester";
        p2.subject = "Physics";
        p2.groups = "B1";

        ResponseEntity<PreferenceDto[]> saveResp = rest.exchange(
            "/api/teacher/preferences",
            HttpMethod.POST,
            new HttpEntity<>(List.of(p1, p2), bearer),
            PreferenceDto[].class
        );
        assertThat(saveResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        PreferenceDto[] saved = saveResp.getBody();
        assertThat(saved).hasSize(2)
                         .extracting(d -> d.subject)
                         .containsExactlyInAnyOrder("Math", "Physics");

        // 5) Получаем их обратно
        ResponseEntity<PreferenceDto[]> getResp = rest.exchange(
            "/api/teacher/preferences?type=semester",
            HttpMethod.GET,
            new HttpEntity<>(bearer),
            PreferenceDto[].class
        );
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        PreferenceDto[] fetched = getResp.getBody();
        assertThat(fetched).hasSize(2)
                           .extracting(d -> d.subject)
                           .containsExactlyInAnyOrder("Math", "Physics");
    }

    @Test
    void teacherCanEditExistingPreferences() {
        // Регистрация и логин нового пользователя
        Map<String, String> regReq = Map.of(
            "fullName", "Jane Doe",
            "login",    "jane",
            "password", "123456"
        );
        rest.postForEntity(
            "/api/auth/register",
            new HttpEntity<>(regReq, jsonHeaders()),
            Map.class
        );
        String token = loginAndGetToken("jane", "123456");

        HttpHeaders bearer = new HttpHeaders();
        bearer.setBearerAuth(token);
        bearer.setContentType(MediaType.APPLICATION_JSON);

        // 1) Сохраняем изначальное предпочтение
        PreferenceDto orig = new PreferenceDto();
        orig.type = "semester";
        orig.subject = "History";
        orig.groups = "C1";

        PreferenceDto savedOrig = rest.exchange(
            "/api/teacher/preferences",
            HttpMethod.POST,
            new HttpEntity<>(List.of(orig), bearer),
            PreferenceDto[].class
        ).getBody()[0];

        // 2) Изменяем его
        PreferenceDto edit = new PreferenceDto();
        edit.id = savedOrig.id;
        edit.type = "semester";
        edit.subject = "History - Updated";
        edit.groups = "C1";
        edit.days = List.of("Mon", "Fri");
        edit.daysPriority = 5;

        PreferenceDto updated = rest.exchange(
            "/api/teacher/preferences",
            HttpMethod.POST,
            new HttpEntity<>(List.of(edit), bearer),
            PreferenceDto[].class
        ).getBody()[0];

        assertThat(updated.subject).isEqualTo("History - Updated");
        assertThat(updated.days).containsExactly("Mon", "Fri");
        assertThat(updated.id).isNotEqualTo(savedOrig.id);
    }
}
