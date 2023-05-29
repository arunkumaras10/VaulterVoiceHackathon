package com.kainosdub.rpbank;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface SpeechToTextService {

    @Multipart
    @POST("decode")
    Call<ASRResponse> transcribe(@Part MultipartBody.Part language, @Part MultipartBody.Part file);
}

