package com.github.klefstad_teaching.cs122b.billing.rest;

import com.github.klefstad_teaching.cs122b.billing.CustomClasses.Item;
import com.github.klefstad_teaching.cs122b.billing.CustomClasses.PaymentId;
import com.github.klefstad_teaching.cs122b.billing.CustomClasses.Sales;
import com.github.klefstad_teaching.cs122b.billing.repo.BillingRepo;
import com.github.klefstad_teaching.cs122b.billing.responses.InsertResponse;
import com.github.klefstad_teaching.cs122b.billing.responses.OrderDetailResponse;
import com.github.klefstad_teaching.cs122b.billing.responses.OrderListResponse;
import com.github.klefstad_teaching.cs122b.billing.responses.OrderPaymentResponse;
import com.github.klefstad_teaching.cs122b.billing.util.Validate;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.nimbusds.jwt.SignedJWT;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class OrderController
{
    private final BillingRepo repo;
    private final Validate    validate;

    @Autowired
    public OrderController(BillingRepo repo,Validate validate)
    {
        this.repo = repo;
        this.validate = validate;
    }

    @GetMapping("/order/payment")
    public ResponseEntity<OrderPaymentResponse> orderPayment (
            @AuthenticationPrincipal SignedJWT signedJWT
    ) throws ParseException, StripeException {

        Long user_id = signedJWT.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        List<Item> order = this.repo.retrieveCartItems(user_id);
        List<String> titles = new ArrayList<>();

        for (int i = 0; i < order.size(); ++i) {
            titles.add(order.get(i).getMovieTitle());
        }

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
                total = total.add(discountedUnit.multiply(BigDecimal.valueOf(items.get(i).getQuantity())));
            }
            else {
                totalForMovie = items.get(i).getUnitPrice().multiply(BigDecimal.valueOf(items.get(i).getQuantity()));
                total = total.add(totalForMovie);
            }
        }

        total = total.setScale(2, RoundingMode.DOWN);

        total = total.multiply(BigDecimal.valueOf(100));

        Long payAmount = total.longValue();

        PaymentIntentCreateParams paymentIntentCreateParams =
                PaymentIntentCreateParams
                        .builder()
                        .setCurrency("USD")
                        .setDescription("Movie Payment")
                        .setAmount(payAmount)
                        .putMetadata("userId", user_id.toString())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods
                                        .builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();
        PaymentIntent paymentIntent;
        try {
            paymentIntent = PaymentIntent.create(paymentIntentCreateParams);
        } catch (StripeException e) {
            throw new ResultError(BillingResults.STRIPE_ERROR);
        }

        OrderPaymentResponse response = new OrderPaymentResponse()
                .setResult(BillingResults.ORDER_PAYMENT_INTENT_CREATED)
                .setPaymentIntentId(paymentIntent.getId())
                .setClientSecret(paymentIntent.getClientSecret());

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @PostMapping("/order/complete")
    public ResponseEntity<InsertResponse> completeOrder (
            @AuthenticationPrincipal SignedJWT signedJWT,
            @RequestBody() PaymentId paymentId
    ) throws StripeException, ParseException {

        Long user_id = signedJWT.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentId.getPaymentIntentId());

        String status = paymentIntent.getStatus();

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

        if (!status.equals("succeeded")) {
            throw new ResultError(BillingResults.ORDER_CANNOT_COMPLETE_NOT_SUCCEEDED);
        }

        if (!(user_id.toString().equals(paymentIntent.getMetadata().get("userId")))) {
            throw new ResultError(BillingResults.ORDER_CANNOT_COMPLETE_WRONG_USER);
        }

        this.repo.addSale(user_id, isPremium);

        InsertResponse response = new InsertResponse()
                .setResult(BillingResults.ORDER_COMPLETED);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @GetMapping("/order/list")
    public ResponseEntity<OrderListResponse> orderList (
            @AuthenticationPrincipal SignedJWT signedJWT
    ) throws ParseException {
        Long user_id = signedJWT.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        List<Sales> sales = this.repo.getUserSales(user_id);

        if (sales.isEmpty()) {
            throw new ResultError(BillingResults.ORDER_LIST_NO_SALES_FOUND);
        }

        OrderListResponse response = new OrderListResponse()
                .setSales(sales)
                .setResult(BillingResults.ORDER_LIST_FOUND_SALES);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }

    @GetMapping("/order/detail/{saleId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetails(
            @AuthenticationPrincipal SignedJWT signedJWT,
            @PathVariable Long saleId
    ) throws ParseException {
        Long user_id = signedJWT.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        OrderDetailResponse response = this.repo.getSaleById(saleId, user_id);

        return ResponseEntity
                .status(response.getResult().status())
                .body(response);
    }
}
