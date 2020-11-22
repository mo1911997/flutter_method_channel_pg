package com.example.hdfcpg;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiInterface {
    @POST("/api/users/calculate_payment_hash")
    Call<GenerateHashResponse> generateHash(@Body GenerateHashRequest req);
}

