package com.ai.demo.finance.controller;

import static com.ai.demo.finance.controller.ApiVersion.ACCEPT_VERSION;
import static com.ai.demo.finance.controller.ApiVersion.API_V1;

import com.ai.demo.finance.dto.AccountDTO;
import com.ai.demo.finance.dto.BalanceDTO;
import com.ai.demo.finance.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(headers = {ACCEPT_VERSION + "=" + API_V1})
@AllArgsConstructor
@Tag(name = "Account", description = "APIs related to account details")
public class AccountController {

    public static final String ENDPOINT = "/account";

    private final AccountService accountService;

    @PostMapping(value = ENDPOINT, produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Create a new account")
    public ResponseEntity<AccountDTO> createAccount(@RequestBody AccountDTO accountDTO) {
        AccountDTO createdAccount = accountService.createAccount(accountDTO);
        return ResponseEntity.created(URI.create(ENDPOINT.concat("/").concat(String.valueOf(createdAccount.id()))))
                .body(createdAccount);
    }

    @GetMapping(value = ENDPOINT + "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get an account by ID")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable Long id) {
        AccountDTO account = accountService.findById(id);
        return ResponseEntity.ok(account);
    }

    @PutMapping(value = ENDPOINT + "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Update an account by ID")
    public ResponseEntity<AccountDTO> updateAccount(@PathVariable Long id, @RequestBody AccountDTO accountDTO) {
        AccountDTO updatedAccount = accountService.updateAccount(id, accountDTO);
        return ResponseEntity.ok(updatedAccount);
    }

    @PatchMapping(value = ENDPOINT + "/{id}/deposit", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Deposit an amount into the account")
    public ResponseEntity<AccountDTO> deposit(@PathVariable Long id, @RequestBody BalanceDTO balanceDTO) {
        AccountDTO updatedAccount = accountService.deposit(id, balanceDTO);
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping(value = ENDPOINT + "/{id}")
    @Operation(summary = "Delete an account by ID")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
