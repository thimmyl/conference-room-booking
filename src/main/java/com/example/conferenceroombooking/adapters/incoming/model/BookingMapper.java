package com.example.conferenceroombooking.adapters.incoming.model;

import com.example.conferenceroombooking.core.domain.Booking;
import com.example.conferenceroombooking.core.domain.ConferenceRoom;
import com.example.conferenceroombooking.core.domain.ConferenceRoomBooking;

import java.util.List;

public final class BookingMapper {

  public static Booking map(BookingRequest bookingRequest) {
    return Booking.builder()
        .from(bookingRequest.from())
        .to(bookingRequest.to())
        .numberOfParticipants(bookingRequest.numberOfParticipants())
        .build();
  }

  public static BookingsResponse mapBookings(List<ConferenceRoomBooking> conferenceRoomBookings) {
    return new BookingsResponse(conferenceRoomBookings.stream()
        .map(BookingMapper::map)
        .toList());
  }

  public static BookingResponse map(ConferenceRoomBooking conferenceRoomBooking) {
    return BookingResponse.builder()
        .from(conferenceRoomBooking.getFromTimestamp())
        .to(conferenceRoomBooking.getToTimestamp())
        .conferenceRoom(conferenceRoomBooking.getConferenceRoom())
        .build();
  }

  public static AvailabilityResponse map(List<ConferenceRoom> availableConferenceRooms) {
    return new AvailabilityResponse(
        availableConferenceRooms.stream().map(ConferenceRoom::getName).toList());
  }
}
