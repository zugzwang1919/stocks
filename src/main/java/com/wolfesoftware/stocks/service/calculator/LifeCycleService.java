package com.wolfesoftware.stocks.service.calculator;

import com.wolfesoftware.stocks.common.LocalDateUtil;
import com.wolfesoftware.stocks.model.*;
import com.wolfesoftware.stocks.model.calculator.*;
import com.wolfesoftware.stocks.service.StockDividendService;
import com.wolfesoftware.stocks.service.StockPriceService;
import com.wolfesoftware.stocks.service.StockSplitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class LifeCycleService {

    @Resource
    OpeningPositionService openingPositionService;

    @Resource
    ClosingPositionService closingPositionService;

    @Resource
    PositionService positionService;

    @Resource
    StockPriceService stockPriceService;

    @Resource
    StockDividendService stockDividendService;

    @Resource
    StockSplitService stockSplitService;

    private static final Logger logger = LoggerFactory.getLogger(LifeCycleService.class);






    public LifeCycle buildStockLifeCycle(Stock stock, LocalDate requestedStartDate, LocalDate requestedEndDate,
                                                List<StockTransaction> transactions, List<OptionTransaction> optionTransactions,
                                                Map<Stock,List<StockDividend>> dividendCache,
                                                Map<Stock,List<StockSplit>> stockSplitCache,
                                                boolean includeDividends, boolean includeOptions) {

        // Clone the list and trim off all transactions beyond the end date
        List<StockTransaction> clonedStockTransactions = new ArrayList<>(transactions);
        Collections.sort(clonedStockTransactions, new StockTransaction.StockTransactionComparator(StockTransaction.SortBy.DATE));
        for(int i = clonedStockTransactions.size()-1; i >= 0 ; i--) {
            if(clonedStockTransactions.get(i).getDate().isAfter(requestedEndDate))
                clonedStockTransactions.remove(i);
        }
        // Try to create the opening position
        LifeCycle lifeCycle = new LifeCycle();
        lifeCycle.setOpeningPosition(openingPositionService.buildOpeningPosition(clonedStockTransactions, requestedStartDate, requestedEndDate));
        lifeCycle.setStock(stock);
        lifeCycle.setRequestedStartDate(requestedStartDate);
        lifeCycle.setRequestedEndDate(requestedEndDate);
        if (lifeCycle.getOpeningPosition() != null) {
            // Remove transactions that contributed to the opening position
            while(clonedStockTransactions.size() > 0 && !clonedStockTransactions.get(0).getDate().isAfter(lifeCycle.getOpeningPosition().getDate())) {
                clonedStockTransactions.remove(0);
            }
            // Create closing position
            ClosingPosition closingPosition = closingPositionService.createClosingPosition(lifeCycle.getOpeningPosition(), clonedStockTransactions, requestedEndDate);
            lifeCycle.setClosingPosition(closingPosition);
            // Remove transactions associated with the closing position
            LocalDate lastDateForIntervening = closingPosition.isPositionActiveAtEndDate() ? closingPosition.getDate() : closingPosition.getDate().minusDays(1);
            while(clonedStockTransactions.size() > 0 && clonedStockTransactions.get(clonedStockTransactions.size()-1).getDate().isAfter(lastDateForIntervening)) {
                clonedStockTransactions.remove(clonedStockTransactions.size()-1);
            }
            lifeCycle.setInterveningStockTransactions(clonedStockTransactions);

            // Handle dividends
            if (includeDividends)
                populateDividendInfo(lifeCycle, dividendCache);
        }
        // Handle Options
        if (includeOptions) {
            populateOptionInfo(lifeCycle, optionTransactions);
        }

        // Go ahead an make the simple calculation of "Total Long Exposure"
        // This is simply the value of the closing position plus any potential to "have to" purchase shares based on PUTS sold
        if (lifeCycle.getClosingPosition() != null) {
            lifeCycle.setTotalLongExposure(lifeCycle.getClosingPosition().getValue().add(lifeCycle.getOptionExposureToPutsAtRequestedEndDate()));
        }
        // At this point, a bunch of very simple calculations have been performed.
        // Subsequent calculations are based on what was found.

        // Calculate a "Simple Return" if there were stock transactions
        // For now we can only calculate a return if we have the actual purchase of sale of stocks
        if (lifeCycle.getOpeningPosition() != null)
            // Calculate basic values including return
            calculateBasicValues(lifeCycle);

        // If there were either stock transactions or options transactions, we can calculate income related values
        if (lifeCycle.getOpeningPosition() != null || optionsRelevantForThisLifeCycle(lifeCycle))
            calculateIncomeRelatedValues(lifeCycle);
            // If we don't have either return a null lifecycle
        else
            lifeCycle = null;

        return lifeCycle;
    }

    public LifeCycle newBenchmarkBasedOnExisting(LifeCycle thatLifeCycle, Stock newStock, Map<Stock,List<StockDividend>> dividendCache,
                                                        boolean includeDividends) {
        LifeCycle benchmarkLifeCycle = new LifeCycle();
        benchmarkLifeCycle.setStock(newStock);
        benchmarkLifeCycle.setRequestedStartDate(thatLifeCycle.getRequestedStartDate());
        benchmarkLifeCycle.setRequestedEndDate(thatLifeCycle.getRequestedEndDate());
        benchmarkLifeCycle.setOpeningPosition(new OpeningPosition(thatLifeCycle.getOpeningPosition(), newStock));
        benchmarkLifeCycle.setInterveningStockTransactions(createInterveningStockTransactionsBasedOnExisting(benchmarkLifeCycle.getOpeningPosition(), thatLifeCycle.getOpeningPosition(), thatLifeCycle.getInterveningStockTransactions()));
        benchmarkLifeCycle.setClosingPosition(closingPositionService.createClosingPosition(benchmarkLifeCycle.getOpeningPosition(), benchmarkLifeCycle.getInterveningStockTransactions(), thatLifeCycle.getClosingPosition().getDate()));

        // Handle dividends
        if (includeDividends)
            populateDividendInfo(benchmarkLifeCycle, dividendCache);

        // Handle Calls and Puts
        // Note: For the Benchmark - Covered Calls and Naked Puts are not relevant so stick with the default values of BigDecimal.ZERO

        // Calculate basic values including return
        calculateBasicValues(benchmarkLifeCycle);

        // Note Income Analysis does not make sense for benchmark LifeCycles.
        // No need to do anything.

        return benchmarkLifeCycle;
    }



    private static void populateOptionInfo(LifeCycle lifeCycle, List<OptionTransaction> thisSecuritiesOptionTransactions) {
        logger.debug("Beginning option accrual for {}", lifeCycle.getStock());
        BigDecimal accruedOptionAmount = BigDecimal.ZERO;
        BigDecimal exposureToPutsAtRequestedEndDate = BigDecimal.ZERO;
        BigDecimal exposureToCallsAtRequestedEndDate = BigDecimal.ZERO;
        if (thisSecuritiesOptionTransactions != null) {
            for (OptionTransaction ot : thisSecuritiesOptionTransactions) {
                // Add details about each individual option transaction to the lifecycle if it is relevant to the LifeCycle
                if (ot.getDate().isBefore(lifeCycle.getRequestedEndDate()) && ot.getOption().getExpirationDate().isAfter(lifeCycle.getRequestedStartDate()))
                    lifeCycle.getOptionTransactions().add(ot);
                // Calculate the proceeds from options transactions, exposure to having stock put, exposure to having stock called away
                if (ot.getActivity() == OptionTransaction.Activity.SELL_TO_CLOSE ||
                        ot.getActivity() == OptionTransaction.Activity.SELL_TO_OPEN) {
                    accruedOptionAmount = accruedOptionAmount.add(ot.getAmount());
                    logger.debug("Incrementing accrual amount by {}. Total is now {}", ot.getAmount(), accruedOptionAmount);
                    if (ot.getOption().getOptionType().equals(Option.OptionType.PUT)) {
                        if (!ot.getOption().getExpirationDate().isBefore(lifeCycle.getRequestedEndDate())) {
                            exposureToPutsAtRequestedEndDate = exposureToPutsAtRequestedEndDate.add(ot.getExercisableAmount());
                            logger.debug("Incrementing exposure to puts by {}. Total is now {}", ot.getExercisableAmount(), exposureToPutsAtRequestedEndDate);
                        }
                    }
                    else {
                        if (!ot.getOption().getExpirationDate().isBefore(lifeCycle.getRequestedEndDate())) {
                            exposureToCallsAtRequestedEndDate = exposureToCallsAtRequestedEndDate.add(ot.getExercisableAmount());
                            logger.debug("Incrementing exposure to calls by {}. Total is now {}", ot.getExercisableAmount(), exposureToCallsAtRequestedEndDate);
                        }
                    }
                }
                else {
                    accruedOptionAmount = accruedOptionAmount.subtract(ot.getAmount());
                    logger.debug("Decrementing accrual amount by {}. Total is now {}", ot.getAmount(), accruedOptionAmount);
                    if (ot.getOption().getOptionType().equals(Option.OptionType.PUT)) {
                        if (!ot.getOption().getExpirationDate().isBefore(lifeCycle.getRequestedEndDate())) {
                            exposureToPutsAtRequestedEndDate = exposureToPutsAtRequestedEndDate.subtract(ot.getExercisableAmount());
                            logger.debug("Decrementing exposure to puts by {}. Total is now {}", ot.getExercisableAmount(), exposureToPutsAtRequestedEndDate);
                        }
                    }
                    else {
                        if (!ot.getOption().getExpirationDate().isBefore(lifeCycle.getRequestedEndDate())) {
                            exposureToCallsAtRequestedEndDate = exposureToCallsAtRequestedEndDate.subtract(ot.getExercisableAmount());
                            logger.debug("Decrementing exposure to calls by {}. Total is now {}", ot.getExercisableAmount(), exposureToCallsAtRequestedEndDate);
                        }
                    }
                }
            }
        }
        logger.debug("Accrued amount from options = {}", accruedOptionAmount);
        lifeCycle.setOptionProceedsAccrued(accruedOptionAmount);
        logger.debug("Exposure to puts at requested end date = {}", exposureToPutsAtRequestedEndDate);
        lifeCycle.setOptionExposureToPutsAtRequestedEndDate(exposureToPutsAtRequestedEndDate);
        logger.debug("Exposure to calls at requested end date = {}", exposureToCallsAtRequestedEndDate);
        lifeCycle.setOptionExposureToCallsAtRequestedEndDate(exposureToCallsAtRequestedEndDate);
    }

    private void populateDividendInfo(LifeCycle lifeCycle, Map<Stock,List<StockDividend>> dividendCache) {

        OpeningPosition openingPosition = lifeCycle.getOpeningPosition();
        ClosingPosition closingPosition = lifeCycle.getClosingPosition();
        logger.debug("Caclulating Dividends Accrued.");
        logger.debug("Opening position -  {}", openingPosition);
        logger.debug("Closing position -  {}", closingPosition);
        BigDecimal accumulatedDividends = BigDecimal.ZERO;
        List<StockDividend> dividends = retrieveDividends(openingPosition.getStock(), dividendCache);
        List<StockSplit> stockSplits = stockSplitService.retrieveAllForOneStock(lifeCycle.getStock());
        for(StockDividend dividend : dividends) {
            LocalDate rightToDividendDate = dividend.getExDividendDate().minusDays(1);
            // This is a little yucky.  The closing position is created by either a complete sale of the position (in which case the owner IS NOT due the dividend)
            // or the stock is owned on the last day of the calculation (in which case the ownner IS due the dividend).  I've chosen to err on the side of NOT over reporting
            // the dividend for someone that sells on the "rightToDividendDate".  If the user specifies the end date of the calculation to be the "rightToDividendDate",
            // the dividend will (incorrectly) not be included in the calculation.
            if (!rightToDividendDate.isBefore(openingPosition.getDate()) && closingPosition.getDate().isAfter(rightToDividendDate)) {
                Position position = positionService.createPreciseNonValuedPositionBetweenPositions(openingPosition, closingPosition, lifeCycle.getInterveningStockTransactions(), rightToDividendDate);
                logger.debug("PositionService size used for dividend calculation = {}", position.getSize());
                // If there was a time in the lifecycle when there were no shares of stock, don't create a DividendEvent
                if (position.getSize().compareTo(BigDecimal.ZERO) > 0) {
                    // Add details about each individual dividend to the LifeCycle
                    DividendPayment dividendPayment = new DividendPayment(dividend, position.getSize(), stockSplits);
                    lifeCycle.getDividendPayments().add(dividendPayment);
                    // Accrue the dividend
                    logger.debug("One dividend calculated = {}", dividendPayment.getTotalAmount());
                    accumulatedDividends = accumulatedDividends.add(dividendPayment.getTotalAmount());
                }
            }
        }
        lifeCycle.setDividendsAccrued(accumulatedDividends);
        logger.debug("Dividends calculated = {}", accumulatedDividends);
    }

    private void calculateBasicValues(LifeCycle lifeCycle) {
        OpeningPosition openingPosition = lifeCycle.getOpeningPosition();
        ClosingPosition closingPosition = lifeCycle.getClosingPosition();
        logger.debug("Calculating basic return for " + openingPosition.getStock().getTicker());
        BigDecimal inflows = BigDecimal.ZERO;
        BigDecimal outflows = BigDecimal.ZERO;

        if (openingPosition.getSize().compareTo(BigDecimal.ZERO) > 0 )
            inflows = inflows.add(openingPosition.getValue());
        else
            outflows = outflows.subtract(openingPosition.getValue());

        for(StockTransaction transaction : lifeCycle.getInterveningStockTransactions()) {
            StockTransaction.Activity activity = transaction.getActivity();
            switch (activity) {
                case BUY:
                    inflows = inflows.add(transaction.getAmount());
                    break;
                case SELL:
                    outflows = outflows.add(transaction.getAmount());
            }
        }
        if (closingPosition.getSize().compareTo(BigDecimal.ZERO) > 0 )
            outflows = outflows.add(closingPosition.getValue());
        else
            inflows = inflows.subtract(closingPosition.getValue());

        BigDecimal profitsFromSecurities = outflows.subtract(inflows);
        outflows = outflows.add(lifeCycle.getDividendsAccrued());
        outflows = outflows.add(lifeCycle.getOptionProceedsAccrued());
        BigDecimal simpleReturn = outflows.subtract(inflows).divide(inflows, 10, RoundingMode.HALF_EVEN);
        BigDecimal totalGains = profitsFromSecurities.add(lifeCycle.getDividendsAccrued()).add(lifeCycle.getOptionProceedsAccrued());

        lifeCycle.setInflows(inflows);
        lifeCycle.setOutflows(outflows);
        lifeCycle.setProfitsFromSecurities(profitsFromSecurities);
        lifeCycle.setTotalGains(totalGains);
        lifeCycle.setSimpleReturn(simpleReturn);

        if (logger.isDebugEnabled()) {
            logger.debug("Total inflows = " + inflows);
            logger.debug("Total outflows = " + outflows);
            logger.debug("Profits from stocks = " + profitsFromSecurities);
            logger.debug("Total Gains = " + totalGains);
            logger.debug("Simple return = " + simpleReturn);
        }
    }

    private void calculateIncomeRelatedValues(LifeCycle lifeCycle) {
        // Time Period - Count days inclusively (thus the +1) - This prevents us from having to deal with someone
        // who purchases something on the last day of the month for a calculation through the last day of the month
        long lengthOfTimePeriodOfInterest = LocalDateUtil.daysBetween(determineEarliestDate(lifeCycle), determineLatestDate(lifeCycle)) + 1;
        lifeCycle.setLengthOfTimePeriodOfInterest(lengthOfTimePeriodOfInterest);

        // Dollar Days
        BigDecimal totalDollarDays = determineDollarDaysFromSecurities(lifeCycle);
        totalDollarDays = totalDollarDays.add(determineDollarDaysFromOptions(lifeCycle));
        lifeCycle.setTotalDollarDays(totalDollarDays);

        // Average Capital At Risk
        if (lifeCycle.getLengthOfTimePeriodOfInterest() < 1) {
            String exceptionMessage = "LifeCycle has a time period of less than one day for ticker " + lifeCycle.getStock().getTicker() + ".  from " +
                    lifeCycle.getRequestedStartDate() + " to " + lifeCycle.getRequestedEndDate();
            throw new IllegalStateException(exceptionMessage);
        }
        BigDecimal dailyAverageCapitalAtRisk = totalDollarDays.divide(BigDecimal.valueOf(lifeCycle.getLengthOfTimePeriodOfInterest()), 2, RoundingMode.HALF_EVEN);
        // In the event that this was a "short" position, convert this negative number to a positive number
        dailyAverageCapitalAtRisk = dailyAverageCapitalAtRisk.abs();
        lifeCycle.setDailyAverageCapitalAtRisk(dailyAverageCapitalAtRisk);

        lifeCycle.setAnnualizedIncomeReturnOnInvestment(lifeCycle.getTotalGains().multiply(BigDecimal.valueOf(365)).divide(dailyAverageCapitalAtRisk,10,RoundingMode.HALF_EVEN).divide(BigDecimal.valueOf(lengthOfTimePeriodOfInterest),10,RoundingMode.HALF_EVEN));

        logger.debug("Total Gains: {}  Daily Average Capiatal At Risk {}", lifeCycle.getTotalGains(), lifeCycle.getDailyAverageCapitalAtRisk());
        logger.debug("Length of Period: {}  ", lifeCycle.getLengthOfTimePeriodOfInterest());
        logger.debug("Total DollarDays: {}  ", totalDollarDays);
        logger.debug("Annualized Return on Investment calculated to be {}", lifeCycle.getAnnualizedIncomeReturnOnInvestment());
    }


    private BigDecimal determineDollarDaysFromSecurities(LifeCycle lifeCycle) {
        BigDecimal result = BigDecimal.ZERO;
        OpeningPosition openingPosition = lifeCycle.getOpeningPosition();
        if ( openingPosition != null) {
            BigDecimal value = openingPosition.getValue();
            LocalDate date = openingPosition.getDate();
            BigDecimal positionSize = openingPosition.getSize();
            for(StockTransaction transaction : lifeCycle.getInterveningStockTransactions()) {
                BigDecimal segmentDollarDays = value.multiply(BigDecimal.valueOf(LocalDateUtil.daysBetween(date, transaction.getDate())));
                logger.debug("Dollar Days incremented by '{}' based on intervening transaction on {}", segmentDollarDays, transaction.getDate());
                result = result.add(segmentDollarDays);
                // Adjust date, value, and positionSize for subsequent calculations
                date = transaction.getDate();
                if (transaction.getActivity().equals(StockTransaction.Activity.BUY)) {
                    value = value.add(transaction.getAmount());
                    positionSize = positionSize.add(transaction.getTradeSize());
                }
                else {
                    if (positionSize.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal fractionRemaining = positionSize.subtract(transaction.getTradeSize()).divide(positionSize, 2, RoundingMode.HALF_EVEN);
                        value = value.multiply(fractionRemaining);
                    }
                    else {
                        value = value.subtract(transaction.getAmount());
                    }
                    positionSize = positionSize.subtract(transaction.getTradeSize());
                }
            }
            LocalDate finalDate = lifeCycle.getClosingPosition().getDate();
            // daysBetween is exclusive.  I'm adding one to guarantee that there are a non-zero number of dollar days
            // for a purchase on the last day of the month, year, etc.
            BigDecimal finalDollarDays = value.multiply(BigDecimal.valueOf(LocalDateUtil.daysBetween(date, finalDate)+1));
            result = result.add(finalDollarDays).setScale(2,RoundingMode.HALF_EVEN);
        }
        logger.debug("Dollar Days from stock purchases and sales for '{}' calculated to be  {}", lifeCycle.getStock().getTicker(), result);
        return result;
    }

    private BigDecimal determineDollarDaysFromOptions(LifeCycle lifeCycle) {
        BigDecimal result = BigDecimal.ZERO;
        for(OptionTransaction optionTransaction : lifeCycle.getOptionTransactions()) {
            // Capital is put at risk when PUTS are sold
            if (optionTransaction.getActivity().equals(OptionTransaction.Activity.SELL_TO_OPEN) && optionTransaction.getOption().getOptionType().equals(Option.OptionType.PUT)) {
                LocalDate calculationEndDate = optionTransaction.getOption().getExpirationDate().isAfter(lifeCycle.getRequestedEndDate()) ?
                        lifeCycle.getRequestedEndDate() :
                        optionTransaction.getOption().getExpirationDate();
                long duration = LocalDateUtil.daysBetween(optionTransaction.getDate(), calculationEndDate);
                BigDecimal dollarDays = optionTransaction.getOption().getStrikePrice().multiply(BigDecimal.valueOf(duration).multiply(BigDecimal.valueOf(100L).multiply(BigDecimal.valueOf(optionTransaction.getNumberOfContracts()))));
                logger.debug("Dollar Days incremented by '{}' based on option transaction on {} with expiration date of " + optionTransaction.getOption().getExpirationDate(), dollarDays, optionTransaction.getDate() );
                result = result.add(dollarDays);
            }
        }
        result = result.setScale(2, RoundingMode.HALF_EVEN);
        logger.debug("Dollar Days from options for '{}' calculated to be  {}", lifeCycle.getStock().getTicker(), result);
        return result;
    }

    private LocalDate determineEarliestDate(LifeCycle lifeCycle) {
        LocalDate result = null;
        if (lifeCycle.getOpeningPosition() != null)
            result = lifeCycle.getOpeningPosition().getDate();
        LocalDate firstOptionTransactionDate =  lifeCycle.getOptionTransactions().isEmpty() ?
                null :
                lifeCycle.getOptionTransactions().get(0).getDate();
        if (firstOptionTransactionDate != null && (result == null || firstOptionTransactionDate.isBefore(result)))
            result = firstOptionTransactionDate;

        logger.debug("Earliest date of interest for '{}' calculated to be  {}", lifeCycle.getStock().getTicker(), result);
        return result;
    }

    private LocalDate determineLatestDate(LifeCycle lifeCycle) {
        LocalDate result = lifeCycle.getRequestedStartDate();
        if (lifeCycle.getClosingPosition() != null) {
            result = lifeCycle.getClosingPosition().getDate();
            logger.debug("Latest date of interest for '{}' was bumped to {}", lifeCycle.getStock().getTicker(), result);
        }
        for(OptionTransaction optionTransaction : lifeCycle.getOptionTransactions()) {
            LocalDate optionExpirationDate = optionTransaction.getOption().getExpirationDate();
            if (optionExpirationDate.isAfter(result)) {
                result = optionExpirationDate.isAfter(lifeCycle.getRequestedEndDate()) ? lifeCycle.getRequestedEndDate() : optionExpirationDate;
                logger.debug("Latest date of interest for '{}' was bumped to {}", lifeCycle.getStock().getTicker(), result);
            }
        }

        logger.debug("Latest date of interest for '{}' was finalized at  {}", lifeCycle.getStock().getTicker(), result);
        return result;
    }


    private List<StockDividend> retrieveDividends(Stock stock, Map<Stock,List<StockDividend>> dividendCache ) {
        List<StockDividend> dividends = new ArrayList<>();
        List<StockDividend> cachedDividends = dividendCache.get(stock);
        if (cachedDividends != null) {
            dividends.addAll(cachedDividends);
        }
        else {
            List<StockDividend> retrievedDividends = stockDividendService.retrieveAllForOneStock(stock);
            dividends.addAll(retrievedDividends);
            dividendCache.put(stock, retrievedDividends);
        }
        return dividends;
    }

    private  List<StockTransaction> createInterveningStockTransactionsBasedOnExisting(OpeningPosition ourOpeningPosition, OpeningPosition existingOpeningPosition, List<StockTransaction> existingInterveningStockTransactions) {
        List<StockTransaction> ourInterveningStockTransactions = new ArrayList<>();
        BigDecimal existingReferencePositionSize = existingOpeningPosition.getSize();
        BigDecimal ourReferencePositionSize = ourOpeningPosition.getSize();
        for(StockTransaction existingStockTransaction : existingInterveningStockTransactions) {
            StockTransaction newInterveningStockTransaction = new StockTransaction();
            newInterveningStockTransaction.setDate(existingStockTransaction.getDate());
            newInterveningStockTransaction.setStock(ourOpeningPosition.getStock());
            newInterveningStockTransaction.setActivity(existingStockTransaction.getActivity());

            // Mimic BUYs in dollar amounts
            if (newInterveningStockTransaction.getActivity().equals(StockTransaction.Activity.BUY)) {
                newInterveningStockTransaction.setAmount(existingStockTransaction.getAmount());
                BigDecimal price = stockPriceService.retrieveClosingPrice(newInterveningStockTransaction.getStock(), newInterveningStockTransaction.getDate());
                BigDecimal size = newInterveningStockTransaction.getAmount().divide(price, 3, RoundingMode.HALF_EVEN);
                newInterveningStockTransaction.setTradeSize(size);
                existingReferencePositionSize = existingReferencePositionSize.add(existingStockTransaction.getTradeSize());
                ourReferencePositionSize = ourReferencePositionSize.add(size);
            }

            else {
                // Mimic Sells as a percentage of the existing position
                if (existingReferencePositionSize.compareTo(BigDecimal.ZERO) != 0) {
                    Position existingPosition = positionService.createPreciseNonValuedPositionAfterOpeningPosition(existingOpeningPosition, existingInterveningStockTransactions, existingStockTransaction.getDate());
                    BigDecimal percentageOfReferencePosition = existingPosition.getSize().divide(existingReferencePositionSize,3,RoundingMode.HALF_EVEN);
                    BigDecimal desiredSizeOfOurNewPosition = ourReferencePositionSize.multiply(percentageOfReferencePosition, MathContext.UNLIMITED);
                    BigDecimal transactionSize = desiredSizeOfOurNewPosition.subtract(ourReferencePositionSize);
                    newInterveningStockTransaction.setTradeSize(transactionSize.abs());
                    BigDecimal price = stockPriceService.retrieveClosingPrice(newInterveningStockTransaction.getStock(), newInterveningStockTransaction.getDate());
                    BigDecimal value = newInterveningStockTransaction.getTradeSize().multiply(price);
                    newInterveningStockTransaction.setAmount(value);
                    existingReferencePositionSize = existingPosition.getSize();
                    ourReferencePositionSize = desiredSizeOfOurNewPosition;
                }
                // This is a bit of rare situation.  To get here you would have to purchase 300 shares of TGT.  Sell 300 Shares of TGT.
                // Sell short some TGT.  (OK, I actually did this)
                // When processing the Short Sell, the active position is zero shares.  Calculating a percentage of existing the existing position
                // results in a divide by zero.  Oops.  We need a different approach that is similar to what takes place above when buying.
                else {
                    newInterveningStockTransaction.setAmount(existingStockTransaction.getAmount());
                    BigDecimal price = stockPriceService.retrieveClosingPrice(newInterveningStockTransaction.getStock(), newInterveningStockTransaction.getDate());
                    BigDecimal size = newInterveningStockTransaction.getAmount().divide(price, 3, RoundingMode.HALF_EVEN);
                    newInterveningStockTransaction.setTradeSize(size);
                    existingReferencePositionSize = existingReferencePositionSize.subtract(existingStockTransaction.getTradeSize());
                    ourReferencePositionSize = ourReferencePositionSize.subtract(size);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("New transaction created.");
                logger.debug("Date = " + newInterveningStockTransaction.getDate());
                logger.debug("Stock = " + newInterveningStockTransaction.getStock().getTicker());
                logger.debug("Activity = " + newInterveningStockTransaction.getActivity());
                logger.debug("Trade Size = " + newInterveningStockTransaction.getTradeSize());
                logger.debug("Trade Amount = " + newInterveningStockTransaction.getAmount());
            }
            ourInterveningStockTransactions.add(newInterveningStockTransaction);

        }

        return ourInterveningStockTransactions;
    }

    private boolean optionsRelevantForThisLifeCycle(LifeCycle lifeCycle) {

        for (OptionTransaction ot : lifeCycle.getOptionTransactions()) {
            if (ot.getDate().isBefore(lifeCycle.getRequestedEndDate()) && ot.getOption().getExpirationDate().isAfter(lifeCycle.getRequestedStartDate()))
                return true;
        }
        return false;
    }


}
