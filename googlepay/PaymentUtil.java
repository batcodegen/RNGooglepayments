package com.admob.googlepay;

import android.app.Activity;

import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public class PaymentUtil {
    private PaymentUtil() {
    }

    private static JSONObject getBaseRequest() throws JSONException {
        return new JSONObject().put("apiVersion", 2).put("apiVersionMinor", 0);
    }

    private static JSONArray getAllowedCardAuthMethods() {
        return new JSONArray(Constants.SUPPORTED_METHODS);
    }

    private static JSONArray getAllowedCardNetworks() {
        return new JSONArray(Constants.SUPPORTED_NETWORKS);
    }

    public static PaymentsClient createPaymentsClient(Activity activity) {
        Wallet.WalletOptions walletOptions =
                new Wallet.WalletOptions.Builder().setEnvironment(Constants.PAYMENTS_ENVIRONMENT).build();
        return Wallet.getPaymentsClient(activity, walletOptions);
    }

    private static JSONObject getBaseCardPaymentMethod() throws JSONException {
        JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");

        JSONObject parameters = new JSONObject();
        parameters.put("allowedAuthMethods", getAllowedCardAuthMethods());
        parameters.put("allowedCardNetworks", getAllowedCardNetworks());
        // Optionally, you can add billing address/phone number associated with a CARD payment method.
        parameters.put("billingAddressRequired", true);

        JSONObject billingAddressParameters = new JSONObject();
        billingAddressParameters.put("format", "FULL");

        parameters.put("billingAddressParameters", billingAddressParameters);

        cardPaymentMethod.put("parameters", parameters);

        return cardPaymentMethod;
    }

    public static Optional<JSONObject> getIsReadyToPayRequest() {
        try {
            JSONObject isReadyToPayRequest = getBaseRequest();
            isReadyToPayRequest.put("allowedPaymentMethods",
                    new JSONArray().put(getBaseCardPaymentMethod()));
            return Optional.of(isReadyToPayRequest);
        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    private static JSONObject getGatewayTokenizationSpecification(String gatewayIdentifier, String gatewayMerchantId)
            throws JSONException, RuntimeException {
        JSONObject tokenizationSpecification = new JSONObject();
        tokenizationSpecification.put("type", "PAYMENT_GATEWAY");
        tokenizationSpecification.put("parameters", new JSONObject()
                .put("gateway", gatewayIdentifier)
                .put("gatewayMerchantId", gatewayMerchantId));
        return tokenizationSpecification;
    }

    private static JSONObject getCardPaymentMethod(String gatewayIdentifier, String gatewayMerchantId)
            throws JSONException {
        JSONObject cardPaymentMethod = getBaseCardPaymentMethod();
        cardPaymentMethod.put("tokenizationSpecification",
                getGatewayTokenizationSpecification(gatewayIdentifier, gatewayMerchantId));
        return cardPaymentMethod;
    }

    private static JSONObject getTransactionInfo(String price, String currencyCode) throws JSONException {
        JSONObject transactionInfo = new JSONObject();
        transactionInfo.put("totalPrice", price);
        transactionInfo.put("totalPriceStatus", "FINAL");
        transactionInfo.put("currencyCode", currencyCode);

        return transactionInfo;
    }

    private static JSONObject getMerchantInfo(String merchantName) throws JSONException {
        return new JSONObject().put("merchantName", merchantName);
    }

    public static Optional<JSONObject> getPaymentDataRequest(ReadableMap attributes) {
        String gatewayIdentifier = attributes.getString("gatewayIdentifier");
        String gatewayMerchantId = attributes.getString("gatewatMerchantId");
        String price = attributes.getString("price");
        String currencyCode = attributes.getString("currencyCode");
        String merchantName = attributes.getString("merchantName");
        try {
            JSONObject paymentDataRequest = PaymentUtil.getBaseRequest();
            paymentDataRequest.put(
                    "allowedPaymentMethods", new JSONArray()
                            .put(PaymentUtil.getCardPaymentMethod(gatewayIdentifier, gatewayMerchantId)));
            paymentDataRequest
                    .put("transactionInfo", PaymentUtil.getTransactionInfo(price, currencyCode));
            paymentDataRequest.put("merchantInfo", PaymentUtil.getMerchantInfo(merchantName));
            paymentDataRequest.put("shippingAddressRequired", true);

            JSONObject shippingAddressParameters = new JSONObject();
            shippingAddressParameters.put("phoneNumberRequired", false);
            paymentDataRequest.put("shippingAddressParameters", shippingAddressParameters);
            return Optional.of(paymentDataRequest);
        } catch (JSONException e) {
            return Optional.empty();
        }
    }
}
