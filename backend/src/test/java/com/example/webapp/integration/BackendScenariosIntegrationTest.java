package com.example.webapp.integration;

import static org.assertj.core.api.Assertions.assertThat;

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
        "spring.datasource.url=jdbc:h2:mem:integrationdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=this-is-a-very-long-test-secret-key-for-jwt-signing",
        "jwt.expirationMs=86400000"
    }
)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class BackendScenariosIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String registerAndLogin(String fullName, String login) {
        Map<String, String> regReq = Map.of("fullName", fullName, "login", login, "password", "123456");
        ResponseEntity<Map> reg = rest.postForEntity("/api/auth/register", new HttpEntity<>(regReq, jsonHeaders()), Map.class);
        assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> loginReq = Map.of("login", login, "password", "123456");
        ResponseEntity<AuthResponse> loginResp = rest.exchange("/api/auth/login", HttpMethod.POST, new HttpEntity<>(loginReq, jsonHeaders()), AuthResponse.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return loginResp.getBody().token;
    }

    @Test
    void scenario01_duplicateRegistrationReturnsBadRequest() {
        Map<String, String> req = Map.of("fullName", "A", "login", "dupuser", "password", "123456");
        assertThat(rest.postForEntity("/api/auth/register", new HttpEntity<>(req, jsonHeaders()), Map.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rest.postForEntity("/api/auth/register", new HttpEntity<>(req, jsonHeaders()), Map.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void scenario02_loginWithWrongPasswordReturnsUnauthorized() {
        Map<String, String> req = Map.of("fullName", "A", "login", "login2", "password", "123456");
        rest.postForEntity("/api/auth/register", new HttpEntity<>(req, jsonHeaders()), Map.class);
        Map<String, String> badLogin = Map.of("login", "login2", "password", "wrongpass");
        try {
            ResponseEntity<String> resp = rest.postForEntity("/api/auth/login", new HttpEntity<>(badLogin, jsonHeaders()), String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        } catch (ResourceAccessException ex) {
            assertThat(ex.getMessage()).contains("cannot retry due to server authentication");
        }
    }

    @Test
    void scenario03_teacherEndpointWithoutTokenIsUnauthorized() {
        ResponseEntity<String> resp = rest.getForEntity("/api/teacher/preferences?type=semester", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void scenario04_teacherCanSaveSemesterPreference() {
        String token = registerAndLogin("Teacher 4", "teacher4");
        HttpHeaders bearer = jsonHeaders();
        bearer.setBearerAuth(token);
        PreferenceDto dto = new PreferenceDto();
        dto.type = "semester";
        dto.subject = "Math";
        dto.groups = "A1";
        ResponseEntity<PreferenceDto[]> resp = rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(List.of(dto), bearer), PreferenceDto[].class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    void scenario05_saveThenGetReturnsSameCount() {
        String token = registerAndLogin("Teacher 5", "teacher5");
        HttpHeaders bearer = jsonHeaders();
        bearer.setBearerAuth(token);
        PreferenceDto a = new PreferenceDto(); a.type = "semester"; a.subject = "S1";
        PreferenceDto b = new PreferenceDto(); b.type = "semester"; b.subject = "S2";
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(List.of(a, b), bearer), PreferenceDto[].class);
        ResponseEntity<PreferenceDto[]> getResp = rest.exchange("/api/teacher/preferences?type=semester", HttpMethod.GET, new HttpEntity<>(bearer), PreferenceDto[].class);
        assertThat(getResp.getBody()).hasSize(2);
    }

    @Test
    void scenario06_replaceByTypeBehaviorWorks() {
        String token = registerAndLogin("Teacher 6", "teacher6");
        HttpHeaders bearer = jsonHeaders();
        bearer.setBearerAuth(token);
        PreferenceDto a = new PreferenceDto(); a.type = "session"; a.subject = "Old";
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(List.of(a), bearer), PreferenceDto[].class);
        PreferenceDto b = new PreferenceDto(); b.type = "session"; b.subject = "New";
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(List.of(b), bearer), PreferenceDto[].class);
        ResponseEntity<PreferenceDto[]> getResp = rest.exchange("/api/teacher/preferences?type=session", HttpMethod.GET, new HttpEntity<>(bearer), PreferenceDto[].class);
        assertThat(getResp.getBody()).hasSize(1);
        assertThat(getResp.getBody()[0].subject).isEqualTo("New");
    }

    @Test
    void scenario07_adminEndpointRejectsTeacherToken() {
        String token = registerAndLogin("Teacher 7", "teacher7");
        HttpHeaders bearer = jsonHeaders();
        bearer.setBearerAuth(token);
        ResponseEntity<String> resp = rest.exchange("/api/admin/preferences", HttpMethod.GET, new HttpEntity<>(bearer), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void scenario08_teacherSessionAndSemesterSeparated() {
        String token = registerAndLogin("Teacher 8", "teacher8");
        HttpHeaders bearer = jsonHeaders();
        bearer.setBearerAuth(token);
        PreferenceDto sem = new PreferenceDto(); sem.type = "semester"; sem.subject = "Sem";
        PreferenceDto ses = new PreferenceDto(); ses.type = "session"; ses.subject = "Ses";
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(List.of(sem), bearer), PreferenceDto[].class);
        rest.exchange("/api/teacher/preferences", HttpMethod.POST, new HttpEntity<>(List.of(ses), bearer), PreferenceDto[].class);
        ResponseEntity<PreferenceDto[]> semResp = rest.exchange("/api/teacher/preferences?type=semester", HttpMethod.GET, new HttpEntity<>(bearer), PreferenceDto[].class);
        ResponseEntity<PreferenceDto[]> sesResp = rest.exchange("/api/teacher/preferences?type=session", HttpMethod.GET, new HttpEntity<>(bearer), PreferenceDto[].class);
        assertThat(semResp.getBody()).hasSize(1);
        assertThat(sesResp.getBody()).hasSize(1);
    }
}
