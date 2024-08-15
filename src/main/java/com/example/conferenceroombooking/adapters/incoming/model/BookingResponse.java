package com.example.conferenceroombooking.adapters.incoming.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BookingResponse(LocalDateTime from, LocalDateTime to, String conferenceRoom) {
}