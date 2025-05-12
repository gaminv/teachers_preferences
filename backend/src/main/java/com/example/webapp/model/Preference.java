package com.example.webapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "preferences")
public class Preference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(nullable = false)
    private String type;

    private String subject;
    private String groups;

    private String days;
    @Column(name = "days_priority")
    private Integer daysPriority;

    private String times;
    @Column(name = "times_priority")
    private Integer timesPriority;

    // Сессионные поля
    private String preferredDates;
    private String avoidDates;
    private String newYearPref;

    // Общие поля + приоритеты
    private String loadType;
    @Column(name = "load_type_priority")
    private Integer loadTypePriority;

    private String buildingRoom;
    @Column(name = "building_room_priority")
    private Integer buildingRoomPriority;

    private String boardType;
    @Column(name = "board_type_priority")
    private Integer boardTypePriority;

    // CSV-строка для списка компьютеров + приоритет
    @Column(name = "computers", length = 255)
    private String computers;
    @Column(name = "computers_priority")
    private Integer computersPriority;

    private String format;
    @Column(name = "format_priority")
    private Integer formatPriority;

    private String comments;
    @Column(name = "comments_priority")
    private Integer commentsPriority;

    public Preference() {}

    // === Getters & Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getGroups() { return groups; }
    public void setGroups(String groups) { this.groups = groups; }

    public String getDays() { return days; }
    public void setDays(String days) { this.days = days; }

    public Integer getDaysPriority() { return daysPriority; }
    public void setDaysPriority(Integer daysPriority) { this.daysPriority = daysPriority; }

    public String getTimes() { return times; }
    public void setTimes(String times) { this.times = times; }

    public Integer getTimesPriority() { return timesPriority; }
    public void setTimesPriority(Integer timesPriority) { this.timesPriority = timesPriority; }

    public String getPreferredDates() { return preferredDates; }
    public void setPreferredDates(String preferredDates) { this.preferredDates = preferredDates; }

    public String getAvoidDates() { return avoidDates; }
    public void setAvoidDates(String avoidDates) { this.avoidDates = avoidDates; }

    public String getNewYearPref() { return newYearPref; }
    public void setNewYearPref(String newYearPref) { this.newYearPref = newYearPref; }

    public String getLoadType() { return loadType; }
    public void setLoadType(String loadType) { this.loadType = loadType; }

    public Integer getLoadTypePriority() { return loadTypePriority; }
    public void setLoadTypePriority(Integer loadTypePriority) { this.loadTypePriority = loadTypePriority; }

    public String getBuildingRoom() { return buildingRoom; }
    public void setBuildingRoom(String buildingRoom) { this.buildingRoom = buildingRoom; }

    public Integer getBuildingRoomPriority() { return buildingRoomPriority; }
    public void setBuildingRoomPriority(Integer buildingRoomPriority) { this.buildingRoomPriority = buildingRoomPriority; }

    public String getBoardType() { return boardType; }
    public void setBoardType(String boardType) { this.boardType = boardType; }

    public Integer getBoardTypePriority() { return boardTypePriority; }
    public void setBoardTypePriority(Integer boardTypePriority) { this.boardTypePriority = boardTypePriority; }

    public String getComputers() { return computers; }
    public void setComputers(String computers) { this.computers = computers; }

    public Integer getComputersPriority() { return computersPriority; }
    public void setComputersPriority(Integer computersPriority) { this.computersPriority = computersPriority; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public Integer getFormatPriority() { return formatPriority; }
    public void setFormatPriority(Integer formatPriority) { this.formatPriority = formatPriority; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Integer getCommentsPriority() { return commentsPriority; }
    public void setCommentsPriority(Integer commentsPriority) { this.commentsPriority = commentsPriority; }
}
