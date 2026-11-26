package br.com.cafes.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDateTime;

/**
 * Configuracao unica do Gson (JSON <-> objeto Java).
 *
 * O Gson nao sabe converter LocalDateTime sozinho, entao registramos um
 * adaptador que usa o formato ISO (ex.: "2026-11-26T14:30:00").
 */
public final class JsonUtil {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (valor, tipo, ctx) -> new JsonPrimitive(valor.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, tipo, ctx) -> LocalDateTime.parse(json.getAsString()))
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private JsonUtil() {
    }
}
