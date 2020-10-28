package com.wolfesoftware.stocks.service.calculator;

import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.model.calculator.ClosingPosition;
import com.wolfesoftware.stocks.model.calculator.OpeningPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.wolfesoftware.stocks.model.StockTransaction.Activity.BUY;


@Service
public class ClosingPositionService extends PositionService {

    private static final Logger logger = LoggerFactory.getLogger(ClosingPositionService.class);
    private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;

    public ClosingPosition createClosingPosition(OpeningPosition openingPosition, List<StockTransaction> stockTransactions, LocalDate endDate) {

        String debugString = "Creating Closing Position for " + openingPosition.getStock().getTicker() + " " + dtf.format(endDate);
        logger.debug("STARTING - " + debugString);
        StopWatch stopWatch = new StopWatch(debugString);
        stopWatch.start("Creating Closing Position - Calculating size");
        ClosingPosition closingPosition = new ClosingPosition(openingPosition.getStock(), endDate);
        closingPosition.setSize(adjustedSize(openingPosition.getStock(), openingPosition.getDate(), openingPosition.getSize(), endDate));

        for (StockTransaction stockTransaction : stockTransactions ) {
            addStockTransactionSizeToPosition(endDate, closingPosition, stockTransaction);
        }
        stopWatch.stop();
        logger.debug("Transitioning to calculating other values");
        stopWatch.start("Creating Closing Position - Calculating other values");
        closingPosition.setPositionActiveAtEndDate(true);
        // If the position was closed prior to the end date, there's a little
        // bit of special processing
        // FIXME - This is a real hack. compareTo() was not returning 0 for my
        // FIXME - GE closed position.  The closing position was something like 1E-10 which makes
        // FIXME - me think that there is a place where we're not doing BigDecimal math correctly.
        // FIXME - Rather than track it down, I randomly chose to consider your position closed
        // FIXME - if it contains less than .001 shares
        if ((closingPosition.getSize().doubleValue()) < .001) {
            closingPosition.setPositionActiveAtEndDate(false);
            // Set the closing date and the closing value
            LocalDate closingDate = null;

            BigDecimal lastDayAggregatedTradeSize = BigDecimal.ZERO;
            BigDecimal lastDayAggregatedTradeAmount = BigDecimal.ZERO;
            for(int i=stockTransactions.size()-1; i>=0; i--) {
                StockTransaction thisStockTransaction = stockTransactions.get(i);
                if (closingDate == null || closingDate.equals(thisStockTransaction.getDate())) {
                    closingDate = thisStockTransaction.getDate();
                    if (thisStockTransaction.getActivity().equals(BUY)) {
                        lastDayAggregatedTradeSize = lastDayAggregatedTradeSize.subtract(thisStockTransaction.getTradeSize());
                    }
                    else {
                        lastDayAggregatedTradeSize = lastDayAggregatedTradeSize.add(thisStockTransaction.getTradeSize());
                    }
                    lastDayAggregatedTradeAmount = lastDayAggregatedTradeAmount.add(thisStockTransaction.getProceeds());
                }

                else
                    break;
            }
            closingPosition.setDate(closingDate);
            closingPosition.setSize(lastDayAggregatedTradeSize);
            closingPosition.setValue(lastDayAggregatedTradeAmount);
        }
        // If the position is still open on the end date, just calculate the position's value
        else {
            calculateValue(closingPosition);
        }
        stopWatch.stop();
        logger.debug(stopWatch.prettyPrint());
        return closingPosition;
    }



}
