package com.example.conferenceroombooking.adapters.incoming.model;

import com.example.conferenceroombooking.core.domain.Booking;
import com.example.conferenceroombooking.core.domain.ConferenceRoom;
import com.example.conferenceroombooking.core.domain.ConferenceRoomBooking;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {


  @Test
  void shouldMapBookingRequestToBooking() {
    BookingRequest bookingRequest = BookingRequest.builder()
        .from(LocalDateTime.parse("2024-08-12T10:00:00"))
        .to(LocalDateTime.parse("2024-08-12T12:00:00"))
        .numberOfParticipants(10)
        .build();

    Booking booking = BookingMapper.map(bookingRequest);

    assertThat(booking).isNotNull();
    assertThat(booking.from()).isEqualTo("2024-08-12T10:00:00");
    assertThat(booking.to()).isEqualTo("2024-08-12T12:00:00");
    assertThat(booking.numberOfParticipants()).isEqualTo(10);
  }

  @Test
  void shouldMapConferenceRoomBookingToBookingResponse() {
    ConferenceRoomBooking conferenceRoomBooking = ConferenceRoomBooking.builder()
        .fromTimestamp(LocalDateTime.parse("2024-08-12T10:00:00"))
        .toTimestamp(LocalDateTime.parse("2024-08-12T12:00:00"))
        .conferenceRoom("Room A").build();

    BookingResponse bookingResponse = BookingMapper.map(conferenceRoomBooking);

    assertThat(bookingResponse).isNotNull();
    assertThat(bookingResponse.from()).isEqualTo("2024-08-12T10:00:00");
    assertThat(bookingResponse.to()).isEqualTo("2024-08-12T12:00:00");
    assertThat(bookingResponse.conferenceRoom()).isEqualTo("Room A");
  }

  @Test
  void shouldMapAvailableConferenceRoomsToAvailabilityResponse() {
    List<ConferenceRoom> availableConferenceRooms = List.of(
        ConferenceRoom.builder()
            .name("Room A")
            .maxCapacity(7)
            .build(),
        ConferenceRoom.builder()
            .name("Room B")
            .maxCapacity(10)
            .build());

    AvailabilityResponse availabilityResponse = BookingMapper.map(availableConferenceRooms);

    assertThat(availabilityResponse).isNotNull();
    assertThat(availabilityResponse.conferenceRooms())
        .containsExactlyInAnyOrder("Room A", "Room B");
  }
}