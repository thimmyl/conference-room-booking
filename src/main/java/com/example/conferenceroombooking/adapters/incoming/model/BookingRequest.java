package com.example.conferenceroombooking.adapters.incoming.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BookingRequest(LocalDateTime from, LocalDateTime to, int numberOfParticipants) {
}
