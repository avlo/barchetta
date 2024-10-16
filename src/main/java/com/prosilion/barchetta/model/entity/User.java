package com.prosilion.barchetta.model.entity;

import com.prosilion.barchetta.model.dto.UserDto;
import com.prosilion.presto.security.entity.AppUser;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Embeddable
@Entity
public class User extends AppUser {
  private String username;
  private String password;
  private String pubkey;

  @Override
  public User getInstantiatedCustomAppUserType() {
    return this;
  }

  @Override
  public User createNewCustomAppUserInstance() {
    return new User();
  }

  public UserDto convertToDto() {
    UserDto userDto = new UserDto();
    userDto.setName(username);
    userDto.setPassword(password);
    userDto.setPubkey(pubkey);
    return userDto;
  }
}
