package com.example.conferenceroombooking.adapters.incoming;

import com.example.conferenceroombooking.adapters.incoming.model.*;
import com.example.conferenceroombooking.core.domain.ConferenceRoom;
import com.example.conferenceroombooking.core.domain.ConferenceRoomBooking;
import com.example.conferenceroombooking.core.service.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/conference-room")
@AllArgsConstructor
public class BookingController {

  private final BookingService bookingService;

  @PostMapping("/book")
  public ResponseEntity<BookingResponse> bookConferenceRoom(@RequestBody BookingRequest bookingRequest) {
    ConferenceRoomBooking conferenceRoomBooking = bookingService.bookConferenceRoom(BookingMapper.map(bookingRequest));
    return ResponseEntity.status(HttpStatus.CREATED).body(BookingMapper.map(conferenceRoomBooking));
  }

  @GetMapping("/availability")
  public ResponseEntity<AvailabilityResponse> getConferenceRoomAvailability(
      @RequestParam LocalDateTime from, @RequestParam LocalDateTime to) {
    List<ConferenceRoom> availableConferenceRooms = bookingService.getAvailableConferenceRooms(from, to);
    return ResponseEntity.status(HttpStatus.OK).body(BookingMapper.map(availableConferenceRooms));
  }

  @GetMapping("/bookings")
  public ResponseEntity<BookingsResponse> getConferenceRoomBookings(
      @RequestParam LocalDateTime from, @RequestParam LocalDateTime to) {
    List<ConferenceRoomBooking> conferenceRoomBookings = bookingService.getConferenceRoomBookings(from, to);
    return ResponseEntity.status(HttpStatus.OK).body(BookingMapper.mapBookings(conferenceRoomBookings));
  }
}
