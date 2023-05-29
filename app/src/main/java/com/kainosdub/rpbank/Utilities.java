package com.kainosdub.rpbank;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.RequestBody;

public class Utilities {
    public static RequestBody jsonToRequestBody(String json) {
        try {
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(json)).toString());
            return body;
        } catch (JSONException e) {
            return null;
        }
    }

    public static RequestBody jsonToRequestBody(JSONObject json) {
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),json.toString());
        return body;
    }

    public static String jsonToEncryptedText(JSONObject jsonPayload) throws Exception {
        // add token
        try {
            jsonPayload.put("api_token", "rp-rv8fzt1yp6jl2a6");
            // RSA encrypt and then base64 encode the string
            RSAEncryptDecryptSingleton rsa = RSAEncryptDecryptSingleton.getInstance();
            String encryptedPayload = rsa.encrypt(jsonPayload.toString());
            return encryptedPayload;
        } catch (JSONException e) {
            return "";
        }
    }

    public static String decryptResponse(String encryptedResponse) throws Exception {
        String toDecrypt = encryptedResponse.substring(2, encryptedResponse.length()-1);
        RSAEncryptDecryptSingleton rsa = RSAEncryptDecryptSingleton.getInstance();
        return rsa.decrypt(toDecrypt);
    }
}
