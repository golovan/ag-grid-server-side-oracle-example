package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.oracle.demo.Trade;
import com.ag.grid.enterprise.oracle.demo.builder.OracleSqlQueryBuilder;
import com.ag.grid.enterprise.oracle.demo.request.ColumnVO;
import com.ag.grid.enterprise.oracle.demo.request.EnterpriseGetRowsRequest;
import com.ag.grid.enterprise.oracle.demo.response.EnterpriseGetRowsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.ag.grid.enterprise.oracle.demo.builder.EnterpriseResponseBuilder.createResponse;
import static java.lang.Math.floor;
import static java.lang.Math.random;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;

@Repository("tradeDao")
public class TradeDao {

    private JdbcTemplate template;
    private OracleSqlQueryBuilder queryBuilder;

    // add / remove products to change the data set
    private List<String> PRODUCTS = asList("Palm Oil", "Rubber", "Wool", "Amber", "Copper", "Lead", "Zinc", "Tin", "Aluminium",
            "Aluminium Alloy", "Nickel", "Cobalt", "Molybdenum", "Recycled Steel", "Corn", "Oats", "Rough Rice",
            "Soybeans", "Rapeseed", "Soybean Meal", "Soybean Oil", "Wheat", "Milk", "Coca", "Coffee C",
            "Cotton No.2", "Sugar No.11", "Sugar No.14");

    // add / remove portfolios to change the data set
    private List<String> PORTFOLIOS = asList("Aggressive", "Defensive", "Income", "Speculative", "Hybrid");

    // start the book id's and trade id's at some future random number, looks more realistic than starting them at 0
    private long nextBookId = 62472;
    private long nextTradeId = 24287;
    private long nextBatchId = 101;


    @Autowired
    public TradeDao(JdbcTemplate template) {
        this.template = template;
        queryBuilder = new OracleSqlQueryBuilder();

        for (String product : PRODUCTS) {
            for (String portfolio : PORTFOLIOS) {
                for (int k = 0; k < numberBetween(10, 2000); k++) {
                    insertBatch(createTradeData(product, portfolio, nextBatchId++));
                }
            }
        }
    }

    private void insertBatch(final List<Trade> trades) {
        String sql = "INSERT INTO trade (product, portfolio, book, tradeId, submitterId, submitterDealId, " +
                "dealType, bidType, currentValue, previousValue, pl1, pl2, gainDx, sxPx, x99Out, batch) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        template.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Trade trade = trades.get(i);

                ps.setString(1, trade.getProduct());
                ps.setString(2, trade.getPortfolio());
                ps.setString(3, trade.getBook());
                ps.setLong(4, trade.getTradeId());
                ps.setLong(5, trade.getSubmitterId());
                ps.setLong(6, trade.getSubmitterDealId());
                ps.setString(7, trade.getDealType());
                ps.setString(8, trade.getBidType());
                ps.setDouble(9, trade.getCurrentValue());
                ps.setDouble(10, trade.getPreviousValue());
                ps.setLong(11, trade.getPl1());
                ps.setLong(12, trade.getPl2());
                ps.setLong(13, trade.getGainDx());
                ps.setLong(14, trade.getSxPx());
                ps.setLong(15, trade.getX99Out());
                ps.setLong(16, trade.getBatch());
            }

            @Override
            public int getBatchSize() {
                return trades.size();
            }
        });
    }

    private List<Trade> createTradeData(String product, String portfolio, long thisBatch) {
        List<Trade> trades = new ArrayList<>();

        String book = createBookName();
        for (int l = 0; l < numberBetween(10, 10000); l++) {
            trades.add(createTradeRecord(product, portfolio, book, thisBatch));
        }

        return trades;
    }

    private Trade createTradeRecord(String product, String portfolio, String book, Long batch) {

        double current = (floor(random() * 100000) + 1000);
        double previous = current + (floor(random() * 10000) - 20000);

        return new Trade(
                product,
                portfolio,
                book,
                createTradeId(),
                randomNegation(numberBetween(10, 10000)),
                randomNegation(numberBetween(10, 10000)),
                numberBetween(1, 10) > 2 ? "Physical" : "Financial",
                numberBetween(1, 10) > 5 ? "Buy" : "Sell",
                randomNegation(current),
                randomNegation(previous),
                randomNegation(numberBetween(100, 100000)),
                randomNegation(numberBetween(100, 100000)),
                randomNegation(numberBetween(100, 100000)),
                randomNegation(numberBetween(100, 100000)),
                randomNegation(numberBetween(100, 100000)),
                batch);
    }


    public EnterpriseGetRowsResponse getData(EnterpriseGetRowsRequest request) {
        String tableName = "trade"; // could be supplied in request as a lookup key?

        // first obtain the pivot values from the DB for the requested pivot columns
        Map<String, List<String>> pivotValues = getPivotValues(request.getPivotCols());

        // generate sql
        String sql = queryBuilder.createSql(request, tableName, pivotValues);

        // query db for rows
        List<Map<String, Object>> rows = template.queryForList(sql);

        // create response with our results
        return createResponse(request, rows, pivotValues);
    }

    private Map<String, List<String>> getPivotValues(List<ColumnVO> pivotCols) {
        return pivotCols.stream()
                .map(ColumnVO::getField)
                .collect(toMap(pivotCol -> pivotCol, this::getPivotValues, (a, b) -> a, LinkedHashMap::new));
    }

    private List<String> getPivotValues(String pivotColumn) {
        String sql = "SELECT DISTINCT %s FROM trade";
        return template.queryForList(format(sql, pivotColumn), String.class);
    }

    private String createBookName() {
        return format("GL-%d", ++nextBookId);
    }

    private Long createTradeId() {
        return ++nextTradeId;
    }

    private Long numberBetween(long from, long to) {
        return ThreadLocalRandom.current().nextLong(from, to);
    }

    private long randomNegation(long number) {
        return (numberBetween(0, 2) == 0 ? -1 : 1) * number;
    }

    private double randomNegation(double number) {
        return (numberBetween(0, 2) == 0 ? -1 : 1) * number;
    }
}