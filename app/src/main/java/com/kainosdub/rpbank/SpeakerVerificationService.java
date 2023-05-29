package com.kainosdub.rpbank;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface SpeakerVerificationService {
    @Multipart
    @POST("add")
    Call<ResponseBody> enroll(@Part MultipartBody.Part file, @Part MultipartBody.Part spk_id);

    @Multipart
    @POST("verify")
    Call<ResponseBody> verify(@Part MultipartBody.Part file, @Part MultipartBody.Part spk_id);
}
