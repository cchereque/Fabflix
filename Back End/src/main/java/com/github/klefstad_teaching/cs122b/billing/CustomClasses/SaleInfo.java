package com.github.klefstad_teaching.cs122b.billing.CustomClasses;

import java.math.BigDecimal;

public class SaleInfo {
    private BigDecimal total;
    private Long user_id;

    public BigDecimal getTotal() {
        return total;
    }

    public SaleInfo setTotal(BigDecimal total) {
        this.total = total;
        return this;
    }

    public Long getUser_id() {
        return user_id;
    }

    public SaleInfo setUser_id(Long user_id) {
        this.user_id = user_id;
        return this;
    }
}
