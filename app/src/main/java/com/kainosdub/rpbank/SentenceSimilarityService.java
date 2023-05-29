package com.kainosdub.rpbank;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SentenceSimilarityService {
    @Headers({"Authorization: Bearer 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"})
    @POST("all-MiniLM-L6-v2")
    Call<ResponseBody> postEnglish(@Body RequestBody jsonPayload);

    @Headers({"Authorization: Bearer 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"})
    @POST("LaBSE")
    Call<ResponseBody> postTamil(@Body RequestBody jsonPayload);
}
