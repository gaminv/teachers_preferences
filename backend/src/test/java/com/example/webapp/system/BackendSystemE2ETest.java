package com.example.webapp.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.client.ResourceAccessException;

import com.example.webapp.dto.PreferenceDto;
import com.example.webapp.service.AuthService.AuthResponse;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:systemdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=this-is-a-very-long-test-secret-key-for-system-flow",
        "jwt.expirationMs=86400000"
    }
)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class BackendSystemE2ETest {

    @Autowired
    private TestRestTemplate rest;

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String tokenFor(String login) {
        Map<String, String> reg = Map.of("fullName", "System User " + login, "login", login, "password", "123456");
        rest.postForEntity("/api/auth/register", new HttpEntity<>(reg, jsonHeaders()), Map.class);
        Map<String, String> req = Map.of("login", login, "password", "123456");
        ResponseEntity<AuthResponse> resp = rest.exchange("/api/auth/login", HttpMethod.POST, new HttpEntity<>(req, jsonHeaders()), AuthResponse.class);
        return resp.getBody().token;
    }

    @Test
    void e2e01_registerAndLoginWorks() {
        String token = tokenFor("sys01");
        assertThat(token).isNotBlank();
    }

    @Test
    void e2e02_saveAndReadSemesterPreferences() {
        String token = tokenFor("sys02");
        HttpHeaders bearer = jsonHeaders();
        bearer.setBearerAuth(token);
        PreferenceDto dto = new PreferenceDto();
        dto.type = "semester";
        dto.subject = "Math";
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(List.of(dto), bearer), PreferenceDto[].class);
        PreferenceDto[] fetched = rest.exchange("/api/teacher/preferences?type=semester", HttpMethod.GET, new HttpEntity<>(bearer), PreferenceDto[].class).getBody();
        assertThat(fetched).hasSize(1);
    }

    @Test
    void e2e03_saveAndReadSessionPreferences() {
        String token = tokenFor("sys03");
        HttpHeaders bearer = jsonHeaders();
        bearer.setBearerAuth(token);
        PreferenceDto dto = new PreferenceDto();
        dto.type = "session";
        dto.subject = "Session Subject";
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(List.of(dto), bearer), PreferenceDto[].class);
        PreferenceDto[] fetched = rest.exchange("/api/teacher/preferences?type=session", HttpMethod.GET, new HttpEntity<>(bearer), PreferenceDto[].class).getBody();
        assertThat(fetched).hasSize(1);
    }

    @Test
    void e2e04_unauthorizedWithoutToken() {
        ResponseEntity<String> response = rest.getForEntity("/api/teacher/preferences?type=semester", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void e2e05_invalidLoginRejected() {
        Map<String, String> req = Map.of("login", "unknown", "password", "123456");
        try {
            ResponseEntity<String> response = rest.postForEntity("/api/auth/login", new HttpEntity<>(req, jsonHeaders()), String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        } catch (ResourceAccessException ex) {
            assertThat(ex.getMessage()).contains("cannot retry due to server authentication");
        }
    }

    @Test
    void e2e06_multipleSubjectsPersisted() {
        String token = tokenFor("sys06");
        HttpHeaders bearer = jsonHeaders();
        bearer.setBearerAuth(token);
        List<PreferenceDto> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PreferenceDto dto = new PreferenceDto();
            dto.type = "semester";
            dto.subject = "S" + i;
            list.add(dto);
        }
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(list, bearer), PreferenceDto[].class);
        PreferenceDto[] fetched = rest.exchange("/api/teacher/preferences?type=semester", HttpMethod.GET, new HttpEntity<>(bearer), PreferenceDto[].class).getBody();
        assertThat(fetched).hasSize(5);
    }

    @Test
    void e2e07_replaceSemesterDataOnResave() {
        String token = tokenFor("sys07");
        HttpHeaders bearer = jsonHeaders();
        bearer.setBearerAuth(token);
        PreferenceDto first = new PreferenceDto(); first.type = "semester"; first.subject = "Old";
        PreferenceDto second = new PreferenceDto(); second.type = "semester"; second.subject = "New";
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(List.of(first), bearer), PreferenceDto[].class);
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(List.of(second), bearer), PreferenceDto[].class);
        PreferenceDto[] fetched = rest.exchange("/api/teacher/preferences?type=semester", HttpMethod.GET, new HttpEntity<>(bearer), PreferenceDto[].class).getBody();
        assertThat(fetched).hasSize(1);
        assertThat(fetched[0].subject).isEqualTo("New");
    }

    @Test
    void e2e08_crossTypeIsolation() {
        String token = tokenFor("sys08");
        HttpHeaders bearer = jsonHeaders();
        bearer.setBearerAuth(token);
        PreferenceDto sem = new PreferenceDto(); sem.type = "semester"; sem.subject = "Sem";
        PreferenceDto ses = new PreferenceDto(); ses.type = "session"; ses.subject = "Ses";
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(List.of(sem), bearer), PreferenceDto[].class);
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(List.of(ses), bearer), PreferenceDto[].class);
        PreferenceDto[] semFetched = rest.exchange("/api/teacher/preferences?type=semester", HttpMethod.GET, new HttpEntity<>(bearer), PreferenceDto[].class).getBody();
        PreferenceDto[] sesFetched = rest.exchange("/api/teacher/preferences?type=session", HttpMethod.GET, new HttpEntity<>(bearer), PreferenceDto[].class).getBody();
        assertThat(semFetched).hasSize(1);
        assertThat(sesFetched).hasSize(1);
    }

    @Test
    void e2e09_adminEndpointDeniedForTeacher() {
        String token = tokenFor("sys09");
        HttpHeaders bearer = jsonHeaders();
        bearer.setBearerAuth(token);
        ResponseEntity<String> resp = rest.exchange("/api/admin/preferences", HttpMethod.GET, new HttpEntity<>(bearer), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void e2e10_volumeSmoke_20Preferences() {
        String token = tokenFor("sys10");
        HttpHeaders bearer = jsonHeaders();
        bearer.setBearerAuth(token);
        List<PreferenceDto> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            PreferenceDto dto = new PreferenceDto();
            dto.type = "semester";
            dto.subject = "Subject-" + i;
            list.add(dto);
        }
        long start = System.currentTimeMillis();
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(list, bearer), PreferenceDto[].class);
        PreferenceDto[] fetched = rest.exchange("/api/teacher/preferences?type=semester", HttpMethod.GET, new HttpEntity<>(bearer), PreferenceDto[].class).getBody();
        long elapsed = System.currentTimeMillis() - start;
        assertThat(fetched).hasSize(20);
        assertThat(elapsed).isLessThan(10000);
    }
}
