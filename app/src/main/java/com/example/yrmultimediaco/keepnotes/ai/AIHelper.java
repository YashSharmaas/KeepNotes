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

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public static String rephraseText(String inputText, boolean includeSources) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

        String prompt = "You are a quick note-taking assistant. STRICT RULES:\n" +
                "1. ONLY rephrase the input professionally. Do NOT answer questions or give advice.\n" +
                "2. If the user asks a question, rephrase it as an objective to-do or reminder.\n" +
                "3. MAXIMUM output length is 2 sentences.\n" +
                "4. Respond in the same language as the input.\n" +
                "Input: " + inputText + "\n\n" +
                "Add new line at end: KEYWORD: [two nouns]";

        if (includeSources) {
            prompt += "\nAdd another new line at the very end: SOURCE: [Provide exactly ONE valid website URL related to the topic. If none, write SOURCE: NONE]";
        }

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

                        if (jsonResponse.has("usageMetadata")) {
                            JSONObject usage = jsonResponse.getJSONObject("usageMetadata");
                            int totalTokens = usage.optInt("totalTokenCount", 0);
                            int promptTokens = usage.optInt("promptTokenCount", 0);
                            Log.d("Gemini_Usage", "Tokens used -> Prompt: " + promptTokens + " | Total: " + totalTokens);
                        }

                        return jsonResponse.getJSONArray("candidates")
                                .getJSONObject(0).getJSONObject("content")
                                .getJSONArray("parts").getJSONObject(0).getString("text");

                    } else if (response.code() == 429) {
                        Log.e("Quota_Error", "Exact 429 Response: " + responseData);

                        try {
                            if (!responseData.isEmpty()) {
                                JSONObject errorJson = new JSONObject(responseData);
                                if (errorJson.has("error")) {
                                    String apiMessage = errorJson.getJSONObject("error").optString("message", "").toLowerCase();

                                    if (apiMessage.contains("day") || apiMessage.contains("daily")) {
                                        return "Error: Daily limit reached. Use a new API key.";
                                    } else if (apiMessage.contains("hour")) {
                                        return "Error: Hourly limit reached. Please wait.";
                                    } else if (apiMessage.contains("minute")) {
                                        return "Error: Fast usage limit hit. Wait 60 seconds.";
                                    } else if (apiMessage.contains("quota") || apiMessage.contains("exhausted")) {
                                        // Catching the standard Google generic quota error
                                        return "Error: API Quota exhausted. Please switch API Key.";
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Quota_Error", "Failed to parse error JSON", e);
                        }

                        return "Error: Quota exhausted (Wait 60s, or check limits).";

                    } else if (response.code() >= 500) {
                        Log.w("AI_RETRY", "Server busy. Waiting to try again...");
                        Thread.sleep(2000 * (i + 1)); // Exponential backoff
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

    public static String fetchImageUrl(String query) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://api.unsplash.com/photos/random?query=" + query + "&client_id=" + UNSPLASH_KEY;

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            String responseData = response.body().string();

            if (response.isSuccessful()) {
                JSONObject json = new JSONObject(responseData);
                return json.getJSONObject("urls").getString("regular");
            } else {
                android.util.Log.e("UnsplashError", responseData);
            }
        } catch (Exception e) {
            android.util.Log.e("UnsplashException", e.getMessage());
        }
        return null;
    }
}

