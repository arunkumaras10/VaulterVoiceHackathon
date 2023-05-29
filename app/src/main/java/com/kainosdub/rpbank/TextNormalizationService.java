package com.kainosdub.rpbank;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TextNormalizationService {
    @POST("textnorm")
    Call<ResponseBody> post(@Body RequestBody jsonPayload);
}
