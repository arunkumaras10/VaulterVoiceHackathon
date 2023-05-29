package com.kainosdub.rpbank;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SpeechToCommandTask implements Callable<Command> {
    private String speechText, language;
    private Retrofit retrofit;
    private final String API_URL = "https://api-inference.huggingface.co/models/sentence-transformers/";
    private SentenceSimilarityService service;
    private LinkedHashMap<String, Command> textToCommandMappingsEnglish, textToCommandMappingsTamil;
    private ArrayList<String> textEnglish, textTamil;
    private ArrayList<Command> commandsEnglish, commandsTamil;

    public SpeechToCommandTask(String speechText, String language) {
        this.speechText = speechText;
        this.language = language;
        retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(SentenceSimilarityService.class);

        initEnglishCommandsMap();
        initTamilCommandsMap();

        textEnglish = new ArrayList<>(textToCommandMappingsEnglish.keySet());
        commandsEnglish = new ArrayList<>(textToCommandMappingsEnglish.values());
        textTamil = new ArrayList<>(textToCommandMappingsTamil.keySet());
        commandsTamil = new ArrayList<>(textToCommandMappingsTamil.values());
    }

    private void initEnglishCommandsMap() {
        textToCommandMappingsEnglish = new LinkedHashMap<>(30);
        textToCommandMappingsEnglish.put("sign in", Command.LOGIN);
        textToCommandMappingsEnglish.put("log in", Command.LOGIN);
        textToCommandMappingsEnglish.put("signin", Command.LOGIN);
        textToCommandMappingsEnglish.put("login", Command.LOGIN);

        textToCommandMappingsEnglish.put("register", Command.REGISTER);
        textToCommandMappingsEnglish.put("register user", Command.REGISTER);
        textToCommandMappingsEnglish.put("sign up", Command.REGISTER);
        textToCommandMappingsEnglish.put("signup", Command.REGISTER);

        textToCommandMappingsEnglish.put("check balance", Command.CHECK_BALANCE);
        textToCommandMappingsEnglish.put("check account balance", Command.CHECK_BALANCE);
        textToCommandMappingsEnglish.put("check my balance", Command.CHECK_BALANCE);
        textToCommandMappingsEnglish.put("show balance", Command.CHECK_BALANCE);
        textToCommandMappingsEnglish.put("show my balance", Command.CHECK_BALANCE);
        textToCommandMappingsEnglish.put("show account balance", Command.CHECK_BALANCE);
        textToCommandMappingsEnglish.put("show balance", Command.CHECK_BALANCE);

        textToCommandMappingsEnglish.put("last ten transactions", Command.LAST_TEN_TRANSACTION_HISTORY);
        textToCommandMappingsEnglish.put("last 10 transactions", Command.LAST_TEN_TRANSACTION_HISTORY);
        textToCommandMappingsEnglish.put("show last ten transactions", Command.LAST_TEN_TRANSACTION_HISTORY);

        textToCommandMappingsEnglish.put("update details", Command.UPDATE_USER_DETAILS);
        textToCommandMappingsEnglish.put("update my details", Command.UPDATE_USER_DETAILS);
        textToCommandMappingsEnglish.put("update user details", Command.UPDATE_USER_DETAILS);

        textToCommandMappingsEnglish.put("send money", Command.SEND_MONEY);
        textToCommandMappingsEnglish.put("send money to XYZ", Command.SEND_MONEY);
        textToCommandMappingsEnglish.put("send N rupees to XYZ", Command.SEND_MONEY);
        textToCommandMappingsEnglish.put("send N rs to XYZ", Command.SEND_MONEY);

        textToCommandMappingsEnglish.put("request money", Command.REQUEST_MONEY);
        textToCommandMappingsEnglish.put("request money from XYZ", Command.REQUEST_MONEY);
        textToCommandMappingsEnglish.put("request N rupees from XYZ", Command.REQUEST_MONEY);
        textToCommandMappingsEnglish.put("request N rs from XYZ", Command.REQUEST_MONEY);

        textToCommandMappingsEnglish.put("read details", Command.READ_INFO);
        textToCommandMappingsEnglish.put("read my details", Command.READ_INFO);
        textToCommandMappingsEnglish.put("read info", Command.READ_INFO);
        textToCommandMappingsEnglish.put("read my info", Command.READ_INFO);
        textToCommandMappingsEnglish.put("read information", Command.READ_INFO);
        textToCommandMappingsEnglish.put("read my information", Command.READ_INFO);
    }

    private void initTamilCommandsMap() {
        textToCommandMappingsTamil = new LinkedHashMap<>(50);

        textToCommandMappingsTamil.put("சைனின் செய்ய வேண்டும்", Command.LOGIN);
        textToCommandMappingsTamil.put("சைனின் செய்", Command.LOGIN);
        textToCommandMappingsTamil.put("சைனின்", Command.LOGIN);
        textToCommandMappingsTamil.put("லாகின் செய்ய வேண்டும்", Command.LOGIN);
        textToCommandMappingsTamil.put("லாகின் செய்", Command.LOGIN);
        textToCommandMappingsTamil.put("லாகின்", Command.LOGIN);
        textToCommandMappingsTamil.put("உள் நுழைய வேண்டும்", Command.LOGIN);
        textToCommandMappingsTamil.put("உள்நுழைய வேண்டும்", Command.LOGIN);
        textToCommandMappingsTamil.put("உள் நுழை", Command.LOGIN);
        textToCommandMappingsTamil.put("உள்நுழை", Command.LOGIN);
        textToCommandMappingsTamil.put("உள்நுழைக", Command.LOGIN);

        textToCommandMappingsTamil.put("ரிஜிஸ்டர் செய்", Command.REGISTER);
        textToCommandMappingsTamil.put("பதிவு செய்ய வேண்டும்", Command.REGISTER);
        textToCommandMappingsTamil.put("பதிவு செய்", Command.REGISTER);
        textToCommandMappingsTamil.put("பதிவு செய்க", Command.REGISTER);
        textToCommandMappingsTamil.put("பதிவேற்றம்", Command.REGISTER);
        textToCommandMappingsTamil.put("புதிய பயனாளர் பதிவேற்றம்", Command.REGISTER);
        textToCommandMappingsTamil.put("புதிய பயனர் பதிவேற்றம்", Command.REGISTER);

        textToCommandMappingsTamil.put("எனது கணக்கு இருப்பு", Command.CHECK_BALANCE);
        textToCommandMappingsTamil.put("கணக்கு இருப்பு", Command.CHECK_BALANCE);
        textToCommandMappingsTamil.put("இருப்புத் தொகை", Command.CHECK_BALANCE);
        textToCommandMappingsTamil.put("எனது கணக்கு இருப்பை காமி", Command.CHECK_BALANCE);
        textToCommandMappingsTamil.put("எனது அக்கவுண்ட் பேலன்ஸ்", Command.CHECK_BALANCE);
        textToCommandMappingsTamil.put("எனது அக்கவுண்ட் பேலன்ஸ் காண்பி", Command.CHECK_BALANCE);
        textToCommandMappingsTamil.put("எனது கணக்கு இருப்பை காண்பி", Command.CHECK_BALANCE);

        textToCommandMappingsTamil.put("எனது கடைசி பத்து ட்ரான்ஸாக்ஷன்களை காண்பி", Command.LAST_TEN_TRANSACTION_HISTORY);
        textToCommandMappingsTamil.put("எனது கடைசி பத்து பரிவர்த்தனைகளை காட்டு", Command.LAST_TEN_TRANSACTION_HISTORY);
        textToCommandMappingsTamil.put("கடைசி பத்து பரிவர்த்தனைகள்", Command.LAST_TEN_TRANSACTION_HISTORY);
        textToCommandMappingsTamil.put("கடைசி பத்து ட்ரான்ஸாக்ஷன்கள்", Command.LAST_TEN_TRANSACTION_HISTORY);

        textToCommandMappingsTamil.put("எனது விவரங்களை புதுப்பிக்க வேண்டும்", Command.UPDATE_USER_DETAILS);
        textToCommandMappingsTamil.put("எனது விவரங்களை மாற்ற வேண்டும்", Command.UPDATE_USER_DETAILS);
        textToCommandMappingsTamil.put("எனது தகவல்களை அப்டேட் செய்ய வேண்டும்", Command.UPDATE_USER_DETAILS);
        textToCommandMappingsTamil.put("அப்டேட் டீடெயில்ஸ்", Command.UPDATE_USER_DETAILS);

        textToCommandMappingsTamil.put("பணம் அனுப்பு", Command.SEND_MONEY);
        textToCommandMappingsTamil.put("அருணுக்கு பணம் அனுப்பு", Command.SEND_MONEY);
        textToCommandMappingsTamil.put("அருணுக்கு நூறு ரூபாய் பணம் அனுப்பு", Command.SEND_MONEY);
        textToCommandMappingsTamil.put("பணம் அனுப்ப வேண்டும்", Command.SEND_MONEY);

        textToCommandMappingsTamil.put("பணம் கோரிக்கை", Command.REQUEST_MONEY);
        textToCommandMappingsTamil.put("நண்பரிடம் பணம் வேண்டுகோள்", Command.REQUEST_MONEY);
        textToCommandMappingsTamil.put("பணம் கேட்க வேண்டும்", Command.REQUEST_MONEY);
        textToCommandMappingsTamil.put("நண்பரிடம் நூறு ரூபாய் பணம் கோரிக்கை செய்ய வேண்டும்", Command.REQUEST_MONEY);

        textToCommandMappingsTamil.put("எனது விவரங்களைப் படித்துக் காண்பி", Command.READ_INFO);
        textToCommandMappingsTamil.put("விவரங்களைப் படி", Command.READ_INFO);
        textToCommandMappingsTamil.put("எனது தகவல்களைப் படி", Command.READ_INFO);
    }


    @Override
    public Command call() throws Exception {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("source_sentence", speechText);
        if(language != null && language.compareToIgnoreCase("tamil") == 0) {
            innerMap.put("sentences", textTamil);
        } else {
            innerMap.put("sentences", textEnglish);
        }
        map.put("inputs", innerMap);
        JSONObject jsonPayload = new JSONObject(map);
        Log.d("payload", jsonPayload.toString());
        RequestBody body = Utilities.jsonToRequestBody(jsonPayload);
        Call<ResponseBody> call = null;
        if(language != null && language.compareToIgnoreCase("tamil") == 0) {
            call = service.postTamil(body);
        } else {
            call = service.postEnglish(body);
        }
        Response<ResponseBody> response = call.execute();
        String reponseBody = response.body().string();
        Log.d("code", String.valueOf(response.code()));
        Log.d("error", String.valueOf(response.errorBody()));
        Log.d("sentencesimscore", reponseBody);
        if(Integer.valueOf(response.code()) == 200) {
            JSONArray array = new JSONArray(reponseBody);
            Log.d("array", array.toString());
            double maxValue = -100;
            int maxIndex = -1;
            for (int i = 0; i < array.length(); i++) {
                double current = array.getDouble(i);
                if(current > 0.5 && current > maxValue) {
                    maxValue = current;
                    maxIndex = i;
                }
            }
            if(language != null && language.compareToIgnoreCase("tamil") == 0) {
                return maxIndex!=-1 ? commandsTamil.get(maxIndex) : Command.UNRECOGNIZED;
            } else {
                return maxIndex!=-1 ? commandsEnglish.get(maxIndex) : Command.UNRECOGNIZED;
            }
        } else {
            return Command.UNRECOGNIZED;
        }
    }
}
