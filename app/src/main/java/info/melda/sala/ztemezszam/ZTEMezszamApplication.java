/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.melda.sala.ztemezszam;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 *
 */
public class ZTEMezszamApplication extends Application {
    private static final String TAG = ZTEMezszamApplication.class.getSimpleName();
    private SharedPreferences prefs;
    
    @Override
    public void onCreate() {
        super.onCreate();
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if( isStartSync() ) {
            Toast.makeText( this, "Adatbázis frissítése", Toast.LENGTH_SHORT).show();
            startService(new Intent(this, UpdaterService.class));
        }
        Log.i(TAG, "onCreated");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i(TAG, "onTerminated");
    }

    public boolean isStartSync() {
        return this.prefs.getBoolean("startsync", false);
    }
    
}
