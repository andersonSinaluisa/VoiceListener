package com.example.talktoblock;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.talktoblock.MainActivity;
import androidx.annotation.Nullable;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class BackgroundSoundService  extends Service implements RecognitionListener {
    private static final String TAG = "SERVICE VOICE BG";
    private TextToSpeech textToSpeech;
    private MediaPlayer player;
    public String url;
    public Context c = this;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        //Toast.makeText(this,"create Service.",Toast.LENGTH_SHORT).show();
        url = obtenetServer();
        Log.i(TAG,"SE CREO EL SERVICIO");
         textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
             @Override
             public void onInit(int status) {
                 textToSpeech.setLanguage(new Locale("spa","ESP"));

             }
         });
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this,"start Service.",Toast.LENGTH_SHORT).show();
        Log.i(TAG, "HA EMPEZADO A HABLAR");
        capturarvoz();
        return START_STICKY;
    }

    public void capturarvoz(){
        SpeechRecognizer speechRecognizer;

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(this);

        Intent voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voice.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
                .getPackage().getName());
        voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voice.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

        speechRecognizer.startListening(voice);
    }
    @Override
    public void onReadyForSpeech(Bundle params) {
        //Toast.makeText(this, params.toString(), Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG, "HA EMPEZADO A HABLAR");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(TAG, String.valueOf(rmsdB));
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(TAG, String.valueOf(buffer));
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG,"TERMINADO DE ESCUCHAR");
    }

    @Override
    public void onError(int error) {
        capturarvoz();
        Log.i(TAG, String.valueOf(error));
    }

    @Override
    public void onResults(Bundle results) {

        /*List<String> BLOQUEAR = Arrays.asList("bloquear", "bloqueate", "bloqueo","ya","auto");
        List<String> APAGAR_WIFI = Arrays.asList("apagar wifi", "wifi", "apagar guaifai","","");
        List<String> ENCENDER_BLUETOOH = Arrays.asList("", "", "","","");
        List<String> ENCEDER_LINTERNA = Arrays.asList("", "", "","","");
        List<String> MODO_AVION = Arrays.asList("", "", "","","");
        List<String> APAGAR_SERVICIO = Arrays.asList("", "", "","","");
        List<String> ABRIR_APP = Arrays.asList("", "", "","","");
        List<String> ABRIR_T = Arrays.asList("", "", "","","");
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String texto = String.valueOf(data.get(0));
        LevenshteinDistance j = new LevenshteinDistance();
        j.setWords("bloquear",texto);
        float evaluate = j.getAfinidad() * 100;
        Toast.makeText(this,texto,Toast.LENGTH_SHORT).show();

        if(BLOQUEAR.contains(texto)){
            lock();
        }else if(APAGAR_WIFI.contains(texto)){

        }*/
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String texto = String.valueOf(data.get(0));
        JSONObject json = new JSONObject();
        try {
            json.put("texto",texto);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String URL_FILE = "http://192.168.1.105:8000/save-text/";//url;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, URL_FILE, json, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                       Log.d(TAG,response.toString());
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d("Response: ", error.toString());

                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
        capturarvoz();
    }

    

    @Override
    public void onPartialResults(Bundle partialResults) {
        //Toast.makeText(this,String.valueOf(partialResults),Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onEvent(int eventType, Bundle params) {
       //Toast.makeText(this,String.valueOf(eventType)+String.valueOf(params),Toast.LENGTH_SHORT).show();
    }
    public void lock() {

        ComponentName cn = new ComponentName(this, AdminReceiver.class);
        DevicePolicyManager mgr = (DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);

        if (mgr.isAdminActive(cn)) {
            Log.i(TAG, "User is an admin!");
            mgr.lockNow();
        }
    }

    @Override
    public void onDestroy() {

        capturarvoz();

    }

    @Override
    public boolean stopService(Intent name) {
        Log.i(TAG,"SE DETUVO EL SERVICIO");
        //Toast.makeText(this,"DETENIDO",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent("com.example.techtrainner");
        sendBroadcast(intent);
        return true;
    }

    @Override
    public void onLowMemory() {
        //Toast.makeText(this,"MEMORIA INSUFICIENTE",Toast.LENGTH_SHORT).show();
        capturarvoz();

        super.onLowMemory();
    }


    public String obtenetServer(){
        Context c = this;
        final String[] url = new String[1];
        String URL_FILE = "https://drive.google.com/uc?id=1ehy0dU7kWED666bbwGKfTC3J-6KcogCe";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL_FILE, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            url[0] = response.getString("url");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(c,error+"",Toast.LENGTH_SHORT).show();

                        Log.d("Response: ", error.getMessage());
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);

        return url[0];
    }
}
