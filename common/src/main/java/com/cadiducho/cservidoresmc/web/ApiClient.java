package com.cadiducho.cservidoresmc.web;

import com.cadiducho.cservidoresmc.api.CSPlugin;
import com.cadiducho.cservidoresmc.web.model.ServerStats;
import com.cadiducho.cservidoresmc.web.model.VoteResponse;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class ApiClient {

    private static final String API_URL = "https://40servidoresmc.es/api2.php?clave=";

    private final CSPlugin plugin;
    private final Gson gson;

    @Getter
    private ServerStats lastStats = new ServerStats();
    @Getter
    private final Map<String, VoteResponse> savedResponses = new HashMap<>();

    public String apiKey() {
        return plugin.getConfiguration().getString("vote.client.key");
    }

    public int timeOut() {
        return plugin.getConfiguration().getInt("vote.client.readTimeOut");
    }

    public VoteResponse getSavedResponse(String name) {
        return savedResponses.getOrDefault(name, VoteResponse.EMPTY);
    }

    public CompletableFuture<VoteResponse> validateVote(String player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetchData("&nombre=" + player, "GET", VoteResponse.class);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot execute API call", e);
            }
        });
    }

    public void updateServerStats() {
        try {
            lastStats = fetchServerStats().get();
        } catch (Exception e) {
            if (plugin.getLogLevel() >= 4) {
                e.printStackTrace();
            }
        }
    }

    public CompletableFuture<ServerStats> fetchServerStats() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetchData("&estadisticas=1", "GET", ServerStats.class);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot execute API call", e);
            }
        });
    }

    /**
     * Obtener datos de la API, según unos parámetros dados, y parsearlo a un objeto
     * @param params Parámetros HTTP de la petición
     * @param method Método HTTP
     * @param type Clase a la que convertir los datos recibidos
     * @param <T> Tipo que retornará
     * @return El objeto con los datos solicitados a la API
     * @throws IOException Si falla al parsear o al conectarse a la API
     */
    private <T> T fetchData(String params, String method, Class<T> type) throws IOException {
        URL url = new URL(API_URL + apiKey() + params);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setReadTimeout(timeOut());

        try (Reader reader = new InputStreamReader(connection.getInputStream())) {
            return gson.fromJson(reader, type);
        }
    }
}
