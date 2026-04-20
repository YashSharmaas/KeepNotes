package com.example.yrmultimediaco.keepnotes.ai;



import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.yrmultimediaco.keepnotes.BuildConfig;

public class AIHelper {

    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    public static String rephraseText(String inputText) {
        OkHttpClient client = new OkHttpClient();

        // Updated prompt for better results
        String prompt = "Rephrase the following text to be professional and clear. " +
                "Output ONLY the rephrased text. Do not include explanations, " +
                "bullet points, or introductory remarks. " +
                "Text: " + inputText;

        // Use the latest stable model
        String model = "gemini-2.5-flash";
        String url = "https://generativelanguage.googleapis.com/v1/models/" + model + ":generateContent?key=" + API_KEY;

        String json = "{\"contents\": [{\"parts\":[{\"text\": \"" + prompt + "\"}]}]}";

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return "Error: " + response.code();

            String res = response.body().string();
            JSONObject jsonObject = new JSONObject(res);

            // Safety check for candidates array
            if (jsonObject.has("candidates")) {
                return jsonObject.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            }
            return "Error: No candidates found";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
}
