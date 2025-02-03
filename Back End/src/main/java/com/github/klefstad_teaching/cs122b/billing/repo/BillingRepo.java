package com.github.klefstad_teaching.cs122b.billing.repo;

import com.github.klefstad_teaching.cs122b.billing.CustomClasses.Cart;
import com.github.klefstad_teaching.cs122b.billing.CustomClasses.Item;
import com.github.klefstad_teaching.cs122b.billing.CustomClasses.SaleInfo;
import com.github.klefstad_teaching.cs122b.billing.CustomClasses.Sales;
import com.github.klefstad_teaching.cs122b.billing.responses.OrderDetailResponse;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.time.Instant;

@Component
public class BillingRepo
{
    private NamedParameterJdbcTemplate template;
    @Autowired
    public BillingRepo(NamedParameterJdbcTemplate template)
    {
        this.template = template;
    }

    public Boolean itemInCart(Long movie_id, Long user_id) {
        StringBuilder                   sql;
        MapSqlParameterSource source          =   new MapSqlParameterSource();

        sql = new StringBuilder("SELECT c.user_id, c.movie_id " +
                "FROM billing.cart c " +
                "WHERE c.user_id = :userId AND c.movie_id = :movieId ");

        source.addValue("userId", user_id, Types.INTEGER);
        source.addValue("movieId", movie_id, Types.INTEGER);

        try {
            Cart item = this.template.queryForObject(
                    sql.toString(),
                    source,
                    (rs, rowNum) ->
                            new Cart()
                                    .setMovieId(rs.getLong("movie_id"))
            );
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
        return true;
    }

    public void addToCart(Long movie_id, Long user_id, Integer quantity) {
        StringBuilder                   sql;
        MapSqlParameterSource source          =   new MapSqlParameterSource();

        sql = new StringBuilder("INSERT INTO billing.cart (user_id, movie_id, quantity) " +
                "VALUES (:userId, :movieId, :mQuantity)");
        source.addValue("userId", user_id, Types.INTEGER);
        source.addValue("movieId", movie_id, Types.INTEGER);
        source.addValue("mQuantity", quantity, Types.INTEGER);
        System.out.println(sql.toString());

        int rowsUpdate = this.template.update(sql.toString(), source);

    }

    public void updateCart(Long movie_id, Long user_id, Integer quantity) {
        StringBuilder                   sql;
        MapSqlParameterSource source          =   new MapSqlParameterSource();

        sql = new StringBuilder("UPDATE billing.cart " +
                "SET quantity = :mQuantity " +
                "WHERE user_id = :userId AND movie_id = :movieId");
        source.addValue("userId", user_id, Types.INTEGER);
        source.addValue("movieId", movie_id, Types.INTEGER);
        source.addValue("mQuantity", quantity, Types.INTEGER);

        int rowsUpdate = this.template.update(sql.toString(), source);
    }

    public void deleteMovieFromCart(Long movie_id, Long user_id) {
        StringBuilder                   sql;
        MapSqlParameterSource source          =   new MapSqlParameterSource();

        sql = new StringBuilder("DELETE FROM billing.cart " +
                "WHERE user_id = :userId AND movie_id = :movieId");
        source.addValue("userId", user_id, Types.INTEGER);
        source.addValue("movieId", movie_id, Types.INTEGER);

        int rowsUpdate = this.template.update(sql.toString(), source);
    }

    public List<Item> retrieveCartItems(Long user_id) {
        StringBuilder                   sql;
        MapSqlParameterSource source          =   new MapSqlParameterSource();

        sql = new StringBuilder("SELECT c.movie_id, mp.unit_price, c.quantity, m.title, m.backdrop_path, m.poster_path " +
                "FROM billing.cart c " +
                "JOIN movies.movie m ON m.id = c.movie_id " +
                "JOIN billing.movie_price mp ON mp.movie_id = c.movie_id " +
                "WHERE c.user_id = :userId ");
        source.addValue("userId", user_id, Types.INTEGER);

        List<Item> items = this.template.query(
                sql.toString(),
                source,
                (rs, rowNum) ->
                        new Item()
                                .setMovieId(rs.getLong("movie_id"))
                                .setMovieTitle(rs.getString("title"))
                                .setQuantity(rs.getInt("quantity"))
                                .setUnitPrice(rs.getBigDecimal("unit_price"))
                                .setBackdropPath(rs.getString("backdrop_path"))
                                .setPosterPath(rs.getString("poster_path"))
        );

        if (items.size() == 0) {
            throw new ResultError(BillingResults.CART_EMPTY);
        }

        return items;
    }

    public Integer getPremiumDiscount(Long movie_id) {
        StringBuilder                   sql;
        MapSqlParameterSource source          =   new MapSqlParameterSource();

        sql = new StringBuilder("SELECT mp.premium_discount " +
                "FROM billing.movie_price mp " +
                "WHERE mp.movie_id = :movieId ");
        source.addValue("movieId", movie_id, Types.INTEGER);

        Integer discount = this.template.queryForObject(
                sql.toString(),
                source,
                (rs, rowNum) ->
                        rs.getInt("premium_discount")
        );

        return discount;
    }

    public void clearCart(Long user_id) {
        StringBuilder                   sql;
        MapSqlParameterSource source          =   new MapSqlParameterSource();

        sql = new StringBuilder("DELETE FROM billing.cart " +
                "WHERE user_id = :userId ");
        source.addValue("userId", user_id, Types.INTEGER);

        this.template.update(
                sql.toString(),
                source
        );

        try {
            int rowsUpdate = this.template.update(sql.toString(), source);
        } catch (DataAccessException e) {
            throw new ResultError(BillingResults.CART_EMPTY);
        }
    }

    public void addSale(Long user_id, Boolean isPremium) {
        StringBuilder                   sql;
        MapSqlParameterSource source;

        List<Item> cart = retrieveCartItems(user_id);

        BigDecimal total = BigDecimal.valueOf(0);
        for (int i = 0; i < cart.size(); ++i) {
            BigDecimal totalForMovie = BigDecimal.valueOf(0);
            if (isPremium) {
                Integer discount = getPremiumDiscount(cart.get(i).getMovieId());
                BigDecimal discountRate = BigDecimal.valueOf(1).subtract(BigDecimal.valueOf(discount).divide(BigDecimal.valueOf(100.0)));
                BigDecimal discountedUnit = cart.get(i).getUnitPrice().multiply(discountRate);
                total = total.add(discountedUnit.multiply(BigDecimal.valueOf(cart.get(i).getQuantity())));
            }
            else {
                totalForMovie = cart.get(i).getUnitPrice().multiply(BigDecimal.valueOf(cart.get(i).getQuantity()));
                total = total.add(totalForMovie);
            }
        }

        Instant insertTime = Instant.now();

        if (!cart.isEmpty()) {
            source = new MapSqlParameterSource();
            sql = new StringBuilder("INSERT INTO billing.sale (user_id, total, order_date) " +
                    "VALUES (:user_id, :total, :order_date) ");
            source.addValue("user_id", user_id, Types.INTEGER);
            source.addValue("total", total, Types.DECIMAL);
            source.addValue("order_date", Timestamp.from(insertTime), Types.TIMESTAMP);

            this.template.update(
                    sql.toString(),
                    source
            );

            Integer sale_id = this.template.queryForObject(
                    "SELECT s.id FROM billing.sale s WHERE user_id = :uId ORDER BY s.order_date DESC LIMIT 1",
                    new MapSqlParameterSource()
                            .addValue("uId", user_id, Types.INTEGER)
                            .addValue("time", Timestamp.from(insertTime), Types.TIMESTAMP),
                    (rs, rowNum) -> (rs.getInt("id"))
            );

            for (int i = 0; i < cart.size(); ++i) {
                source = new MapSqlParameterSource();
                sql = new StringBuilder("INSERT INTO billing.sale_item (sale_id, movie_id, quantity) " +
                        "VALUES (:sale_id, :movie_id, :quantity) ");
                source.addValue("sale_id", sale_id, Types.INTEGER);
                source.addValue("movie_id", cart.get(i).getMovieId(), Types.INTEGER);
                source.addValue("quantity", cart.get(i).getQuantity(), Types.INTEGER);

                this.template.update(
                        sql.toString(),
                        source
                );
            }
            this.clearCart(user_id);
        }
    }

    public List<Sales> getUserSales(Long user_id) {
        StringBuilder                   sql;
        MapSqlParameterSource source          =   new MapSqlParameterSource();

        sql = new StringBuilder("SELECT s.id, s.total, s.order_date " +
                "FROM billing.sale s " +
                "WHERE s.user_id = :userId " +
                "ORDER BY s.order_date DESC " +
                "LIMIT 5 ");
        source.addValue("userId", user_id, Types.INTEGER);

        List<Sales> sales = this.template.query(
                sql.toString(),
                source,
                (rs, rowNum) ->
                        new Sales()
                                .setSaleId(rs.getLong("id"))
                                .setTotal(rs.getBigDecimal("total"))
                                .setOrderDate(rs.getTimestamp("order_date").toInstant())
        );

        return sales;
    }

    public OrderDetailResponse getSaleById(Long sale_id, Long user_id) {
        StringBuilder                   sql;
        MapSqlParameterSource source          =   new MapSqlParameterSource();

        sql = new StringBuilder("SELECT s.total, s.user_id " +
                "FROM billing.sale s " +
                "WHERE s.id = :saleId ");
        source.addValue("saleId", sale_id, Types.INTEGER);

        SaleInfo info = null;
        try {
            info = this.template.queryForObject(
                    sql.toString(),
                    source,
                    (rs, rowNum) ->
                            new SaleInfo()
                                    .setTotal(rs.getBigDecimal("total"))
                                    .setUser_id(rs.getLong("user_id"))
            );
        } catch (DataAccessException e) {
            throw new ResultError(BillingResults.ORDER_DETAIL_NOT_FOUND);
        }

        if (!user_id.equals(info.getUser_id())) {
            throw new ResultError(BillingResults.ORDER_DETAIL_NOT_FOUND);
        }


        OrderDetailResponse response = new OrderDetailResponse().setTotal(info.getTotal());

        sql = new StringBuilder("SELECT mp.unit_price, si.quantity, si.movie_id, m.title, m.backdrop_path, m.poster_path " +
                "FROM billing.sale_item si " +
                "JOIN billing.movie_price mp ON mp.movie_id = si.movie_id " +
                "JOIN movies.movie m ON m.id = si.movie_id " +
                "WHERE si.sale_id = :saleId ");

        List<Item> items = this.template.query(
                sql.toString(),
                source,
                (rs, rowNum) ->
                        new Item()
                                .setUnitPrice(rs.getBigDecimal("unit_price"))
                                .setQuantity(rs.getInt("quantity"))
                                .setMovieId(rs.getLong("movie_id"))
                                .setMovieTitle(rs.getString("title"))
                                .setBackdropPath(rs.getString("backdrop_path"))
                                .setPosterPath(rs.getString("poster_path"))
        );

        response.setItems(items);
        response.setResult(BillingResults.ORDER_DETAIL_FOUND);

        return response;
    }
}
