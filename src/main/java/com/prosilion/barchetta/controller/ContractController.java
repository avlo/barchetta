package com.prosilion.barchetta.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.ContractAppUser;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.service.ContractAppUserService;
import com.prosilion.barchetta.service.ContractService;
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

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/contract")
public class ContractController {
  private final ContractAppUserService contractAppUserService;
  private final ContractService contractService;

  @Autowired
  public ContractController(ContractAppUserService contractAppUserService, ContractService contractService) {
    this.contractAppUserService = contractAppUserService;
    this.contractService = contractService;
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
  public String showAvailableContracts(@AuthenticationPrincipal AuthUserDetails user, @PathVariable("id") Long id, Model model) throws JsonProcessingException {
    log.info("Fetching selected contract: [{}]", id);
    Contract contract = contractService.getContractById(id);
    model.addAttribute("contract", contract);
    model.addAttribute("username", user.getUsername());
    model.addAttribute("counterPartyId", contractAppUserService.findByUsername(user.getUsername()).getId());
    log.info("CounterPartyId: [{}]", contractAppUserService.findByUsername(user.getUsername()).getId());
    log.info("User for potential contract: {}", user.getUsername());
    return "thymeleaf/contract/preview_contract";
  }

  @GetMapping("/my_contract/{id}")
  public String showMyContracts(@AuthenticationPrincipal AuthUserDetails user, @PathVariable("id") Long id, Model model) throws JsonProcessingException {
    log.info("Fetching my contract: [{}]", id);
    Contract contract = contractService.getContractById(id);
    model.addAttribute("contract", contract);
    model.addAttribute("username", user.getUsername());
    model.addAttribute("role", new RoleDeterminer(contract, user).getRoleEnum(contract.getCreatorRole()));
    return "thymeleaf/contract/view_contract";
  }

  @PostMapping("/apply")
  public String applyForContract(@AuthenticationPrincipal AuthUserDetails user, Contract contract, Model model) throws JsonProcessingException {
    contractService.save(contract);
    List<Contract> contractList = contractService.getAll();
    model.addAttribute("contracts", contractList);
    return "redirect:display_all";
  }

  @PostMapping("/vote")
  public String voteOnContract(@AuthenticationPrincipal AuthUserDetails user, Contract contract, Model model) throws JsonProcessingException {
    log.info("User [{}] voting on contract [{}]", user.getUsername(), contract);
    log.info("Contract id: [{}] ", contract.getId());
    log.info("Contract text: [{}] ", contract.getText());
    log.info("Contract appUserId: [{}] ", contract.getAppUserId());
    contractService.save(contract);
    List<Contract> contractList = contractService.getAll();
    model.addAttribute("contracts", contractList);
    return "redirect:display_all";
  }
  /////////////////////////
  // private methods
  /////////////////////////

  private void setCanonicalModelAttributes(@NonNull AuthUserDetails user, @NonNull Model model) {
    ContractAppUser contractAppUser = contractAppUserService.findByUsername(user.getUsername());
    model.addAttribute("user_contracts", contractAppUserService.getAllContractsFor(contractAppUser));
    model.addAttribute("open_contracts", contractAppUserService.getOpenContractsFor(contractAppUser));
    model.addAttribute("contract", contractAppUserService.constructContract(contractAppUser));
    model.addAttribute("username", user.getUsername());
  }

  private class RoleDeterminer {
    Long contractAppUserId;
    Long appUserId;

    public RoleDeterminer(@NonNull Contract contract, @NonNull AuthUserDetails user) {
      this.contractAppUserId = contract.getAppUserId();
      this.appUserId = contractAppUserService.findByUsername(user.getUsername()).getId();
    }

    CreatorRoleEnum getRoleEnum(CreatorRoleEnum role) {
      if (contractAppUserId.equals(appUserId) && role.equals(CreatorRoleEnum.PAYER)) {
        return CreatorRoleEnum.PAYER;
      }
      if (contractAppUserId.equals(appUserId) && role.equals(CreatorRoleEnum.PAYEE)) {
        return CreatorRoleEnum.PAYEE;
      }
      if (!contractAppUserId.equals(appUserId) && role.equals(CreatorRoleEnum.PAYER)) {
        return CreatorRoleEnum.PAYEE;
      }
      return CreatorRoleEnum.PAYER;
    }
  }
}
