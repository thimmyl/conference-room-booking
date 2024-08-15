package com.example.conferenceroombooking.core.service;

import com.example.conferenceroombooking.core.domain.Booking;
import com.example.conferenceroombooking.core.domain.ConferenceRoom;
import com.example.conferenceroombooking.core.domain.ConferenceRoomBooking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

  ConferenceRoomBooking bookConferenceRoom(Booking booking);
  List<ConferenceRoom> getAvailableConferenceRooms(LocalDateTime from, LocalDateTime to);
  List<ConferenceRoomBooking> getConferenceRoomBookings(LocalDateTime from, LocalDateTime to);
}
