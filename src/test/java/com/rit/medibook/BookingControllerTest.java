package com.rit.medibook;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void healthReturnsUp() throws Exception {
        mvc.perform(get("/health"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void listsDoctors() throws Exception {
        mvc.perform(get("/api/doctors"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].speciality").value("Cardiology"));
    }

    @Test
    void bookingReturns201ThenDoubleBookingReturns409() throws Exception {
        String body = "{\"doctorId\":2,\"patientName\":\"Ravi\",\"slot\":\"15:00\"}";
        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.patientName").value("Ravi"));
        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isConflict());
    }

    @Test
    void unknownDoctorReturns404() throws Exception {
        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"doctorId\":99,\"patientName\":\"A\",\"slot\":\"09:00\"}"))
           .andExpect(status().isNotFound());
    }

    @Test
    void serviceRejectsBlankName() {
        BookingService s = new BookingService();
        assertThrows(BookingException.class, () -> s.book(1, " ", "09:00"));
        assertEquals(0, s.findAppointments().size());
    }
}
