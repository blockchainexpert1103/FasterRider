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
import android.text.format.DateFormat;
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
import java.util.Random;

import repartidor.faster.com.ec.Getset.paymentMethodGetSet;
import repartidor.faster.com.ec.R;
import repartidor.faster.com.ec.utils.Config;


public class ManageCash extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "Upload Image";
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private int PICK_IMAGE_REQUEST = 100;
    private String PaymentNo, deliveryBoyId, notaPago, cashId, closingDate = null;
    private String regId, currentPhotoPath;
    private Button input_status, detail_list;
    ImageView uploadImage;
    Button cameraUploadButton;
    Button galleryUploadButton;
    Button uploadButton;
    Bitmap bitmap;
    Uri filePath;
    Spinner spinnerTypeDocument;
    private ProgressDialog dialog;
    private CharSequence CurrentDate;
    private TextInputLayout comprobanteLayout, descripcionLayout, amountLayout;
    String selectedFilePath;
    private Integer NumRandom;
    private String selectType;
    private TextInputEditText nComprobante, description, amount;
    private ArrayList<paymentMethodGetSet> TypeDocument;
    private static final int REQUEST_CODE_CAMERA_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_cash);
        Intent i = getIntent();
        cashId = i.getStringExtra("cash_id");
        deliveryBoyId = i.getStringExtra("DeliveryBoyId");
        displayFirebaseRegId();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Cargando el comprobante. Por favor espere...");
        random();
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    private void random(){
        final int min = 1;
        final int max = 999;
        NumRandom = new Random().nextInt((max - min) + 1) + min;
        Date d = new Date();
        CurrentDate = DateFormat.format("mmss", d.getTime());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.detail_list) {
            Intent k = new Intent(ManageCash.this, ManageCashList.class);
            k.putExtra("cash_id", cashId);
            k.putExtra("DeliveryBoyId", deliveryBoyId);
            startActivity(k);
        }
    }
    public void cardViewClicked(View view) {
        if (view.getId() == R.id.button_info) {
            String source = "<h4><font color=#ff9e00>INFORMACIÓN IMPORTANTE</font></h4>"
                    + "<font color=#6b0091>- Revisa la factura, nota de venta o ticket que corresponda al día de hoy.</string></font><br/>"
                    + "<font color=#6b0091>- No dupliques los registros.</font><br/>"
                    + "<font color=#6b0091>- No esta permitido subir gastos de días anteriores.</font><br/>"
                    + "<font color=#6b0091>- Por cada gasto realizado debe subir una foto de factura, nota de venta o ticket.</font>";

            AlertDialog.Builder builder1 = new AlertDialog.Builder(ManageCash.this, R.style.MyDialogTheme);
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
            nComprobante = findViewById(R.id.comprobante_input);
            description = findViewById(R.id.descripcion_input);
            amount = findViewById(R.id.amount_input);
            comprobanteLayout = findViewById(R.id.comprobanteLayout);
            amountLayout = findViewById(R.id.amountLayout);
            descripcionLayout = findViewById(R.id.descripcionLayout);
            detail_list = findViewById(R.id.detail_list);
            spinnerTypeDocument = findViewById(R.id.spinner_type_document);
            ImageButton ib_back = findViewById(R.id.ib_back);
            ib_back.setOnClickListener(v -> onBackPressed());
            detail_list.setOnClickListener(this);
            checkdetaillist();

            TextView txt_header = findViewById(R.id.txt_header);
            txt_header.setText("Movimiento caja #" + cashId);
            uploadImage = findViewById(R.id.IdProf);
            cameraUploadButton = findViewById(R.id.upload_button_from_camera);
            galleryUploadButton = findViewById(R.id.upload_button_from_gallery);
            uploadButton = findViewById(R.id.upload_button_save);

            cameraUploadButton.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(
                        ManageCash.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            ManageCash.this,
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
                            //ManageCash.this.finish();
                        }
                    //}
                }
            });

            uploadButton.setOnClickListener(v -> {
                dialog.show();
                try {
                    if (selectType == null) {
                        Toast.makeText(ManageCash.this,"Seleccione una imagen",Toast.LENGTH_LONG).show();
                        dialog.hide();
                    } else {
                        if (selectType.equalsIgnoreCase("camera")) {
                            if(currentPhotoPath == null) {
                                Toast.makeText(ManageCash.this,"Seleccione una imagen",Toast.LENGTH_LONG).show();
                                dialog.hide();
                            } else {
                                uploadOrderImage();
                            }
                        } else {
                            if(bitmap == null) {
                                Toast.makeText(ManageCash.this,"Seleccione una imagen",Toast.LENGTH_LONG).show();
                                dialog.hide();
                            } else {
                                uploadOrderImage();
                            }
                        }
                    }
                    validateTextEdit();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            });
            galleryUploadButton.setOnClickListener(v -> selectImage());
        } catch (Exception e) {
            //
        }
    }

    private void checkdetaillist() {
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "riderGetManageCash.php";
        Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, hp,
                response -> {
                    //hiding the progressbar after completion
                    try {
                        JSONObject obj = new JSONObject(response);
                        String txt_success = obj.getString("success");
                        String txt_message = obj.getString("message");
                        closingDate = obj.getString("closing_date");
                        String txt_num_rows = obj.getString("num_rows");
                        // Log.e("notaPago: ", closingDate);

                        if (closingDate.equals("null")) {
                            uploadButton.setVisibility(View.VISIBLE);
                        } else {
                            uploadButton.setVisibility(View.GONE);
                        }

                        if(txt_success.equals("1")) {
                            getTypeDocument();
                            int num_rows = Integer.parseInt(txt_num_rows);
                            if (num_rows > 0) {
                                detail_list.setVisibility(View.VISIBLE);
                            } else {
                                detail_list.setVisibility(View.GONE);
                            }
                        }
                        if(txt_success.equals("0")){
                            Toast.makeText(ManageCash.this, txt_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        // Log.d("Error.Response", error.toString());
                        String message = null;
                        if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(ManageCash.this, R.style.MyDialogTheme);
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
                params.put("cash_id", cashId);
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
        RequestQueue requestQueue = Volley.newRequestQueue(ManageCash.this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }
    private void getTypeDocument() {

        String hp = getString(R.string.link) + getString(R.string.servicepath) + "riderGetTypeDocument.php";
        // Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, hp,
                response -> {
                    //hiding the progressbar after completion
                    // Log.e("Response777", response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        String txt_success = obj.getString("success");
                        TypeDocument = new ArrayList<>();
                        if(txt_success.equals("1")) {
                            // payment method
                            JSONArray t_data = obj.getJSONArray("type_document");
                            // Log.e("payment_method", t_data +" - "+ txt_success);
                            paymentMethodGetSet t_pay_met;
                            String[] array_pay_met = new String[t_data.length()];

                            for (int i = 0; i < t_data.length(); i++) {
                                JSONObject jo_orderDetail = t_data.getJSONObject(i);
                                t_pay_met = new paymentMethodGetSet();
                                t_pay_met.setId(jo_orderDetail.getString("id"));
                                t_pay_met.setPaymentMethod(jo_orderDetail.getString("name_document"));
                                array_pay_met[i] = jo_orderDetail.getString("name_document");
                                TypeDocument.add(t_pay_met);
                            }

                            ArrayAdapter adapter = new ArrayAdapter(ManageCash.this,android.R.layout.simple_spinner_item, array_pay_met);
                            adapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerTypeDocument.setAdapter(adapter);

                            // Asume que tienes un array llamado a_costs que contiene tus opciones
                            spinnerTypeDocument.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    // Verifica la posición seleccionada
                                    // Log.e("closingDate", closingDate);
                                    if (closingDate.equals("null") || closingDate.isEmpty()){
                                        if (position == 0) {
                                            uploadButton.setVisibility(View.GONE);
                                            // Muestra un mensaje usando Toast
                                            Toast.makeText(ManageCash.this, "Selecciona un tipo de movimiento y sube la foto.", Toast.LENGTH_LONG).show();
                                        } else {
                                            uploadButton.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        Toast.makeText(ManageCash.this, "Caja cerrada, no puedes registrar un nuevo movimiento.", Toast.LENGTH_LONG).show();
                                    }
                                    // Log.e("PaymentMethodXXX: ", TypeDocument.get(spinnerTypeDocument.getSelectedItemPosition()).getId());
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });

                        }
                        if(txt_success.equals("0")){
                            Toast.makeText(ManageCash.this, "Error al listar el tipo de documento.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        // Log.d("Error.Response", error.toString());
                        if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(ManageCash.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setCancelable(false);
                            builder1.setMessage("Por favor verifica tu conexión a Internet");
                            builder1.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    getTypeDocument();
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
        RequestQueue requestQueue = Volley.newRequestQueue(ManageCash.this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }
    void uploadOrderImage () throws JSONException, IOException {
        if (ActivityCompat.checkSelfPermission(
                ManageCash.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                ManageCash.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        dialog.show();
        String url = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_cash_photo.php";

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
                .addFormDataPart("cash_id", cashId)
                .addFormDataPart("amount", amount.getText().toString().trim())
                .addFormDataPart("description", description.getText().toString().trim())
                .addFormDataPart("token", deliveryBoyId + NumRandom + cashId + CurrentDate)
                .addFormDataPart("deliveryboy_id", deliveryBoyId)
                .addFormDataPart("comprobante_num", nComprobante.getText().toString().trim())
                .addFormDataPart("type_document", TypeDocument.get(spinnerTypeDocument.getSelectedItemPosition()).getId())
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
                ManageCash.this.runOnUiThread(new Runnable() { @SuppressLint("CommitPrefEdits")
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

    private void validateTextEdit() {
        if (nComprobante.getText().toString().trim().isEmpty()) {
            comprobanteLayout.setError(getString(R.string.err_msg_voucher));
        } else {
            comprobanteLayout.setErrorEnabled(false);
        }

        if (amount.getText().toString().trim().isEmpty()) {
            amountLayout.setError(getString(R.string.err_msg_amount_cash));
        } else {
            amountLayout.setErrorEnabled(false);
        }

        if (description.getText().toString().trim().isEmpty()) {
            descripcionLayout.setError(getString(R.string.err_msg_detail_cash));
        } else {
            descripcionLayout.setErrorEnabled(false);
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