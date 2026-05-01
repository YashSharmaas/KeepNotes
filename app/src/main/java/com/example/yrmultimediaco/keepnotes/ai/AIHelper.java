package com.example.yrmultimediaco.keepnotes.ai;



import android.util.Log;

import org.json.JSONObject;
import org.json.JSONArray;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.yrmultimediaco.keepnotes.BuildConfig;


import java.util.concurrent.TimeUnit;

public class AIHelper {

    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String UNSPLASH_KEY = BuildConfig.UNSPLASH_ACCESS_KEY;
    private static final MediaType JSON = MediaType.parse("application/json");

    /*public static String rephraseText(String inputText, boolean includeSources) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        String prompt =
                "You are a text rewriter.\n\n" +

                        "STRICT RULES (follow exactly):\n" +
                        "1. Detect input language (English, Hindi, Hinglish).\n" +
                        "2. Rephrase ONLY in the SAME language.\n" +
                        "3. DO NOT translate.\n" +
                        "4. DO NOT provide multiple versions.\n" +
                        "5. DO NOT add headings, labels, or explanations.\n" +
                        "6. Output ONLY one sentence.\n\n" +

                        "Return ONLY the final rephrased sentence.\n\n" +

                        "Input:\n" + inputText;

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

        try {
            JSONObject requestJson = new JSONObject()
                    .put("contents", new org.json.JSONArray()
                            .put(new JSONObject()
                                    .put("parts", new org.json.JSONArray()
                                            .put(new JSONObject().put("text", prompt))
                                    )
                            )
                    );

            RequestBody body = RequestBody.create(requestJson.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                return "Error: API failed - " + response.code();
            }

            String resBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(resBody);

            return jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }


    }*/

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /*public static String rephraseText(String inputText, boolean includeSources) {
        // Correct model: gemini-1.5-flash
        //String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
        //String url = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash-latest:generateContent?key=" + API_KEY;
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;


        String prompt = "Rephrase this professionally. Detect language and respond in the same language: " + inputText +
                "\n\nAt the end, add a new line: KEYWORD: [two nouns]";

        for (int i = 0; i < 3; i++) {
            try {
                JSONObject requestJson = new JSONObject()
                        .put("contents", new JSONArray()
                                .put(new JSONObject().put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", prompt)))));

                RequestBody body = RequestBody.create(requestJson.toString(), JSON);
                Request request = new Request.Builder().url(url).post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        return jsonResponse.getJSONArray("candidates")
                                .getJSONObject(0).getJSONObject("content")
                                .getJSONArray("parts").getJSONObject(0).getString("text");
                    } else if (response.code() == 429 || response.code() >= 500) {
                        // FIX: Handle both 429 (Too Many Requests) AND 50x (Server Errors like 503)
                        // This will force the loop to wait and try again instead of immediately failing.
                        Log.w("AI_RETRY", "Server busy (HTTP " + response.code() + "). Retrying...");
                        Thread.sleep(3000 * (i + 1));
                    } else {
                        // Return the exact error code for client-side errors (400, 403, 404)
                        return "Error: HTTP " + response.code();
                    }
                }
            } catch (Exception e) {
                Log.e("AI_ERROR", "Attempt " + i + " failed", e);
            }
        }
        return "Error: Network issue or AI busy after retries";
    }*/

    public static String rephraseText(String inputText, boolean includeSources) {
        // FIX 1: Use the correct, stable model name: gemini-1.5-flash-latest
        //String url = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash-latest:generateContent?key=" + API_KEY.trim();
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;


        String prompt = "Rephrase this professionally. Detect language and respond in the same language: " + inputText +
                "\n\nAt the end, add a new line: KEYWORD: [two nouns]";

        for (int i = 0; i < 3; i++) {
            try {
                JSONObject requestJson = new JSONObject()
                        .put("contents", new JSONArray()
                                .put(new JSONObject().put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", prompt)))));

                RequestBody body = RequestBody.create(requestJson.toString(), JSON);
                Request request = new Request.Builder().url(url).post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    String responseData = response.body() != null ? response.body().string() : "";

                    if (response.isSuccessful()) {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        return jsonResponse.getJSONArray("candidates")
                                .getJSONObject(0).getJSONObject("content")
                                .getJSONArray("parts").getJSONObject(0).getString("text");

                    } else if (response.code() == 429) {
                        // FIX 2: If quota is exceeded, DO NOT retry. Inform the user.
                        return "Error: You are clicking too fast! Please wait 60 seconds.";

                    } else if (response.code() == 404) {
                        // FIX 3: If model is not found, stop immediately.
                        return "Error: AI Model configuration is wrong (404).";

                    } else if (response.code() >= 500) {
                        // ONLY retry on 500+ (Server busy/503)
                        Log.w("AI_RETRY", "Server busy. Waiting to try again...");
                        Thread.sleep(2000 * (i + 1));
                    } else {
                        return "Error: HTTP " + response.code();
                    }
                }
            } catch (Exception e) {
                Log.e("AI_ERROR", "Attempt " + i + " failed", e);
            }
        }
        return "Error: AI is currently unavailable. Try again later.";
    }
    public static String convertToEnglish(String inputText) {
        String prompt =  "Convert the following text into clear English.\n" +
                "It may be Hindi, Hinglish, or mixed.\n" +
                "Return ONLY English sentence.\n\n" +
                "Input:\n" + inputText;

                //"Convert the following text into clear English. Return ONLY English sentence.\n\nInput:\n" + inputText;

        try {
            JSONObject requestJson = new JSONObject()
                    .put("contents", new JSONArray()
                            .put(new JSONObject().put("parts", new JSONArray()
                                    .put(new JSONObject().put("text", prompt))))
                    );

            RequestBody body = RequestBody.create(requestJson.toString(), JSON);

            // Fixed the model name here too
            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            if(response.isSuccessful()) {
                String resBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(resBody);
                return jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0).getJSONObject("content")
                        .getJSONArray("parts").getJSONObject(0).getString("text");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputText;
    }

    /*public static String convertToEnglish(String inputText) {

        OkHttpClient client = new OkHttpClient();

        String prompt =
                "Convert the following text into clear English.\n" +
                        "It may be Hindi, Hinglish, or mixed.\n" +
                        "Return ONLY English sentence.\n\n" +
                        "Input:\n" + inputText;

        try {
            JSONObject requestJson = new JSONObject()
                    .put("contents", new JSONArray()
                            .put(new JSONObject()
                                    .put("parts", new JSONArray()
                                            .put(new JSONObject().put("text", prompt))
                                    )
                            )
                    );

            RequestBody body = RequestBody.create(requestJson.toString(), JSON);

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();

            String resBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(resBody);

            return jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

        } catch (Exception e) {
            return inputText;
        }
    }*/

    public static String fetchImageUrl(String query) {
        OkHttpClient client = new OkHttpClient();

        // Use the Access Key here
        String url = "https://api.unsplash.com/photos/random?query=" + query + "&client_id=" + UNSPLASH_KEY;

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            String responseData = response.body().string();

            if (response.isSuccessful()) {
                JSONObject json = new JSONObject(responseData);
                // 'urls' -> 'regular' is the standard high-quality image path
                return json.getJSONObject("urls").getString("regular");
            } else {
                // Log the error body to see if your API key is expired/wrong
                android.util.Log.e("UnsplashError", responseData);
            }
        } catch (Exception e) {
            android.util.Log.e("UnsplashException", e.getMessage());
        }
        return null;
    }
}

