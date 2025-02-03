package com.github.klefstad_teaching.cs122b.billing.responses;

import com.github.klefstad_teaching.cs122b.core.result.Result;

public class InsertResponse {
    private Result result;

    public Result getResult() {
        return result;
    }

    public InsertResponse setResult(Result result) {
        this.result = result;
        return this;
    }
}
