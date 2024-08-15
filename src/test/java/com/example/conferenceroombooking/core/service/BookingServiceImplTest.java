package com.example.conferenceroombooking.core.service;

import com.example.conferenceroombooking.adapters.outgoing.ConferenceRoomBookingRepository;
import com.example.conferenceroombooking.adapters.outgoing.ConferenceRoomRepository;
import com.example.conferenceroombooking.core.domain.Booking;
import com.example.conferenceroombooking.core.domain.ConferenceRoom;
import com.example.conferenceroombooking.core.domain.ConferenceRoomBooking;
import com.example.conferenceroombooking.core.domain.NoConferenceRoomAvailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

  @Mock
  private Clock clock;

  @Mock
  private ConferenceRoomRepository conferenceRoomRepository;

  @Mock
  private ConferenceRoomBookingRepository conferenceRoomBookingRepository;

  @InjectMocks
  private BookingServiceImpl bookingServiceImpl;

  @Test
  void shouldBookConferenceRoom() {
    Booking booking = Booking.builder()
        .from(LocalDateTime.parse("2024-08-12T10:00:00"))
        .to(LocalDateTime.parse("2024-08-12T12:00:00"))
        .numberOfParticipants(10)
        .build();
    ConferenceRoom conferenceRoomA = ConferenceRoom.builder().name("Room A").maxCapacity(5).build();
    ConferenceRoom conferenceRoomB = ConferenceRoom.builder().name("Room B").maxCapacity(20).build();
    LocalDateTime now = LocalDateTime.parse("2024-08-12T10:00:00");

    when(conferenceRoomRepository.findAll()).thenReturn(List.of(conferenceRoomA, conferenceRoomB));
    when(clock.instant()).thenReturn(now.toInstant(ZoneOffset.UTC));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    when(conferenceRoomRepository.findAll()).thenReturn(List.of(conferenceRoomA, conferenceRoomB));
    when(conferenceRoomBookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ConferenceRoomBooking actual = bookingServiceImpl.bookConferenceRoom(booking);

    assertThat(actual).isNotNull();
    assertThat(actual.getFromTimestamp()).isEqualTo(booking.from());
    assertThat(actual.getToTimestamp()).isEqualTo(booking.to());
    assertThat(actual.getNumberOfParticipants()).isEqualTo(booking.numberOfParticipants());
    assertThat(actual.getConferenceRoom()).isEqualTo(conferenceRoomB.getName());
  }

  @Test
  void shouldThrowExceptionWhenNoConferenceRoomAvailableOnBookConferenceRoom() {
    Booking booking = Booking.builder()
        .from(LocalDateTime.parse("2024-08-12T10:00:00"))
        .to(LocalDateTime.parse("2024-08-12T12:00:00"))
        .numberOfParticipants(10)
        .build();
    ConferenceRoom roomA = ConferenceRoom.builder().name("Room A").maxCapacity(10).build();
    ConferenceRoom roomB = ConferenceRoom.builder().name("Room B").maxCapacity(15).build();
    LocalDateTime now = LocalDateTime.parse("2024-08-12T10:00:00");

    when(conferenceRoomRepository.findAll()).thenReturn(List.of(roomA, roomB));
    when(clock.instant()).thenReturn(now.toInstant(ZoneOffset.UTC));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);

    assertThatThrownBy(() -> bookingServiceImpl.bookConferenceRoom(booking))
        .isInstanceOf(NoConferenceRoomAvailableException.class)
        .hasMessageContaining("There is no available conference rooms given your requested time frame");
  }

  @Test
  void shouldGetAvailableConferenceRooms() {
    LocalDateTime now = LocalDateTime.parse("2024-08-12T08:00:00");
    LocalDateTime from = LocalDateTime.parse("2024-08-12T10:00:00");
    LocalDateTime to = LocalDateTime.parse("2024-08-12T12:00:00");
    ConferenceRoomBooking conferenceRoomBooking = ConferenceRoomBooking.builder().conferenceRoom("Room A").build();
    ConferenceRoom roomB = ConferenceRoom.builder().name("Room B").maxCapacity(15).build();

    when(clock.instant()).thenReturn(now.toInstant(ZoneOffset.UTC));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    when(conferenceRoomBookingRepository.findConferenceRoomConflicts(from, to))
        .thenReturn(List.of(conferenceRoomBooking));
    when(conferenceRoomRepository.findAllByNameNotIn(any())).thenReturn(List.of(roomB));

    List<ConferenceRoom> availableRooms = bookingServiceImpl.getAvailableConferenceRooms(from, to);

    assertThat(availableRooms).containsExactlyInAnyOrder(roomB);
  }

  @Test
  void shouldThrowExceptionWhenInvalidBookingDateOnGetAvailableConferenceRooms() {
    LocalDateTime now = LocalDateTime.parse("2024-08-12T08:00:00");
    LocalDateTime from = LocalDateTime.parse("2024-08-11T10:00:00");
    LocalDateTime to = LocalDateTime.parse("2024-08-11T12:00:00");

    when(clock.instant()).thenReturn(now.toInstant(ZoneOffset.UTC));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);

    assertThatThrownBy(() -> bookingServiceImpl.getAvailableConferenceRooms(from, to))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Bookings are only allowed with the current date");
  }

  @Test
  void shouldThrowExceptionWhenInvalidBookingTimeOnGetAvailableConferenceRooms() {
    LocalDateTime now = LocalDateTime.parse("2024-08-12T08:00:00");
    LocalDateTime from = LocalDateTime.parse("2024-08-12T09:00:00");
    LocalDateTime to = LocalDateTime.parse("2024-08-12T09:10:00");

    when(clock.instant()).thenReturn(now.toInstant(ZoneOffset.UTC));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);

    assertThatThrownBy(() -> bookingServiceImpl.getAvailableConferenceRooms(from, to))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Booking is within a maintenance window for the room and can therefore not be booked, in this case");
  }

  @ParameterizedTest
  @MethodSource("participantsMethodSource")
  void shouldValidateParticipants(Booking booking, boolean valid) {
    LocalDateTime now = LocalDateTime.parse("2024-08-12T08:00:00");
    ConferenceRoom roomA = ConferenceRoom.builder().name("Room A").maxCapacity(10).build();
    ConferenceRoom roomB = ConferenceRoom.builder().name("Room B").maxCapacity(15).build();

    when(conferenceRoomRepository.findAll()).thenReturn(List.of(roomA, roomB));

    if (valid) {
      when(clock.instant()).thenReturn(now.toInstant(ZoneOffset.UTC));
      when(clock.getZone()).thenReturn(ZoneOffset.UTC);
      when(conferenceRoomBookingRepository.findConferenceRoomConflicts(booking.from(), booking.to())).thenReturn(List.of());
      when(conferenceRoomBookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
      bookingServiceImpl.bookConferenceRoom(booking);
    } else {
      assertThatThrownBy(() -> bookingServiceImpl.bookConferenceRoom(booking))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Number of participants must be greater than or equal end 1 and be less than the max capacity of the rooms which is 15");
    }
  }

  @ParameterizedTest
  @MethodSource("bookingDatesMethodSource")
  void shouldValidateBookingDate(Booking booking, boolean valid) {
    LocalDateTime now = LocalDateTime.parse("2024-08-12T01:00:00");
    ConferenceRoom roomA = ConferenceRoom.builder().name("Room A").maxCapacity(10).build();

    when(conferenceRoomRepository.findAll()).thenReturn(List.of(roomA));
    when(clock.instant()).thenReturn(now.toInstant(ZoneOffset.UTC));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);

    if (valid) {
      when(conferenceRoomBookingRepository.findConferenceRoomConflicts(booking.from(), booking.to()))
          .thenReturn(List.of());
      when(conferenceRoomBookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
      bookingServiceImpl.bookConferenceRoom(booking);
    } else {
      assertThatThrownBy(() -> bookingServiceImpl.bookConferenceRoom(booking))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Bookings are only allowed with the current date");
    }
  }

  @ParameterizedTest
  @MethodSource("bookingTimesMethodSource")
  void shouldValidateBookingTimes(Booking booking, boolean valid) {
    LocalDateTime now = LocalDateTime.parse("2024-08-12T08:00:00");
    ConferenceRoom roomA = ConferenceRoom.builder().name("Room A").maxCapacity(10).build();

    when(conferenceRoomRepository.findAll()).thenReturn(List.of(roomA));
    when(clock.instant()).thenReturn(now.toInstant(ZoneOffset.UTC));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);

    if (valid) {
      when(conferenceRoomBookingRepository.findConferenceRoomConflicts(booking.from(), booking.to()))
          .thenReturn(List.of());
      when(conferenceRoomBookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
      bookingServiceImpl.bookConferenceRoom(booking);

    } else {
      assertThatThrownBy(() -> bookingServiceImpl.bookConferenceRoom(booking))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Booking is within a maintenance window for the room and can therefore not be booked, in this case");
    }
  }

  private static Stream<Arguments> participantsMethodSource() {
    return Stream.of(
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T08:45:00")).to(LocalDateTime.parse("2024-08-12T09:00:00")).numberOfParticipants(5).build(), true),
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T08:45:00")).to(LocalDateTime.parse("2024-08-12T09:00:00")).numberOfParticipants(12).build(), true),
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T08:45:00")).to(LocalDateTime.parse("2024-08-12T09:00:00")).numberOfParticipants(17).build(), false)
    );
  }

  private static Stream<Arguments> bookingDatesMethodSource() {
    return Stream.of(
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T08:45:00")).to(LocalDateTime.parse("2024-08-12T09:00:00")).numberOfParticipants(5).build(), true),
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T08:45:00")).to(LocalDateTime.parse("2024-08-14T09:00:00")).numberOfParticipants(5).build(), false),
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-11T08:45:00")).to(LocalDateTime.parse("2024-08-12T09:00:00")).numberOfParticipants(5).build(), false)
    );
  }

  private static Stream<Arguments> bookingTimesMethodSource() {
    return Stream.of(
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T08:45:00")).to(LocalDateTime.parse("2024-08-12T09:00:00")).numberOfParticipants(5).build(), true),
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T08:45:00")).to(LocalDateTime.parse("2024-08-12T09:15:00")).numberOfParticipants(5).build(), false),
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T09:15:00")).to(LocalDateTime.parse("2024-08-12T09:30:00")).numberOfParticipants(5).build(), true),
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T08:45:00")).to(LocalDateTime.parse("2024-08-12T09:10:00")).numberOfParticipants(5).build(), false),
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T09:10:00")).to(LocalDateTime.parse("2024-08-12T09:30:00")).numberOfParticipants(5).build(), false),
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T08:45:00")).to(LocalDateTime.parse("2024-08-12T14:00:00")).numberOfParticipants(5).build(), false),
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T08:45:00")).to(LocalDateTime.parse("2024-08-12T13:10:00")).numberOfParticipants(5).build(), false),
        Arguments.of(Booking.builder().from(LocalDateTime.parse("2024-08-12T09:10:00")).to(LocalDateTime.parse("2024-08-12T14:00:00")).numberOfParticipants(5).build(), false)
    );
  }
}
