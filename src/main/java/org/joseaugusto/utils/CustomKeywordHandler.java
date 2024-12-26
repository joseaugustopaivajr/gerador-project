package org.joseaugusto.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CustomKeywordHandler {
    private static final Path KEYWORDS_FILE = Path.of("custom_keywords.json");

    /**
     * Carrega palavras-chave existentes.
     */
    public static JSONObject loadExistingKeywords() {
        try {
            if (Files.exists(KEYWORDS_FILE)) {
                return new JSONObject(Files.readString(KEYWORDS_FILE));
            }
            return new JSONObject();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar palavras-chave personalizadas: " + e.getMessage());
        }
    }

    /**
     * Salva palavras-chave no arquivo JSON.
     */
    public static void saveKeywords(JSONObject keywords) {
        try {
            Files.writeString(KEYWORDS_FILE, keywords.toString(4)); // Salvar com indentação
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar palavras-chave personalizadas: " + e.getMessage());
        }
    }

    /**
     * Verifica se uma palavra-chave já é conhecida.
     */
    public static boolean isKnownKeyword(String keyword) {
        JSONObject existingKeywords = loadExistingKeywords();
        return existingKeywords.has(keyword);
    }

    /**
     * Adiciona uma nova palavra-chave ao armazenamento.
     */
    public static void addKeyword(String keyword, String language, String[] libraries, String[] examples) {
        JSONObject existingKeywords = loadExistingKeywords();
        JSONObject newEntry = new JSONObject();
        newEntry.put("language", language);
        newEntry.put("libraries", new JSONArray(libraries));
        newEntry.put("examples", new JSONArray(examples));

        existingKeywords.put(keyword, newEntry);
        saveKeywords(existingKeywords);
    }

    /**
     * Retorna as bibliotecas associadas a uma palavra-chave.
     */
    public static String[] getLibrariesForKeyword(String keyword) {
        JSONObject existingKeywords = loadExistingKeywords();
        if (existingKeywords.has(keyword)) {
            JSONArray libraries = existingKeywords.getJSONObject(keyword).getJSONArray("libraries");
            return libraries.toList().toArray(new String[0]);
        }
        return new String[0];
    }

    /**
     * Retorna exemplos associados a uma palavra-chave.
     */
    public static String[] getExamplesForKeyword(String keyword) {
        JSONObject existingKeywords = loadExistingKeywords();
        if (existingKeywords.has(keyword) && existingKeywords.getJSONObject(keyword).has("examples")) {
            JSONArray examples = existingKeywords.getJSONObject(keyword).getJSONArray("examples");
            return examples.toList().toArray(new String[0]);
        }
        return new String[0];
    }
}
