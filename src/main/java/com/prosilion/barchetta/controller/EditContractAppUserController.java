package com.prosilion.barchetta.controller;

import com.prosilion.barchetta.model.dto.ContractAppUserDto;
import com.prosilion.barchetta.service.ContractAppUserServiceIF;
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
public class EditContractAppUserController {
  private final ContractAppUserServiceIF contractAppUserServiceIF;

  @Autowired
  public EditContractAppUserController(ContractAppUserServiceIF contractAppUserServiceIF) {
    this.contractAppUserServiceIF = contractAppUserServiceIF;
  }

  @GetMapping("/edit/{user_id}")
  public String editUser(Model model, @PathVariable("user_id") Long userId) throws InvocationTargetException, IllegalAccessException {
    model.addAttribute("user", contractAppUserServiceIF.findUserByUserId(userId).convertToDto());
    return "thymeleaf/edit";
  }

  @PostMapping("/edit")
  public String updateUser(@ModelAttribute("user") ContractAppUserDto contractAppUserDto, BindingResult result, Model model) {

    if (result.hasErrors()) {
      log.info("User [{}] returned with with binding errors:\n\t{}", contractAppUserDto.getUsername(), result.getFieldErrors());
      model.addAttribute("user", contractAppUserDto);
      return "redirect:/edit";
    }

    try {
      ContractAppUserDto updatedContractAppUserDto = contractAppUserServiceIF.update(contractAppUserDto);
      model.addAttribute("user", updatedContractAppUserDto);
      return "redirect:/users";
    } catch (InvocationTargetException | IllegalAccessException e) {
      log.info("User [{}] InvocationTarget / IllegalAccess exception.", contractAppUserDto.getUsername());
      model.addAttribute("user", contractAppUserDto);
      return "redirect:/users";
    }
  }
}
