package com.github.klefstad_teaching.cs122b.billing.CustomClasses;

public class Cart {
    private Long movieId;
    private Integer quantity;

    public Long getMovieId() {
        return movieId;
    }

    public Cart setMovieId(Long movie_id) {
        this.movieId = movie_id;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Cart setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }
}
