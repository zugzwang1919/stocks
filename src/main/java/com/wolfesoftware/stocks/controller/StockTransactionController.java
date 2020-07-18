package com.wolfesoftware.stocks.controller;

import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.service.StockTransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/stock-transaction")
public class StockTransactionController {

    @Resource
    StockTransactionService stockTransactionService;

    // CREATE
    @PostMapping("")
    public StockTransaction createStockTransaction( @RequestParam("portfolioId") Long portfolioId, @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                    @RequestParam("stockId") Long stockId, @RequestParam("activity") StockTransaction.Activity activity,
                                                    @RequestParam("tradeSize") BigDecimal tradeSize, @RequestParam("amount") BigDecimal amount) {
        return stockTransactionService.create(portfolioId, date, stockId, activity, tradeSize, amount);
    }

    // RETRIEVE
    @GetMapping("/{id}")
    public StockTransaction retrieveOneStockTransaction(@PathVariable("id") Long id) {
        return stockTransactionService.retrieveById(id);
    }

    @GetMapping("")
    public List<StockTransaction> retrieveAllStockTransactions() {
        return stockTransactionService.retrieveAll();
    }

    // UPDATE
    @PostMapping("/{id}")
    public StockTransaction updateStockTransaction(@PathVariable("id") Long id,
                                       @RequestParam("portfolioId") Long portfolioId, @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                       @RequestParam("stockId") Long stockId, @RequestParam("activity") StockTransaction.Activity activity,
                                       @RequestParam("tradeSize") BigDecimal tradeSize, @RequestParam("amount") BigDecimal amount) {

        return stockTransactionService.update(id, portfolioId, date, stockId, activity, tradeSize, amount);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteStockTransaction(@PathVariable("id") Long id) {
        stockTransactionService.deleteById(id);
    }
}
