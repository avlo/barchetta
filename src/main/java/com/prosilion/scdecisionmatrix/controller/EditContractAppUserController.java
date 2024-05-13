package com.prosilion.scdecisionmatrix.controller;

import com.prosilion.scdecisionmatrix.model.dto.ContractAppUserDto;
import com.prosilion.scdecisionmatrix.service.ContractAppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.InvocationTargetException;

@Controller
public class EditContractAppUserController {
	private static final Logger LOGGER = LoggerFactory.getLogger(EditContractAppUserController.class);
	private final ContractAppUserService contractAppUserService;

	@Autowired
	public EditContractAppUserController(ContractAppUserService contractAppUserService) {
		this.contractAppUserService = contractAppUserService;
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(Model model, @PathVariable("id") Long id) throws InvocationTargetException, IllegalAccessException {
		model.addAttribute("user", contractAppUserService.findById(id).convertToDto());
		return "thymeleaf/edit";
	}

	@PostMapping("/edit")
	public String updateUser(@ModelAttribute("user") ContractAppUserDto contractAppUserDto, BindingResult result, Model model) {

		if (result.hasErrors()) {
			LOGGER.info("User [{}] returned with with following binding errors:", result.getFieldErrors());
			model.addAttribute("user", contractAppUserDto);
			return "redirect:/edit";
		}

		try {
			ContractAppUserDto updatedContractAppUserDto = contractAppUserService.update(contractAppUserDto);
			model.addAttribute("user", updatedContractAppUserDto);
			return "redirect:/users";
		} catch (InvocationTargetException | IllegalAccessException e) {
			LOGGER.info("User [{}] InvocationTarget / IllegalAccess exception.");
			model.addAttribute("user", contractAppUserDto);
			return "redirect:/users";
		}
	}
}
