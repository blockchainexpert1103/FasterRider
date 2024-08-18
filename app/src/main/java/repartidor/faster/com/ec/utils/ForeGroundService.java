package repartidor.faster.com.ec.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import androidx.core.content.ContextCompat;
import repartidor.faster.com.ec.R;
import repartidor.faster.com.ec.motorizado.DeliveryStatus;

public class ForeGroundService extends Service {
    private static String TAG = ForeGroundService.class.getSimpleName();
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private FusedLocationProviderClient fusedLocationProviderClient;

    public static final String LOCATION_UPDATE = GeoLocationService.class.getSimpleName();
    public static final String LOCATION_DATA = "location_data";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10000; // 10 Seconds
    private static final float LOCATION_DISTANCE = 1f; // meters = 1 m
    private RequestQueue requestQueue;
    private PendingIntent Intent;
    private String BatteryLevel;


    public static void start(Context context) {
        try {
            context.startService(new Intent(context, ForeGroundService.class));
        } catch (Exception e) {
            //
        }
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, ForeGroundService.class));
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            try {
                if (location != null) {
                    mLastLocation.set(location);
                    sendLocation(mLastLocation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    //

    /*private LocationCallback callback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                try {
                    if (location != null) {
                        sendLocation(location);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.repartidor";
        String channelName = "En este momento estás conectado.";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        Intent notificationIntent = new Intent(this, DeliveryStatus.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            Intent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            //PendingIntent mIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(false)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle(channelName)
                .setSmallIcon(getNotificationIcon())
                //.setSound(null)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setContentIntent(Intent)
                .setAutoCancel(true)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else {
            startForeground(1, new Notification());
        }
        //createLocationRequest();

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);

            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (SecurityException | IllegalArgumentException ex) {
            //
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        /*super.onDestroy();
        try {
            if (fusedLocationProviderClient != null) {
                fusedLocationProviderClient.removeLocationUpdates(callback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (checkLocationPermission()) {
                        mLocationManager.removeUpdates(mLocationListeners[i]);
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT < 23)
            return true;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /*protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1);
        locationRequest.setFastestInterval(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ForeGroundService.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper());
    }*/

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_notification_small : R.drawable.ic_notification_icon;
    }

    private void sendLocation(final Location location) {

        String deliverboy_id = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserId", "");
        if (deliverboy_id.equals("")) return;

        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_position.php?";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, hp,
                response -> {
                    //Log.e("~~Success.ForeGround", response);
                    BatteryLevel = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("BatteryLevel", "");
                    //if (BatteryLevel.equals("")) return;
                    //Log.e("BatteryLevel", " ==>" + BatteryLevel);
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //Log.e("~~Error.Response", error.toString());
                        BatteryLevel = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("BatteryLevel", "");
                        //if (BatteryLevel.equals("")) return;
                        //Log.e("BatteryLevel", " ==>" + BatteryLevel);
                    }
                }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("deliverboy_id", deliverboy_id);
                MyData.put("lat", String.valueOf(location.getLatitude()));
                MyData.put("lon", String.valueOf(location.getLongitude()));
                if (BatteryLevel != null) {
                    if (!BatteryLevel.isEmpty()){
                        MyData.put("battery_level", BatteryLevel);
                    }
                }
                //Log.e("BatteryLevel", BatteryLevel);
                return MyData;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
                headers.put("Authorization", "Bearer " + pref.getString("regId", null));
                return headers;
            }
        };

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ForeGroundService.this);
            //adding the string request to request queue
            requestQueue.add(stringRequest);
        } else {
            //adding the string request to request queue
            requestQueue.add(stringRequest);
        }
        requestQueue.start();
    }
}