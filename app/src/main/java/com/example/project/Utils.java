package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {

    public static User currentUser;

    public static FirebaseAuth auth;
    public static FirebaseUser user;

    public interface CurrentUserCallback {
        void onUserReady(User user);
    }

    public static void getCurrentUser(CurrentUserCallback callback) {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (currentUser != null) {
            callback.onUserReady(currentUser);
        } else if (user != null) {
            FirebaseDatabaseManager dbManager = new FirebaseDatabaseManager();
            dbManager.fetchUserByUid(user.getUid())
                    .addOnSuccessListener(fetchedUser -> {
                        currentUser = fetchedUser;
                        callback.onUserReady(currentUser);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Utils", "Kullanıcı verisi alınamadı: " + e.getMessage());
                        callback.onUserReady(null); // ya da error durumu için farklı callback verilebilir
                    });
        } else {
            callback.onUserReady(null);
        }
    }

    public static void setCurrentUser(User currentUser) {
        Utils.currentUser = currentUser;
    }

    public static void getCitiesFromApi(String countryName, Context context, Consumer<List<String>> callback) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("country", countryName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MediaType.get("application/json"), json.toString());
        Request request = new Request.Builder()
                .url("https://countriesnow.space/api/v0.1/countries/cities")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String resBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(resBody);
                        JSONArray citiesArray = jsonResponse.getJSONArray("data");

                        List<String> cityList = new ArrayList<>();
                        for (int i = 0; i < citiesArray.length(); i++) {
                            cityList.add(citiesArray.getString(i));
                        }

                        new Handler(Looper.getMainLooper()).post(() -> callback.accept(cityList));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
