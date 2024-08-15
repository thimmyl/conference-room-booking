package com.example.conferenceroombooking.core.service;

import com.example.conferenceroombooking.adapters.outgoing.ConferenceRoomBookingRepository;
import com.example.conferenceroombooking.adapters.outgoing.ConferenceRoomRepository;
import com.example.conferenceroombooking.core.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

  private static final List<MaintenanceTime> MAINTENANCE_TIMES = List.of(
      new MaintenanceTime(LocalTime.of(9, 0), LocalTime.of(9, 15)),
      new MaintenanceTime(LocalTime.of(13, 0), LocalTime.of(13, 15)),
      new MaintenanceTime(LocalTime.of(17, 0), LocalTime.of(17, 15)));

  private final Clock clock;
  private final ConferenceRoomRepository conferenceRoomRepository;
  private final ConferenceRoomBookingRepository conferenceRoomBookingRepository;

  @Override
  public ConferenceRoomBooking bookConferenceRoom(Booking booking) {
    validateBooking(booking);
    return getAvailableConferenceRooms(booking.from(), booking.to()).stream()
        .filter(conferenceRoom -> conferenceRoom.getMaxCapacity() >= booking.numberOfParticipants())
        .min(Comparator.comparingInt(ConferenceRoom::getMaxCapacity))
        .map(conferenceRoom -> conferenceRoomBookingRepository.save(
            ConferenceRoomBooking.builder()
                .fromTimestamp(booking.from())
                .toTimestamp(booking.to())
                .numberOfParticipants(booking.numberOfParticipants())
                .conferenceRoom(conferenceRoom.getName())
                .build()))
        .orElseThrow(() -> new NoConferenceRoomAvailableException("There is no available conference rooms given your requested time frame"));
  }

  @Override
  public List<ConferenceRoom> getAvailableConferenceRooms(LocalDateTime from, LocalDateTime to) {
    LocalDateTime now = LocalDateTime.now(clock);
    validateBookingDate(from.toLocalDate(), to.toLocalDate(), now);
    validateBookingTime(from, to, now);
    List<String> conferenceRoomConflictNames = conferenceRoomBookingRepository.findConferenceRoomConflicts(from, to).stream().map(ConferenceRoomBooking::getConferenceRoom).toList();

    return conferenceRoomConflictNames.isEmpty()
        ? StreamSupport.stream(conferenceRoomRepository.findAll().spliterator(), false).toList()
        : conferenceRoomRepository.findAllByNameNotIn(conferenceRoomConflictNames);
  }

  @Override
  public List<ConferenceRoomBooking> getConferenceRoomBookings(LocalDateTime from, LocalDateTime to) {
    return conferenceRoomBookingRepository.findConferenceRoomConflicts(from, to);
  }

  private void validateBooking(Booking booking) {
    validateParticipants(booking.numberOfParticipants());
    LocalDateTime now = LocalDateTime.now(clock);
    validateBookingDate(booking, now);
    validateBookingTime(booking, now);
  }

  private void validateParticipants(int numberOfParticipants) {
    List<Integer> maxCapacities = StreamSupport.stream(conferenceRoomRepository.findAll().spliterator(), false)
        .map(ConferenceRoom::getMaxCapacity)
        .toList();
    if (maxCapacities.isEmpty()) {
      throw new IllegalStateException("There are no available conference rooms available");
    }
    Integer maxCapacity = Collections.max(maxCapacities);
    if (numberOfParticipants < 1 || numberOfParticipants > maxCapacity) {
      throw new IllegalArgumentException("Number of participants must be greater than or equal end 1 and be less than the max capacity of the rooms which is " + maxCapacity);
    }
  }

  private static void validateBookingDate(Booking booking, LocalDateTime now) {
    validateBookingDate(booking.from().toLocalDate(), booking.to().toLocalDate(), now);
  }

  private static void validateBookingDate(LocalDate from, LocalDate to, LocalDateTime now) {
    if (!from.equals(now.toLocalDate()) || !to.equals(now.toLocalDate())) {
      throw new IllegalArgumentException("Bookings are only allowed with the current date: " + now.toLocalDate());
    }
  }

  private static void validateBookingTime(Booking booking, LocalDateTime now) {
    validateBookingTime(booking.from(), booking.to(), now);
  }

  private static void validateBookingTime(LocalDateTime from, LocalDateTime to, LocalDateTime now) {
    if ((!from.isAfter(now) && !from.equals(now)) || !to.isAfter(from)) {
      throw new IllegalArgumentException(
          "Bookings are not allowed end be in the past, current timestamp %s, times provided start: %s, end: %s".formatted(now, from, to));
    }
    MAINTENANCE_TIMES.forEach(maintenanceTime -> {
      if (to.toLocalTime().isBefore(maintenanceTime.start()) || to.toLocalTime().equals(maintenanceTime.start())) {
        return;
      }
      if (from.toLocalTime().isAfter(maintenanceTime.end()) || from.toLocalTime().equals(maintenanceTime.end())) {
        return;
      }
      throw new IllegalArgumentException(
          "Booking is within a maintenance window for the room and can therefore not be booked, in this case: %s. The maintenance windows are: %s"
              .formatted(maintenanceTime, MAINTENANCE_TIMES));
    });
  }
}
