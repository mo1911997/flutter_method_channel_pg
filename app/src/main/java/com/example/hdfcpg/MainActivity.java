package com.example.hdfcpg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.payu.base.models.ErrorResponse;
import com.payu.base.models.PayUPaymentParams;
import com.payu.checkoutpro.PayUCheckoutPro;
import com.payu.checkoutpro.utils.PayUCheckoutProConstants;
import com.payu.ui.model.listeners.PayUCheckoutProListener;
import com.payu.ui.model.listeners.PayUHashGenerationListener;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_HASH_NAME;
import static com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_HASH_STRING;

public class MainActivity extends AppCompatActivity {
    ApiInterface apiInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiInterface = APIClient.getClient().create(ApiInterface.class);
        pgBuilder();

    }
    String generateStaticHash() {
        try {
            String input = "7rnFly|payment_related_details_for_mobile_sdk|7rnFly:mmd26.aka@gmail.com|"+"pjVQAWpA";
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] messageDigest = md.digest(input.getBytes());
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }

    }

    void pgBuilder() {
        HashMap<String, Object> additionalParams = new HashMap<>();
        additionalParams.put(PayUCheckoutProConstants.CP_PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK, generateStaticHash());
        Log.d("STATICHASH", generateStaticHash());
        PayUPaymentParams.Builder builder = new PayUPaymentParams.Builder();
        builder.setAmount("1.0")
                .setIsProduction(false)
                .setProductInfo("Macbook Pro")
                .setKey("7rnFly")
                .setPhone("8208888973")
                .setTransactionId(String.valueOf(System.currentTimeMillis()))
                .setFirstName("Mohit")
                .setEmail("mmd26.aka@gmail.com")
                .setSurl("https://payuresponse.firebaseapp.com/success")
                .setFurl("https://payuresponse.firebaseapp.com/failure")
                .setUserCredential("7rnFly" + ":mmd26.aka@gmail.com")
                .setAdditionalParams(additionalParams);

        PayUPaymentParams payUPaymentParams = builder.build();
        PayUCheckoutPro.open(
                this,
                payUPaymentParams,
                new PayUCheckoutProListener() {
                    @Override
                    public void onPaymentSuccess(@NotNull Object response) {
                        //Cast response object to HashMap
                        HashMap<String, Object> result = (HashMap<String, Object>) response;
                        String payuResponse = (String) result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE);
                        String merchantResponse = (String) result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE);
                    }

                    @Override
                    public void onPaymentFailure(@NotNull Object response) {
                        //Cast response object to HashMap
                        HashMap<String, Object> result = (HashMap<String, Object>) response;
                        String payuResponse = (String) result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE);
                        String merchantResponse = (String) result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE);
                    }

                    @Override
                    public void onPaymentCancel(boolean isTxnInitiated) {
                    }

                    @Override
                    public void onError(@NotNull ErrorResponse errorResponse) {
                        String errorMessage = errorResponse.getErrorMessage();
                    }

                    @Override
                    public void generateHash(@NotNull HashMap<String, String> valueMap, @NotNull PayUHashGenerationListener hashGenerationListener) {
                        String hashName = valueMap.get(CP_HASH_NAME);
                        String hashData = valueMap.get(CP_HASH_STRING);
                        Log.d("HASHNAME", hashName);
                        Log.d("HASHDATA", hashData);
                        if (!TextUtils.isEmpty(hashName) && !TextUtils.isEmpty(hashData)) {
                            //Generate Hash from your backend here
                            GenerateHashRequest generateHashRequest = new GenerateHashRequest();
                            generateHashRequest.setHashString(hashData);
                            Call<GenerateHashResponse> responseCall = apiInterface.generateHash(generateHashRequest);
                            responseCall.enqueue(new Callback<GenerateHashResponse>() {
                                @Override
                                public void onResponse(Call<GenerateHashResponse> call, Response<GenerateHashResponse> response) {
                                    Log.d("RESPONSE", response.body().toString());
                                    GenerateHashResponse generateHashResponse = response.body();
                                    String hash = generateHashResponse.hash;
//                            String hash = HashGenerationUtils.INSTANCE.generateHashFromSDK(hashData, salt);
                                    HashMap<String, String> dataMap = new HashMap<>();
                                    dataMap.put(hashName, hash);
                                    hashGenerationListener.onHashGenerated(dataMap);
                                }

                                @Override
                                public void onFailure(Call<GenerateHashResponse> call, Throwable t) {
                                    t.getMessage();
                                }
                            });

                        }
                    }
                });
    }
}