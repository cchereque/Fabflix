package com.github.klefstad_teaching.cs122b.billing.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.klefstad_teaching.cs122b.billing.CustomClasses.Sales;
import com.github.klefstad_teaching.cs122b.core.result.Result;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderListResponse {
    private Result result;
    private List<Sales> sales;

    public Result getResult() {
        return result;
    }

    public OrderListResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public List<Sales> getSales() {
        return sales;
    }

    public OrderListResponse setSales(List<Sales> sales) {
        this.sales = sales;
        return this;
    }
}
