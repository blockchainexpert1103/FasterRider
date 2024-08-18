package repartidor.faster.com.ec.motorizado;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import repartidor.faster.com.ec.Getset.paymentMethodGetSet;
import repartidor.faster.com.ec.R;
import repartidor.faster.com.ec.utils.Config;


public class TakePhotoPay extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "Upload Image";
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private int PICK_IMAGE_REQUEST = 100;
    private String PaymentNo, deliveryBoyId, notaPago;
    private String regId, currentPhotoPath;
    private Button input_status, detail_list;
    ImageView uploadImage;
    Button cameraUploadButton;
    Button galleryUploadButton;
    Button uploadButton;
    Bitmap bitmap;
    Uri filePath;
    Spinner spinnerPaymentMethod;
    private ProgressDialog dialog;
    private CharSequence CurrentDate;
    private TextInputLayout comprobanteLayout;
    String selectedFilePath;
    double lat, lon;
    LocationManager locationManager;
    private String selectType;
    private TextInputEditText nComprobante;
    private TextView TextNotaPago;
    private ArrayList<paymentMethodGetSet> PaymentMethod;
    private static final int REQUEST_CODE_CAMERA_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo_pay);
        Intent i = getIntent();
        PaymentNo = i.getStringExtra("PaymentNo");
        deliveryBoyId = i.getStringExtra("DeliveryBoyId");
        // Log.e("PaymentNo: ", deliveryBoyId);
        displayFirebaseRegId();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Cargando el comprobante. Por favor espere...");
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPaymentMethod();
        initView();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.detail_list) {
            Intent k = new Intent(TakePhotoPay.this, TakePhotoListPay.class);
            k.putExtra("PaymentNo", PaymentNo);
            k.putExtra("DeliveryBoyId", deliveryBoyId);
            startActivity(k);
        }
    }
    public void cardViewClicked(View view) {
        if (view.getId() == R.id.button_info) {
            String source = "<h4><font color=#ff9e00>INFORMACIÓN IMPORTANTE</font></h4>"
                    + "<font color=#6b0091>- Revisa que el comprobante corresponda al pago actual.</string></font><br/>"
                    + "<font color=#6b0091>- Solo tienes dos intentos para enviar el comprobante.</font><br/>"
                    + "<font color=#6b0091>- Se aprobará el pago previo a la confirmación del valor acreditado.</font><br/>"
                    + "<font color=#6b0091>- Si tienes dos o más comprobantes de pago, debes tomar un solo foto o subir en una sola imagen todos los comprobantes.</font><br/>"
                    + "<font color=#6b0091>- Ejemplo para subir dos o más comprobantes 1234567,7654321</font><br/>"
                    + "<font color=#6b0091>- Solo se acepta transferencias de la misma entidad bancaria.</font>";

            AlertDialog.Builder builder1 = new AlertDialog.Builder(TakePhotoPay.this, R.style.MyDialogTheme);
            //builder1.setTitle(Html.fromHtml("<font color=#ff9e00>Datos de entrega</font>"));
            builder1.setMessage(Html.fromHtml(source));
            builder1.setCancelable(false);
            builder1.setNegativeButton(Html.fromHtml("<font color=#2abb9b>Entendido</font>"), new DialogInterface.OnClickListener() {
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
    }
    private void initView() {

        try {
            comprobanteLayout =  findViewById(R.id.comprobanteLayout);
            nComprobante = findViewById(R.id.comprobante_input);
            detail_list = findViewById(R.id.detail_list);
            ImageButton ib_back = findViewById(R.id.ib_back);
            ib_back.setOnClickListener(v -> onBackPressed());
            detail_list.setOnClickListener(this);
            checkdetaillist();

            TextView txt_header = findViewById(R.id.txt_header);
            txt_header.setText("Pago #" + PaymentNo);
            uploadImage = findViewById(R.id.IdProf);
            cameraUploadButton = findViewById(R.id.upload_button_from_camera);
            galleryUploadButton = findViewById(R.id.upload_button_from_gallery);
            uploadButton = findViewById(R.id.upload_button_save);
            spinnerPaymentMethod = findViewById(R.id.spinner_payment_method);
            TextNotaPago =  findViewById(R.id.txt_nota);

            cameraUploadButton.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(
                        TakePhotoPay.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            TakePhotoPay.this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CODE_CAMERA_PERMISSIONS);
                } else {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Log.e("Camera open Failed", "Error");
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(this,
                                    "repartidor.faster.com.ec.provider",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            selectType = "camera";
                            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                            //TakePhotoPay.this.finish();
                        }
                    //}
                }
            });

            uploadButton.setOnClickListener(v -> {
                dialog.show();
                try {
                    if (selectType == null) {
                        Toast.makeText(TakePhotoPay.this,"Seleccione una imagen",Toast.LENGTH_LONG).show();
                        dialog.hide();
                    } else {
                        if (selectType.equalsIgnoreCase("camera")) {
                            if(currentPhotoPath == null) {
                                Toast.makeText(TakePhotoPay.this,"Seleccione una imagen",Toast.LENGTH_LONG).show();
                                dialog.hide();
                            } else {
                                uploadOrderImage();
                            }
                        } else {
                            if(bitmap == null) {
                                Toast.makeText(TakePhotoPay.this,"Seleccione una imagen",Toast.LENGTH_LONG).show();
                                dialog.hide();
                            } else {
                                uploadOrderImage();
                            }
                        }
                    }
                    validateVoucher();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            });
            galleryUploadButton.setOnClickListener(v -> selectImage());
        } catch (Exception e) {
            //
        }
    }
    private void getPaymentMethod() {

        String hp = getString(R.string.link) + getString(R.string.servicepath) + "riderGetPaymentMethod.php?";
        Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, hp,
                response -> {
                    //hiding the progressbar after completion
                    Log.e("Response777", response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        String txt_success = obj.getString("success");
                        PaymentMethod = new ArrayList<>();
                        if(txt_success.equals("1")) {
                            // payment method
                            JSONArray t_data = obj.getJSONArray("payment_method");
                            paymentMethodGetSet t_pay_met;
                            String[] array_pay_met = new String[t_data.length()];

                            for (int i = 0; i < t_data.length(); i++) {
                                JSONObject jo_orderDetail = t_data.getJSONObject(i);
                                t_pay_met = new paymentMethodGetSet();
                                t_pay_met.setId(jo_orderDetail.getString("id"));
                                t_pay_met.setPaymentMethod(jo_orderDetail.getString("payment"));
                                array_pay_met[i] = jo_orderDetail.getString("payment");
                                PaymentMethod.add(t_pay_met);
                            }

                            ArrayAdapter adapter = new ArrayAdapter(TakePhotoPay.this,android.R.layout.simple_spinner_item, array_pay_met);
                            adapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerPaymentMethod.setAdapter(adapter);

                            // Asume que tienes un array llamado a_costs que contiene tus opciones
                            spinnerPaymentMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    // Verifica la posición seleccionada
                                    if (position == 0) {
                                        uploadButton.setVisibility(View.GONE);
                                        // Muestra un mensaje usando Toast
                                        Toast.makeText(TakePhotoPay.this, "Seleccione un método de pago y carga el comprobante.", Toast.LENGTH_LONG).show();
                                    } else {
                                        uploadButton.setVisibility(View.VISIBLE);
                                    }
                                    //Log.e("PaymentMethodXXX: ", PaymentMethod.get(spinnerPaymentMethod.getSelectedItemPosition()).getId());
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });

                        }
                        if(txt_success.equals("0")){
                            Toast.makeText(TakePhotoPay.this, "Error al listar el método de pago.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                        String message = null;
                        if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(TakePhotoPay.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setCancelable(false);
                            builder1.setMessage("Por favor verifica tu conexión a Internet");
                            builder1.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    getPaymentMethod();
                                    checkdetaillist();
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
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return super.getBodyContentType();
            }
        };

        //MySingleton.getInstance(DeliveryOrderDetail.this).addToRequestQueue(stringRequest);
        RequestQueue requestQueue = Volley.newRequestQueue(TakePhotoPay.this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }

    private void checkdetaillist() {
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "riderGetPhotoListPayment.php?";
        Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, hp,
                response -> {
                    //hiding the progressbar after completion
                    try {
                        JSONObject obj = new JSONObject(response);
                        String txt_success = obj.getString("success");
                        String txt_message = obj.getString("message");
                        String txt_make_payment = obj.getString("make_payment");
                        notaPago = obj.getString("motivo_rechazo");

                        if(txt_success.equals("1")) {
                            int makePayment = Integer.parseInt(txt_make_payment);
                            if (makePayment > 0) {
                                detail_list.setVisibility(View.VISIBLE);
                            } else {
                                detail_list.setVisibility(View.GONE);
                            }
                        }
                        if(txt_success.equals("0")){
                            Toast.makeText(TakePhotoPay.this, txt_message, Toast.LENGTH_SHORT).show();
                        }
                        // Log.e("notaPago: ", notaPago);
                        // Nota de rechazo o impago
                        if (notaPago.equals("null") || notaPago.isEmpty()){
                            TextNotaPago.getResources().getString(R.string.photo_note_pay);
                        } else {
                            TextNotaPago.setText(notaPago);
                            TextNotaPago.setTextColor(getResources().getColor(R.color.red));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                        String message = null;
                        if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(TakePhotoPay.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setCancelable(false);
                            builder1.setMessage("Por favor verifica tu conexión a Internet");
                            builder1.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    checkdetaillist();
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
                params.put("payment_id", PaymentNo);
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
        RequestQueue requestQueue = Volley.newRequestQueue(TakePhotoPay.this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }
    void uploadOrderImage () throws JSONException, IOException {
        if (ActivityCompat.checkSelfPermission(
                TakePhotoPay.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                TakePhotoPay.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        dialog.show();
        String url = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_payment_photo.php?";

        OkHttpClient client = new OkHttpClient();

        File sourceFile = new File(getApplicationContext().getCacheDir(), "image");
        sourceFile.createNewFile();

        if(sourceFile == null){
            Toast.makeText(getApplicationContext(), "Por favor seleccione una imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        if (selectType.equalsIgnoreCase("camera")) {
            Bitmap bmp = BitmapFactory.decodeFile(currentPhotoPath);
            bmp.compress(Bitmap.CompressFormat.JPEG, 15, bos);
        } else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bos);
        }

        byte[] bitmapdata = bos.toByteArray();

        FileOutputStream fos = new FileOutputStream(sourceFile);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();

        final MediaType MEDIA_TYPE = MediaType.parse("image/png");
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("photo", "profile.png", RequestBody.create(MEDIA_TYPE, sourceFile))
                .addFormDataPart("payment_id", PaymentNo)
                .addFormDataPart("comprobante_num", nComprobante.getText().toString().trim())
                .addFormDataPart("deliveryboy_id", deliveryBoyId)
                .addFormDataPart("payment_method", PaymentMethod.get(spinnerPaymentMethod.getSelectedItemPosition()).getId())
                .addFormDataPart("code", getString(R.string.version_app))
                .addFormDataPart("operative_system", getString(R.string.sistema_operativo))
                .build();

        Request request = new Request.Builder()
                .header("Authorization","Bearer "+ regId)
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("Communication ERROR", "Exception", e);
            }
            @Override
            public void onResponse(Response response) throws IOException {
                ResponseBody responseBody = response.body();
                TakePhotoPay.this.runOnUiThread(new Runnable() { @SuppressLint("CommitPrefEdits")
                public void run() {
                    if (response.code() == 200) {
                        dialog.dismiss();

                        try {
                            JSONObject obj = new JSONObject(response.body().string());
                            String txt_success = obj.getString("success");
                            final String txt_message = obj.getString("message");
                            if(txt_success.equals("1")) {
                                Toast.makeText(getApplicationContext(), txt_message, Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), txt_message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        dialog.hide();
                        Toast.makeText(getApplicationContext(), "Invalid data", Toast.LENGTH_SHORT).show();
                    }
                }});
            }

        });
    }

    private void validateVoucher() {
        if (nComprobante.getText().toString().trim().isEmpty()) {
            comprobanteLayout.setError(getString(R.string.err_msg_comprobante));
        } else {
            comprobanteLayout.setErrorEnabled(false);
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        selectType = "gallery";
        startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // upload compressed photo from gallery
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                Bitmap b = BitmapFactory.decodeStream(new ByteArrayInputStream(stream.toByteArray()));

                uploadImage.setImageBitmap(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // upload compressed photo from camera
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && currentPhotoPath != null) {
            Bitmap bmp = BitmapFactory.decodeFile(currentPhotoPath);
            uploadImage.setImageBitmap(bmp);
        }

    }

    // Recibir resultado de la cámara
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permiso de cámara concedido.", Toast.LENGTH_LONG).show();

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                //if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.e("Camera open Failed", "Error");
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(this,
                                "repartidor.faster.com.ec.provider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        selectType = "camera";
                        startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                    }
                //}
            } else {
                Toast.makeText(getApplicationContext(), "Permiso de cámara denegado.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}