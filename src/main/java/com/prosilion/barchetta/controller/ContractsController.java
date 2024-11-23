package com.prosilion.barchetta.controller;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.service.ControllerServiceIF;
import com.prosilion.presto.nostr.entity.NostrUser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.util.NostrException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/contract")
public class ContractsController {
  public static final String CONTRACT_STR = "contractDto";
  public static final String CONTRACTS_STR = "contracts";
  public static final String COUNTER_PARTY_ID_STR = "counter_party_id";
  public static final String OPEN_CONTRACTS_STR = "open_contracts";
  public static final String ROLE_STR = "role";
  public static final String USER_CONTRACTS_STR = "user_contracts";
  public static final String USERNAME_STR = "username";

  private final ControllerServiceIF controllerService;

  @Autowired
  public ContractsController(ControllerServiceIF contractAppUserService) {
    this.controllerService = contractAppUserService;
  }

  @GetMapping({"/index.html", "/"})
  public String redirectIndex() {
    return "forward:/register";
  }

  @Secured({"ROLE_USER", "USER"})
  @PostMapping("/create")
  public String createContract(
      @AuthenticationPrincipal NostrUser user,
      ContractDto contractDto,
      Model model) throws IOException, NostrException {
    controllerService.createContract(contractDto, user);
    model.addAttribute(CONTRACT_STR, contractDto);
    setCanonicalModelAttributes(user, model);
    return "thymeleaf/contract/display_allRxR";
  }

  @GetMapping("/display_all")
  public String showUserContracts(@AuthenticationPrincipal NostrUser user, Model model) {
    setCanonicalModelAttributes(user, model);
    return "thymeleaf/contract/display_allRxR";
  }

  @GetMapping("/display_contract/{id}")
  public String showAvailableContracts(@AuthenticationPrincipal NostrUser user, @PathVariable("id") Long contractId, Model model) throws IOException {
    log.info("Fetching selected contract: [{}]", contractId);
    model.addAttribute(CONTRACT_STR, controllerService.getContractByContractId(contractId));
    model.addAttribute(USERNAME_STR, user.getUsername());
    model.addAttribute(COUNTER_PARTY_ID_STR, controllerService.findByUsername(user.getUsername()).getId());
    log.info("CounterPartyId: [{}]", controllerService.findByUsername(user.getUsername()).getId());
    log.info("User for potential contract: {}", user.getUsername());
    return "thymeleaf/contract/contract_application_form";
  }

  @GetMapping("/my_contract/{id}")
  public String showMyContracts(@AuthenticationPrincipal NostrUser user, @PathVariable("id") Long contractId, Model model) throws IOException {
    log.info("Fetching my contract: [{}]", contractId);
    Contract contract = controllerService.getContractByContractId(contractId);
    model.addAttribute(CONTRACT_STR, contract);
    model.addAttribute(USERNAME_STR, user.getUsername());
    model.addAttribute(ROLE_STR, controllerService.getRole(contract, controllerService.findByUsername(user.getUsername())));
    return "thymeleaf/contract/view_contract";
  }

  @PostMapping("/apply")
  public String applyForContract(@AuthenticationPrincipal NostrUser user, Contract contract, Model model) throws IOException, NostrException {
    controllerService.saveAsCounterParty(contract, user);
    model.addAttribute(CONTRACTS_STR, controllerService.getAll());
    return "redirect:display_allRxR";
  }

  @PostMapping("/vote")
  public String voteOnContract(@AuthenticationPrincipal NostrUser user, Contract contract, Model model) throws IOException, NostrException {
    log.info("User [{}] voting on contract [{}]", user.getUsername(), contract);
    log.info("Contract id: [{}] ", contract.getId());
    log.info("Contract text: [{}] ", contract.getText());
    log.info("Contract appUserId: [{}] ", contract.getAppUserId());
    controllerService.save(contract);
    model.addAttribute(CONTRACTS_STR, controllerService.getAll());
    return "redirect:display_allRxR";
  }

  private void setCanonicalModelAttributes(@NonNull NostrUser user, @NonNull Model model) {
    User contractAppUser = controllerService.findByUsername(user.getUsername());
    model.addAttribute(USER_CONTRACTS_STR, controllerService.getAllContractsFor(contractAppUser));
    model.addAttribute(OPEN_CONTRACTS_STR, controllerService.getOpenContractsFor(contractAppUser));
    model.addAttribute(CONTRACT_STR, controllerService.constructContract(contractAppUser));
    model.addAttribute(USERNAME_STR, user.getUsername());
  }
}
