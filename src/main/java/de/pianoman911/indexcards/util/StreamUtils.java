package de.pianoman911.indexcards.util;

import com.google.gson.JsonObject;
import de.pianoman911.indexcards.IndexCards;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class StreamUtils {

    public static String readFully(InputStream is) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonObject readJsonFully(InputStream is) {
        return IndexCards.GSON.fromJson(readFully(is), JsonObject.class);
    }

    public static void writeJsonFully(JsonObject json, OutputStream os) {
        try (OutputStreamWriter writer = new OutputStreamWriter(os)) {
            writer.write(IndexCards.GSON.toJson(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
