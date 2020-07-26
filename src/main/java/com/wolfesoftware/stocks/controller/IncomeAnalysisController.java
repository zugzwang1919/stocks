/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.controller;


import com.wolfesoftware.stocks.config.JwtTokenUtil;
import com.wolfesoftware.stocks.model.JwtResponse;
import com.wolfesoftware.stocks.model.calculator.IncomeAnalysisResponse;
import com.wolfesoftware.stocks.service.JwtUserDetailsService;
import com.wolfesoftware.stocks.service.calculator.IncomeAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@RestController
public class IncomeAnalysisController {

    @Resource
    IncomeAnalysisService incomeAnalysisService;


    @RequestMapping(value = "/income-analysis", method = RequestMethod.POST)
    public IncomeAnalysisResponse createAuthenticationToken(@RequestParam LocalDate beginDate, @RequestParam LocalDate endDate,
                                                            @RequestParam List<Long> portfolioIds, @RequestParam List<Long> stockIds,
                                                            @RequestParam Boolean includeDividends, @RequestParam Boolean includeOptions) {
        return incomeAnalysisService.analyze(beginDate, endDate, portfolioIds, stockIds, includeDividends, includeOptions);
    }


}
