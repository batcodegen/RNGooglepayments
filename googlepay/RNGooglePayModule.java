package com.admob.googlepay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

/*
 * gateway identifier
 * gateway acc id(merchant id)
 * total price
 * currency code
 * merchant name
 * */

public class RNGooglePayModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private PaymentsClient mPaymentsClient;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 992;
    private static final String PACKAGE_NAME = "GPay";
    private Callback mReadyToPayCallback;
    private Callback onSuccessOrFailCallback;

    public RNGooglePayModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    /*
    * Call this method in componentDidMount
    * Sets up payment request
    * */

    @ReactMethod
    public void isReadyToPay(final Callback callback) {
        mPaymentsClient = PaymentUtil.createPaymentsClient(getCurrentActivity());
        final Optional<JSONObject> isReadyToPayJson = PaymentUtil.getIsReadyToPayRequest();
        if (!isReadyToPayJson.isPresent()) {
            callback.invoke("Pay JSON not ready", null);
            return;
        }
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        if (request == null) {
            callback.invoke("Pay request not ready", null);
            return;
        }

        Task<Boolean> task = mPaymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                try {
                    boolean result = task.getResult(ApiException.class);
                    if (result) {
                        callback.invoke(null, "ReadyToPay task load success");
                    } else {
                        callback.invoke("Unfortunately, Google Pay is not available on this phone.", null);
                    }
                } catch (ApiException exception) {
                    callback.invoke("ReadyToPay task failed", null);
                    Log.w("isReadyToPay failed", exception);
                }
            }
        });
    }

    @ReactMethod
    public void showPayment(ReadableMap attributes, Callback callback) {
        this.onSuccessOrFailCallback = callback;
        Optional<JSONObject> paymentDataRequestJson = PaymentUtil.getPaymentDataRequest(attributes);
        if (!paymentDataRequestJson.isPresent()) {
            callback.invoke("No payment data json present", null);
            return;
        }
        PaymentDataRequest request =
                PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());
        if (request != null) {
            AutoResolveHelper.resolveTask(
                    mPaymentsClient.loadPaymentData(request), getCurrentActivity(), LOAD_PAYMENT_DATA_REQUEST_CODE
            );
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        handlePaymentSuccess(paymentData);
                        break;
                    case Activity.RESULT_CANCELED:
                        // Nothing to here normally - the user simply cancelled without selecting a
                        // payment method.
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        handleError(status.getStatusCode());
                        break;
                    default:
                        // Do nothing.
                }
                break;
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        String paymentInformation = paymentData.toJson();
        if (paymentInformation == null) {
            return;
        }
        JSONObject paymentMethodData;

        try {
            paymentMethodData = new JSONObject(paymentInformation).getJSONObject("paymentMethodData");
            if (paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("type")
                    .equals("PAYMENT_GATEWAY")
                    && paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token")
                    .equals("examplePaymentMethodToken")
                    ) {
                AlertDialog alertDialog =
                        new AlertDialog.Builder(getCurrentActivity())
                                .setTitle("Warning")
                                .setMessage(
                                        "Gateway name set to \"example\" - please modify "
                                                + "Constants.java and replace it with your own gateway.")
                                .setPositiveButton("OK", null)
                                .create();
                alertDialog.show();
            }
            String billingName =
                    paymentMethodData.getJSONObject("info")
                            .getJSONObject("billingAddress").getString("name");
            Log.d("BillingName", billingName);
            WritableNativeMap writableMap = new WritableNativeMap();
            writableMap.putString("billingName", billingName);
            writableMap.putString("token", paymentMethodData.getJSONObject("tokenizationData").getString("token"));
            Toast.makeText(getCurrentActivity(),
                    "Successfully recieved payment", Toast.LENGTH_LONG)
                    .show();
            Log.d("GooglePaymentToken",
                    paymentMethodData.getJSONObject("tokenizationData").getString("token"));
            onSuccessOrFailCallback.invoke(null, writableMap);
        } catch (JSONException e) {
            Log.e("handlePaymentSuccess", "Error: " + e.toString());
            onSuccessOrFailCallback.invoke("handle payment error", null);
            return;
        }
    }

    private void handleError(int statusCode) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
        onSuccessOrFailCallback.invoke("loadPaymentData failed", null);
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public String getName() {
        return PACKAGE_NAME;
    }
}
