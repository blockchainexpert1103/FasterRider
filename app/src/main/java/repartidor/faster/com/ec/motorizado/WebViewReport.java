package repartidor.faster.com.ec.motorizado;

// Importar las clases necesarias
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.Random;

import repartidor.faster.com.ec.R;

// Clase principal de la actividad
public class WebViewReport extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "Fooddelivery";
    // Variable para la WebView
    private WebView webView;
    private String deliveryBoyId;
    private Integer NumRandom;
    private CharSequence CurrentDate;
    ImageView SinConexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        SinConexion = findViewById(R.id.imagenSinConexion);
        // Obtener el id del repartidor
        SharedPreferences prefsDeliveryBoyId = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        deliveryBoyId = prefsDeliveryBoyId.getString("DeliveryUserId", null);
        Log.e("deliveryBoyId: ", deliveryBoyId);
        MyWebView();
        random();
    }
    private void MyWebView() {
        // Obtener la referencia de la WebView desde el layout
        webView = findViewById(R.id.webview);

        // Habilitar JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Verificar si hay conexión a Internet
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            //SinConexion.setVisibility(View.GONE);
            // Cargar la página web
            String pdfUrl = "https://app.faster.com.ec/reports/RiderReport.php?token=" + deliveryBoyId;
            webView.loadUrl(pdfUrl);
        } else {
            SinConexion.setVisibility(View.VISIBLE);
            // Mostrar un mensaje de error
            Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
        }

        // Asignar el WebViewClient al WebView
        webView.setWebViewClient(new MyWebViewClient());

        // Empieza la descarga del reporte
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            // Crear una solicitud de descarga utilizando DownloadManager
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

            // Establecer el título y la descripción del archivo descargado
            request.setTitle("ReporteEntregasFasterRider_" + CurrentDate + "_" + NumRandom + "");
            request.setDescription("Descargando archivo...");

            // Establecer el destino de la descarga (por ejemplo, el directorio de descargas del dispositivo)
            request.setDestinationInExternalPublicDir(Environment.
                    DIRECTORY_DOWNLOADS, "ReporteEntregasFasterRider_" + CurrentDate + "_" + NumRandom + ".pdf");

            // Obtener el servicio de descarga del sistema
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            // Iniciar la descarga y obtener el ID de la descarga
            long downloadId = downloadManager.enqueue(request);

            // Mostrar un mensaje o realizar alguna acción adicional si es necesario
            Toast.makeText(getApplicationContext(), "Reporte PDF decargado, revisa en tu carpeta 'Descargas'.", Toast.LENGTH_SHORT).show();
        });
    }

    // Clase WebViewClient
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Cargar el enlace dentro del WebView
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }
    private void initView() {
        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(v -> onBackPressed());
    }
    private void random(){
        final int min = 1;
        final int max = 999;
        NumRandom = new Random().nextInt((max - min) + 1) + min;
        Date d = new Date();
        CurrentDate = DateFormat.format("yyyyMMddHHmmss", d.getTime());
    }
}

