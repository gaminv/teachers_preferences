package com.example.webapp.dto;

import java.util.List;

public class PreferenceDto {
    public Long      id;
    public String    type;          
    public String    subject;
    public String    groups;

    // Теперь List вместо String
    public List<String> days;
    public Integer      daysPriority;
    public String       times;
    public Integer      timesPriority;

    // Только для сессии
    public String    preferredDates;
    public String    avoidDates;
    public String    newYearPref;

    // Общие поля
    public String         loadType;
    public Integer        loadTypePriority;
    public String         buildingRoom;
    public Integer        buildingRoomPriority;
    public String         boardType;
    public Integer        boardTypePriority;
    public List<String>   computers;
    public Integer        computersPriority;
    public String         format;
    public Integer        formatPriority;
    public String         comments;
    public Integer        commentsPriority;

    // Поля преподавателя
    public String         teacherName;
    public String         teacherLogin;

    public PreferenceDto() {}

    public PreferenceDto(
        Long id,
        String type,
        String subject,
        String groups,
        List<String> days,
        Integer daysPriority,
        String times,
        Integer timesPriority,
        String preferredDates,
        String avoidDates,
        String newYearPref,
        String loadType,
        Integer loadTypePriority,
        String buildingRoom,
        Integer buildingRoomPriority,
        String boardType,
        Integer boardTypePriority,
        List<String> computers,
        Integer computersPriority,
        String format,
        Integer formatPriority,
        String comments,
        Integer commentsPriority,
        String teacherName,
        String teacherLogin
    ) {
        this.id                   = id;
        this.type                 = type;
        this.subject              = subject;
        this.groups               = groups;
        this.days                 = days;
        this.daysPriority         = daysPriority;
        this.times                = times;
        this.timesPriority        = timesPriority;
        this.preferredDates       = preferredDates;
        this.avoidDates           = avoidDates;
        this.newYearPref          = newYearPref;
        this.loadType             = loadType;
        this.loadTypePriority     = loadTypePriority;
        this.buildingRoom         = buildingRoom;
        this.buildingRoomPriority = buildingRoomPriority;
        this.boardType            = boardType;
        this.boardTypePriority    = boardTypePriority;
        this.computers            = computers;
        this.computersPriority    = computersPriority;
        this.format               = format;
        this.formatPriority       = formatPriority;
        this.comments             = comments;
        this.commentsPriority     = commentsPriority;
        this.teacherName          = teacherName;
        this.teacherLogin         = teacherLogin;
    }
}
