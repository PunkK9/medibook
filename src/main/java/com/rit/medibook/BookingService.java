package com.rit.medibook;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory doctors and appointments (no database). */
@Service
public class BookingService {

    private final List<Doctor> doctors = List.of(
            new Doctor(1, "Dr. Meera Rao MD", "Cardiology", List.of("09:00", "10:00", "11:00")),
            new Doctor(2, "Dr. Arjun Nair", "Dermatology", List.of("14:00", "15:00", "16:00")),
            new Doctor(3, "Dr. Sana Khan", "Pediatrics", List.of("10:00", "11:30", "13:00")));

    private final List<Appointment> appointments = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong();

    public List<Doctor> findDoctors() {
        return doctors;
    }

    public List<Appointment> findAppointments() {
        return new ArrayList<>(appointments);
    }

    public synchronized Appointment book(long doctorId, String patientName, String slot) {
        Doctor doctor = doctors.stream().filter(d -> d.id() == doctorId).findFirst()
                .orElseThrow(() -> new BookingException(404, "Doctor not found"));
        if (patientName == null || patientName.isBlank()) {
            throw new BookingException(400, "patientName is required");
        }
        if (!doctor.slots().contains(slot)) {
            throw new BookingException(400, "Slot not offered by this doctor");
        }
        boolean taken = appointments.stream()
                .anyMatch(a -> a.doctorId() == doctorId && a.slot().equals(slot));
        if (taken) {
            throw new BookingException(409, "Slot already booked");
        }
        Appointment appt = new Appointment(sequence.incrementAndGet(), doctorId, patientName.trim(), slot);
        appointments.add(appt);
        return appt;
    }
}
