package com.rit.medibook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

@RestController
public class BookingController {

    private final BookingService service;

    @Value("${app.message:Welcome to Medibook}")
    private String message;

    @Value("${app.version:1.0.0}")
    private String version;

    public BookingController(BookingService service) {
        this.service = service;
    }

    /** Endpoint 1: doctors with their available slots */
    @GetMapping("/api/doctors")
    public List<Doctor> doctors() {
        return service.findDoctors();
    }

    /** Endpoint 2: all booked appointments */
    @GetMapping("/api/appointments")
    public List<Appointment> appointments() {
        return service.findAppointments();
    }

    /** Endpoint 3: book a slot, body: {"doctorId":1,"patientName":"Ravi","slot":"09:00"} */
    @PostMapping("/api/appointments")
    public ResponseEntity<Appointment> book(@RequestBody Map<String, Object> body) {
        long doctorId;
        try {
            doctorId = Long.parseLong(String.valueOf(body.get("doctorId")));
        } catch (NumberFormatException e) {
            throw new BookingException(400, "doctorId must be a number");
        }
        Appointment a = service.book(doctorId,
                (String) body.get("patientName"), (String) body.get("slot"));
        return ResponseEntity.status(HttpStatus.CREATED).body(a);
    }

    /** Config info; values come from the ConfigMap in Kubernetes. Also shows which pod answered. */
    @GetMapping("/api/info")
    public Map<String, String> info() throws Exception {
        return Map.of("message", message, "version", version,
                "pod", InetAddress.getLocalHost().getHostName());
    }

    /** Health: uptime + readiness, used by K8s probes */
    @GetMapping("/health")
    public Map<String, Object> health() {
        long uptimeSec = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        return Map.of("status", "UP", "uptimeSeconds", uptimeSec);
    }

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<Map<String, String>> handle(BookingException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
    }
}
