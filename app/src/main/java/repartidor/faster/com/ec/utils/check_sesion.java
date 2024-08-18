package repartidor.faster.com.ec.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import repartidor.faster.com.ec.R;
import repartidor.faster.com.ec.motorizado.Splash;


public class check_sesion extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "Fooddelivery";
    private static CountDownTimer countDownTimer = null;

    public void validate_sesion(final Context con, String sucess, String message){
        switch (sucess) {
            case "-2": {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(con, R.style.MyDialogTheme);
                builder1.setTitle("Existe nueva versión de Faster Rider");
                builder1.setIcon(R.mipmap.information);
                builder1.setCancelable(false);
                builder1.setMessage(message);
                builder1.setPositiveButton("Descargar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {
                            Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=repartidor.faster.com.ec"));
                            con.startActivity(viewIntent);

                        } catch (Exception e) {
                            Toast.makeText(con, "No se puede conectar intente nuevamente...",
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
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
            }
            case "-3": {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(con, R.style.MyDialogTheme);
                builder1.setTitle("¡Tu cuenta ha sido inhabilitada!");
                builder1.setIcon(R.mipmap.information);
                builder1.setCancelable(false);
                builder1.setMessage(message);
                builder1.setPositiveButton("Contactar a Soporte", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String toNumber = "+593969764774";
                        toNumber = toNumber.replace("+", "").replace(" ", "");
                        try {
                            Intent sendIntent = new Intent("android.intent.action.MAIN");
                            // sendIntent.setComponent(new ComponentName(“com.whatsapp”, “com.whatsapp.Conversation”));
                            sendIntent.putExtra("jid", toNumber + "@s.whatsapp.net");
                            sendIntent.putExtra(Intent.EXTRA_TEXT, "¡Hola!, deseo recuperar mi cuenta de para generar dinero extra como Rider en Faster.");
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.setPackage("com.whatsapp");
                            sendIntent.setType("text/plain");
                            con.startActivity(sendIntent);
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(con, "WhatsApp no está instalado.", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
                builder1.setNegativeButton("Entendido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        System.exit(0);
                    }
                });
                AlertDialog alert11 = builder1.create();
                try {
                    alert11.show();
                } catch (Exception e) {
                    //
                }

                //}  else if (sucess.equals("-6"))  {
                //pedido pendiente
                //}  else if (sucess.equals("-7"))  {
                //pago pendiente
                break;
            }
            case "-4": {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(con, R.style.MyDialogTheme);
                builder1.setTitle("Información");
                builder1.setCancelable(false);
                builder1.setIcon(R.mipmap.information);
                builder1.setMessage(message);
                builder1.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            /*Intent iv = new Intent(con, Splash.class);
                            iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            con.startActivity(iv);*/
                            SharedPreferences settings = con.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
                            settings.edit().clear().apply();
                            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                                ((ActivityManager) con.getSystemService(ACTIVITY_SERVICE))
                                        .clearApplicationUserData();
                            }
                        } catch (Exception e) {
                            Toast.makeText(con, "No se puede conectar intente nuevamente...",
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
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
            }
            case "-7": {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(con, R.style.MyDialogTheme);
                builder1.setTitle("Información");
                builder1.setCancelable(false);
                builder1.setIcon(R.mipmap.information);
                builder1.setMessage(message);
                builder1.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
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

                break;
            }
            default:
                Toast.makeText(con, message, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public static void reverseTimer(int Seconds, final TextView tv){
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(Seconds * 1000 + 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                if(minutes==0 && seconds<=60){
                    tv.setTextColor(Color.RED);
                }
                tv.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
            }
            @SuppressLint("ResourceType")
            public void onFinish() {
//                tv.setText(Color.BLACK);
                tv.setText("");
            }

        }.start();
    }


    public static void reverseTimerHour(int Seconds, final TextView tv){
        CountDownTimer countDownTimer1 = null;
        if (countDownTimer1 != null) {
            countDownTimer1.cancel();
        }

        countDownTimer1 = new CountDownTimer(Seconds* 1000+1000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000)  % 60;
                int minutes = (int) ((millisUntilFinished / (1000*60)) % 60);
                int hours   = (int) ((millisUntilFinished / (1000*60*60)) % 24);

                if(hours==0 && minutes<=60 && seconds<=60){
                    tv.setTextColor(Color.RED);
                }

                tv.setText(String.format("%02d", hours)+ ":"+
                        String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds));
            }
            public void onFinish() {
                tv.setText("VENCIDO");
                tv.setTextColor(Color.RED);
            }
        }.start();
    }

}
