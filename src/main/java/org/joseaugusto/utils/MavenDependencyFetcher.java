package org.joseaugusto.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;

public class MavenDependencyFetcher {

    private static final String MAVEN_SEARCH_URL = "https://search.maven.org/solrsearch/select";

    /**
     * Busca a dependência no Maven Central Repository.
     *
     * @param query Nome da biblioteca a ser buscada.
     * @return Dependência no formato Maven (groupId:artifactId:version).
     */
    public static String fetchDependency(String query) {
        try {
            // Construir URL de busca
            String url = MAVEN_SEARCH_URL + "?q=" + query + "&rows=1&wt=json";

            // Fazer a requisição HTTP
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                // Processar resposta JSON
                JSONObject json = new JSONObject(response.body().string());
                JSONObject doc = json.getJSONObject("response").getJSONArray("docs").getJSONObject(0);

                // Retornar no formato Maven
                return doc.getString("g") + ":" + doc.getString("a") + ":" + doc.getString("latestVersion");
            } else {
                throw new IOException("Erro na busca de dependências: " + response.message());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar dependência: " + e.getMessage());
        }
    }
}
