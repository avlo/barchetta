package com.prosilion.scdecisionmatrix.service;

import org.springframework.messaging.Message;
public interface StompListener<T> {
  void getReturnVal(Message<T> message);
}
