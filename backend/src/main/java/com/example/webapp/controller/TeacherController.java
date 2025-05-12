package com.example.webapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.webapp.dto.PreferenceDto;
import com.example.webapp.service.TeacherService;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired private TeacherService teacherService;

    /** GET  /api/teacher/preferences?type=semester */
    @GetMapping("/preferences")
    public ResponseEntity<List<PreferenceDto>> getPrefs(
            @RequestParam(defaultValue = "semester") String type,
            Authentication auth) {
        // из JwtAuthFilter principal — это наш UserDetails, у которого есть getId()
        Long userId = ((com.example.webapp.model.User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(teacherService.getPreferences(userId, type));
    }

    /** POST /api/teacher/preferences */
    @PostMapping("/preferences")
    public ResponseEntity<List<PreferenceDto>> savePrefs(
            @RequestBody List<PreferenceDto> dtos,
            Authentication auth) {
        Long userId = ((com.example.webapp.model.User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(teacherService.savePreferences(userId, dtos));
    }
}
