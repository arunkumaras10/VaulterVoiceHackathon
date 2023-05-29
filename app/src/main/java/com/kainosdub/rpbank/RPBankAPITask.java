package com.kainosdub.rpbank;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RPBankAPITask implements Callable<String> {
    private JSONObject jsonPayload;
    private Retrofit retrofit;
    private final String API_URL = "https://events.respark.iitm.ac.in:3000/";
    private RPBankService rpBankService;

    public RPBankAPITask(JSONObject jsonPayload) {
        this.jsonPayload = jsonPayload;

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            httpClientBuilder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            httpClientBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        OkHttpClient okHttpClient = httpClientBuilder.build();

        retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        rpBankService = retrofit.create(RPBankService.class);
    }

    @Override
    public String call() throws Exception {
        // RequestBody body = Utilities.jsonToRequestBody(jsonPayload);
        String body = Utilities.jsonToEncryptedText(jsonPayload);
        Call<String> call = rpBankService.post(body);
        Response<String> response = call.execute();
        String encryptedResponse = response.body();
        System.out.println(encryptedResponse);
        String decryptedResponse = Utilities.decryptResponse(encryptedResponse);
        System.out.println("decrypted:" + decryptedResponse);
        return decryptedResponse;
    }
}
