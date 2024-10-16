package com.prosilion.barchetta.model.dto;

import com.prosilion.barchetta.model.entity.User;
import com.prosilion.presto.web.model.AppUserDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class UserDto extends AppUserDto {
  private String name;
  private String password;
  private String pubkey;
  public User convertToUser() {
    User user = new User();
    user.setUsername(name);
    user.setPassword(password);
    user.setPubkey(pubkey);
    return user;
  }
}
