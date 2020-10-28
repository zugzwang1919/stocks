package com.wolfesoftware.stocks.controller;

import com.wolfesoftware.stocks.model.OptionTransaction;
import com.wolfesoftware.stocks.service.OptionTransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/option-transaction")
public class OptionTransactionController {

    @Resource
    OptionTransactionService optionTransactionService;

    // CREATE
    @PostMapping("")
    public OptionTransaction createOptionTransaction( @RequestParam("portfolioId") Long portfolioId, @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                    @RequestParam("optionId") Long optionId, @RequestParam("activity") OptionTransaction.Activity activity,
                                                    @RequestParam("numberOfContracts") Long numberOfContracts, @RequestParam("amount") BigDecimal amount) {
        return optionTransactionService.create(portfolioId, date, optionId, activity, numberOfContracts, amount);
    }

    // RETRIEVE
    @GetMapping("/{id}")
    public OptionTransaction retrieveOneOptionTransaction(@PathVariable("id") Long id) {
        return optionTransactionService.retrieveById(id);
    }

    @GetMapping("")
    public List<OptionTransaction> retrieveAllOptionTransactions() {
        return optionTransactionService.retrieveAll();
    }

    // UPDATE
    @PostMapping("/{id}")
    public OptionTransaction updateOptionTransaction(@PathVariable("id") Long id,
                                       @RequestParam("portfolioId") Long portfolioId, @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                       @RequestParam("optionId") Long optionId, @RequestParam("activity") OptionTransaction.Activity activity,
                                       @RequestParam("numberOfContracts") Long numberOfContracts, @RequestParam("amount") BigDecimal amount) {

        return optionTransactionService.update(id, portfolioId, date, optionId, activity, numberOfContracts, amount);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteOptionTransaction(@PathVariable("id") Long id) {
        optionTransactionService.deleteById(id);
    }
}
