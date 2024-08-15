package com.example.conferenceroombooking.adapters.outgoing;

import com.example.conferenceroombooking.core.domain.ConferenceRoomBooking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConferenceRoomBookingRepository extends CrudRepository<ConferenceRoomBooking, Long> {
  @Query("SELECT crb FROM ConferenceRoomBooking crb WHERE crb.fromTimestamp <= :toRequest AND crb.toTimestamp >= :fromRequest")
  List<ConferenceRoomBooking> findConferenceRoomConflicts(LocalDateTime fromRequest, LocalDateTime toRequest);
}
