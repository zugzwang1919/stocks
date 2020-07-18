/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Russ
 */
public class BigDecimalUtil {
    
    public static final BigDecimal    ONE_HUNDRED = new BigDecimal("100");
    
    // Formats for output
    public static final DecimalFormat USDOutputFormat = new DecimalFormat("$#,##0.00");
    
    // Formats where users have typed in strings
    private static final DecimalFormat USDInputFormat1 = new DecimalFormat("'$'#,##0.00");
    private static final DecimalFormat USDInputFormat2 = new DecimalFormat("#,##0.00");
    private static final List<DecimalFormat> USDInputFormats = new ArrayList<>();
    
    
    public static final DecimalFormat amountFormat1 = new DecimalFormat("$###,###.######");
    public static final DecimalFormat amountFormat2 = new DecimalFormat("$#.######");
    public static final DecimalFormat amountFormat3 = new DecimalFormat("###,###.######");
    public static final DecimalFormat amountFormat4 = new DecimalFormat("#.######");
    public static final List<DecimalFormat> acceptableAmountFormats = new ArrayList<>();
    static {
        USDOutputFormat.setParseBigDecimal(true);
        
        USDInputFormat1.setParseBigDecimal(true);
        USDInputFormat2.setParseBigDecimal(true);
        USDInputFormats.add(USDInputFormat1);
        USDInputFormats.add(USDInputFormat2);
        
        
        
        amountFormat1.setParseBigDecimal(true);
        amountFormat2.setParseBigDecimal(true);
        amountFormat3.setParseBigDecimal(true);
        amountFormat4.setParseBigDecimal(true);
        
        acceptableAmountFormats.add(amountFormat1);
        acceptableAmountFormats.add(amountFormat2);
        acceptableAmountFormats.add(amountFormat3);
        acceptableAmountFormats.add(amountFormat4);
    }
    
    public static String createUSDString(BigDecimal bd) {
        return USDOutputFormat.format(bd);
    }

    public static BigDecimal createUSDBigDecimal(String USDString) {
        for(DecimalFormat df : USDInputFormats ) {
            try {
                ParsePosition pp = new ParsePosition(0);
                BigDecimal result = (BigDecimal)df.parse(USDString,pp);
                // Make sure the entire string was parsed
                if (pp.getIndex() == USDString.length() && pp.getErrorIndex() == -1)
                    // Make sure that the resulting scale is two or less 
                    // (If the input had more than two digits to the right of the decimal point, it's not a US dollar string)
                    if (result.scale() <= 2)
                        return result.setScale(2);
            }
            catch (Exception e) {
                // Just keep trying
            }
        }
        // If we get to this point, we don't have a string that would describe US Dollars
        return null;
    }
    
    public static BigDecimal createUSDBigDecimal( BigDecimal bigDecimal ) {
        return bigDecimal.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal createUSDBigDecimalDividend( BigDecimal bigDecimal ) {
        return bigDecimal.setScale(4, RoundingMode.HALF_EVEN);
    }

      
 
    public static BigDecimal calculateWeightedAverage(List<WeightedAverageParticipant> weightedAverageParticipants) {
        BigDecimal numerator = BigDecimal.ZERO;
        BigDecimal denominator = BigDecimal.ZERO;
        for (WeightedAverageParticipant weightedAverageParticipant : weightedAverageParticipants) {
            numerator = numerator.add(weightedAverageParticipant.weightingFactor.multiply(weightedAverageParticipant.value));
            denominator = denominator.add(weightedAverageParticipant.weightingFactor);
        }
        return numerator.divide(denominator, RoundingMode.HALF_EVEN);
    }
    
    public static class WeightedAverageParticipant {
        private final BigDecimal    weightingFactor;
        private final BigDecimal    value;

        public WeightedAverageParticipant(BigDecimal weightingFactor, BigDecimal value) {
            this.weightingFactor = weightingFactor;
            this.value = value;
        }

        public BigDecimal getWeightingFactor() {
            return weightingFactor;
        }

        public BigDecimal getValue() {
            return value;
        }
    }
    
}
