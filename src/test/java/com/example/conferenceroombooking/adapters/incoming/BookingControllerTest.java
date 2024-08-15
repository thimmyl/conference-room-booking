package com.example.conferenceroombooking.adapters.incoming;

import com.example.conferenceroombooking.adapters.incoming.model.BookingRequest;
import com.example.conferenceroombooking.core.domain.Booking;
import com.example.conferenceroombooking.core.domain.ConferenceRoom;
import com.example.conferenceroombooking.core.domain.ConferenceRoomBooking;
import com.example.conferenceroombooking.core.domain.NoConferenceRoomAvailableException;
import com.example.conferenceroombooking.core.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
class BookingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  BookingService bookingService;

  @Test
  void shouldBookConferenceRoom() throws Exception {
    BookingRequest request = BookingRequest.builder()
        .from(LocalDateTime.parse("2024-08-12T10:00:00"))
        .to(LocalDateTime.parse("2024-08-12T12:00:00"))
        .numberOfParticipants(10)
        .build();
    ConferenceRoomBooking booking = ConferenceRoomBooking.builder()
        .fromTimestamp(request.from())
        .toTimestamp(request.to())
        .numberOfParticipants(10)
        .conferenceRoom("Room A").build();

    when(bookingService.bookConferenceRoom(any(Booking.class)))
        .thenReturn(booking);

    mockMvc.perform(post("/api/conference-room/book")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"from\":\"2024-08-12T10:00:00\",\"to\":\"2024-08-12T12:00:00\",\"numberOfParticipants\":10}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.from", is("2024-08-12T10:00:00")))
        .andExpect(jsonPath("$.to", is("2024-08-12T12:00:00")))
        .andExpect(jsonPath("$.conferenceRoom", is("Room A")));
  }

  @Test
  void shouldGetConflictWhenBookConferenceRoomOnNoConferenceRoomAvailableException() throws Exception {
    when(bookingService.bookConferenceRoom(any(Booking.class)))
        .thenThrow(new NoConferenceRoomAvailableException("No room"));

    mockMvc.perform(post("/api/conference-room/book")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"from\":\"2024-08-12T10:00:00\",\"to\":\"2024-08-12T12:00:00\",\"numberOfParticipants\":10}"))
        .andExpect(status().isConflict())
        .andExpect(content().string("No room"));
  }

  @Test
  void shouldGetBadRequestWhenBookConferenceRoomOnIllegalArgumentException() throws Exception {
    when(bookingService.bookConferenceRoom(any(Booking.class)))
        .thenThrow(new IllegalArgumentException("Invalid input"));

    mockMvc.perform(post("/api/conference-room/book")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"from\":\"2024-08-12T10:00:00\",\"to\":\"2024-08-12T12:00:00\",\"numberOfParticipants\":10}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid input"));
  }

  @Test
  void shouldGetInternalServerErrorWhenBookConferenceRoomOnIllegalStateException() throws Exception {
    when(bookingService.bookConferenceRoom(any(Booking.class)))
        .thenThrow(new IllegalStateException("No rooms configured"));

    mockMvc.perform(post("/api/conference-room/book")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"from\":\"2024-08-12T10:00:00\",\"to\":\"2024-08-12T12:00:00\",\"numberOfParticipants\":10}"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("No rooms configured"));
  }

  @Test
  void shouldGetConferenceRoomAvailability() throws Exception {
    ConferenceRoom ConferenceRoomA = ConferenceRoom.builder().name("Room A").maxCapacity(7).build();
    ConferenceRoom conferenceRoomB = ConferenceRoom.builder().name("Room B").maxCapacity(10).build();

    LocalDateTime from = LocalDateTime.parse("2024-08-12T10:00:00");
    LocalDateTime to = LocalDateTime.parse("2024-08-12T12:00:00");
    when(bookingService.getAvailableConferenceRooms(from, to))
        .thenReturn(List.of(ConferenceRoomA, conferenceRoomB));

    mockMvc.perform(get("/api/conference-room/availability")
            .param("from", from.toString())
            .param("to", to.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.conferenceRooms", hasSize(2)))
        .andExpect(jsonPath("$.conferenceRooms", containsInAnyOrder("Room A", "Room B")));
  }

  @Test
  void shouldGetConferenceRoomBookings() throws Exception {
    LocalDateTime from = LocalDateTime.parse("2024-08-12T10:00:00");
    LocalDateTime to = LocalDateTime.parse("2024-08-12T12:00:00");

    when(bookingService.getConferenceRoomBookings(from, to))
        .thenReturn(List.of(ConferenceRoomBooking.builder()
            .id(1L)
            .fromTimestamp(from)
            .toTimestamp(to)
            .numberOfParticipants(5)
            .conferenceRoom("Room A")
            .build()));

    mockMvc.perform(get("/api/conference-room/bookings")
        .param("from", from.toString())
        .param("to", to.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookings", hasSize(1)))
        .andExpect(jsonPath("$.bookings[0].conferenceRoom", is("Room A")))
        .andExpect(jsonPath("$.bookings[0].from", is("2024-08-12T10:00:00")))
        .andExpect(jsonPath("$.bookings[0].to", is("2024-08-12T12:00:00")));
  }
}