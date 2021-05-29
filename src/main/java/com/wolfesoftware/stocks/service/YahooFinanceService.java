/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.common.BigDecimalUtil;
import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockDividend;
import com.wolfesoftware.stocks.model.StockPrice;
import com.wolfesoftware.stocks.model.StockSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;


/**
 *
 * @author Russ
 */
@Service
public class YahooFinanceService {
    
    
    private static final String TODAYS_PRICE_URL_TEMPLATE = "https://query1.finance.yahoo.com/v7/finance/download/%TICKER%?period1=%FROM_DATE_WITH_TIME%&period2=%TO_DATE_WITH_TIME%&interval=1d&events=history&crumb=%CRUMB%";
    private static final String HISTORICAL_PRICE_URL_TEMPLATE = "https://query1.finance.yahoo.com/v7/finance/download/%TICKER%?period1=%FROM_DATE%&period2=%TO_DATE%&interval=1d&events=history&crumb=%CRUMB%";
    private static final String HISTORICAL_DIVIDEND_URL_TEMPLATE = "https://query1.finance.yahoo.com/v7/finance/download/%TICKER%?period1=%FROM_DATE%&period2=%TO_DATE%&interval=1d&events=dividends&crumb=%CRUMB%";
    private static final String HISTORICAL_STOCK_SPLIT_URL_TEMPLATE = "https://query1.finance.yahoo.com/v7/finance/download/%TICKER%?period1=%FROM_DATE%&period2=%TO_DATE%&interval=1d&events=splits&crumb=%CRUMB%";

    // NOTE: This is a particularly important URL.
    // NOTE: Yahoo Finance let's you perform a GET on this URL without having a cookie or crumb
    // NOTE: and will return both in addition to a page containing info about the current price of the stock.
    // NOTE: Append a ticker symbol to the end to create a valid URL;
    private static final String CURRENT_PRICE_CRUMB_AND_COOKIE_RETRIEVER_URL = "https://finance.yahoo.com/quote/";

    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Integer MAXIMUM_NUMBER_OF_RETRIES = 5;
    private static final int CONNECTION_TIMEOUT = 15000;  // For now, set the connection timeout with Yahoo Finance at 15 seconds
    private static final int COOKIE_AND_CRUMB_TIMEOUT_IN_MINUTES = 5;  // Get a new cookie and crumb every so often just in case we ever get a bad one or Yahoo starts expiring them

    private static final Logger logger = LoggerFactory.getLogger(YahooFinanceService.class);


    // NOTE:  We only need one token for anyone using this service.  The token is capable of upgrading itself
    // NOTE:  as the crumb / cookie inside it expires
    private final YahooToken  yahooToken = new YahooToken();


    
    public String getTickersStockName(String tickerSymbol) {
        StockQueryResults sqr = getStockNameCrumbAndCookie(tickerSymbol);
        return sqr.stockName;
    }
    
    public boolean isTickerValid(String tickerSymbol) {
        String url = substituteTickerAndStartEndDates(HISTORICAL_PRICE_URL_TEMPLATE, tickerSymbol, LocalDate.now().minusDays(10), LocalDate.now());
        List<List<String>> yahooRecentPrices = null;
        try {
            yahooRecentPrices = errorProneRetrieveCsvFromYahoo(url,7);
        }
        catch (IOException ioe) {
            logger.debug("Inside YahooFinanceService.isTickerValid().  IOException occurred.");
        }
        return yahooRecentPrices != null && yahooRecentPrices.size() > 0;            
    }


    public StockPrice getTodaysStockPrice(Stock stock) {
        
        logger.debug("Attempting to get today's price for {}...", stock.getTicker());
        StockPrice result = null;
        // Get the prices for the last ten calendar days
        LocalDate today = LocalDate.now();
        LocalDate tenDaysAgo = today.minusDays(10);
        List<StockPrice> stockPrices = getHistoricalStockPrices(stock, tenDaysAgo, today, TODAYS_PRICE_URL_TEMPLATE);
        // Return the last result only if it is for today; otherwise, return null
        if (!stockPrices.isEmpty()  && stockPrices.get(stockPrices.size()-1).getDate().equals(today)) {
            result = stockPrices.get(stockPrices.size()-1);
        }
        return result;            
    }
    
