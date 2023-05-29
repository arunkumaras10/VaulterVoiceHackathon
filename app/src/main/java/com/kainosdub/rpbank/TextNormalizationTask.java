package com.kainosdub.rpbank;

import org.json.JSONObject;

import java.util.concurrent.Callable;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TextNormalizationTask implements Callable<String> {
    private JSONObject jsonPayload;
    private Retrofit retrofit;
    private final String API_URL = "https://nltm.iitm.ac.in/";
    private TextNormalizationService textNormalizationService;

    public TextNormalizationTask(JSONObject jsonPayload) {
        this.jsonPayload = jsonPayload;
        retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        textNormalizationService = retrofit.create(TextNormalizationService.class);
    }

    @Override
    public String call() throws Exception {
        RequestBody body = Utilities.jsonToRequestBody(jsonPayload);
        Call<ResponseBody> call = textNormalizationService.post(body);
        Response<ResponseBody> response = call.execute();
        return response.body().string();
    }
}

