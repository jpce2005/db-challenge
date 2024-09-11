package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  @Test
  void transferAmount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-456\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(
                    put("/v1/accounts/transfer").contentType("multipart/form-data")
                            .param("accountFromId", String.valueOf("Id-123") )
                            .param("accountToId", String.valueOf("Id-456"))
                            .param("amount", String.valueOf("200"))
            )
            .andExpect(status().isOk());

    Account fromAccount = accountsService.getAccount("Id-123");
    Account toAccount = accountsService.getAccount("Id-456");

    assertThat(fromAccount.getAccountId()).isEqualTo("Id-123");
    assertThat(toAccount.getAccountId()).isEqualTo("Id-456");
    assertThat(fromAccount.getBalance()).isEqualByComparingTo(new BigDecimal(800));
    assertThat(toAccount.getBalance()).isEqualByComparingTo(new BigDecimal(1200));
  }

  @Test
  void transferAmountWithNegativeAmount() throws Exception {
    this.mockMvc.perform(
                    put("/v1/accounts/transfer").contentType("multipart/form-data")
                            .param("accountFromId", String.valueOf("Id-123") )
                            .param("accountToId", String.valueOf("Id-456"))
                            .param("amount", String.valueOf("-200"))
            )
            .andExpect(status().isBadRequest());
  }

  @Test
  void transferAmountWithNoAmount() throws Exception {
    this.mockMvc.perform(
                    put("/v1/accounts/transfer").contentType("multipart/form-data")
                            .param("accountFromId", String.valueOf("Id-123") )
                            .param("accountToId", String.valueOf("Id-456"))
                            .param("amount", String.valueOf(""))
            )
            .andExpect(status().isBadRequest());
  }

  @Test
  void transferAmountWithEmptyFromAccountId() throws Exception {
    this.mockMvc.perform(
                    put("/v1/accounts/transfer").contentType("multipart/form-data")
                            .param("accountFromId", String.valueOf("") )
                            .param("accountToId", String.valueOf("Id-456"))
                            .param("amount", String.valueOf("200"))
            )
            .andExpect(status().isBadRequest());
  }

  @Test
  void transferAmountWithEmptyToAccountId() throws Exception {
    this.mockMvc.perform(
                    put("/v1/accounts/transfer").contentType("multipart/form-data")
                            .param("accountFromId", String.valueOf("Id-123") )
                            .param("accountToId", String.valueOf(""))
                            .param("amount", String.valueOf("200"))
            )
            .andExpect(status().isBadRequest());
  }

  @Test
  void transferAmountWithInvalidFromAccount() throws Exception {

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-456\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(
                    put("/v1/accounts/transfer").contentType("multipart/form-data")
                            .param("accountFromId", String.valueOf("Id-1231") )
                            .param("accountToId", String.valueOf("Id-456"))
                            .param("amount", String.valueOf("200"))
            )
            .andExpect(status().isBadRequest());
  }

  @Test
  void transferAmountWithInvalidToAccount() throws Exception {

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-456\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(
                    put("/v1/accounts/transfer").contentType("multipart/form-data")
                            .param("accountFromId", String.valueOf("Id-123") )
                            .param("accountToId", String.valueOf("Id-4567"))
                            .param("amount", String.valueOf("200"))
            )
            .andExpect(status().isBadRequest());
  }

  @Test
  void transferAmountWithLowBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-456\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(
                    put("/v1/accounts/transfer").contentType("multipart/form-data")
                            .param("accountFromId", String.valueOf("Id-123") )
                            .param("accountToId", String.valueOf("Id-456"))
                            .param("amount", String.valueOf("1200"))
            )
            .andExpect(status().isBadRequest());
  }
}
