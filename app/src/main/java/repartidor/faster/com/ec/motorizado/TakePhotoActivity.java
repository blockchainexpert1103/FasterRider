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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import repartidor.faster.com.ec.R;
import repartidor.faster.com.ec.utils.Config;


public class TakePhotoActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "Upload Image";
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private int PICK_IMAGE_REQUEST = 100;
    private String orderNo, deliveryBoyId;
    private String regId, currentPhotoPath;
    private Integer NumRandom;
    private Button input_status, detail_list;
    private TextInputEditText travel_history_detail_edit;
    ImageView uploadImage;
    Button cameraUploadButton;
    Button galleryUploadButton;
    Button uploadButton;
    Bitmap bitmap;
    Uri filePath;
    private ProgressDialog dialog;
    private CharSequence CurrentDate;

    String selectedFilePath;
    double lat, lon;
    LocationManager locationManager;
    private String selectType;

    private static final int REQUEST_CODE_CAMERA_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        Intent i = getIntent();
        orderNo = i.getStringExtra("OrderNo");
        deliveryBoyId = i.getStringExtra("DeliveryBoyId");
        displayFirebaseRegId();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Cargando la foto. Por favor espere...");
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.detail_list) {
            Intent k = new Intent(TakePhotoActivity.this, TakePhotoList.class);
            k.putExtra("OrderNo", orderNo);
            k.putExtra("DeliveryBoyId", deliveryBoyId);
            startActivity(k);
        }
    }

    private void random(){
        final int min = 1;
        final int max = 999;
        NumRandom = new Random().nextInt((max - min) + 1) + min;
        Date d = new Date();
        CurrentDate = DateFormat.format("mmss", d.getTime());
    }

    private void initView() {

        detail_list =  findViewById(R.id.detail_list);
        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(v -> onBackPressed());
        detail_list.setOnClickListener(this);
        checkdetaillist();

        TextView txt_header = findViewById(R.id.txt_header);
        txt_header.setText("Foto en la "+getString(R.string.txt_order_no) + orderNo);
        uploadImage = findViewById(R.id.IdProf);
        cameraUploadButton = findViewById(R.id.upload_button_from_camera);
        //galleryUploadButton = findViewById(R.id.upload_button_from_gallery);
        uploadButton = findViewById(R.id.upload_button_id);
        travel_history_detail_edit =  findViewById(R.id.travel_history_detail_edit);

        cameraUploadButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    TakePhotoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        TakePhotoActivity.this,
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
                        //TakePhotoActivity.this.finish();
                    }
                //}
            }
        });

        uploadButton.setOnClickListener(v -> {
            dialog.show();
            try {
                if (selectType == null) {
                    Toast.makeText(TakePhotoActivity.this,"Seleccione una imagen",Toast.LENGTH_LONG).show();
                    dialog.hide();
                } else {
                    if (selectType.equalsIgnoreCase("camera")) {
                        if(currentPhotoPath == null) {
                            Toast.makeText(TakePhotoActivity.this,"Seleccione una imagen",Toast.LENGTH_LONG).show();
                            dialog.hide();
                        } else {
                            uploadOrderImage();
                        }
                    } else {
                        if(bitmap == null) {
                            Toast.makeText(TakePhotoActivity.this,"Seleccione una imagen",Toast.LENGTH_LONG).show();
                            dialog.hide();
                        } else {
                            uploadOrderImage();
                        }
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        });
        /*galleryUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });*/
    }

    private void checkdetaillist() {
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_get_photo_list.php?";
        Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, hp,
                response -> {
                    //hiding the progressbar after completion
                    try {
                        JSONObject obj = new JSONObject(response);
                        String txt_success = obj.getString("success");
                        String txt_message = obj.getString("message");
                        String txt_rows = obj.getString("num_row");
                        if(txt_success.equals("1")) {
                            detail_list.setVisibility(View.VISIBLE);
                            int numRows = Integer.parseInt(txt_rows);
                            if (numRows < 2) {
                                uploadButton.setVisibility(View.VISIBLE);
                            } else {
                                uploadButton.setVisibility(View.GONE);
                            }
                        }
                        if(txt_success.equals("0")){
                            Toast.makeText(TakePhotoActivity.this, txt_message, Toast.LENGTH_SHORT).show();
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
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(TakePhotoActivity.this, R.style.MyDialogTheme);
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
        RequestQueue requestQueue = Volley.newRequestQueue(TakePhotoActivity.this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }

    void uploadOrderImage () throws JSONException, IOException {
        if (ActivityCompat.checkSelfPermission(
                TakePhotoActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                TakePhotoActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        dialog.show();
        String url = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_history_photo.php?";

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

        final MediaType MEDIA_TYPE=MediaType.parse("image/png");
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("photo", "profile.png", RequestBody.create(MEDIA_TYPE, sourceFile))
                .addFormDataPart("order_id", orderNo)
                .addFormDataPart("deliveryboy_id", deliveryBoyId)
                .addFormDataPart("details", travel_history_detail_edit.getText().toString().trim())
                .addFormDataPart("lon", String.valueOf(lon))
                .addFormDataPart("lat", String.valueOf(lat))
                .addFormDataPart("num_random", deliveryBoyId + NumRandom + orderNo + CurrentDate)
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
                TakePhotoActivity.this.runOnUiThread(new Runnable() { @SuppressLint("CommitPrefEdits")
                public void run() {
                    if (response.code() == 200) {
                        dialog.dismiss();
                        //Toast.makeText(getApplicationContext(), "La foto se cargó correctamente.", Toast.LENGTH_SHORT).show();
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