package com.wolfesoftware.stocks.controller;

import com.wolfesoftware.stocks.model.Option;
import com.wolfesoftware.stocks.service.OptionService;
import com.wolfesoftware.stocks.service.StockTransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/option")
public class OptionController extends BaseController<Option> {

    @Resource
    OptionService optionService;

    // CREATE
    @PostMapping("")
    public Option createStockTransaction( @RequestParam("optionType") Option.OptionType optionType, @RequestParam("stockId") Long stockId,
                                          @RequestParam("strikePrice") BigDecimal strikePrice,
                                          @RequestParam("expirationDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate) {
        return optionService.create(optionType, stockId, strikePrice, expirationDate);
    }

    // RETRIEVE
    @GetMapping("/{id}")
    public Option retrieveOneStockTransaction(@PathVariable("id") Long id) {
        return optionService.retrieveById(id);
    }

    @GetMapping("")
    public List<Option> retrieveAllStockTransactions() {
        return optionService.retrieveAll();
    }

    // UPDATE
    @PostMapping("/{id}")
    public Option updateStockTransaction(   @PathVariable("id") Long id,
                                            @RequestParam("optionType") Option.OptionType optionType, @RequestParam("stockId") Long stockId,
                                            @RequestParam("strikePrice") BigDecimal strikePrice,
                                            @RequestParam("expirationDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate) {

        return optionService.update(id, optionType, stockId, strikePrice, expirationDate);
    }

    // DELETE - Handled by BaseController


    // Used by Base Class
    protected OptionService getService() {
        return optionService;
    }
}
