package com.rit.medibook;

public record Appointment(long id, long doctorId, String patientName, String slot) {
}
