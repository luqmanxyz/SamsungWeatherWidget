package com.example.samsungweatherwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeatherWidgetProvider extends AppWidgetProvider {
    private static final String PREFS = "widget_prefs";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final PendingResult pendingResult = goAsync();
        final Context appContext = context.getApplicationContext();
        new Thread(() -> {
            try {
                updateAllWidgets(appContext);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, WeatherWidgetProvider.class));
        for (int id : ids) updateWidget(context, manager, id);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Good Morning" : (hour < 18 ? "Good Afternoon" : "Good Evening");
        views.setTextViewText(R.id.greetingText, greeting);
        views.setTextViewText(R.id.dateText, new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.ENGLISH).format(new Date()));

        int[] dowIds = {R.id.dow1,R.id.dow2,R.id.dow3,R.id.dow4,R.id.dow5,R.id.dow6,R.id.dow7};
        int[] dayIds = {R.id.day1,R.id.day2,R.id.day3,R.id.day4,R.id.day5,R.id.day6,R.id.day7};
        String[] dows = {"SUN","MON","TUE","WED","THU","FRI","SAT"};
        Calendar start = (Calendar) now.clone();
        start.add(Calendar.DAY_OF_MONTH, -(now.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY));
        for (int i=0;i<7;i++) {
            Calendar d = (Calendar) start.clone();
            d.add(Calendar.DAY_OF_MONTH, i);
            views.setTextViewText(dowIds[i], dows[i]);
            views.setTextViewText(dayIds[i], String.valueOf(d.get(Calendar.DAY_OF_MONTH)));
            views.setInt(dayIds[i], "setBackgroundResource", i == now.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY ? R.drawable.date_today : R.drawable.date_chip);
        }

        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        double lat = Double.longBitsToDouble(p.getLong("lat", Double.doubleToLongBits(24.4539)));
        double lon = Double.longBitsToDouble(p.getLong("lon", Double.doubleToLongBits(54.3773)));
        String city = p.getString("city", "Abu Dhabi");
        views.setTextViewText(R.id.locationText, city);

        try {
            WeatherClient.WeatherData data = WeatherClient.fetch(lat, lon);
            views.setTextViewText(R.id.tempText, Math.round(data.temperature) + "°");
            views.setTextViewText(R.id.conditionText, WeatherClient.description(data.weatherCode));
            double windMs = data.windKmh / 3.6;
            String details = String.format(Locale.US, "Feels like %.0f° · Humidity %.0f%% · Wind %.1f m/s", data.apparent, data.humidity, windMs);
            views.setTextViewText(R.id.detailsText, details);
            views.setTextViewText(R.id.weatherIcon, WeatherClient.icon(data.weatherCode));
        } catch (Exception e) {
            views.setTextViewText(R.id.conditionText, "Tap to refresh weather");
            views.setTextViewText(R.id.detailsText, "Check network or location settings");
        }

        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetRoot, pi);
        manager.updateAppWidget(widgetId, views);
    }
}
