package com.example.talktoblock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "BROADCAST START"+intent.getAction(), Toast.LENGTH_SHORT).show();
        Log.d("BROADCAST","SE INICIO EL SERVICO POR BROADCAST");
        context.startService(new Intent(context, BackgroundSoundService.class));;

    }
}
