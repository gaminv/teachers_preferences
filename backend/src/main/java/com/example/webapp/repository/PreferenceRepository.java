package com.example.webapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.webapp.model.Preference;
import com.example.webapp.model.Teacher;

@Repository
public interface PreferenceRepository extends JpaRepository<Preference, Long> {
    List<Preference> findAllByTeacherAndType(Teacher teacher, String type);
    void deleteAllByTeacherAndType(Teacher teacher, String type);
}