    public List<StockPrice> getHistoricalStockPrices(Stock stock, LocalDate fromDate, LocalDate toDate, String optionalUrlTemplate) {
        String templateToBeUsed = optionalUrlTemplate == null ? HISTORICAL_PRICE_URL_TEMPLATE : optionalUrlTemplate;
        List<StockPrice> result = new ArrayList<>();
        List<List<String>> listOfPrices = getParsedHistoricalData(templateToBeUsed,stock.getTicker(), fromDate, toDate, 7);
        if (listOfPrices.size() > 1) {
            // The first row of the csv is a header row remove it
            listOfPrices.remove(0);
            // Create a StockPrice for everything left in the table
            for(List<String> onePrice : listOfPrices) {
                // Sometimes the word "null" will be returned as a price.  If this occurs, don't try to construct a StockPrice for that day
                if (!("null".equals(onePrice.get(4)))) {
                    // I've seen a few times when there's garbage in just one of a stock's historical data.  Just keep trucking.
                    try {
                        StockPrice stockPrice = new StockPrice( stock,
                                                                convertYahooStringToLocalDate(onePrice.get(0)),
                                                                BigDecimalUtil.createUSDBigDecimal(new BigDecimal(onePrice.get(4))));
                        result.add(stockPrice);
                    }
                    // Here's the "Just keep trucking" part referenced above.
                    catch (Exception e) {
                        logger.error("Encountered an error while getting historical prices for " + stock.getTicker() + ".  Exception = " + e);
                    }
                }
            }
        } else {
            logger.info("No historical prices were returned for {}.  If this was not expected, there may be issues.", stock.getTicker());
        }
        return result;
    }




    public List<StockDividend> getHistoricalStockDividends(Stock stock, LocalDate fromDate, LocalDate toDate) {
        logger.debug("Getting dividends for {}.", stock.getTicker());
        List<StockDividend> result = new ArrayList<>();
        List<List<String>> listOfDividends = getParsedHistoricalData(HISTORICAL_DIVIDEND_URL_TEMPLATE, stock.getTicker(), fromDate, toDate, 2);
        if (listOfDividends.size() > 1) {
            // The first row of the csv is a header row remove it
            listOfDividends.remove(0);
            // Create a SecurityDividend for everything left in the table
            
            for(List<String> oneDividend : listOfDividends) {
                LocalDate exDividendDate =  convertYahooStringToLocalDate(oneDividend.get(0));
                BigDecimal bigDecimal = new BigDecimal(oneDividend.get(1));
                StockDividend dividend = new StockDividend( stock, exDividendDate, BigDecimalUtil.createUSDBigDecimalDividend(bigDecimal));
                result.add(dividend);
            }
        } 
        return result;
    }




    public List<StockSplit> getHistoricalStockSplits(Stock stock, LocalDate fromDate, LocalDate toDate) {
        logger.debug("Getting stock splits for {}.", stock.getTicker());
        List<StockSplit> result = new ArrayList<>();
        List<List<String>> listOfStockSplits = getParsedHistoricalData(HISTORICAL_STOCK_SPLIT_URL_TEMPLATE, stock.getTicker(), fromDate, toDate, 2);
        if (listOfStockSplits.size() > 1) {
            // The first row of the csv is a header row remove it
            listOfStockSplits.remove(0);

            // Create a StockSplit for everything left in the table
            
            for(List<String> oneStockSplit : listOfStockSplits) {
                StockSplit stockSplit = new StockSplit();
                stockSplit.setStock(stock);
                stockSplit.setDate(convertYahooStringToLocalDate(oneStockSplit.get(0)));
                String trimmedSplitData = oneStockSplit.get(1).trim();
                String[] afterBefore = trimmedSplitData.split(":");
                stockSplit.setAfterAmount(new BigDecimal(afterBefore[0]));
                stockSplit.setBeforeAmount(new BigDecimal(afterBefore[1]));
                result.add(stockSplit);
            }  
        } 
        return result;
    }



    /***********  Private Methods ************/


    

    private String createYahooDate(LocalDate inputDate) {
        
       LocalDate beginningOfYahooTime = LocalDate.of(1970, Month.JANUARY, 1);
       long daysBetween = DAYS.between(beginningOfYahooTime, inputDate);
       // For some reason Yahoo wants the value to be at 5:00 am on the day
       return String.valueOf(daysBetween*86400 + 5*3600);
    }
        
    private String createYahooDateWithTime(LocalDate inputDate) {
        
       LocalDate beginningOfYahooTime = LocalDate.of(1970, Month.JANUARY, 1);
       long daysBetween = DAYS.between(beginningOfYahooTime, inputDate);
       // Use the current time.  Yahoo seems to want this if requesting today's date
       LocalDateTime now = LocalDateTime.now();
       return String.valueOf(daysBetween*86400 + now.getHour()*3600 + now.getMinute()*60 + now.getSecond());
    }


