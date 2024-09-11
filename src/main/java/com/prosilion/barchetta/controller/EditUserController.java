package com.prosilion.barchetta.controller;

import com.prosilion.barchetta.model.dto.UserDto;
import com.prosilion.barchetta.service.user.UserServiceIF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.InvocationTargetException;

@Slf4j
@Controller
public class EditUserController {
  private final UserServiceIF userServiceIF;

  @Autowired
  public EditUserController(UserServiceIF userServiceIF) {
    this.userServiceIF = userServiceIF;
  }

  @GetMapping("/edit/{user_id}")
  public String editUser(Model model, @PathVariable("user_id") Long userId) throws InvocationTargetException, IllegalAccessException {
    model.addAttribute("user", userServiceIF.findByUserId(userId).convertToDto());
    return "thymeleaf/edit";
  }

  @PostMapping("/edit")
  public String updateUser(@ModelAttribute("user") UserDto userDto, BindingResult result, Model model) {

    if (result.hasErrors()) {
      log.info("User [{}] returned with with binding errors:\n\t{}", userDto.getUsername(), result.getFieldErrors());
      model.addAttribute("user", userDto);
      return "redirect:/edit";
    }

    try {
      UserDto updatedUserDto = userServiceIF.update(userDto);
      model.addAttribute("user", updatedUserDto);
      return "redirect:/users";
    } catch (InvocationTargetException | IllegalAccessException e) {
      log.info("User [{}] InvocationTarget / IllegalAccess exception.", userDto.getUsername());
      model.addAttribute("user", userDto);
      return "redirect:/users";
    }
  }
}
