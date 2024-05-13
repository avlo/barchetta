package com.prosilion.barchetta.service;

import org.springframework.messaging.Message;
public interface StompListener<T> {
  void getReturnVal(Message<T> message);
}
