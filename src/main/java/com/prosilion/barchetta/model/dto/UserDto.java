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
  private String nostrPubKey;
  public User convertToUser() {
    User user = new User();
    user.setNostrPubKey(nostrPubKey);
    return user;
  }
}
