package com.example.conferenceroombooking.core.domain;

public class NoConferenceRoomAvailableException extends RuntimeException {
  public NoConferenceRoomAvailableException(String message) {
    super(message);
  }
}
