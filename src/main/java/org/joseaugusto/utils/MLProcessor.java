package org.joseaugusto.utils;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class MLProcessor {
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public static JSONObject processUserInput(String userInput) {
        OkHttpClient client = new OkHttpClient();

        String prompt = "Dado o seguinte texto: \"" + userInput + "\", forneça:\n"
                + "1. Linguagem principal do projeto.\n"
                + "2. Funcionalidades desejadas (em forma de lista).\n"
                + "3. Bibliotecas associadas (em forma de lista de strings).\n\n"
                + "Resposta esperada:\n"
                + "{\n"
                + "  \"language\": \"<linguagem>\",\n"
                + "  \"features\": [\"<funcionalidade1>\", \"<funcionalidade2>\"],\n"
                + "  \"dependencies\": [\"<lib1>\", \"<lib2>\"]\n"
                + "}";

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", new org.json.JSONArray(new JSONObject[] {
                new JSONObject().put("role", "system").put("content", "Você é um assistente que interpreta descrições de projetos."),
                new JSONObject().put("role", "user").put("content", prompt)
        }));

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String content = new JSONObject(response.body().string())
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                return new JSONObject(content); // Converta o JSON String em um JSONObject
            } else {
                throw new RuntimeException("Erro ao conectar com OpenAI: " + response.message());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar entrada: " + e.getMessage());
        }
    }
}

