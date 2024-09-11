package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  void transferAmount() throws Exception {
    Account fromAccount = new Account("Id-123");
    fromAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(fromAccount);

    Account toAccount = new Account("Id-456");
    toAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(toAccount);

    this.accountsService.transferAmount("Id-123", "Id-456", new BigDecimal(200) );

    assertThat(fromAccount.getBalance()).isEqualByComparingTo(new BigDecimal(800));
    assertThat(toAccount.getBalance()).isEqualByComparingTo(new BigDecimal(1200));
  }

  @Test
  void transferAmountWithNegativeAmount() throws Exception {
    try {
      this.accountsService.transferAmount("Id-123", "Id-456", new BigDecimal(-200) );
      fail("Should have failed when amount is negative");
    } catch (Exception ex) {
      assertThat(ex.getMessage()).isEqualTo("Transfer Amount should be greater than 0.");
    }

  }

  @Test
  void transferAmountWithInvalidFromAccount() throws Exception {

    Account fromAccount = new Account("Id-123");
    fromAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(fromAccount);

    Account toAccount = new Account("Id-456");
    toAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(toAccount);

    try {
      this.accountsService.transferAmount("Id-1231", "Id-456", new BigDecimal("200") );
      fail("Should have failed when Invalid From Account Id");
    } catch (Exception ex) {
      assertThat(ex.getMessage()).isEqualTo("Account does not exist.");
    }

  }

  @Test
  void transferAmountWithInvalidToAccount() throws Exception {

    Account fromAccount = new Account("Id-123");
    fromAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(fromAccount);

    Account toAccount = new Account("Id-456");
    toAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(toAccount);

    try {
      this.accountsService.transferAmount("Id-123", "Id-4567", new BigDecimal("200") );
      fail("Should have failed when Invalid To Account Id");
    } catch (Exception ex) {
      assertThat(ex.getMessage()).isEqualTo("Account does not exist.");
    }

  }

  @Test
  void transferAmountWithLowBalance() throws Exception {
    Account fromAccount = new Account("Id-123");
    fromAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(fromAccount);

    Account toAccount = new Account("Id-456");
    toAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(toAccount);

    try {
      this.accountsService.transferAmount("Id-123", "Id-456", new BigDecimal("1200") );
      fail("Should have failed when Low Balance in From Account");
    } catch (Exception ex) {
      assertThat(ex.getMessage()).isEqualTo("Low Balance in From Account.");
    }
  }
}
