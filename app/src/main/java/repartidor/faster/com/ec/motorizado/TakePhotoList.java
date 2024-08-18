package repartidor.faster.com.ec.motorizado;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import repartidor.faster.com.ec.Adapter.TravelPhotoListAdapter;
import repartidor.faster.com.ec.Getset.TravelHistorySet;
import repartidor.faster.com.ec.R;
import repartidor.faster.com.ec.utils.Config;

public class TakePhotoList extends AppCompatActivity {

    private String orderNo, deliveryBoyId;
    private String regId;
    private ListView listView_travelhistory;
    private ArrayList<TravelHistorySet> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo_list);
        Intent i = getIntent();
        orderNo = i.getStringExtra("OrderNo");
        deliveryBoyId = i.getStringExtra("DeliveryBoyId");
        displayFirebaseRegId();
    }
    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        //Log.e("fireBaseRid", "Firebase Reg id: " + regId);
    }
    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {
        ImageButton ib_back = findViewById(R.id.ib_back);
        TextView txt_header = findViewById(R.id.txt_header);
        txt_header.setText("Ver foto "+getString(R.string.txt_order_no) + orderNo);

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        listView_travelhistory = findViewById(R.id.list_travel_history);
        data = new ArrayList<>();
        getdetaillist();
    }
    private void getdetaillist() {
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_get_photo_list.php?";
        Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, hp,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //hiding the progressbar after completion
                        // Log.e("Response777", response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            String txt_success = obj.getString("success");
                            final String txt_message = obj.getString("message");
                            if(txt_success.equals("1")) {
                                data.clear();
                                listView_travelhistory.setAdapter(null);

                                JSONArray ja_order = obj.getJSONArray("data");
                                TravelHistorySet getSet;
                                //Boolean llevando = false;
                                for (int i = 0; i < ja_order.length(); i++) {
                                    JSONObject jo_orderDetail = ja_order.getJSONObject(i);
                                    getSet = new TravelHistorySet();

                                    getSet.setHistoryNo(jo_orderDetail.getString("id"));
                                    getSet.setPhoto(getResources().getString(R.string.link) + getString(R.string.photopath) + jo_orderDetail.getString("photo"));
                                    getSet.setDetail(jo_orderDetail.getString("details"));
                                    getSet.setHistoryDate(jo_orderDetail.getString("date"));
                                    getSet.setStatus(jo_orderDetail.getString("status"));
                                    data.add(getSet);
                                    //Log.e("JSONObject", jo_orderDetail.getString("photo"));
                                }
                                TravelPhotoListAdapter adapter = new TravelPhotoListAdapter(data, TakePhotoList.this);
                                listView_travelhistory.setAdapter(adapter);
                            }
                            if(txt_success.equals("0")){
                                Toast.makeText(TakePhotoList.this, txt_message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(TakePhotoList.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setCancelable(false);
                            builder1.setMessage("Por favor verifica tu conexión a Internet");
                            builder1.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    getdetaillist();
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
                                if (this != null) {
                                    alert11.show();
                                }
                            } catch (Exception e) {
                                //
                            }

                        } else
                            Toast.makeText(getApplicationContext(), "Por el momento no podemos procesar tu solicitud", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("order_id", orderNo);
                params.put("deliveryboy_id", deliveryBoyId);
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
        RequestQueue requestQueue = Volley.newRequestQueue(TakePhotoList.this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }
}