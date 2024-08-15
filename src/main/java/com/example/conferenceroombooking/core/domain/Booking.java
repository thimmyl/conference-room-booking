package com.example.conferenceroombooking.core.domain;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record Booking(LocalDateTime from, LocalDateTime to, int numberOfParticipants) {
}
