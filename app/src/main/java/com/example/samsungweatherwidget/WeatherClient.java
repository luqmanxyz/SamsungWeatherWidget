package com.example.samsungweatherwidget;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public final class WeatherClient {
    public static final class WeatherData {
        public double temperature;
        public double apparent;
        public double humidity;
        public double windKmh;
        public int weatherCode;
    }

    private WeatherClient() {}

    public static WeatherData fetch(double lat, double lon) throws Exception {
        String endpoint = String.format(Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.5f&longitude=%.5f&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto&forecast_days=1",
                lat, lon);
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "SamsungWeatherWidget/1.0");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
            reader.close();

            JSONObject root = new JSONObject(body.toString());
            JSONObject current = root.getJSONObject("current");
            WeatherData data = new WeatherData();
            data.temperature = current.getDouble("temperature_2m");
            data.apparent = current.getDouble("apparent_temperature");
            data.humidity = current.getDouble("relative_humidity_2m");
            data.weatherCode = current.getInt("weather_code");
            data.windKmh = current.getDouble("wind_speed_10m");
            return data;
        } finally {
            connection.disconnect();
        }
    }

    public static String description(int code) {
        if (code == 0) return "Clear Sky";
        if (code == 1) return "Mainly Clear";
        if (code == 2) return "Partly Cloudy";
        if (code == 3) return "Overcast";
        if (code == 45 || code == 48) return "Foggy";
        if (code >= 51 && code <= 57) return "Drizzle";
        if (code >= 61 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Rain Showers";
        if (code >= 85 && code <= 86) return "Snow Showers";
        if (code >= 95) return "Thunderstorm";
        return "Weather";
    }

    public static String icon(int code) {
        if (code == 0) return "☀";
        if (code <= 3) return "⛅";
        if (code == 45 || code == 48) return "🌫";
        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) return "🌧";
        if ((code >= 71 && code <= 77) || (code >= 85 && code <= 86)) return "❄";
        if (code >= 95) return "⛈";
        return "☁";
    }
}
