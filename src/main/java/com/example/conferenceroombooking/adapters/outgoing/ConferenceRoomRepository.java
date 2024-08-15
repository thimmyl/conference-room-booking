package com.example.conferenceroombooking.adapters.outgoing;

import com.example.conferenceroombooking.core.domain.ConferenceRoom;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConferenceRoomRepository extends CrudRepository<ConferenceRoom, Long> {
  List<ConferenceRoom> findAllByNameNotIn(List<String> names);
}
