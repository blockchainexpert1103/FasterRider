package repartidor.faster.com.ec.motorizado;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.provider.Settings;

import repartidor.faster.com.ec.Adapter.OrderDetailAdapter;
import repartidor.faster.com.ec.R;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.*;

import repartidor.faster.com.ec.Getset.orderDetailGetSet;
import repartidor.faster.com.ec.helperTracking.FirebaseHelper;
import repartidor.faster.com.ec.helperTracking.UiHelper;
import repartidor.faster.com.ec.model.Driver;
import repartidor.faster.com.ec.utils.Config;
import repartidor.faster.com.ec.utils.GPSTracker;
import repartidor.faster.com.ec.utils.check_sesion;
import servicios.BubbleHeadService;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeliveryOrderDetail extends NotificationToAlertActivity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2161;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 100;
    private static final int REQUEST_CODE_WRITE_STORAGE_PERMISSION = 101;
    private static String DRIVER_ID;   // Id must be unique for every driver.
    private static final int REQUEST_CODE_READ_AND_WRITE_TO_MEMORY = 123;
    private FirebaseHelper firebaseHelper;
    private AtomicBoolean driverOnlineFlag = new AtomicBoolean(false);
    private FusedLocationProviderClient locationProviderClient;
    private UiHelper uiHelper;
    private LocationRequest locationRequest;
    private Location previousLocation;
    private boolean locationFlag = true;
    //-----------------------------
    private ListView list_order;
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private static final String MY_PREFS_ACTIVITY = "DeliveryActivity";
    private String deliveryBoyId, orderNo, orderStatus, entregado, isPick, level_id, desc_levels, full_charge;
    private String orderTextStatus, orderAssign, notes, orderMessage, orderType,
            orderPrice, orderTotal, orderDeliveryPrice, orderAmountPay,
            orderPayment, orderPaymentId, orderTimeRest, orderChange, orderNote, validationStatus,
            imageOrder, validationType, selfAcceptanceResponse, selfAccepted, countPhoto;
    private String userAddress, userName, userPhone,
            userLat, userLon, userImage, userId, userDni, deliveryAddress, deliveryAlias, deliveryPhone,
            deliveryNote, departmentNumber, factIdentification, factNameClient, factAddress, factEmail, factPhone, factIdentifTipo, deliveryQuantity;
    private String restaurantName, restaurantAddress, restaurantPhoto, restaurantPhone, restaurantLat, restaurantLon;
    private String DataUserName, DataDeliveryAlias, DataDeliveryAddress, DataDepartmentNumber, DataDeliveryPhone, DataDeliveryNote;
    private ProgressDialog progressDialog;
    Button btn_picked;
    private ArrayList<orderDetailGetSet> getsetDeliveryorderdetail;
    private static DeliveryOrderDetail instance;
    private static androidx.appcompat.widget.AppCompatButton btn_bell;
    private boolean active;
    private int count;
    private String timearrive;
    private String regId;
    private TextView txt_orderTime;
    private TextView txt_origin_orders;
    private ProgressBar api_Loader;
    private static final int REQUEST_CODE_CAMERA_PERMISSIONS = 123;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_order_detail);
        Objects.requireNonNull(getSupportActionBar()).hide();
        gettingIntents();

        driverOnlineFlag.set(false);
        instance = this;
        active = false;
        count = 0;
        timearrive = "";
        displayFirebaseRegId();

        btn_picked = findViewById(R.id.btn_picked);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    private void initializeView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
            editor.putBoolean("Main", false);
            editor.putString("Activity", "DeliveryBurble");
            editor.apply();
            Intent resultIntent = new Intent(this, BubbleHeadService.class);
            resultIntent.putExtra("OrderNo", orderNo);
            resultIntent.putExtra("DeliveryBoyId", deliveryBoyId);
            this.startService(resultIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        new get_order_details().execute();
    }

    public static DeliveryOrderDetail getInstance() {
        return instance;
    }

    private void gettingIntents() {
        Intent i = getIntent();
        SharedPreferences prefsDeliveryBoyId = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        deliveryBoyId = prefsDeliveryBoyId.getString("DeliveryUserId", null);
        orderNo = i.getStringExtra("OrderNo");
        //Log.d("ORerNumberExperiment", deliveryBoyId + orderNo);
    }
    private void dialogpicked(String mesage) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryOrderDetail.this, R.style.MyDialogTheme);
        builder1.setTitle("Confirmación");
        builder1.setIcon(R.mipmap.confirmation_2);
        builder1.setCancelable(false);
        builder1.setMessage(mesage);

        builder1.setPositiveButton(Html.fromHtml("<font color=#2abb9b>Si, estoy seguro</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                new postingData().execute();
                Intent i = new Intent(DeliveryOrderDetail.this, DeliveryStatus.class);
                startActivity(i);
            }
        });
        builder1.setNegativeButton(Html.fromHtml("<font color=#ff9e00>No, cancelar</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert11 = builder1.create();
        try {
            alert11.show();
        } catch (Exception e) {
            //
        }
    }

    private void dialogSelfAccepted() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryOrderDetail.this, R.style.MyDialogTheme);
        builder1.setTitle("Confirmar Autoaceptación");
        builder1.setIcon(R.mipmap.confirmation_2);
        builder1.setCancelable(false);
        builder1.setMessage(Html.fromHtml("<span>Rechazar muchas órdenes puede disminuir tu calificación.</span><div><font color=#2abb9b>Orden con autoaceptación. ¿Deseas aceptar?</font></div>"));

        builder1.setPositiveButton(Html.fromHtml("<font color=#2abb9b>Si, aceptar</font>"), (dialog, id) -> {
            new postingData().execute();
            Intent i = new Intent(DeliveryOrderDetail.this, DeliveryStatus.class);
            startActivity(i);
        });
        builder1.setNegativeButton(Html.fromHtml("<font color=#ff3f2e>No, rechazar</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                isPick = "rejected";
                new postingData().execute();
                Intent i = new Intent(DeliveryOrderDetail.this, DeliveryStatus.class);
                startActivity(i);
                // dialog.cancel();
            }
        });

        AlertDialog alert11 = builder1.create();
        try {
            alert11.show();
        } catch (Exception e) {
            //
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location location = locationResult.getLastLocation();
            if (location == null) return;
            if (locationFlag) {
                locationFlag = false;
            }
            if (driverOnlineFlag.get()) {
                if (notes.equals("App")) {
                    if (orderStatus.equals("1") || orderStatus.equals("3")) {
                        firebaseHelper.updateDriver(new Driver(location.getLatitude(), location.getLongitude(), orderNo));
                        //Log.e("gggggggggggg", orderNo);
                    } else {
                        firebaseHelper.deleteDriver();
                        driverOnlineFlag.set(false);
                    }
                }
            }
            //previousLocation = location;
        }
    };

    /*public void deleteDriver() {
        firebaseHelper.deleteDriver();
    }*/

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
        } else if (requestCode == REQUEST_CODE_WRITE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // downloadImage();
            } else {
                Toast.makeText(this, "Permiso denegado para escribir en almacenamiento externo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void cardViewClicked(View view) {
        if (view.getId() == R.id.button_whatsapp) {
            String phoneNumber = userPhone;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("whatsapp://send?phone=" + phoneNumber));

            try {
                // Si WhatsApp está instalado, iniciar la actividad
                startActivity(intent);
            } catch (android.content.ActivityNotFoundException ex) {
                // Si WhatsApp no está instalado, mostrar un mensaje de error o lanzar una excepción
                Toast.makeText(this, "WhatsApp no está instalado en este dispositivo.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {

        //btn_bell = findViewById(R.id.btn_bell2);
        TextView txt_header = findViewById(R.id.txt_header);
        txt_header.setText(getString(R.string.txt_order_no) + orderNo);
        repartidor.faster.com.ec.utils.RoundedImageView img_user = findViewById(R.id.image);

        try {
            TextView txt_orderStatus = findViewById(R.id.txt_orderStatus);
            txt_orderStatus.setText(orderTextStatus);
            TextView txt_orderAmount = findViewById(R.id.txt_orderAmount);
            TextView txt_deliveryPrice = findViewById(R.id.txt_deliveryPrice);
            LinearLayout linear_whatsapp = findViewById(R.id.linear_whatsapp);

            //No visible del boton de WhatsApp
            if (orderStatus.equals("0") || orderStatus.equals("2") || orderStatus.equals("4") || orderStatus.equals("5")) {
                linear_whatsapp.setVisibility(View.GONE);
            } else {
                linear_whatsapp.setVisibility(View.VISIBLE);
            }

            switch (level_id) {
                case "1": // Oro
                    if (orderStatus.equals("0") || orderStatus.equals("5") || orderStatus.equals("1")) {
                        txt_deliveryPrice.setText("Información no disponible");
                        if (orderPrice != null || orderTotal != null) {
                            txt_orderAmount.setText("Pedido: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderPrice)).replace(".", ","));
                        }
                    } else {
                        txt_deliveryPrice.setText("Delivery: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderDeliveryPrice)).replace(".", ",") + " | Vuelto: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderChange)).replace(".", ","));
                        if (orderPrice != null || orderTotal != null) {
                            txt_orderAmount.setText("Pedido: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderPrice)).replace(".", ",") + " | Total a cobrar: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderTotal)).replace(".", ","));
                        }
                    }
                    break;
                case "2": // Platino
                    if (orderStatus.equals("0")) {
                        txt_deliveryPrice.setText("Información no disponible");
                        if (orderPrice != null || orderTotal != null) {
                            txt_orderAmount.setText("Pedido: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderPrice)).replace(".", ","));
                        }
                    } else {
                        txt_deliveryPrice.setText("Delivery: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderDeliveryPrice)).replace(".", ",") + " | Vuelto: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderChange)).replace(".", ","));
                        if (orderPrice != null || orderTotal != null) {
                            txt_orderAmount.setText("Pedido: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderPrice)).replace(".", ",") + " | Total a cobrar: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderTotal)).replace(".", ","));
                        }
                    }
                    break;
                case "3": // Diamante
                    txt_deliveryPrice.setText("Delivery: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderDeliveryPrice)).replace(".", ",") + " | Vuelto: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderChange)).replace(".", ","));
                    if (orderPrice != null || orderTotal != null) {
                        txt_orderAmount.setText("Pedido: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderPrice)).replace(".", ",") + " | Total a cobrar: " + getString(R.string.currency) + String.format("%.2f", Double.parseDouble(orderTotal)).replace(".", ","));
                    }
                    break;
            }
            txt_orderTime = findViewById(R.id.txt_orderTime);

        } catch (Exception e) {
            //
        }

        if (!orderTimeRest.equals("0")) {
            check_sesion.reverseTimer(Integer.parseInt(orderTimeRest), txt_orderTime);
        }

        ImageView image_pay = findViewById(R.id.image_pay);
        TextView txt_name = findViewById(R.id.txt_name);
        TextView txt_address = findViewById(R.id.txt_address);
        TextView txt_modePay = findViewById(R.id.txt_modePay);
        TextView txt_notes = findViewById(R.id.txt_notes);
        TextView button_noteRestaurant = findViewById(R.id.btn_restaurant_order);
        TextView txt_deliveryQuantity = findViewById(R.id.txt_deliveryQuantity);
        RelativeLayout rel_note_restaurant = findViewById(R.id.rel_note_restaurant);
        txt_origin_orders = findViewById(R.id.txt_origin_orders);
        rel_note_restaurant.setVisibility(View.GONE);
        //Log.d("estadores", "" + userId);

        if (userId.equals("-1")) {
            rel_note_restaurant.setVisibility(View.VISIBLE);
            button_noteRestaurant.setText(orderNote);
        } else {
            rel_note_restaurant.setVisibility(View.GONE);
        }

        double dQuantity = Double.parseDouble(deliveryQuantity);
        if (dQuantity >= 0 && dQuantity <= 10) {
            txt_deliveryQuantity.setText("Entregas realizadas con éxito (" + deliveryQuantity + ")");
            txt_deliveryQuantity.setTextColor(getResources().getColor(R.color.red));
        } else if (dQuantity >= 11 && dQuantity <= 20) {
            txt_deliveryQuantity.setText("Entregas realizadas con éxito (" + deliveryQuantity + ")");
            txt_deliveryQuantity.setTextColor(getResources().getColor(R.color.oro));
        } else if (dQuantity >= 21) {
            txt_deliveryQuantity.setText("Entregas realizadas con éxito (" + deliveryQuantity + ")");
            txt_deliveryQuantity.setTextColor(getResources().getColor(R.color.green2));
        }

        Button btn_call = findViewById(R.id.btn_call);
        Button btn_call_client = findViewById(R.id.btn_call_client);
        Button btn_fact = findViewById(R.id.btn_fact);
        ImageView btn_data = findViewById(R.id.btn_data);
        ImageView btn_imagen = findViewById(R.id.btn_imagen);
        ImageView btn_take_photo = findViewById(R.id.btn_take_photo);
        RelativeLayout rela_map = findViewById(R.id.rel_fourth);
        RelativeLayout call_client = findViewById(R.id.rel_fourth2);
        RelativeLayout rela_data = findViewById(R.id.rel_fourth3);

        // Log.e("orderPaymentId", orderPaymentId);
        switch (orderPaymentId) {
            case "1":  //Efectivo
                image_pay.setImageDrawable(getResources().getDrawable(R.drawable.pay_efectivo));
                break;
            case "2":  //Transferencia
                image_pay.setImageDrawable(getResources().getDrawable(R.drawable.pay_transferencia));
                break;
            case "3":  //Datafast Tarjeta Débito/Crédito
                image_pay.setImageDrawable(getResources().getDrawable(R.drawable.pay_pos_terminal));
                break;
            case "4":  //PayPhone Tarjeta Débito/Crédito
                image_pay.setImageDrawable(getResources().getDrawable(R.drawable.payphone));
                break;
            case "5":  //Pagado, no cobrar
                image_pay.setImageDrawable(getResources().getDrawable(R.drawable.pay_nocobrar));
                break;
            default: //Pago no definido
                image_pay.setImageDrawable(getResources().getDrawable(R.drawable.pay_nodefinido));
                break;
        }
        txt_modePay.setText(Html.fromHtml("<font color=#2abb9b>" + orderPayment + "</font> | Factura: SI"));

        if (orderStatus.equals("0")){
            if (desc_levels.equals("Diamante")){
                txt_notes.setText(Html.fromHtml("<font color=#2abb9b><strong>Inst: </strong></font>" + orderNote));
                txt_notes.setTextColor(getResources().getColor(R.color.black));
            } else {
                txt_notes.setVisibility(View.GONE);
            }
            rela_map.setVisibility(View.GONE);
            call_client.setVisibility(View.GONE);
        } else if (orderStatus.equals("1") || orderStatus.equals("2") || orderStatus.equals("3") || orderStatus.equals("4") || orderStatus.equals("5")){
            txt_notes.setText(Html.fromHtml("<font color=#2abb9b><strong>Inst: </strong></font>" + orderNote));
            txt_notes.setTextColor(getResources().getColor(R.color.black));
            txt_notes.setVisibility(View.VISIBLE);
        }

        if (orderStatus.equals("0") || orderStatus.equals("3")) {
            txt_name.setText(userName);
            txt_address.setText(userAddress);
            Picasso.get()
                    .load(getResources().getString(R.string.link) + "uploads/restaurant/" + userImage)
                    .resize(200, 200)
                    .centerCrop()
                    .into(img_user);

            if (orderStatus.equals("3")) {
                uiHelper = new UiHelper(this);
                locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                locationRequest = uiHelper.getLocationRequest();
                if (!uiHelper.isPlayServicesAvailable()) {
                    Toast.makeText(this, "Play Services no está instalado", Toast.LENGTH_SHORT).show();
                    finish();
                } else requestLocationUpdates();
                firebaseHelper = new FirebaseHelper(orderNo);
                driverOnlineFlag.set(true);
                btn_call.setText("Llamar tienda");
                btn_call_client.setText("Llamar cliente");
                //rela_map.setVisibility(View.VISIBLE);

            } else {
                driverOnlineFlag.set(false);
            }

        } else {
            txt_name.setText(restaurantName);
            txt_address.setText(restaurantAddress);
            Picasso.get()
                    .load(getResources().getString(R.string.link) + "uploads/restaurant/" + restaurantPhoto)
                    .resize(200, 200)
                    .centerCrop()
                    .into(img_user);
            call_client.setVisibility(View.GONE);
            rela_data.setVisibility(View.GONE);

            if (orderStatus.equals("1")) {
                uiHelper = new UiHelper(this);
                locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                locationRequest = uiHelper.getLocationRequest();
                if (!uiHelper.isPlayServicesAvailable()) {
                    Toast.makeText(this, "¡Play Services no se instaló!", Toast.LENGTH_SHORT).show();
                    finish();
                } else requestLocationUpdates();
                firebaseHelper = new FirebaseHelper(orderNo);
                driverOnlineFlag.set(true);
                btn_call.setText("Llamar tienda");
                btn_call_client.setText("Llamar cliente");
                //rela_map.setVisibility(View.VISIBLE);
                call_client.setVisibility(View.VISIBLE);
                rela_data.setVisibility(View.VISIBLE);
                btn_call.setEnabled(true);
            }
            /*if (!userDni.equals("9999999999")) {
                txt_modePay.setText("Pago con: <font color=#2abb9b>" + orderPayment + "</font> | Factura: SI");
            } else {
                if (userId.equals("-1")) {
                    txt_modePay.setText("Dirigirse a la Tienda a: " + orderNote);
                } else {
                    txt_modePay.setText("Pago con: <font color=#2abb9b>" + orderPayment + "</font> | Factura: SI");
                }
            }*/
            if (orderStatus.equals("4") || orderStatus.equals("2") || orderStatus.equals("6") || orderStatus.equals("7")) {
                driverOnlineFlag.set(false);
                btn_picked.setVisibility(View.GONE);
                call_client.setVisibility(View.GONE);
                rela_data.setVisibility(View.GONE);
                rela_map.setVisibility(View.GONE);
            }
            if (orderStatus.equals("5")){
                //Log.e("No_disponible", "No disponibleSSSSSSSSS");
                btn_call.setText("No disponible");
                btn_call.setEnabled(false);
                btn_call_client.setVisibility(View.GONE);
            }
        }

        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri;
                if (orderStatus.equals("1") || orderStatus.equals("3")) {
                    uri = "tel:" + restaurantPhone;
                } else {
                    uri = "tel:" + userPhone;
                }
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse(uri));
                startActivity(i);
                initializeView();
            }
        });

        btn_call_client.setOnClickListener(v -> {
            String uri;
            if (orderStatus.equals("1") || orderStatus.equals("3")) {
                uri = "tel:" + userPhone;
            } else {
                uri = "tel:" + restaurantPhone;
            }
            Intent i = new Intent(Intent.ACTION_DIAL);
            i.setData(Uri.parse(uri));
            startActivity(i);
            initializeView();
        });

        btn_fact.setOnClickListener(view -> {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryOrderDetail.this, R.style.MyDialogTheme);
            builder1.setTitle("Facturación");

            String source;
            if (notes.equals("App")){
                source = "<b><font color=#303C44> Nombre: </font></b>" + userName + "<br/> "
                        + "<b><font color=#303C44> Cédula/Ruc: </font></b>" + userDni + "<br/> "
                        + "<b><font color=#303C44> Dirección: </font></b>" + userAddress + "<br/> "
                        + "<b><font color=#303C44> Teléfono: </font></b>" + userPhone + "<br/> "
                        + "<b><font color=#303C44> Total Producto: </font></b> $" + orderPrice.replace(".", ",")
                        + "<br/>";
            } else {
                if (!factIdentification.isEmpty() && !factNameClient.isEmpty()) {
                    source = "<b><font color=#303C44> Nombre: </font></b>" + factNameClient + "<br/> "
                            + "<b><font color=#303C44> " + factIdentifTipo + ": </font></b>" + factIdentification + "<br/> "
                            + "<b><font color=#303C44> Dirección: </font></b>" + factAddress + "<br/> "
                            + "<b><font color=#303C44> Teléfono: </font></b>" + factPhone + "<br/> "
                            + "<b><font color=#303C44> Correo: </font></b>" + factEmail + "<br/> "
                            + "<b><font color=#303C44> Total Producto: </font></b> $" + orderPrice.replace(".", ",") + "<br/>";
                } else {
                    source = "<b><font color=#303C44> Consumidor final o preguntar los datos al cliente.</font>";
                }
            }

            builder1.setMessage(Html.fromHtml(source));
            builder1.setCancelable(false);
            builder1.setPositiveButton(Html.fromHtml("<font color=#f66666>Cerrar</font>"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert11 = builder1.create();
            try {
                alert11.show();
            } catch (Exception e) {
                //
            }
        });

        //btn data
        btn_data.setOnClickListener(view -> {
            if (!userName.isEmpty()) {
                DataUserName = userName;
            } else DataUserName = "Sin datos";

            if (!deliveryAlias.isEmpty()) {
                DataDeliveryAlias = deliveryAlias;
            } else DataDeliveryAlias = "Sin datos";

            if (!deliveryAddress.isEmpty()) {
                DataDeliveryAddress = deliveryAddress;
            } else DataDeliveryAddress = "Sin datos";

            if (!departmentNumber.isEmpty()) {
                DataDepartmentNumber = departmentNumber;
            } else DataDepartmentNumber = "Sin datos";

            if (!deliveryPhone.isEmpty()) {
                //DataDeliveryPhone = deliveryPhone;
                DataDeliveryPhone = deliveryPhone.replace("+593", "0");
            } else DataDeliveryPhone = "Sin datos";

            if (!deliveryNote.isEmpty()) {
                DataDeliveryNote = deliveryNote;
            } else DataDeliveryNote = "Sin datos";

            if (level_id.equals("1")){
                if (orderStatus.equals("0") || orderStatus.equals("5") || orderStatus.equals("1")){
                    full_charge = "";
                } else {
                    full_charge = "<b><h2><font color=#2abb9b> TOTAL A COBRAR $" + String.format("%.2f", Double.parseDouble(orderTotal)).replace(".", ",") + "</font></h2></b>";
                }
            } else {
                full_charge = "<b><h2><font color=#2abb9b> TOTAL A COBRAR $" + String.format("%.2f", Double.parseDouble(orderTotal)).replace(".", ",") + "</font></h2></b>";
            }

            String source = "<b><font color=#303C44> Cliente: </font></b>" + DataUserName + "<br/> "
                    +"<b><font color=#303C44> Alias: </font></b>" + DataDeliveryAlias + "<br/> "
                    + "<b><font color=#303C44> Dirección: </font></b>" + DataDeliveryAddress + "<br/> "
                    + "<b><font color=#303C44> Piso/Apartamento: </font></b>" + DataDepartmentNumber + "<br/> "
                    + "<b><font color=#303C44> Teléfono: </font></b>" + DataDeliveryPhone+ "<br/> "
                    + "<b><font color=#303C44> Nota: </font></b>" + DataDeliveryNote + "<br/> <br/>"
                    + full_charge
                    + "<h4><font color=#ff9e00>Información</font></h4>"
                    + "<em><font color=#6b0091>Recuerda comunicarte siempre con el cliente para confirmar el pedido con el protocolo de saludo.</font></em><br/>"
                    + "<em><font color=#6b0091>Realiza el proceso en tiempo real, el cliente podrá revisar en tiempo real como avanza su pedido.</font></em><br/>"
                    + "<em><font color=#6b0091>Debes cobrar el valor que muestra en la aplicación, en caso de existir cambios comunícate con soporte.</font></em><br/>"
                    + "<em><font color=#6b0091>En “Instrucciones: Ellos envían”, debes avanzar a la tienda lo más pronto y comunícate con soporte para solicitar los datos de entrega. No llames a la tienda.</font></em>";

            AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryOrderDetail.this, R.style.MyDialogTheme);
            builder1.setTitle(Html.fromHtml("<font color=#ff9e00>Datos de entrega</font>"));
            builder1.setMessage(Html.fromHtml(source));
            builder1.setCancelable(false);
            builder1.setNegativeButton(Html.fromHtml("<font color=#2abb9b>Entendido</font>"), (dialog, id) -> dialog.cancel());
            if (!deliveryPhone.isEmpty()) {
                builder1.setPositiveButton("Llamar al cliente", (dialog, id) -> {
                    String uri;
                    uri = "tel:" + deliveryPhone;
                    Intent i = new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse(uri));
                    startActivity(i);
                    initializeView();
                });
            }

            AlertDialog alert11 = builder1.create();
            try {
                alert11.show();
            } catch (Exception e) {
                //
            }
        });

        // Mostrar botón de ver imagen
        if (imageOrder.isEmpty() || imageOrder.equals("null")) {
            btn_imagen.setVisibility(View.GONE);
        } else {
            btn_imagen.setVisibility(View.VISIBLE);
            btn_imagen.setOnClickListener(v -> {
                showDialog();
                checkPermission();
            });
        }

        // Take A Photo Button
        btn_take_photo.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    DeliveryOrderDetail.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            DeliveryOrderDetail.this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CODE_CAMERA_PERMISSIONS);
            } else {
                try {
                    Intent k = new Intent(DeliveryOrderDetail.this, TakePhotoActivity.class);
                    k.putExtra("OrderNo", orderNo);
                    k.putExtra("DeliveryBoyId", deliveryBoyId);
                    startActivity(k);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //botón de recorrdio de aliado y cliente
        Button btn_map = findViewById(R.id.btn_map);
        if (orderStatus.equals("3")) {
            btn_map.setText("Ruta cliente");
        } else {
            btn_map.setText("Ruta tienda/retira");
        }

        /*if (notes.equals("App") && orderStatus.equals("1")){
            btn_map.setText("Ruta Tienda");
        } else if(notes.equals("App") && orderStatus.equals("3")){
            btn_map.setText("Ruta Cliente");
        } else if(notes.equals("App") && orderStatus.equals("5")){
            btn_map.setText("Ruta Tienda");
            // app cliente fin
        } else if(notes.equals("Compra Web") && orderStatus.equals("1")){
            btn_map.setText("Ruta Tienda");
        } else if(notes.equals("Compra Web") && orderStatus.equals("3")){
            btn_map.setText("Ruta Cliente");
        } else if(notes.equals("Compra Web") && orderStatus.equals("5")){
            btn_map.setText("Ruta Tienda");
            // compra web fin
        } else if(notes.equals("Web Aliado") && orderStatus.equals("1")){
            btn_map.setText("Ruta Tienda");
        } else if(notes.equals("Web Aliado") && orderStatus.equals("3")){
            btn_map.setText("Ruta Cliente");
        } else if(notes.equals("Web Aliado") && orderStatus.equals("5")){
            btn_map.setText("Ruta Tienda");
            // web aliado fin
        } else if(notes.equals("Web") && orderStatus.equals("1")){
            btn_map.setText("Ruta Tienda");
        } else if(notes.equals("Web") && orderStatus.equals("3")){
            btn_map.setText("Ruta Cliente");
        } else if(notes.equals("Web") && orderStatus.equals("5")){
            btn_map.setText("Ruta Tienda");

        } // web con tipo de orden (prepara orden y enviar orden) fin
        else if(notes.equals("Web") && orderType.equals("null")){
            btn_map.setText("Ruta Cliente");
        }*/

        btn_map.setOnClickListener(view -> gettingGPSLocation());

        switch (orderStatus) {
            case "0":
                btn_picked.setVisibility(View.VISIBLE);

                // Mensaje de orden autoaceptada
                if (selfAccepted.equals("self_accepted")) {
                    btn_picked.setText(R.string.self_accepted);
                    btn_picked.setBackground(getResources().getDrawable(R.drawable.custom_shape_self_accepted));
                    // Log.e("selfAcceptanceResponse", selfAccepted);
                } else {
                    btn_picked.setText(R.string.picked);
                    btn_picked.setBackground(getResources().getDrawable(R.drawable.custom_shape_iniciar));
                }
                break;
            case "1":
                btn_picked.setVisibility(View.VISIBLE);
                btn_picked.setText(R.string.restaurantok);
                btn_picked.setBackground(getResources().getDrawable(R.drawable.custom_shape));
                break;
            case "3":
                btn_picked.setVisibility(View.VISIBLE);
                btn_picked.setText(R.string.complete);
                btn_picked.setBackground(getResources().getDrawable(R.drawable.custom_shape));
                break;
            case "5":
                btn_picked.setVisibility(View.VISIBLE);
                btn_picked.setText("Llamar Tienda");
                btn_picked.setBackground(getResources().getDrawable(R.drawable.custom_shape));
                break;
        }

        btn_picked.setOnClickListener(v -> {
            switch (orderStatus) {
                case "0":
                    isPick = "accepted";
                    if (selfAccepted.equals("self_accepted")) {
                        btn_picked.setBackground(getResources().getDrawable(R.drawable.custom_shape_self_accepted));
                        dialogSelfAccepted();
                    } else {
                        btn_picked.setBackground(getResources().getDrawable(R.drawable.custom_shape_iniciar));
                        dialogpicked("¿Estás seguro que deseas aceptar la orden?");
                    }
                    break;
                case "1":
                    btn_picked.setBackground(getResources().getDrawable(R.drawable.custom_shape));

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryOrderDetail.this, R.style.MyDialogTheme);
                    builder1.setTitle("Información");
                    builder1.setIcon(R.mipmap.information);
                    builder1.setCancelable(false);
                    builder1.setMessage("Te recomendamos que antes de confirmar el tiempo de entrega, revises la ruta del cliente.");
                    builder1.setPositiveButton(Html.fromHtml("<font color=#2abb9b><strong>Confirmar</font>"), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            final NumberPicker picker = new NumberPicker(DeliveryOrderDetail.this);
                            final String[] data = new String[]{"                5 MINUTOS                ", "                10 MINUTOS                ", "                15 MINUTOS                ", "                20 MINUTOS                ", "                25 MINUTOS                ", "                30 MINUTOS                ", "                35 MINUTOS                ", "                40 MINUTOS                "};
                            picker.setMinValue(0);
                            picker.setMaxValue(data.length - 1);
                            picker.setDisplayedValues(data);

                            FrameLayout layout = new FrameLayout(DeliveryOrderDetail.this);
                            layout.addView(picker, new FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.WRAP_CONTENT,
                                    FrameLayout.LayoutParams.WRAP_CONTENT,
                                    Gravity.CENTER));

                            AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryOrderDetail.this, R.style.AlertDialogTheme);
                            builder.setTitle("¿Qué tiempo estimas que te tomará llevar la orden al destino?")
                                    .setView(layout)
                                    .setCancelable(false)
                                    .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogo1, int id) {
                                            isPick = "picked";
                                            int item = picker.getValue();

                                            if (item == 0) {
                                                timearrive = "5";
                                            } else if (item == 1) {
                                                timearrive = "10";
                                            } else if (item == 2) {
                                                timearrive = "15";
                                            }
                                            if (item == 3) {
                                                timearrive = "20";
                                            } else if (item == 4) {
                                                timearrive = "25";
                                            } else if (item == 5) {
                                                timearrive = "30";
                                            } else if (item == 6) {
                                                timearrive = "35";
                                            } else if (item == 7) {
                                                timearrive = "40";
                                            }
                                            new postingData().execute();
                                        }
                                    })
                                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogo1, int id) {
                                            dialog.cancel();
                                        }
                                    })
                                    .show();

                        }
                    });
                    builder1.setNegativeButton("Ruta del Cliente", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            GPSTracker gps = new GPSTracker();
                            gps.init(DeliveryOrderDetail.this);
                            // check if GPS enabled
                            if (gps.canGetLocation()) {
                                try {
                                    String uri = "";
                                    uri = "geo:0,0?q=" + userLat + "," + userLon;
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                                    initializeView();
                                } catch (NullPointerException | NumberFormatException e) {
                                    // TODO: handle exception
                                    e.printStackTrace();
                                }
                            } else {
                                gps.showSettingsAlert();
                            }
                        }
                    });

                    AlertDialog alert11 = builder1.create();
                    try {
                        alert11.show();
                    } catch (Exception e) {
                        //
                    }

                    break;
                case "3":
                    getData();
                    if (validationStatus.equals("active")) {
                        double countPhotoInt = Double.parseDouble(countPhoto);
                        //Log.e("xxxxxxxxxxx", String.valueOf(countPhotoEntero));
                        //!countPhoto.equals("0")
                        if (countPhotoInt > 0) {
                            isPick = "complete";
                            dialogpicked("¿Estás seguro que ya entregaste la orden al cliente? ");
                        } else {
                            Toast.makeText(getApplicationContext(), "Para guardar la orden debes subir al menos una foto.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        isPick = "complete";
                        dialogpicked("¿Estás seguro que ya entregaste la orden al cliente? ");
                    }
                    break;
                case "5":
                    String uri;
                    uri = "tel:" + restaurantPhone;
                    Intent i = new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse(uri));
                    startActivity(i);
                    break;
                /*case "4":
                case "2":
                case "6":
                case "7":
                    btn_picked.setVisibility(View.GONE);
                    break;*/
            }
        });
        list_order = findViewById(R.id.list_order2);

        switch (notes) {
            case "App":
                txt_origin_orders.setText("Generada desde la app del cliente.");
                break;
            case "Compra Web":
                txt_origin_orders.setText("Compra generada desde soporte.");
                break;
            case "Web Aliado":
                txt_origin_orders.setText("Generada desde la web del aliado.");
                break;
            case "Web":
                txt_origin_orders.setText("Generada desde soporte." + orderMessage);
                break;
        }
    }
    public void showDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog);

        PhotoView photoView = dialog.findViewById(R.id.photo_view);
        TextView text = dialog.findViewById(R.id.text_dialog);
        text.setText("Imagen de la orden");

        // Cargar la imagen en el PhotoView dentro del diálogo
        loadImage(photoView);

        // Agregar OnClickListener al PhotoView para el zoom
        photoView.setOnClickListener(view -> zoomImage(photoView));

        Button btnDownload = dialog.findViewById(R.id.btnDownload);
        ImageButton closeBtn = dialog.findViewById(R.id.close);
        closeBtn.setOnClickListener(v -> dialog.dismiss());

        // Descargar imagen
        btnDownload.setOnClickListener(view -> {
            downloadImage();
        });
        // btnDownload.setOnClickListener(view -> checkPermission());

        // Mostrar el diálogo
        dialog.show();
    }
    private void loadImage(PhotoView photoView) {
        String IMAGE_URL = getString(R.string.link) + getString(R.string.photopath) + imageOrder;
        Picasso.get()
                .load(IMAGE_URL)
                .placeholder(R.drawable.imagen_error)
                .error(R.drawable.imagen_error)
                .into(photoView);
        // Log.e("IMAGE_URL: ", IMAGE_URL);
    }
    private void zoomImage(PhotoView photoView) {
        float scale = photoView.getScale();
        if (scale < photoView.getMaximumScale()) {
            photoView.setScale(photoView.getMaximumScale(), true);
        } else {
            photoView.setScale(photoView.getMinimumScale(), true);
        }
    }
    private void downloadImage() {
        String IMAGE_URL = getString(R.string.link) + getString(R.string.photopath) + imageOrder;
        // Generar un nombre único para el archivo descargado
        String fileName = generateFileName();

        // Crear una solicitud de descarga utilizando DownloadManager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(IMAGE_URL));

        // Establecer el título y la descripción del archivo descargado
        request.setTitle("Orden_" + orderNo + "_" + fileName);
        request.setDescription("Descargando imagen...");

        // Establecer el destino de la descarga en el directorio de descargas del dispositivo
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Orden_" + orderNo + "_" + fileName + ".png");

        // Obtener el servicio de descarga del sistema
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        // Iniciar la descarga
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(this, "Descargando imagen... Revisa en tu carpeta 'Descargas'.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al iniciar la descarga", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateFileName() {
        // Obtener la fecha y hora actual
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());

        // Combinar la fecha/hora para obtener un nombre único
        return currentDateAndTime;
    }
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void gettingGPSLocation() {
        GPSTracker gps = new GPSTracker();
        gps.init(DeliveryOrderDetail.this);
        // check if GPS enabled
        if (gps.canGetLocation()) {
            try {
                String uri = "";
                if (orderStatus.equals("3")) {
                    uri = "geo:0,0?q=" + userLat + "," + userLon;
                } else {
                    uri = "geo:0,0?q=" + restaurantLat + "," + restaurantLon;
                }
                /*if (notes.equals("App") || notes.equals("Compra Web")
                        || notes.equals("Web Aliado") || notes.equals("Web")) {
                    if (orderStatus.equals("3")) {
                        uri = "geo:0,0?q=" + userLat + "," + userLon;
                    } else {
                        uri = "geo:0,0?q=" + restaurantLat + "," + restaurantLon;
                    }
                }
                lse if (orderType.equals("1") || orderType.equals("2")) {
                    if (orderStatus.equals("3")) {
                        uri = "geo:0,0?q=" + userLat + "," + userLon;
                    } else {
                        uri = "geo:0,0?q=" + restaurantLat + "," + restaurantLon;
                    }
                } else if (notes.equals("Web")) {
                    uri = "geo:0,0?q=" + userLat + "," + userLon;
                } else if (notes.equals("Web") || notes.equals("Web Aliado")) {
                    uri = "geo:0,0?q=" + userLat + "," + userLon;
                }*/
                startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
                initializeView();

            } catch (NullPointerException | NumberFormatException e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        } else {
            gps.showSettingsAlert();
        }

    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);
    }


    class get_order_details extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DeliveryOrderDetail.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            if (DeliveryOrderDetail.this != null && !DeliveryOrderDetail.this.isFinishing()) {
                progressDialog.show();
            }
            //progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            // new showResponse().execute();
        }
    }

    public void getData() {

        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_order_details.php?";
        //Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, hp,
                response -> {
                    //hiding the progressbar after completion
                    //Log.e("Response777", response);
                    try {
                        //getting the whole json object from the response
                        JSONObject obj = new JSONObject(response);
                        String txt_success = obj.getString("success");
                        final String txt_message = obj.getString("message");
                        // orderAssign = obj.getString("assign");
                        // Log.e("ingresoforeground", orderAssign);
                        /*if (orderAssign.equals("0")) {
                            Intent i = new Intent(DeliveryOrderDetail.this, DeliveryStatus.class);
                            startActivity(i);
                        }*/
                        if (txt_success.equals("1")) {
                            getsetDeliveryorderdetail = new ArrayList<>();
                            orderDetailGetSet getSet;

                            //Order
                            JSONObject ja_order = obj.getJSONObject("order");
                            orderTextStatus = ja_order.getString("text_status");
                            orderStatus = ja_order.getString("status");
                            notes = ja_order.getString("notes");
                            orderPrice = ja_order.getString("order_price");
                            orderTotal = ja_order.getString("total_general");
                            orderDeliveryPrice = ja_order.getString("delivery_price");
                            orderAmountPay = ja_order.getString("amount_pay");
                            orderPaymentId = ja_order.getString("payment_id");
                            orderPayment = ja_order.getString("payment");
                            orderTimeRest = ja_order.getString("time_rest");
                            orderChange = ja_order.getString("order_change");
                            orderNote = ja_order.getString("order_note");
                            orderNo = ja_order.getString("order_no");
                            orderMessage = ja_order.getString("order_message");
                            orderType = ja_order.getString("order_type");
                            validationType = ja_order.getString("validation_type");
                            validationStatus = ja_order.getString("validation_status");
                            imageOrder = ja_order.getString("image_order");
                            selfAcceptanceResponse = ja_order.getString("rider_response");
                            selfAccepted = ja_order.getString("self_accepted");
                            countPhoto = ja_order.getString("count_photo");
                            // Log.e("orderMessage ", selfAcceptanceResponse);

                            //Cliente
                            JSONObject ja_customer = obj.getJSONObject("user");
                            userAddress = ja_customer.getString("user_address");
                            userName = ja_customer.getString("user_name");
                            userPhone = ja_customer.getString("user_phone");
                            userLat = ja_customer.getString("user_lat");
                            userLon = ja_customer.getString("user_long");
                            userImage = ja_customer.getString("user_image");
                            userId = ja_customer.getString("user_id");
                            userDni = ja_customer.getString("user_dni");
                            deliveryAddress = ja_customer.getString("delivery_address");
                            deliveryAlias = ja_customer.getString("delivery_alias");
                            deliveryPhone = ja_customer.getString("delivery_phone");
                            deliveryNote = ja_customer.getString("delivery_note");
                            departmentNumber = ja_customer.getString("department_number");
                            deliveryQuantity = ja_customer.getString("delivery_quantity");
                            // Log.e("deliveryQuantity", userName + " ---- " + userPhone +" -- "+ deliveryQuantity);

                            //Datos factura cliente
                            JSONObject ja_dataInvoice = obj.getJSONObject("dataInvoice");
                            factIdentifTipo = ja_dataInvoice.getString("fact_identif_tipo");
                            factIdentification = ja_dataInvoice.getString("fact_identification");
                            factNameClient = ja_dataInvoice.getString("fact_name_client");
                            factAddress = ja_dataInvoice.getString("fact_address");
                            factEmail = ja_dataInvoice.getString("fact_email");
                            factPhone = ja_dataInvoice.getString("fact_phone");

                            //Restaurante
                            JSONObject ja_restaurant = obj.getJSONObject("restaurant");
                            restaurantName = ja_restaurant.getString("restaurant_name");
                            restaurantAddress = ja_restaurant.getString("restaurant_address");
                            restaurantPhoto = ja_restaurant.getString("restaurant_photo");
                            restaurantPhone = ja_restaurant.getString("restaurant_phone");
                            restaurantLat = ja_restaurant.getString("restaurant_lat");
                            restaurantLon = ja_restaurant.getString("restaurant_lon");

                            //Levels Riders
                            JSONObject ja_riders = obj.getJSONObject("DeliveryBoyLevel");
                            level_id = ja_riders.getString("level_id");
                            desc_levels = ja_riders.getString("desc_levels");

                            //items
                            initViews();

                            JSONArray jsonArray = ja_order.getJSONArray("item_name");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jo_orderDetail = jsonArray.getJSONObject(i);

                                getSet = new orderDetailGetSet();
                                getSet.setItemName(jo_orderDetail.getString("name"));
                                getSet.setItemDesc(jo_orderDetail.getString("description"));
                                getSet.setItemQuantity(jo_orderDetail.getString("qty"));
                                getSet.setItemPrice(jo_orderDetail.getString("amount"));
                                getsetDeliveryorderdetail.add(getSet);
                            }
                            OrderDetailAdapter adapter = new OrderDetailAdapter(getsetDeliveryorderdetail, DeliveryOrderDetail.this);
                            list_order.setAdapter(adapter);
                            api_Loader.setVisibility(View.GONE);

                        } else if (txt_success.equals("-4") || (txt_success.equals("-5")) || (txt_success.equals("-8")) || (txt_success.equals("-11"))) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryOrderDetail.this, R.style.MyDialogTheme);
                                    builder1.setTitle("Información");
                                    builder1.setIcon(R.mipmap.information);
                                    builder1.setCancelable(false);
                                    builder1.setMessage(txt_message);
                                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {

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

                                            Intent iv = new Intent(DeliveryOrderDetail.this, Splash.class);
                                            iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(iv);
                                        }
                                    });
                                    AlertDialog alert11 = builder1.create();
                                    try {
                                        alert11.show();
                                    } catch (Exception e) {
                                        //
                                    }
                                }
                            });

                        } else {
                            check_sesion se = new check_sesion();
                            se.validate_sesion(DeliveryOrderDetail.this, txt_success, txt_message);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // error
                    Log.d("Error.Response", error.toString());
                    String message = null;
                    if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryOrderDetail.this, R.style.MyDialogTheme);
                        builder1.setTitle("Información");
                        builder1.setIcon(R.mipmap.information);
                        builder1.setCancelable(false);
                        builder1.setMessage("Por favor verifica tu conexión a Internet");
                        builder1.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                getData();
                            }
                        });
                        builder1.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                finishAffinity();
                            }
                        });
                        builder1.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog alert11 = builder1.create();
                        try {
                            alert11.show();
                        } catch (Exception e) {
                            //
                        }

                    } else
                        Toast.makeText(getApplicationContext(), "Por el momento no podemos procesar tu solicitud", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("order_id", orderNo);
                params.put("deliverboy_id", deliveryBoyId);
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

        RequestQueue requestQueue = Volley.newRequestQueue(DeliveryOrderDetail.this);
        //adding the string request to request queue
        requestQueue.add(stringRequest);

    }

    class postingData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DeliveryOrderDetail.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            //Log.e("sourceFile", "" + orderNo);
            Log.e("isPickXXXXX", isPick);
            String hp = "";
            switch (isPick) {
                case "rejected":
                    hp = getString(R.string.link) + getString(R.string.servicepath) + "order_rejected_deliveryboy.php";
                    break;
                case "accepted":
                    hp = getString(R.string.link) + getString(R.string.servicepath) + "order_accept_deliveryboy.php";
                    break;
                case "picked":
                    hp = getString(R.string.link) + getString(R.string.servicepath) + "order_picked_deliveryboy.php";
                    break;
                case "complete":
                    hp = getString(R.string.link) + getString(R.string.servicepath) + "order_delivered_deliveryboy.php";
                    firebaseHelper.deleteDriver(); //delete data
                    driverOnlineFlag.set(true);
                    break;
            }

            StringRequest postRequest = new StringRequest(Request.Method.POST, hp, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject responsedat = new JSONObject(response);
                        String txt_success = responsedat.getString("success");
                        String txt_message = responsedat.getString("message");

                        if (txt_success.equals("1")) {
                            if (orderStatus.equals("1")) {
                                check_sesion.reverseTimer(Integer.parseInt("0"), txt_orderTime);
                            } else if (orderStatus.equals("3") && (isPick.equals("complete"))) {
                                check_sesion.reverseTimer(Integer.parseInt("0"), txt_orderTime);
                                btn_picked.setVisibility(View.GONE);

                            } else if (orderStatus.equals("4") || orderStatus.equals("2") || orderStatus.equals("6") || orderStatus.equals("7")) {
                                check_sesion.reverseTimer(Integer.parseInt("0"), txt_orderTime);
                                btn_picked.setVisibility(View.GONE);
                            }

                            new get_order_details().execute();

                        } else {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryOrderDetail.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setIcon(R.mipmap.information);
                            builder1.setCancelable(false);
                            builder1.setMessage(txt_message);
                            builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    new get_order_details().execute();
                                }
                            });

                            AlertDialog alert11 = builder1.create();
                            try {
                                alert11.show();
                            } catch (Exception e) {
                                //
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("JSON Parser", "Error parsing data " + e.toString());
                    }

                }
            },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                        String message = null;
                        if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(), "Por favor revisa tu conexión a Internet", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplicationContext(), "Por el momento no podemos procesar tu solicitud", Toast.LENGTH_SHORT).show();
                    }

                }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("order_id", orderNo);
                    params.put("deliverboy_id", deliveryBoyId);
                    params.put("self_accepted", selfAccepted);
                    params.put("code", getString(R.string.version_app));
                    params.put("operative_system", getString(R.string.sistema_operativo));

                    if (isPick.equals("picked")) {
                        params.put("time_orden", timearrive);
                    }
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

            RequestQueue requestQueue = Volley.newRequestQueue(DeliveryOrderDetail.this);

            //adding the string request to request queue
            requestQueue.add(postRequest);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private void initView() {
        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(v -> onBackPressed());
        api_Loader = findViewById(R.id.api_Loader);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

}