// src/main/java/com/example/webapp/controller/AdminController.java
package com.example.webapp.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.webapp.dto.PreferenceDto;
import com.example.webapp.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private AdminService adminService;

    /** Возвращает все предпочтения */
    @GetMapping("/preferences")
    public ResponseEntity<List<PreferenceDto>> getAll() {
        return ResponseEntity.ok(adminService.getAllPreferences());
    }

    /** Выгружаем Excel */
    @GetMapping("/preferences/export")
    public ResponseEntity<ByteArrayResource> export() throws IOException {
        ByteArrayResource excel = adminService.exportPreferencesToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=preferences.xlsx");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(headers)
                .body(excel);
    }
}