    private List<List<String>> getParsedHistoricalData(String urlTemplate, String tickerSymbol, LocalDate fromDate, LocalDate toDate, int numberOfRowsExpected) {
        String completedUrl = substituteTickerAndStartEndDates(urlTemplate, tickerSymbol, fromDate, toDate);
        List<List<String>> allHistoricalData = retrieveCsvFromYahoo(completedUrl, numberOfRowsExpected);
        return allHistoricalData;
    }

    
    private String substituteTickerAndStartEndDates(String templateUrl, String tickerSymbol, LocalDate fromDate, LocalDate toDate) {
        String url = templateUrl.replace("%TICKER%", tickerSymbol);
        url = url.replace("%FROM_DATE%", createYahooDate(fromDate)); 
        url = url.replace("%TO_DATE%", createYahooDate(toDate)); 
        url = url.replace("%FROM_DATE_WITH_TIME%", createYahooDateWithTime(fromDate)); 
        url = url.replace("%TO_DATE_WITH_TIME%", createYahooDateWithTime(toDate)); 
        url = url.replace("%CRUMB%", URLEncoder.encode(yahooToken.getCrumb(), StandardCharsets.UTF_8));
        logger.debug("URL generated = " + url);
        return url;
    }
    
    private List<List<String>> retrieveCsvFromYahoo(String url, Integer numberOfExpectedColumns) {
        int retriesToDate = 0;
        while(retriesToDate < MAXIMUM_NUMBER_OF_RETRIES) {
            try {
                return errorProneRetrieveCsvFromYahoo(url, numberOfExpectedColumns);
            }
            catch (IOException ioe) {
                retriesToDate++;
                logger.info("Could not retrieve {}.  About to try retry #{}", url, retriesToDate);
                try {
                    Thread.sleep(1000L * retriesToDate * retriesToDate);                 //1000 milliseconds is one second.
                } 
                catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        logger.error("Retries have been exhausted for the following URL:  {}", url);
        throw new IllegalStateException("Sadly, we were not able to retrieve data from Yahoo Finance.");
    }
    
    
    
    private List<List<String>> errorProneRetrieveCsvFromYahoo(String url, Integer numberOfExpectedColumns) throws IOException {
        List<List<String>> results = new ArrayList<>();
        ICsvListReader listReader = null;
        try {
            String USER_AGENT = "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11"; 
            URL request = new URL(url);
            URLConnection connection = request.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(CONNECTION_TIMEOUT);
            connection.setRequestProperty("Cookie", yahooToken.getCookie());
            
            //add request header
            connection.setRequestProperty("User-Agent", USER_AGENT);

            logger.debug("Ready to fire off the following URL to Yahoo Finance: {}", url);
            connection.connect();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int statusCode = httpConnection.getResponseCode();
            
            if (statusCode == 200) {
                // Use Try-with-resources so the InputStreamReader and BufferedReader won't have to be explicitly closed by our code
                try (InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream()) ) {
                    try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                        List<String> line;
                        listReader = new CsvListReader(bufferedReader, CsvPreference.STANDARD_PREFERENCE);
                        while ((line = listReader.read()) != null) {
                            results.add(line);
                            // Should never happen, but just in case
                            if (numberOfExpectedColumns != -1 && line.size() != numberOfExpectedColumns) {
                                logger.error("Completely unexpected number of columns returned in Yahoo Finance's result set.");
                                logger.error("Expected " + numberOfExpectedColumns + " .  Found " + line.size() + ". Details of what was found follow:");
                                for (String result : line) {
                                    logger.error(result);
                                }

                            }
                        }                   
                    }    
                }
            }
            else {
                logger.info("Status Code of {} was returned for url {} while interacting with Yahoo Finance.", statusCode, url);   
            }
        } 
        finally {
            // Unfortunately csvReader does not implement Closeable
            try {                
                if (listReader != null) 
                    listReader.close();
            }
            catch (IOException ex) {
                // Just log an error.  This really should never happen.
                logger.error("Unable to close the CSV Reader for Yahoo Finance.");
            }
        }
        return results;
    } 

    
    private LocalDate convertYahooStringToLocalDate(String yahooDateString) {
        LocalDate result;
        String strippedYahooDateString = yahooDateString.trim();
        try {
            result = LocalDate.parse(strippedYahooDateString, YYYY_MM_DD);
        }
        catch (DateTimeParseException dtpe) {
            logger.error("The following string could not be parsed into a LocalDate using {}: {}", YYYY_MM_DD, strippedYahooDateString);
            throw dtpe;
        }
        return result;   
    }

