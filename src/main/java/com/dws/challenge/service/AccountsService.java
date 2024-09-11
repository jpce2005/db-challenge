package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.validation.AccountValidation;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  private final AccountValidation accountValidation;
  private final NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, AccountValidation accountValidation, NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.accountValidation = accountValidation;
    this.notificationService = notificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public void transferAmount(String accountFromId, String accountToId, BigDecimal amount ) throws Exception {
    // validate amount should be positive
    this.accountValidation.validateAmount( amount );

    // Fetch accont details based on provided ids
    Account fromAccount = getAccount( accountFromId );
    Account toAccount = getAccount( accountToId );

    // validate accounts
    this.accountValidation.validateAccount( fromAccount );
    this.accountValidation.validateAccount( toAccount );

    // validate from account has enough balance
    this.accountValidation.validateAccountBalance( fromAccount, amount );

    // perform transfer operation
    this.accountsRepository.transferAmount( fromAccount, toAccount, amount );

    // Notify both the account holder about transfer
    this.notificationService.notifyAboutTransfer( fromAccount, amount + " amount transferred to account " + accountToId );
    this.notificationService.notifyAboutTransfer( toAccount, amount + " amount transferred from account " + accountFromId );

  }
}
