package repartidor.faster.com.ec.motorizado;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import repartidor.faster.com.ec.Adapter.ManageCashListAdapter;
import repartidor.faster.com.ec.Getset.TravelHistorySet;
import repartidor.faster.com.ec.R;
import repartidor.faster.com.ec.utils.Config;

public class ManageCashList extends AppCompatActivity {

    private String CashId, deliveryBoyId;
    private String regId;
    Button btn_save;
    private ListView listView_travelhistory;
    private ProgressDialog progressDialog;
    private ArrayList<TravelHistorySet> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list_cash);
        Intent i = getIntent();
        CashId = i.getStringExtra("cash_id");
        deliveryBoyId = i.getStringExtra("DeliveryBoyId");
        // Log.e("orderNoXXXXXX ", deliveryBoyId + " - " + PaymentNo);
        displayFirebaseRegId();
        getdetaillist();
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
        getdetaillist();
    }

    private void initView() {
        btn_save = findViewById(R.id.btn_save);
        ImageButton ib_back = findViewById(R.id.ib_back);
        TextView txt_header = findViewById(R.id.txt_header);
        txt_header.setText("Movimiento caja #"+ CashId);
        ib_back.setOnClickListener(v -> onBackPressed());
        listView_travelhistory = findViewById(R.id.list_travel_history);
        data = new ArrayList<>();
        btn_save.setOnClickListener(v -> dialogpicked());
    }
    private void dialogpicked() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ManageCashList.this, R.style.MyDialogTheme);
        builder1.setTitle("Confirmación");
        builder1.setIcon(R.mipmap.confirmation_2);
        builder1.setCancelable(false);
        builder1.setMessage("¿Estás seguro que deseas cerrar caja?");

        builder1.setPositiveButton(Html.fromHtml("<font color=#2abb9b>Si, estoy seguro</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                new ManageCashList.postingData().execute();
                Intent i = new Intent(ManageCashList.this, ManageCashHistory.class);
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
    private void getdetaillist() {
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "riderGetPhotoListCash.php?";
        // Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, hp,
                response -> {
                    //hiding the progressbar after completion
                    // Log.e("Response777", response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        String txt_success = obj.getString("success");
                        String txt_message = obj.getString("message");
                        String txt_closing_date = obj.getString("closing_date");
                        // Log.e("txt_closing_date: ", txt_closing_date);

                        if (txt_closing_date.equals("null")) {
                            btn_save.setVisibility(View.VISIBLE);
                            btn_save.setEnabled(true);
                        } else {
                            btn_save.setVisibility(View.VISIBLE);
                            btn_save.setEnabled(false);
                            btn_save.setText("Caja Cerrada");
                            btn_save.setTextColor(getResources().getColor(R.color.red));
                            btn_save.setBackgroundColor(getResources().getColor(R.color.white));
                        }
                        if(txt_success.equals("1")) {
                            data.clear();
                            listView_travelhistory.setAdapter(null);

                            JSONArray ja_order = obj.getJSONArray("data");
                            TravelHistorySet getSet;
                            for (int i = 0; i < ja_order.length(); i++) {
                                JSONObject jo_manageCash = ja_order.getJSONObject(i);
                                getSet = new TravelHistorySet();

                                getSet.setHistoryNo(jo_manageCash.getString("id"));
                                getSet.setPhoto(getResources().getString(R.string.link) + getString(R.string.accountingPath) + jo_manageCash.getString("image_name"));
                                getSet.setCashVoucher(jo_manageCash.getString("voucher"));
                                getSet.setHistoryDate(jo_manageCash.getString("created"));
                                getSet.setCashDetail(jo_manageCash.getString("description"));
                                getSet.setCashAmount(jo_manageCash.getString("amount"));
                                data.add(getSet);
                                //Log.e("JSONObject", String.valueOf(ja_order));
                            }
                            ManageCashListAdapter adapter = new ManageCashListAdapter(data, ManageCashList.this);
                            listView_travelhistory.setAdapter(adapter);
                        }
                        if(txt_success.equals("0")){
                            Toast.makeText(ManageCashList.this, txt_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                        String message = null;
                        if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(ManageCashList.this, R.style.MyDialogTheme);
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
                params.put("cash_id", CashId);
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
        RequestQueue requestQueue = Volley.newRequestQueue(ManageCashList.this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }
    class postingData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ManageCashList.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            //Log.e("sourceFile", "" + orderNo);
            String hp = getString(R.string.link) + getString(R.string.servicepath) + "riderSaveDailyIncome.php";
            StringRequest postRequest = new StringRequest(Request.Method.POST, hp, response -> {
                try {
                    JSONObject responsedat = new JSONObject(response);
                    String txt_success = responsedat.getString("success");
                    String txt_message = responsedat.getString("message");
                    Log.e("txt_messageXXXX; ", String.valueOf(responsedat));

                    if (txt_success.equals("1")) {
                        Toast.makeText(getApplicationContext(), txt_message, Toast.LENGTH_SHORT).show();
                        // Log.e("XXXXXXXXXX: ", txt_message);
                    } else {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ManageCashList.this, R.style.MyDialogTheme);
                        builder1.setTitle("Información");
                        builder1.setIcon(R.mipmap.information);
                        builder1.setCancelable(false);
                        builder1.setMessage(txt_message);
                        builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //new DeliveryOrderDetail.get_order_details().execute();
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
                    //Log.e("JSON Parser", "Error parsing data " + e.toString());
                }

            },
                error -> {
                    // error
                    //Log.d("Error.Response", error.toString());
                    String message = null;
                    if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                        Toast.makeText(getApplicationContext(), "Por favor revisa tu conexión a Internet", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getApplicationContext(), "Por el momento no podemos procesar tu solicitud", Toast.LENGTH_SHORT).show();
                }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("cash_id", CashId);
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
            RequestQueue requestQueue = Volley.newRequestQueue(ManageCashList.this);

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
}