    private class YahooToken {
        private String          crumb = null;
        private String          cookie = null;
        private LocalDateTime   tokenDateTime = null;

        public String getCrumb() {

            try {
                // Sleep for one second prior to handing out the token
                // Yahoo seems to occasionally send back a 401 without the sleep
                Thread.sleep(1000);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            // We get a new token every so often just to ensure we don't get "stuck" with a bad one for a long period of time
            if (tokenDateTime == null  || crumb == null || tokenDateTime.isBefore(LocalDateTime.now().minusMinutes(COOKIE_AND_CRUMB_TIMEOUT_IN_MINUTES))) {
                refreshToken();
            }
            return crumb;
        }

        public String getCookie() {
            // We get a new token every so often just to ensure we don't get "stuck" with a bad one for a long period of time
            if (tokenDateTime == null || cookie == null || tokenDateTime.isBefore(LocalDateTime.now().minusMinutes(COOKIE_AND_CRUMB_TIMEOUT_IN_MINUTES)) ) {
                refreshToken();
            }
            return cookie;
        }


        private void refreshToken() {
            // Let's retrieve info about AAPL to get the crumb and cookie
            StockQueryResults sqr = getStockNameCrumbAndCookie("AAPL");
            logger.info("New Crumb and Cookie to be used with Yahoo.  Crumb = {}.  Cookie = {}", sqr.crumb, sqr.cookie);
            crumb = sqr.crumb;
            cookie = sqr.cookie;
            tokenDateTime = LocalDateTime.now();
        }
    }




    private static class StockQueryResults {
        String stockName;
        String cookie;
        String crumb;
    }

    private StockQueryResults getStockNameCrumbAndCookie(String tickerSymbol) {
        StockQueryResults results = new StockQueryResults();
        StringBuilder pageContent = new StringBuilder();
        URLConnection connection;
        try {
            String url = CURRENT_PRICE_CRUMB_AND_COOKIE_RETRIEVER_URL + tickerSymbol;
            URL request = new URL(url);
            connection = request.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(CONNECTION_TIMEOUT);
            logger.debug("Ready to fire off the following URL to Yahoo Finance: {}", url);
            connection.connect();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int statusCode = httpConnection.getResponseCode();
            String localCookie;
            if (statusCode == 200) {
                localCookie = getCookie(httpConnection);
                try (BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        pageContent.append(inputLine);
                    }
                }
            }
            else {
                throw new IllegalStateException("A status code of " + statusCode + "was return inside YahooFinanceService.Token.refreshToken()");
            }

            results.cookie = localCookie;
            results.crumb = getCrumb(pageContent.toString());
            results.stockName = getStockName(pageContent.toString(), tickerSymbol);
        }
        catch(MalformedURLException me) {
            throw new IllegalStateException("Unexpected MalformedException was caught inside YahooFinanceService.Token.refreshToken()", me);
        }
        catch(IOException ioe) {
            throw new IllegalStateException("Unexpected IOException was caught inside YahooFinanceService.Token.refreshToken()", ioe);
        }
        return results;
    }

    private String getCrumb(String pageContent) {
        int indexOfStore = pageContent.indexOf("\"CrumbStore\":{\"crumb\":\"");
        int indexOfCrumb = indexOfStore + 23;
        int indexOfTrailingQuote = pageContent.indexOf("\"", indexOfCrumb);
        String crumbValue = (pageContent.substring(indexOfCrumb, indexOfTrailingQuote));
        // NOTE Crumb may contain \u002f forward slash
        crumbValue = crumbValue.replace("\\u002F","/");
        return crumbValue;
    }

    private String getStockName(String pageContent, String tickerSymbol) {
        int indexOfName = pageContent.indexOf("<title>") + 7;
        int indexOfTickerName = pageContent.indexOf("("+tickerSymbol+")", indexOfName);
        if (indexOfTickerName == -1)
            throw new NotFoundException("The ticker symbol specified could not be found.");
        String stockName = pageContent.substring(indexOfName, indexOfTickerName);
        stockName = stockName.trim();
        return stockName;
    }

    private String getCookie(HttpURLConnection openConnection) {
        for (Map.Entry<String, List<String>> oneHeader : openConnection.getHeaderFields().entrySet()) {
            String headerName = oneHeader.getKey();
            if ("Set-Cookie".equalsIgnoreCase(headerName)) {
                List<String> headerValues = oneHeader.getValue();
                for (String value: headerValues) {
                    String[] fields = value.split(";\\s*");
                    if (fields[0].startsWith("B=")) {
                        return fields[0];
                    }
                }
            }
        }
        throw new IllegalStateException("Could not find Cookie");
    }



}
