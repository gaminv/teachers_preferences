package com.example.webapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class PreferenceModelUnitTest {

    private Preference buildPreference() {
        Preference p = new Preference();
        Teacher t = new Teacher(1L, "John", "john");
        p.setTeacher(t);
        p.setType("semester");
        p.setSubject("Math");
        p.setGroups("A1");
        p.setDays("Пн, Вт");
        p.setDaysPriority(3);
        p.setTimes("08:00");
        p.setTimesPriority(2);
        p.setPreferredDates("2026-01-10");
        p.setAvoidDates("2026-01-12");
        p.setNewYearPref("none");
        p.setLoadType("compact");
        p.setLoadTypePriority(1);
        p.setBuildingRoom("1/101");
        p.setBuildingRoomPriority(4);
        p.setBoardType("marker");
        p.setBoardTypePriority(5);
        p.setComputers("Windows, Linux");
        p.setComputersPriority(2);
        p.setFormat("in-person");
        p.setFormatPriority(3);
        p.setComments("comment");
        p.setCommentsPriority(1);
        return p;
    }

    @Test
    void fullRoundtrip() {
        Preference p = buildPreference();
        assertEquals("semester", p.getType());
        assertEquals("Math", p.getSubject());
        assertEquals("A1", p.getGroups());
        assertEquals("Пн, Вт", p.getDays());
        assertEquals(3, p.getDaysPriority());
        assertEquals("08:00", p.getTimes());
        assertEquals(2, p.getTimesPriority());
        assertEquals("2026-01-10", p.getPreferredDates());
        assertEquals("2026-01-12", p.getAvoidDates());
        assertEquals("none", p.getNewYearPref());
        assertEquals("compact", p.getLoadType());
        assertEquals(1, p.getLoadTypePriority());
        assertEquals("1/101", p.getBuildingRoom());
        assertEquals(4, p.getBuildingRoomPriority());
        assertEquals("marker", p.getBoardType());
        assertEquals(5, p.getBoardTypePriority());
        assertEquals("Windows, Linux", p.getComputers());
        assertEquals(2, p.getComputersPriority());
        assertEquals("in-person", p.getFormat());
        assertEquals(3, p.getFormatPriority());
        assertEquals("comment", p.getComments());
        assertEquals(1, p.getCommentsPriority());
    }

    @ParameterizedTest
    @CsvSource({
        "semester,Math,A1",
        "session,Physics,B1",
        "semester,Programming,C1",
        "session,Databases,D1",
        "semester,Networks,E1",
        "session,ML,F1",
        "semester,OS,G1",
        "session,Security,H1",
        "semester,Algorithms,I1",
        "session,Statistics,J1",
        "semester,AI,K1",
        "session,Cloud,L1"
    })
    void typeSubjectGroupsRoundtrip(String type, String subject, String groups) {
        Preference p = new Preference();
        p.setType(type);
        p.setSubject(subject);
        p.setGroups(groups);
        assertEquals(type, p.getType());
        assertEquals(subject, p.getSubject());
        assertEquals(groups, p.getGroups());
    }

    @ParameterizedTest
    @CsvSource({
        "0,0,0,0,0,0,0,0",
        "1,1,1,1,1,1,1,1",
        "2,2,2,2,2,2,2,2",
        "3,3,3,3,3,3,3,3",
        "4,4,4,4,4,4,4,4",
        "5,5,5,5,5,5,5,5",
        "-1,-1,-1,-1,-1,-1,-1,-1",
        "6,6,6,6,6,6,6,6",
        "10,10,10,10,10,10,10,10"
    })
    void prioritiesStoreExactValues(int d, int t, int l, int b, int bt, int c, int f, int cm) {
        Preference p = new Preference();
        p.setDaysPriority(d);
        p.setTimesPriority(t);
        p.setLoadTypePriority(l);
        p.setBuildingRoomPriority(b);
        p.setBoardTypePriority(bt);
        p.setComputersPriority(c);
        p.setFormatPriority(f);
        p.setCommentsPriority(cm);
        assertEquals(d, p.getDaysPriority());
        assertEquals(t, p.getTimesPriority());
        assertEquals(l, p.getLoadTypePriority());
        assertEquals(b, p.getBuildingRoomPriority());
        assertEquals(bt, p.getBoardTypePriority());
        assertEquals(c, p.getComputersPriority());
        assertEquals(f, p.getFormatPriority());
        assertEquals(cm, p.getCommentsPriority());
    }
}
