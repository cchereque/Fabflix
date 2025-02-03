package com.github.klefstad_teaching.cs122b.billing.rest;

import com.github.klefstad_teaching.cs122b.billing.CustomClasses.Cart;
import com.github.klefstad_teaching.cs122b.billing.CustomClasses.Item;
import com.github.klefstad_teaching.cs122b.billing.repo.BillingRepo;
import com.github.klefstad_teaching.cs122b.billing.responses.InsertResponse;
import com.github.klefstad_teaching.cs122b.billing.responses.RetrieveResponse;
import com.github.klefstad_teaching.cs122b.billing.util.Validate;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;

@RestController
public class CartController
{
    private final BillingRepo repo;
    private final Validate    validate;

    @Autowired
    public CartController(BillingRepo repo, Validate validate)
    {
        this.repo = repo;
        this.validate = validate;
    }

    @PostMapping("/cart/insert")
    public ResponseEntity<InsertResponse> insertToCart (
            @AuthenticationPrincipal SignedJWT signedJWT,
            @RequestBody() Cart cartInfo
    ) throws ParseException {
        Long movieId = cartInfo.getMovieId();
        Integer quantity = cartInfo.getQuantity();

        System.out.println(movieId);
        System.out.println(quantity);

        if (quantity <= 0) {
            throw new ResultError(BillingResults.INVALID_QUANTITY);
        }
        if (quantity > 10) {
            throw new ResultError(BillingResults.MAX_QUANTITY);
        }

        Long user_id = signedJWT.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        System.out.println(user_id);

        Boolean inCart = this.repo.itemInCart(movieId, user_id);

        if (inCart) {
            throw new ResultError(BillingResults.CART_ITEM_EXISTS);
        }

        this.repo.addToCart(movieId, user_id, quantity);

        InsertResponse response = new InsertResponse()
                .setResult(BillingResults.CART_ITEM_INSERTED);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @PostMapping("/cart/update")
    public ResponseEntity<InsertResponse> updateCart (
            @AuthenticationPrincipal SignedJWT signedJWT,
            @RequestBody() Cart cartInfo
    ) throws ParseException {
        Long movieId = cartInfo.getMovieId();
        Integer quantity = cartInfo.getQuantity();

        if (quantity <= 0) {
            throw new ResultError(BillingResults.INVALID_QUANTITY);
        }
        if (quantity > 10) {
            throw new ResultError(BillingResults.MAX_QUANTITY);
        }

        Long user_id = signedJWT.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        Boolean inCart = this.repo.itemInCart(movieId, user_id);

        if (!inCart) {
            throw new ResultError(BillingResults.CART_ITEM_DOES_NOT_EXIST);
        }

        this.repo.updateCart(movieId, user_id, quantity);

        InsertResponse response = new InsertResponse()
                .setResult(BillingResults.CART_ITEM_UPDATED);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @DeleteMapping("/cart/delete/{movieId}")
    public ResponseEntity<InsertResponse> deleteFromCart (
            @AuthenticationPrincipal SignedJWT signedJWT,
            @PathVariable Long movieId
    ) throws ParseException {
        Long user_id = signedJWT.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        Boolean inCart = this.repo.itemInCart(movieId, user_id);

        if (!inCart) {
            throw new ResultError(BillingResults.CART_ITEM_DOES_NOT_EXIST);
        }

        this.repo.deleteMovieFromCart(movieId, user_id);

        InsertResponse response = new InsertResponse()
                .setResult(BillingResults.CART_ITEM_DELETED);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @GetMapping("/cart/retrieve")
    public ResponseEntity<RetrieveResponse> retrieveCart (
            @AuthenticationPrincipal SignedJWT signedJWT
    ) throws ParseException {
        Long user_id = signedJWT.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        Boolean isPremium = false;
        List<String> roles = null;
        try {
            roles = signedJWT.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        } catch (ParseException p) {
            p.printStackTrace();
        }

        for (int i = 0; i < roles.size(); ++i) {
            if (roles.get(i).equalsIgnoreCase("Premium")) {
                isPremium = true;
            }
        }

        List<Item> items = this.repo.retrieveCartItems(user_id);

        BigDecimal total = BigDecimal.valueOf(0);
        for (int i = 0; i < items.size(); ++i) {
            BigDecimal totalForMovie = BigDecimal.valueOf(0);
            if (isPremium) {
                Integer discount = this.repo.getPremiumDiscount(items.get(i).getMovieId());
                BigDecimal discountRate = BigDecimal.valueOf(1).subtract(BigDecimal.valueOf(discount).divide(BigDecimal.valueOf(100.0)));
                BigDecimal discountedUnit = items.get(i).getUnitPrice().multiply(discountRate);
                discountedUnit = discountedUnit.setScale(2, RoundingMode.DOWN);
                items.get(i).setUnitPrice(discountedUnit);
                total = total.add(discountedUnit.multiply(BigDecimal.valueOf(items.get(i).getQuantity())));
            }
            else {
                totalForMovie = items.get(i).getUnitPrice().multiply(BigDecimal.valueOf(items.get(i).getQuantity()));
                total = total.add(totalForMovie);
            }
        }

        total = total.setScale(2, RoundingMode.DOWN);

        RetrieveResponse response = new RetrieveResponse()
                .setItems(items)
                .setTotal(total)
                .setResult(BillingResults.CART_RETRIEVED);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @PostMapping("/cart/clear")
    public ResponseEntity<InsertResponse> clearCart (
            @AuthenticationPrincipal SignedJWT signedJWT
    ) throws ParseException {
        Long user_id = signedJWT.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        this.repo.clearCart(user_id);

        InsertResponse response = new InsertResponse()
                .setResult(BillingResults.CART_CLEARED);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }
}
