package repartidor.faster.com.ec.motorizado;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import repartidor.faster.com.ec.Adapter.DeliveryRecyclerViewAdapter;
import repartidor.faster.com.ec.Adapter.OrderDetailAdapter;
import repartidor.faster.com.ec.Getset.DeliveryGetSet;
import repartidor.faster.com.ec.R;
import repartidor.faster.com.ec.utils.Config;
import repartidor.faster.com.ec.utils.check_sesion;

public class DeliveryOrderHistory extends NotificationToAlertActivity {

    // private ArrayList<DeliveryGetSet> data;
    private ArrayList< DeliveryGetSet > data = new ArrayList < DeliveryGetSet> ();
    private OrderDetailAdapter adapter;
    private RecyclerView lv_order_history;
    private ProgressBar data_loader;
    private ProgressBar api_Loader;
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private static DeliveryOrderHistory instance;
    private static androidx.appcompat.widget.AppCompatButton btn_bell;
    private boolean active;
    private int count;
    private static final String MY_PREFS_ACTIVITY = "DeliveryActivity";
    private String regId,DeliveryBoyId,CurrentDeliveryPrice;
    private TextView txt_delivery;

    private int pageNo = 1;
    private boolean data_loader_status = true;

    private  DeliveryRecyclerViewAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_delivery_order_history);
        instance = this;
        active = false;
        count = 0;
        Objects.requireNonNull(getSupportActionBar()).hide();
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        DeliveryBoyId = pref.getString("DeliveryUserId",null);

        //Log.d("TAG", "onCreate: DeliveryBoyId is " + DeliveryBoyId);
        //Log.d("TAG", "onCreate: DeliveryBoyId is " + regId);
    }
    public static DeliveryOrderHistory getInstance() {
        return instance;
    }

    private void initView() {
        txt_delivery = findViewById(R.id.txt_delivery);
        lv_order_history = findViewById(R.id.lv_order_history_recyclerView);
        data_loader = findViewById(R.id.ItemLoader);
        api_Loader = findViewById(R.id.api_Loader);

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
        editor.putBoolean("Main", false);
        editor.putString("Activity", "DeliveryOrderHistory");
        editor.apply();

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(v -> onBackPressed());

        recyclerAdapter = new DeliveryRecyclerViewAdapter( DeliveryOrderHistory.this, data);
        lv_order_history.setHasFixedSize(true);
        lv_order_history.setLayoutManager(new LinearLayoutManager(DeliveryOrderHistory.this));
        lv_order_history.setAdapter(recyclerAdapter);
        lv_order_history.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)) {
                    if(data_loader_status && data != null && data.size() > 0){
                        lv_order_history.setPadding(0,0,0,0);
                        data_loader.setVisibility(View.VISIBLE);
                        data_loader_status = false;
                        pageNo = pageNo + 1;
                        settingData(pageNo);
                        //Log.d("TAG", "onScrolled: Recyclerview bottom reached!!.." +pageNo );
                    }
                }
            }
        });
        if(data_loader_status && data != null && data.size() > 0){
            lv_order_history.setPadding(0,0,0,0);
        } else {
            settingData(pageNo);
        }
    }

    private void settingData(int pageNo) {
        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "order_history_delivery.php?";
        //Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, hp,
                response -> {
                    //hiding the progressbar after completion
                    //Log.e("Response", response);
                    try {
                        //getting the whole json object from the response
                        JSONObject obj = new JSONObject(response);
                        String txt_success=obj.getString("success");
                        final String txt_message=obj.getString("message");
                        if (txt_success.equals("1")) {
                            JSONArray ja_order = obj.getJSONArray("order");
                            DeliveryGetSet getSet;
                            //data = new ArrayList<>();
                            for (int i = 0; i < ja_order.length(); i++) {
                                JSONObject jo_orderDetail = ja_order.getJSONObject(i);
                                getSet = new DeliveryGetSet();
                                getSet.setResName(jo_orderDetail.getString("restaurant_name"));
                                getSet.setOrderAddress(jo_orderDetail.getString("order_address"));
                                getSet.setOrderNo(jo_orderDetail.getString("order_no"));
                                getSet.setOrderDelivery(jo_orderDetail.getString("delivery_price"));
                                getSet.setOrderTime(jo_orderDetail.getString("time_rest"));
                                getSet.setOrderDate(jo_orderDetail.getString("date"));
                                getSet.setOrderAmount(jo_orderDetail.getString("total_amount"));
                                getSet.setOrderComision(jo_orderDetail.getString("payment_amount"));
                                getSet.setStatus(jo_orderDetail.getString("status"));
                                getSet.setCountRows(jo_orderDetail.getString("numrow_delivery"));
                                String price = jo_orderDetail.getString("current_delivery_price");
                                String countrows = jo_orderDetail.getString("numrow_delivery");
                                if (countrows.equals("1")){
                                    txt_delivery.setText(Html.fromHtml("Hoy generaste <font color=#2abb9b><strong>$"
                                            + price + "</strong></font> completando con éxito <font color=#2abb9b><strong>"
                                            + countrows + "</strong></font> orden"));
                                } else {
                                    txt_delivery.setText(Html.fromHtml("Hoy generaste <font color=#2abb9b><strong>$"
                                            + price + "</strong></font> completando con éxito <font color=#2abb9b><strong>"
                                            + countrows + "</strong></font> órdenes"));
                                }
                                txt_delivery.setVisibility(View.VISIBLE);
                                //Log.e("current_deliveryXX", jo_orderDetail.getString("current_delivery_price"));
                                data.add(getSet);
                            }
                            //Log.e("OrderList","=======>>"+ja_order.length());
                            api_Loader.setVisibility(View.GONE);
                            data_loader_status = true;
                            lv_order_history.setPadding(0,0,0,0); //120 para tamaño data_loader_status
                            data_loader.setVisibility(View.GONE);
                            recyclerAdapter.notifyDataSetChanged();

                            //Log.d("TAG", "onResponse: notify adapter success!!..");

                        } else if (txt_success.equals("-4")||(txt_success.equals("-5"))||(txt_success.equals("-8"))||(txt_success.equals("-11")))  {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryOrderHistory.this, R.style.MyDialogTheme);
                                    builder1.setTitle("Información");
                                    builder1.setCancelable(false);
                                    builder1.setMessage(txt_message);
                                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {

                                            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                            editor.putBoolean("isDeliverAccountActive", false);
                                            editor.putString("DeliveryUserId", "");
                                            editor.putString("DeliveryUserName", "");
                                            editor.putString("DeliveryUserPhone", "");
                                            editor.putString("DeliveryUserEmail", "");
                                            editor.putString("DeliveryUserVNo", "");
                                            editor.putString("DeliveryUserVType", "");
                                            editor.putString("DeliveryUserImage", "");
                                            editor.apply();

                                            SharedPreferences.Editor editor2 = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0).edit();
                                            editor2.putString("regId",null);
                                            editor2.apply();

                                            Intent iv = new Intent(DeliveryOrderHistory.this, Splash.class);
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

                        }  else{
                            //Log.d("TAG", "onResponse: data not exist");
                            data_loader_status = false;
                            lv_order_history.setPadding(0,0,0,0);
                            data_loader.setVisibility(View.GONE);
                            check_sesion se=new check_sesion();
                            se.validate_sesion(DeliveryOrderHistory.this,txt_success,txt_message);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    //displaying the error in toast if occurs
                    //Log.e("Error", "onErrorResponse: "+error.toString() );

                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null) {
                        Log.e("Status code", String.valueOf(networkResponse.statusCode));
                        Toast.makeText(getApplicationContext(), String.valueOf(networkResponse.statusCode), Toast.LENGTH_SHORT).show();
                    }
                })    {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("deliverboy_id", getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserId", ""));
                params.put("code", getString(R.string.version_app));
                params.put("operative_system",  getString(R.string.sistema_operativo));
                params.put("pageno", String.valueOf(pageNo));

                Log.d("TAG", "getParams: "+params);
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
        RequestQueue requestQueue = Volley.newRequestQueue(DeliveryOrderHistory.this);
        //adding the string request to request queue
        requestQueue.add(stringRequest);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

}