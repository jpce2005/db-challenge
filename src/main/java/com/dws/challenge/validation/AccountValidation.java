package com.dws.challenge.validation;

import com.dws.challenge.domain.Account;
import jakarta.validation.constraints.Null;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountValidation {

    public boolean validateAmount(BigDecimal amount) throws Exception {
        if ( 0 >  amount.compareTo( BigDecimal.ZERO ) ) {
            throw new Exception("Transfer Amount should be greater than 0.");
        }
        return true;
    }

    public boolean validateAccount( Account account ) throws Exception {
        if ( account == null ) {
            throw new Exception("Account does not exist.");
        }
        return true;
    }

    public boolean validateAccountBalance( Account account, BigDecimal amount ) throws Exception {
        if ( 0 > account.getBalance().subtract(amount).compareTo( BigDecimal.ZERO ) ) {
            throw new Exception("Low Balance in From Account.");
        }
        return true;
    }
}
