package com.prosilion.barchetta.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.service.ControllerServiceIF;
import com.prosilion.presto.security.entity.AuthUserDetails;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/contract")
public class ContractsController {
  public static final String CONTRACT_STR = "contract";
  public static final String CONTRACTS_STR = "contracts";
  public static final String COUNTER_PARTY_ID_STR = "counter_party_id";
  public static final String OPEN_CONTRACTS_STR = "open_contracts";
  public static final String ROLE_STR = "role";
  public static final String USER_CONTRACTS_STR = "user_contracts";
  public static final String USERNAME_STR = "username";

  private final ControllerServiceIF contractAppUserService;

  @Autowired
  public ContractsController(ControllerServiceIF contractAppUserService) {
    this.contractAppUserService = contractAppUserService;
  }

  @PostMapping("/create")
  public String createContract(@AuthenticationPrincipal AuthUserDetails user, @NonNull Contract contract, Model model) throws JsonProcessingException {
    contractAppUserService.create(contract, contractAppUserService.findByUsername(user.getUsername()).getId());
    setCanonicalModelAttributes(user, model);
    return "thymeleaf/contract/display";
  }

  @GetMapping("/display_all")
  public String showUserContracts(@AuthenticationPrincipal AuthUserDetails user, Model model) {
    setCanonicalModelAttributes(user, model);
    return "thymeleaf/contract/display";
  }

  @GetMapping("/display_contract/{id}")
  public String showAvailableContracts(@AuthenticationPrincipal AuthUserDetails user, @PathVariable("id") Long contractId, Model model) throws JsonProcessingException {
    log.info("Fetching selected contract: [{}]", contractId);
    model.addAttribute(CONTRACT_STR, contractAppUserService.getContractByContractId(contractId));
    model.addAttribute(USERNAME_STR, user.getUsername());
    model.addAttribute(COUNTER_PARTY_ID_STR, contractAppUserService.findByUsername(user.getUsername()).getId());
    log.info("CounterPartyId: [{}]", contractAppUserService.findByUsername(user.getUsername()).getId());
    log.info("User for potential contract: {}", user.getUsername());
    return "thymeleaf/contract/preview_contract";
  }

  @GetMapping("/my_contract/{id}")
  public String showMyContracts(@AuthenticationPrincipal AuthUserDetails user, @PathVariable("id") Long contractId, Model model) throws JsonProcessingException {
    log.info("Fetching my contract: [{}]", contractId);
    Contract contract = contractAppUserService.getContractByContractId(contractId);
    model.addAttribute(CONTRACT_STR, contract);
    model.addAttribute(USERNAME_STR, user.getUsername());
    model.addAttribute(ROLE_STR, contractAppUserService.getRole(contract, user));
    return "thymeleaf/contract/view_contract";
  }

  @PostMapping("/apply")
  public String applyForContract(@AuthenticationPrincipal AuthUserDetails user, Contract contract, Model model) throws JsonProcessingException {
    contractAppUserService.save(contract);
    model.addAttribute(CONTRACTS_STR, contractAppUserService.getAll());
    return "redirect:display_all";
  }

  @PostMapping("/vote")
  public String voteOnContract(@AuthenticationPrincipal AuthUserDetails user, Contract contract, Model model) throws JsonProcessingException {
    log.info("User [{}] voting on contract [{}]", user.getUsername(), contract);
    log.info("Contract id: [{}] ", contract.getId());
    log.info("Contract text: [{}] ", contract.getText());
    log.info("Contract appUserId: [{}] ", contract.getAppUserId());
    contractAppUserService.save(contract);
    model.addAttribute(CONTRACTS_STR, contractAppUserService.getAll());
    return "redirect:display_all";
  }

  private void setCanonicalModelAttributes(@NonNull AuthUserDetails user, @NonNull Model model) {
    User contractAppUser = contractAppUserService.findByUsername(user.getUsername());
    model.addAttribute(USER_CONTRACTS_STR, contractAppUserService.getAllContractsFor(contractAppUser));
    model.addAttribute(OPEN_CONTRACTS_STR, contractAppUserService.getOpenContractsFor(contractAppUser));
    model.addAttribute(CONTRACT_STR, contractAppUserService.constructContract(contractAppUser));
    model.addAttribute(USERNAME_STR, user.getUsername());
  }
}
