package com.wolfesoftware.stocks.controller;


import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.service.StockService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/stock")
public class StockController extends BaseController<Stock> {

    @Resource
    StockService stockService;

    // CREATE
    @PostMapping("")
    public Stock createStock(@RequestParam String ticker, @RequestParam String name, @RequestParam Boolean benchmark) {
        return stockService.create(ticker, name, benchmark);
    }

    // RETRIEVE
    @GetMapping("/benchmarks")
    public List<Stock> retrieveAllBenchmarks() {
        return stockService.retrieveAllBenchmarks();
    }
    @GetMapping("/{id}")
    public Stock retrieveOneStock(@PathVariable("id") Long id) {
        return stockService.retrieveById(id);
    }
    @GetMapping("")
    public List<Stock> retrieveAllStocks() {
        return stockService.retrieveAll();
    }

    // UPDATE
    @PostMapping("/{id}")
    public Stock updateStock(@PathVariable("id") Long id, @RequestParam String name, @RequestParam Boolean benchmark) {
        return stockService.updateStock(id, name, benchmark);
    }

    // DELETE - Handled by BaseController


    // OTHER Functionality
    @GetMapping("/ticker/{tickerSymbol}")
    public Stock suggestName(@PathVariable("tickerSymbol") String tickerSymbol) {
        return stockService.suggestName(tickerSymbol);
    }

    // Used by Base Class
    protected StockService getService() {
        return stockService;
    }
}
