package com.kainosdub.rpbank;

import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.Callable;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SpeakerVerificationTask implements Callable<Boolean> {

    private SpeakerVerificationService speakerVerificationService;
    private Retrofit retrofit;
    private final String API_URL = "https://nltm.iitm.ac.in/spkver/";
    private String filePath, spkId;
    boolean enroll;

    public SpeakerVerificationTask(String filePath, String spkId, boolean enroll) {
        this.filePath = filePath;
        this.spkId = spkId;
        this.enroll = enroll;
        retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        speakerVerificationService = retrofit.create(SpeakerVerificationService.class);
    }

    @Override
    public Boolean call() throws Exception {
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
        MultipartBody.Part spkid = MultipartBody.Part.createFormData("spk_id", spkId);
        Call<ResponseBody> call = null;
        if(enroll) {
            call = speakerVerificationService.enroll(body, spkid);
            Response<ResponseBody> response = call.execute();
            String out = response.body() != null ? response.body().string(): "";
            Log.d("enroll", out);
            return true;
        } else {
            call = speakerVerificationService.verify(body, spkid);
            Response<ResponseBody> response = call.execute();
            String out = response.body() != null ? response.body().string(): "";
            JSONObject jsonObject = new JSONObject(out);
            //System.out.println("json" + jsonObject.toString());
            //System.out.println("vs" + jsonObject.get("verification_status") + ("null".equals(jsonObject.get("verification_status"))));
            Boolean verified = jsonObject.getBoolean("verification_status");
            return verified;
        }
    }
}