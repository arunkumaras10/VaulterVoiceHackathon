package com.kainosdub.rpbank;

import android.util.Log;

import java.io.File;
import java.util.concurrent.Callable;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ASRTask implements Callable<String> {

    private SpeechToTextService sttService;
    private Retrofit retrofit;
    private final String ASR_API_URL = "https://asr.iitm.ac.in/asr/v2/";
    private String filePath, language;

    public ASRTask(String filePath, String language) {
        this.filePath = filePath;
        this.language = language;
        retrofit = new Retrofit.Builder()
                .baseUrl(ASR_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        sttService = retrofit.create(SpeechToTextService.class);
    }

    @Override
    public String call() throws Exception {
        File file = new File(this.filePath);
        String mediaType="audio";
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(mediaType),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        MultipartBody.Part lang = MultipartBody.Part.createFormData("language", language);
        Call<ASRResponse> call = sttService.transcribe(lang, body);
        Response<ASRResponse> response = call.execute();
        String out = response.body().getTranscript() != null ? response.body().getTranscript(): "";
        return out;
    }
}
