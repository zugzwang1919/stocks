package com.wolfesoftware.stocks.controller;

import com.wolfesoftware.stocks.model.Portfolio;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.service.PortfolioService;
import com.wolfesoftware.stocks.service.StockTransactionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController extends BaseController<Portfolio>{

    @Resource
    PortfolioService portfolioService;

    // CREATE
    @PostMapping("")
    public Portfolio createPortfolio(@RequestParam(name = "portfolioName") String portfolioName) {
        return portfolioService.create(portfolioName);
    }

    // RETRIEVE
    @GetMapping("/{id}")
    public Portfolio retrieveOnePortfolio(@PathVariable("id") Long id) {
        return portfolioService.retrieveById(id);
    }

    @GetMapping("")
    public List<Portfolio> retrieveAllPortfolios() {
        return portfolioService.retrieveAll();
    }

    // UPDATE
    @PostMapping("/{id}")
    public Portfolio updatePortfolio(@PathVariable("id") Long id, @RequestParam(name = "portfolioName") String portfolioName) {
        return portfolioService.updatePortfolio(id, portfolioName);
    }


    // GET STOCKS!!! that are contained in the list of portfolios
    @GetMapping("/tickers")
    public List<Stock> retrieveStocksUsedInPortfolios(@RequestParam(required = false) List<Long> portfolioIds) {
        return portfolioService.retrieveStocksUsedInPortfolios(portfolioIds);
    }


    // DELETE - Handled by BaseController

    // Used by Base Class
    protected PortfolioService getService() {
        return portfolioService;
    }}
