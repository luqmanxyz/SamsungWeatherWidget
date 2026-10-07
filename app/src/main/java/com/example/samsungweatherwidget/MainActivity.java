package com.example.samsungweatherwidget;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

public class MainActivity extends Activity {
    private static final int REQ_LOCATION = 1001;
    private static final String PREFS = "widget_prefs";
    private EditText cityEdit, latEdit, lonEdit;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cityEdit = findViewById(R.id.editCity);
        latEdit = findViewById(R.id.editLat);
        lonEdit = findViewById(R.id.editLon);
        statusText = findViewById(R.id.statusText);

        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        cityEdit.setText(p.getString("city", "Abu Dhabi"));
        latEdit.setText(String.format(Locale.US, "%.5f", Double.longBitsToDouble(p.getLong("lat", Double.doubleToLongBits(24.4539)))));
        lonEdit.setText(String.format(Locale.US, "%.5f", Double.longBitsToDouble(p.getLong("lon", Double.doubleToLongBits(54.3773)))));

        findViewById(R.id.buttonLocation).setOnClickListener(v -> requestLocation());
        findViewById(R.id.buttonSave).setOnClickListener(v -> saveAndRefresh());
    }

    private void saveAndRefresh() {
        try {
            double lat = Double.parseDouble(latEdit.getText().toString().trim());
            double lon = Double.parseDouble(lonEdit.getText().toString().trim());
            String city = cityEdit.getText().toString().trim();
            if (city.isEmpty()) city = "My Location";
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString("city", city)
                    .putLong("lat", Double.doubleToRawLongBits(lat))
                    .putLong("lon", Double.doubleToRawLongBits(lon))
                    .apply();
            statusText.setText("已保存，正在刷新桌面 Widget…");
            new Thread(() -> {
                WeatherWidgetProvider.updateAllWidgets(getApplicationContext());
                runOnUiThread(() -> statusText.setText("刷新完成。"));
            }).start();
        } catch (Exception e) {
            Toast.makeText(this, "请填写有效的经纬度", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
            return;
        }
        loadLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadLocation();
        } else {
            statusText.setText("未获得定位权限，可手动输入经纬度。默认位置为 Abu Dhabi。 ");
        }
    }

    @SuppressWarnings("MissingPermission")
    private void loadLocation() {
        statusText.setText("正在获取当前位置…");
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            String provider = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
            Executor executor = getMainExecutor();
            lm.getCurrentLocation(provider, new CancellationSignal(), executor, this::applyLocation);
        } else {
            Location best = null;
            try {
                List<String> providers = lm.getProviders(true);
                for (String provider : providers) {
                    Location l = lm.getLastKnownLocation(provider);
                    if (l != null && (best == null || l.getAccuracy() < best.getAccuracy())) best = l;
                }
            } catch (Exception ignored) {}
            if (best != null) applyLocation(best);
            else statusText.setText("暂时无法获取定位，请稍后再试或手动输入经纬度。 ");
        }
    }

    private void applyLocation(Location location) {
        if (location == null) {
            statusText.setText("暂时无法获取定位，请稍后再试或手动输入经纬度。 ");
            return;
        }
        latEdit.setText(String.format(Locale.US, "%.5f", location.getLatitude()));
        lonEdit.setText(String.format(Locale.US, "%.5f", location.getLongitude()));
        if (cityEdit.getText().toString().trim().isEmpty()) cityEdit.setText("My Location");
        statusText.setText("已读取当前位置。请点“保存并刷新 Widget”。");
    }
}
