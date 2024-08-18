package repartidor.faster.com.ec.motorizado;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import repartidor.faster.com.ec.Adapter.DeliveryAdapter;
import repartidor.faster.com.ec.Getset.DeliveryGetSet;
import repartidor.faster.com.ec.R;
import repartidor.faster.com.ec.helperTracking.FirebaseHelper;
import repartidor.faster.com.ec.helperTracking.UiHelper;
import repartidor.faster.com.ec.model.Driver;
import repartidor.faster.com.ec.utils.CheckForPermissions;
import repartidor.faster.com.ec.utils.Config;
import repartidor.faster.com.ec.utils.ForeGroundService;
import repartidor.faster.com.ec.utils.check_sesion;


public class DeliveryStatus extends NotificationToAlertActivity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2161;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    private FirebaseHelper firebaseHelper;
    private final AtomicBoolean driverOnlineFlag = new AtomicBoolean(false);

    private FusedLocationProviderClient locationProviderClient;
    private UiHelper uiHelper;
    private LocationRequest locationRequest;

    private boolean locationFlag = true;
    private String orderNo = "-1";
    private ArrayList<DeliveryGetSet> data;
    private ArrayList<DeliveryGetSet> tempData;
    private ListView listView_delivery;
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private static final String MY_PREFS_ACTIVITY = "DeliveryActivity";
    private String status, DeliveryBoyId, DeliveryNotes,
            statusOrder, regId, level_id, count_order, current_delivery, type_rider;
    private String msg = "";
    private SwitchCompat sw_radius_onoff;
    private static DeliveryStatus instance;
    private Boolean internet = false;
    private TextView txt_perfil;
    private TextView txt_presenceOn;
    private TextView btn_order_history;
    private TextView btn_my_payment;
    private TextView txt_bateria;
    private LinearLayout ll_cash;
    private MediaPlayer player;
    private Uri notification;
    private boolean sound;
    private Vibrator vibrator;
    private DrawerLayout mDrawerLayout;
    private DeliveryAdapter adapter;
    private static final String TAG = DeliveryStatus.class.getSimpleName();
    public static boolean isServiceRunning = false;
    private final int dot = 200;
    private final int dash = 500;
    private final int gap = 200;
    private int battery_level;
    private boolean endVibration = false;
    private final long[] pattern = new long[]{
            0,
            dot, gap, dash, gap, dot, gap, dot
    };
    private final Handler handler = new Handler();

    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST = 1337;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 3;
    private static DatabaseReference mDatabase;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_status);
        Objects.requireNonNull(getSupportActionBar()).hide();
        instance = this;
        SharedPreferences prefsDeliveryBoyId = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        DeliveryBoyId = prefsDeliveryBoyId.getString("DeliveryUserId", null);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        initialization();
        drawer();
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
        editor.putBoolean("Main", true);
        editor.putString("Activity", "DeliveryStatus");
        editor.apply();
        data.clear();
        driverOnlineFlag.set(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
        }

        initializeView();
        initViews();
        batteryLevel();
        settingData();
        //clearData();
    }

    //Elimina los datos de Firebase Realtime Database
    /*public static void clearData(){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("online_drivers").setValue(null);
    }*/

    private void batteryLevel(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        battery_level = (int) ((level / (float) scale) * 100);

        SharedPreferences.Editor edit = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        edit.putString("BatteryLevel", String.valueOf(battery_level));
        edit.apply();
        showBattery();
    }

    @SuppressLint("SetTextI18n")
    private void showBattery(){
        if (battery_level > 95 && battery_level <= 100){
            txt_bateria.setText("Batería: " + battery_level + "%");
            txt_bateria.setTextColor(getResources().getColor(R.color.battery_full));
        } else if (battery_level > 50 && battery_level <= 95){
            txt_bateria.setText("Batería: " + battery_level + "%");
            txt_bateria.setTextColor(getResources().getColor(R.color.header));
        } else if (battery_level > 10 && battery_level <= 50){
            txt_bateria.setText("Batería: " + battery_level + "%");
            txt_bateria.setTextColor(getResources().getColor(R.color.txt_orange));
        } else if (battery_level >= 1 && battery_level <= 10){
            txt_bateria.setText("Batería: " + battery_level + "%");
            txt_bateria.setTextColor(getResources().getColor(R.color.red));
        }
    }

    private void initializeView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
        getActivateGps();
    }

    private void getActivateGps () {
        uiHelper = new UiHelper(getBaseContext());
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getBaseContext());
        locationRequest = uiHelper.getLocationRequest();
        if (!uiHelper.isPlayServicesAvailable()) {
            Toast.makeText(getBaseContext(), "Play Services did not installed!", Toast.LENGTH_SHORT).show();
            finish();
        } else requestLocationUpdates();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if (data != null && data.size() > 0) {
            data.clear();
            listView_delivery.setAdapter(null);
        }
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
        editor.putBoolean("Main", true);
        editor.putString("Activity", "DeliveryStatus");
        editor.apply();
        driverOnlineFlag.set(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (data != null && data.size() > 0) {
            data.clear();
            listView_delivery.setAdapter(null);
        }
        batteryLevel();
        settingData();
        getActivateGps();
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
        editor.putBoolean("Main", true);
        editor.putString("Activity", "DeliveryStatus");
        editor.apply();
        driverOnlineFlag.set(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopSoundVibrate();
        driverOnlineFlag.set(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            player.release();
            player = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
        editor.putBoolean("Main", false);
        editor.clear();
        editor.apply();
        driverOnlineFlag.set(false);
    }

    @Override
    protected void onPause() {
        try{
            // cleanUp();
            if (player != null && player.isPlaying()) {
                player.release();
                player = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
        //stopSoundVibrate();
    }

    private void clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) Objects.requireNonNull(getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
            } else {
                String packageName = getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear "+packageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(Objects.requireNonNull(cache.getParent()));
        if (appDir.exists()) {
            String[] children = appDir.list();
            assert children != null;
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    //Log.i("ERROR", "* File /data/data/APP_PACKAGE/" + s + " DELETED *");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            int i = 0;
            while (true) {
                assert children != null;
                if (!(i < children.length)) break;
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
                i++;
            }
        }

        assert dir != null;
        return dir.delete();
    }

    public static DeliveryStatus getInstance() {
        return instance;
    }

    private void drawer() {
        LinearLayout ll_historial = findViewById(R.id.ll_historial);
        LinearLayout ll_mispagos = findViewById(R.id.ll_mispagos);
        LinearLayout ll_perfil = findViewById(R.id.ll_perfil);
        LinearLayout ll_work_time = findViewById(R.id.ll_work_time);
        LinearLayout ll_terms = findViewById(R.id.ll_terms);
        LinearLayout ll_share = findViewById(R.id.ll_share);
        LinearLayout ll_aboutus = findViewById(R.id.ll_aboutus);
        LinearLayout ll_pay = findViewById(R.id.ll_pay);
        LinearLayout ll_report = findViewById(R.id.ll_report);
        LinearLayout ll_logout = findViewById(R.id.ll_logout);

        ll_mispagos.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
            editor.putBoolean("Main", false);
            editor.putString("Activity", "DeliveryPayment");
            editor.apply();
            Intent i = new Intent(DeliveryStatus.this, DeliveryPaymentHistory.class);
            startActivity(i);
        });

        ll_historial.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
            editor.putBoolean("Main", false);
            editor.putString("Activity", "DeliveryOrderHistory");
            editor.apply();
            Intent i = new Intent(DeliveryStatus.this, DeliveryOrderHistory.class);
            startActivity(i);
        });

        ll_perfil.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
            editor.putBoolean("Main", false);
            editor.putString("Activity", "DeliveryUserProfile");
            editor.apply();
            Intent i = new Intent(DeliveryStatus.this, DeliveryUserProfile.class);
            startActivity(i);

        });

        //markmark
        ll_work_time.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
            editor.putBoolean("Main", false);
            editor.putString("Activity", "DeliveryUserProfile");
            editor.apply();
            Intent i = new Intent(DeliveryStatus.this, DeliveryWorkTime.class);
            startActivity(i);

        });

        ll_share.setOnClickListener(v -> {
            String url1 = "Faster, una app que usa tecnología de punta para ordenar a domicilio, además de la posibilidad de rastrear en tiempo real, me ha gustado mucho y te la recomiendo ¿Qué esperas para usarla? \n\n _*FASTER - DELIVERY APP*_, DESCÁRGALA GRATIS AQUÍ: \n https://play.google.com/store/apps/details?id=" + DeliveryStatus.this.getPackageName();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "FASTER - DELIVERY APP");
            intent.putExtra(Intent.EXTRA_TEXT, url1);

            startActivity(Intent.createChooser(intent, "Compartir FASTER - DELIVERY APP con: "));
        });

        ll_aboutus.setOnClickListener(v -> {
            Intent iv = new Intent(DeliveryStatus.this, Aboutus.class);
            startActivity(iv);
        });

        ll_terms.setOnClickListener(v -> {
            Intent iv = new Intent(DeliveryStatus.this, Termcondition.class);
            startActivity(iv);
        });

        //gestionar pagos
        ll_pay.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
            editor.putBoolean("Main", false);
            editor.putString("Activity", "DeliveryPayment");
            editor.apply();
            Intent i = new Intent(DeliveryStatus.this, DeliveryPayment.class);
            startActivity(i);
        });

        // reportes
        ll_report.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
            editor.putBoolean("Main", false);
            editor.putString("Activity", "WebViewReport");
            editor.apply();
            Intent i = new Intent(DeliveryStatus.this, WebViewReport.class);
            startActivity(i);
        });

        ll_logout.setOnClickListener(v -> {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryStatus.this, R.style.MyDialogTheme);
            builder1.setTitle(getString(R.string.logout));
            builder1.setIcon(R.mipmap.sign_off);
            builder1.setMessage(getString(R.string.msg_logout));
            builder1.setCancelable(false);
            builder1.setPositiveButton(Html.fromHtml("<font color=#2abb9b><strong>" + getString(R.string.btn_logout) + "</strong></font>"), (dialog, id) -> {
                try {
                    status = "no";
                    sendPresence(status);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //TODO Auto-generated catch block e.printStackTrace();
                }
                clearApplicationData();
                clearAppData();
                SharedPreferences settings = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
                settings.edit().clear().apply();
                System.exit(0);
            });
            builder1.setNegativeButton(Html.fromHtml("<font color=#ff9e00><strong>" + getString(R.string.cancel) + "</strong></font>"), (dialog, id) -> {
                //status = "yes";
                //sendPresence(status);
                dialog.cancel();
            });
            AlertDialog alert11 = builder1.create();
            try {
                alert11.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //refresh button
    public void cardViewClicked(View view) {
        if (view.getId() == R.id.button_refresh) {
            settingData();
            data.clear();
            listView_delivery.setAdapter(null);
        }
    }

    private void clearListViewDelivery(){
        if (data != null && data.size() > 0) {
            data.clear();
            listView_delivery.setAdapter(null);
        }
    }
    private void initialization() {

        TextView txt_nameuser = findViewById(R.id.txt_nameuser);
        txt_perfil = findViewById(R.id.txt_perfil);
        txt_presenceOn = findViewById(R.id.txt_presenceOn);
        btn_order_history = findViewById(R.id.btn_order_history);
        btn_my_payment = findViewById(R.id.btn_my_pay);
        repartidor.faster.com.ec.utils.RoundedImageView img_profile = findViewById(R.id.img_profile);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        sw_radius_onoff = findViewById(R.id.Sw_radius_onoff);
        listView_delivery = findViewById(R.id.list_order_info);
        txt_bateria = findViewById(R.id.txt_bateria);
        ll_cash = findViewById(R.id.ll_cash);

        //getting shared pref and setting data
        Picasso.get()
                .load(getResources().getString(R.string.link) + "uploads/deliveryboys/" + getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
                        .getString("DeliveryUserImage", ""))
                .resize(200, 200)
                .centerCrop().into(img_profile);

        try {
            //Niveles de Riders
            switch (getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserDescLevel", "")) {
                case "Oro":
                    txt_perfil.setText(getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserDescLevel", ""));
                    txt_perfil.setTextColor(getResources().getColor(R.color.oro));
                    break;
                case "Platino":
                    txt_perfil.setText(getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserDescLevel", ""));
                    txt_perfil.setTextColor(getResources().getColor(R.color.platino));
                    break;
                case "Diamante":
                    txt_perfil.setText(getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserDescLevel", ""));
                    txt_perfil.setTextColor(getResources().getColor(R.color.diamante));
                    break;
            }
        } catch(Exception e) {
            //
        }

        txt_nameuser.setText(getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserName", ""));
        sw_radius_onoff.setChecked(getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getBoolean("isPresent", false));

        if (sw_radius_onoff.isChecked()){
            isServiceRunning = true;
            initService(true);
        }

        data = new ArrayList<>();
    }

    private void initService(boolean b) {
        //Log.d(TAG, "initService: "+b);

        Intent intent = new Intent(getApplicationContext(), ForeGroundService.class);

        if (b){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } else {
            stopService(intent);
        }
        isServiceRunning = b;
    }

    private void initViews() {
        sw_radius_onoff.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (count_order != null){
                if (count_order.equals("1")){
                    msg = count_order + " orden en curso, no puedes desactivarte";
                } else {
                    msg = count_order + " órdenes en curso, no puedes desactivarte";
                }
            }

            //if (isChecked) {
                CheckForPermissions.CheckForLocationPermission(DeliveryStatus.this, DeliveryStatus.this, 123, new CheckForPermissions.Results() {
                    @Override
                    public void HavePermission() {
                        if (isChecked) {
                            status = "yes";
                            sw_radius_onoff.setChecked(true);
                            initService(true);
                            getActivateGps();
                            batteryLevel();
                            listView_delivery.setAdapter(null);
                        } else {
                            if (current_delivery != null){
                                if (current_delivery.equals("0")) {
                                    status = "no";
                                    sw_radius_onoff.setChecked(false);
                                    initService(false);
                                    batteryLevel();
                                    getActivateGps();
                                    clearListViewDelivery();
                                    listView_delivery.setAdapter(null);
                                    data.clear();
                                } else {
                                    status = "yes";
                                    sw_radius_onoff.setChecked(true);
                                    initService(true);
                                    getActivateGps();
                                    batteryLevel();
                                    clearListViewDelivery();
                                    data.clear();
                                    listView_delivery.setAdapter(null);
                                    if(current_delivery != null && !current_delivery.equals("0")){
                                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                    @Override
                    public void Requested() {
                        if (isChecked) {
                            status = "yes";
                            sw_radius_onoff.setChecked(true);
                            initService(true);
                            getActivateGps();
                            batteryLevel();
                            listView_delivery.setAdapter(null);
                        } else {
                            if (current_delivery != null){
                                if (current_delivery.equals("0")) {
                                    status = "no";
                                    sw_radius_onoff.setChecked(false);
                                    initService(false);
                                    batteryLevel();
                                    getActivateGps();
                                    clearListViewDelivery();
                                    listView_delivery.setAdapter(null);
                                    data.clear();
                                } else {
                                    status = "yes";
                                    sw_radius_onoff.setChecked(true);
                                    initService(true);
                                    getActivateGps();
                                    batteryLevel();
                                    clearListViewDelivery();
                                    data.clear();
                                    listView_delivery.setAdapter(null);
                                    if(current_delivery != null && !current_delivery.equals("0")){
                                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                });

            sendPresence(status);
        });

        btn_my_payment.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
            editor.putBoolean("Main", false);
            editor.putString("Activity", "DeliveryUserProfile");
            editor.apply();

            Intent i = new Intent(DeliveryStatus.this, DeliveryUserProfile.class);
            startActivity(i);

        });

        btn_my_payment.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
            editor.putBoolean("Main", false);
            editor.putString("Activity", "DeliveryPayment");
            editor.apply();

            Intent i = new Intent(DeliveryStatus.this, DeliveryPaymentHistory.class);
            startActivity(i);

        });

        btn_order_history.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
            editor.putBoolean("Main", false);
            editor.putString("Activity", "DeliveryOrderHistory");
            editor.apply();
            Intent i = new Intent(DeliveryStatus.this, DeliveryOrderHistory.class);
            startActivity(i);
        });

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(v -> {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        listView_delivery.setOnItemClickListener((parent, view, position, id) -> {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
            editor.putBoolean("Main", false);
            editor.putString("Activity", "DeliveryOrderDetail");
            editor.apply();

            Intent i = new Intent(DeliveryStatus.this, DeliveryOrderDetail.class);

            i.putExtra("DeliveryBoyId", DeliveryBoyId);
            i.putExtra("OrderNo", tempData.get(position).getOrderNo());
            i.putExtra("OrderAmount", tempData.get(position).getOrderAmount());
            i.putExtra("status", tempData.get(position).getStatus());
            i.putExtra("DeliveryNotes", tempData.get(position).getDeliveryNotes());
            i.putExtra("OrderTime", tempData.get(position).getOrderTime());
            i.putExtra("DeliveryAmount", tempData.get(position).getOrderDelivery());
            startActivity(i);
        });

    }

    private void sendPresence(final String status) {

        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_presence.php?";

        @SuppressLint("SetTextI18n")
        StringRequest stringRequest = new StringRequest(Request.Method.POST, hp,
                response -> {
                    //hiding the progressbar after completion
                    try {
                        JSONObject jo_main = new JSONObject(response);
                        final String txt_message = jo_main.getString("message");
                        String txt_success = jo_main.getString("success");

                        if (txt_success.equals("1")) {
                            settingData();
                            internet = true;
                            String isPresent = jo_main.getString("attendance");
                            getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit().putBoolean("isPresent", returnBool(isPresent)).apply();

                            //validar si esta activo o inactivo rider
                            if (!isPresent.isEmpty()){
                                if (isPresent.equals("no")){
                                    txt_presenceOn.setText("Estás desconectado");
                                    txt_presenceOn.setTextColor(getResources().getColor(R.color.red));
                                } else if (isPresent.equals("yes")){
                                    txt_presenceOn.setText("Estás conectado");
                                    txt_presenceOn.setTextColor(getResources().getColor(R.color.green2));
                                }
                            } else {
                                Toast.makeText(getBaseContext(), "Existe un problema. Revisa tu conexión a Internet o Cierra Sesión.", Toast.LENGTH_SHORT).show();
                            }
                        } else if (txt_success.equals("-4") || (txt_success.equals("-5")) || (txt_success.equals("-8")) || (txt_success.equals("-11"))) {
                            runOnUiThread(() -> {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryStatus.this, R.style.MyDialogTheme);
                                builder1.setTitle("Información");
                                builder1.setIcon(R.mipmap.information);
                                builder1.setCancelable(false);
                                builder1.setMessage(txt_message);
                                builder1.setPositiveButton("OK", (dialog, id) -> {

                                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                    editor.putBoolean("isDeliverAccountActive", false);
                                    editor.putString("DeliveryUserId", "");
                                    editor.putString("DeliveryUserName", "");
                                    editor.putString("DeliveryUserLevelId", "");
                                    editor.putString("DeliveryUserPhone", "");
                                    editor.putString("DeliveryUserEmail", "");
                                    editor.putString("DeliveryUserVNo", "");
                                    editor.putString("DeliveryUserVType", "");
                                    editor.putString("DeliveryUserImage", "");
                                    editor.putString("DeliveryUserDescLevel", "");
                                    editor.apply();

                                    SharedPreferences.Editor editor2 = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0).edit();
                                    editor2.putString("regId", null);
                                    editor2.apply();

                                    Intent iv = new Intent(DeliveryStatus.this, Splash.class);
                                    iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(iv);

                                });
                                AlertDialog alert11 = builder1.create();
                                try {
                                    alert11.show();
                                } catch (Exception e) {
                                    //
                                }
                            });

                        } else {;
                            check_sesion se = new check_sesion();
                            se.validate_sesion(DeliveryStatus.this, txt_success, txt_message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        stopSoundVibrate();
                    }
                },
                error -> {
                    // error
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null) {
                        Log.e("Status code", String.valueOf(networkResponse.statusCode));
                        Toast.makeText(getApplicationContext(), "Revisa tu conexión a Internet", Toast.LENGTH_SHORT).show();
                    }
                }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("attendance", status); //Add the data you'd like to send to the server.
                MyData.put("session", "yes"); //Add the data you'd like to send to the server.
                MyData.put("deliverboy_id", getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserId", ""));
                MyData.put("code", getString(R.string.version_app));
                MyData.put("operative_system", getString(R.string.sistema_operativo));
                return MyData;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + regId);
                return headers;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000*60, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(DeliveryStatus.this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);

    }

    @SuppressLint("MissingPermission")
    public void stopSoundVibrate() {
        try {
            if (player != null) { // if player is not null
                if (player.isPlaying()) {
                    player.stop();
                    player.seekTo(0);
                    player.pause();
                    player.setLooping(false);
                }
                player.reset();//It requires again setDataSource for player object.
                player.release(); // aqui we stop the player
                player = null; // fixed typo.
                //data.clear();
            }

            //This code will check if vibration is still working and it stops it
            if(vibrator != null){
                vibrator.cancel();
            }

            endVibration = true;

            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
            editor.putBoolean("sound", false);
            editor.apply();

        } catch (Exception e) {
            Log.e("mainactivity", e.toString());
        }
    }

    public void settingData() {
        batteryLevel();
        runOnUiThread(() -> {
            stopSoundVibrate();
            clearListViewDelivery();
        });

        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_order.php?";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, hp,
                new Response.Listener<String>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(String response) {
                        //hiding the progressbar after completion
                        try {
                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);
                            String txt_success = obj.getString("success");
                            String txt_message = obj.getString("message");

                            if (txt_success.equals("-2") || txt_success.equals("-3") || txt_success.equals("-4") || txt_success.equals("-7")){
                                //-2 descargar nueva versión || -3 mensaje de bloqueo | -4 iniciar sesión desde otro dispositivo | -7 pago pendiente
                                check_sesion se = new check_sesion();
                                se.validate_sesion(DeliveryStatus.this, txt_success, txt_message);
                            }

                            type_rider = obj.getString("typerRider");
                            // gestionar caja
                            if (type_rider.equals("35")){
                                //Log.e("type_rider ", type_rider);
                                ll_cash.setVisibility(View.VISIBLE);
                                ll_cash.setOnClickListener(v -> {
                                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
                                    editor.putBoolean("Main", false);
                                    editor.putString("Activity", "ManageCashHistory");
                                    editor.apply();
                                    Intent i = new Intent(DeliveryStatus.this, ManageCashHistory.class);
                                    startActivity(i);
                                });
                            } else {
                                ll_cash.setVisibility(View.GONE);
                            }

                            String isPresent2 = obj.getString("DeliveryBoyAttendance");
                            if (isPresent2.equals("no") || isPresent2.equals("null")){
                                sw_radius_onoff.setChecked(false);
                            } else if (isPresent2.equals("yes")){
                                sw_radius_onoff.setChecked(true);
                            }

                            //validar si esta activo o inactivo rider
                            if (!isPresent2.isEmpty()){
                                if (isPresent2.equals("no") || isPresent2.equals("null")){
                                    txt_presenceOn.setText("Estás desconectado");
                                    txt_presenceOn.setTextColor(getResources().getColor(R.color.red));
                                } else if (isPresent2.equals("yes")){
                                    txt_presenceOn.setText("Estás conectado");
                                    txt_presenceOn.setTextColor(getResources().getColor(R.color.green2));
                                }
                            } else {
                                Toast.makeText(getBaseContext(), "Existe un problema. Revisa tu conexión a Internet o Cierra Sesión.", Toast.LENGTH_SHORT).show();
                            }

                            current_delivery = obj.getString("assigned");
                            //Log.e("current_delivery", current_delivery);

                            //Niveles de Riders
                            level_id = obj.getString("DeliveryBoyLevel");
                            switch (level_id) {
                                case "Oro":
                                    txt_perfil.setText("Estás en "+level_id);
                                    txt_perfil.setTextColor(getResources().getColor(R.color.oro));
                                    break;
                                case "Platino":
                                    txt_perfil.setText("Estás en "+level_id);
                                    txt_perfil.setTextColor(getResources().getColor(R.color.platino));
                                    break;
                                case "Diamante":
                                    txt_perfil.setText("Estás en "+level_id);
                                    txt_perfil.setTextColor(getResources().getColor(R.color.diamante));
                                    break;
                            }


                            if (txt_success.equals("1")) {

                                String txt_assigned = obj.getString("assigned");

                                // Log.e("txt_assigned", txt_assigned);

                                //Si tiene un pedido asignado no suene
                                //caso contrario si no está sonando que inicie el sonido
                                if (txt_assigned.equals("0")) { //aqui cuando el estado es igual a 0
                                    try {
                                        stopSoundVibrate();
                                        notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                                                + "://" + getApplicationContext().getPackageName()
                                                + "/" + R.raw.faster_sound);

                                        endVibration = false;
                                        SharedPreferences prefsDeliveryBoyId = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                                        sound = prefsDeliveryBoyId.getBoolean("sound", false);

                                        if (!sound) {
                                            if (player == null) {
                                                player = MediaPlayer.create(getBaseContext(), notification);
                                                player.setLooping(false);
                                            }

                                            final Handler stopwatchhanderler = new Handler();
                                            final long EXECUTION_TIME = 40000; //llama método a ejecutarse cada 30 segundos
                                            stopwatchhanderler.postDelayed(new Runnable() {
                                                public void run() {
                                                    stopSoundVibrate();
                                                    stopwatchhanderler.postDelayed(this, EXECUTION_TIME); //now is every 1 minutes
                                                }
                                            }, EXECUTION_TIME);

                                            handler.postDelayed(new Runnable() {
                                                @SuppressLint("MissingPermission")
                                                @Override
                                                public void run() {
                                                    vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                                    vibrator.vibrate(1000);
                                                    if (!endVibration) {
                                                        handler.postDelayed(this, 1500);
                                                        endVibration = false;
                                                    }
                                                }
                                            }, 3);

                                            /*SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
                                            editor.putBoolean("sound", true);
                                            editor.apply();*/

                                            if (player != null) {
                                                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
                                                editor.putBoolean("sound", true);
                                                editor.apply();
                                                player.start();
                                            }

                                        } else {
                                            stopSoundVibrate();
                                            data.clear();
                                        }

                                    } catch (Exception e) {
                                        Log.e("mainactivity", "entró aqui al error al iniciar player");
                                        e.printStackTrace();
                                        stopSoundVibrate();
                                    }
                                } else {
                                    stopSoundVibrate();
                                    data.clear();
                                    listView_delivery.setAdapter(null);
                                    //parar si esta sonando
                                }

                                data.clear();
                                listView_delivery.setAdapter(null);

                                JSONArray ja_order = obj.getJSONArray("order");
                                DeliveryGetSet getSet;
                                boolean llevando = false;
                                for (int i = 0; i < ja_order.length(); i++) {
                                    JSONObject jo_orderDetail = ja_order.getJSONObject(i);
                                    getSet = new DeliveryGetSet();
                                    getSet.setResName(jo_orderDetail.getString("restaurant_name"));
                                    getSet.setOrderDate(jo_orderDetail.getString("created_at"));
                                    getSet.setOrderAddress(jo_orderDetail.getString("order_address"));
                                    getSet.setOrderNo(jo_orderDetail.getString("order_no"));
                                    getSet.setOrderDelivery(jo_orderDetail.getString("delivery_price"));
                                    getSet.setOrderTime(jo_orderDetail.getString("time_rest"));
                                    getSet.setOrderAmount(jo_orderDetail.getString("total_amount"));
                                    getSet.setStatus(jo_orderDetail.getString("status"));
                                    getSet.setDeliveryNotes(jo_orderDetail.getString("DeliveryNotes"));
                                    getSet.setDeliveryBoyLevelId(jo_orderDetail.getString("DeliveryBoyLevelId"));
                                    getSet.setFreeDelivery(jo_orderDetail.getString("free_delivery"));
                                    getSet.setRiderResponse(jo_orderDetail.getString("rider_response"));
                                    getSet.setRiderIsAssigned(jo_orderDetail.getString("rider_is_assigned"));
                                    getSet.setRiderId(DeliveryBoyId);
                                    count_order = jo_orderDetail.getString("count_order");

                                     Log.e("rider_response", DeliveryBoyId);

                                    DeliveryNotes = jo_orderDetail.getString("DeliveryNotes");
                                    statusOrder = jo_orderDetail.getString("status");
                                    if (jo_orderDetail.getString("status").equals("1") || jo_orderDetail.getString("status").equals("3")) {
                                        orderNo = jo_orderDetail.getString("order_no");
                                        llevando = true;
                                    }
                                    data.add(getSet);
                                }


                                if (!statusOrder.equals("null")){
                                    tempData = new ArrayList<>();
                                    for (int i = 0; i < data.size(); i++){
                                        DeliveryGetSet temp = new DeliveryGetSet();
                                        temp.setResName(data.get(i).getResName());
                                        temp.setOrderDate(data.get(i).getOrderDate());
                                        temp.setOrderAddress(data.get(i).getOrderAddress());
                                        temp.setOrderNo(data.get(i).getOrderNo());
                                        temp.setOrderDelivery(data.get(i).getOrderDelivery());
                                        temp.setOrderTime(data.get(i).getOrderTime());
                                        temp.setOrderAmount(data.get(i).getOrderAmount());
                                        temp.setStatus(data.get(i).getStatus());
                                        temp.setDeliveryNotes(data.get(i).getDeliveryNotes());
                                        temp.setDeliveryBoyLevelId(data.get(i).getDeliveryBoyLevelId());
                                        temp.setRiderResponse(data.get(i).getRiderResponse());
                                        temp.setFreeDelivery(data.get(i).getFreeDelivery());
                                        temp.setRiderId(data.get(i).getRiderId());
                                        temp.setRiderIsAssigned(data.get(i).getRiderIsAssigned());
                                        tempData.add(temp);
                                    }
                                    adapter = new DeliveryAdapter(tempData, DeliveryStatus.this);
                                    listView_delivery.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                }

                                if (llevando) {
                                    uiHelper = new UiHelper(getBaseContext());
                                    locationProviderClient = LocationServices.getFusedLocationProviderClient(getBaseContext());
                                    locationRequest = uiHelper.getLocationRequest();
                                    if (!uiHelper.isPlayServicesAvailable()) {
                                        Toast.makeText(getBaseContext(), "Play Services did not installed!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else requestLocationUpdates();
                                    firebaseHelper = new FirebaseHelper(orderNo);
                                    driverOnlineFlag.set(true);
                                } else {
                                    driverOnlineFlag.set(false);
                                }
                            } else if (txt_success.equals("-4") || (txt_success.equals("-5")) || (txt_success.equals("-8")) || (txt_success.equals("-11"))) {
                                driverOnlineFlag.set(false);
                                if (!data.isEmpty()) {
                                    data.clear();
                                }
                                stopSoundVibrate();
                                runOnUiThread(() -> {
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryStatus.this, R.style.MyDialogTheme);
                                    builder1.setTitle("Información");
                                    builder1.setIcon(R.mipmap.information);
                                    builder1.setCancelable(false);
                                    builder1.setMessage(txt_message);
                                    builder1.setPositiveButton("OK", (dialog, id) -> {

                                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                        editor.putBoolean("isDeliverAccountActive", false);
                                        editor.putString("DeliveryUserId", "");
                                        editor.putString("DeliveryUserName", "");
                                        editor.putString("DeliveryUserLevelId", "");
                                        editor.putString("DeliveryUserPhone", "");
                                        editor.putString("DeliveryUserEmail", "");
                                        editor.putString("DeliveryUserVNo", "");
                                        editor.putString("DeliveryUserVType", "");
                                        editor.putString("DeliveryUserImage", "");
                                        editor.putString("DeliveryUserDescLevel", "");
                                        editor.apply();

                                        SharedPreferences.Editor editor2 = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0).edit();
                                        editor2.putString("regId", null);
                                        editor2.apply();

                                        Intent iv = new Intent(DeliveryStatus.this, Splash.class);
                                        iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(iv);

                                    });
                                    AlertDialog alert11 = builder1.create();
                                    try {
                                        alert11.show();
                                    } catch (Exception e) {
                                        //
                                    }
                                });

                            } else {
                                driverOnlineFlag.set(false);
                                if (!data.isEmpty()) {
                                    data.clear();
                                }

                                check_sesion se = new check_sesion();
                                se.validate_sesion(DeliveryStatus.this, txt_success, txt_message);
                            }

                        } catch (JSONException e) {
                            driverOnlineFlag.set(false);
                            stopSoundVibrate();
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    // Si el sonido está activo pare de sonar
                    // error
                    stopSoundVibrate();

                    driverOnlineFlag.set(false);
                    // Log.d("Error.Response", error.toString());
                    // String message = null;
                    if (error instanceof TimeoutError || error instanceof NetworkError) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryStatus.this, R.style.MyDialogTheme);
                        builder1.setTitle("Información");
                        builder1.setIcon(R.mipmap.information);
                        builder1.setCancelable(false);
                        builder1.setMessage("Por favor verifica tu conexión a Internet");
                        builder1.setPositiveButton("Reintentar", (dialog, id) -> settingData());
                        builder1.setNegativeButton("Salir", (dialog, id) -> finishAffinity());
                        AlertDialog alert11 = builder1.create();
                        try {
                            alert11.show();
                        } catch (Exception e) {
                            //
                        }
                    } else {
                        stopSoundVibrate();
                        settingData();
                        Toast.makeText(getApplicationContext(), "Espere, estamos buscando nuevas órdenes.", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("deliverboy_id", getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserId", ""));
                params.put("code", getString(R.string.version_app));
                params.put("operative_system", getString(R.string.sistema_operativo));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + regId);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return super.getBodyContentType();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000*60, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(DeliveryStatus.this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);


    }

    @SuppressLint("WrongConstant")
    @Override
    public void onBackPressed() {
        driverOnlineFlag.set(false);
        stopSoundVibrate();
        showExitDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViews();
        data.clear();
        listView_delivery.setAdapter(null);
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
        editor.putBoolean("Main", true);
        editor.putString("Activity", "DeliveryStatus");
        editor.apply();

    }


    private void showExitDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryStatus.this, R.style.MyDialogTheme);
        builder1.setTitle(getString(R.string.Quit));
        builder1.setIcon(R.mipmap.information);
        builder1.setMessage(getString(R.string.statementquit));
        builder1.setCancelable(false);
        builder1.setPositiveButton(Html.fromHtml("<font color=#2abb9b><strong>" + getString(R.string.yes) + "</strong></font>"), (dialog, id) -> {
            stopSoundVibrate();
            finishAffinity();
        });
        builder1.setNegativeButton(getString(R.string.no), (dialog, id) -> dialog.cancel());
        AlertDialog alert11 = builder1.create();
        try {
            alert11.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean returnBool(String status) {
        return Objects.equals(status, "yes");
    }

    public static void changeStatsBarColor(Activity activity) {
        Window window = activity.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.my_statusbar_color));
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location location = locationResult.getLastLocation();
            if (locationFlag) {
                locationFlag = false;
            }
            if (driverOnlineFlag.get()) {
                if (DeliveryNotes.equals("App")) {
                    if (statusOrder.equals("1") || statusOrder.equals("3")) {
                        firebaseHelper.updateDriver(new Driver(location.getLatitude(), location.getLongitude(), orderNo));
                    } else {
                        firebaseHelper.deleteDriver();
                        driverOnlineFlag.set(false);
                    }
                }

            }

        }
    };

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        if (!uiHelper.isHaveLocationPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        if (uiHelper.isLocationProviderEnabled())
            uiHelper.showPositiveDialogWithListener
                    (this, getResources().getString(R.string.need_location), getResources().getString(R.string.location_content),
                            () -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)), "ACTIVAR", false);
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Objects.requireNonNull(Looper.myLooper()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            try {
                if (grantResults.length > 0) {
                    int value = grantResults[0];
                    if (value == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (value == PackageManager.PERMISSION_GRANTED) requestLocationUpdates();
                }
            } catch (Exception e) {
                //
            }
        }
    }

}