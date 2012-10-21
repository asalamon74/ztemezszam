/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.melda.sala.ztemezszam;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author salamon
 */
public class UpdaterService extends Service {

    private static final String TAG = "UpdaterService";
    public static final String DB_UPDATED_INTENT = "info.melda.sala.DB_UPDATED";
    private Updater updater;
    private ZTEMezszamApplication application;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.application = (ZTEMezszamApplication) getApplication();
        Log.d(TAG, "onCreated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        this.updater = new Updater();
        this.updater.execute();
        Log.d(TAG, "onStarted");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        this.updater.cancel(false);
        this.updater = null;
        Log.d(TAG, "onDestroyed");
    }

    /**
     * Thread that performs the actual update from the online service
     */
    private class Updater extends AsyncTask<String,String,String> {

        static final String RECEIVE_ZTEDB_NOTIFICATION = "info.melda.sala.RECEIVE_ZTEDB_UPDATED_NOTIFICATION";
        private DbHelper dbHelper;
        private SQLiteDatabase db;
        private Intent intent;

        Updater() {
            dbHelper = new DbHelper(UpdaterService.this);
            db = dbHelper.getWritableDatabase();
        }

        BufferedReader readURL(String fileName) throws IOException {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httppost = new HttpGet("http://sala.melda.info/mezszam/"+fileName);
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity ht = response.getEntity();

            BufferedHttpEntity buf = new BufferedHttpEntity(ht);
            InputStream is = buf.getContent();

            return new BufferedReader(new InputStreamReader(is));
        }

        protected String doInBackground(String... params) {
            Log.d(TAG, "Updater running");
            try {
                BufferedReader r = readURL("seasons.csv");
                String line;
                db.beginTransaction();
                DbHelper.processSeasons(db, r);
                r = readURL("players.csv");
                DbHelper.processPlayers( db, r );
                r = readURL("shirts.csv");
                DbHelper.processShirts( db, r );
                db.setTransactionSuccessful();
                db.endTransaction();

//                Thread.sleep(Long.MAX_VALUE);
            } catch( Exception e) {
                Log.d(TAG, "Exception" + e);
            }
            Log.d(TAG, "Updater ran");
            return null;
        }

        // Called once the background activity has completed
        @Override
        protected void onPostExecute(String result) {
            UpdaterService updaterService = UpdaterService.this;
            intent = new Intent( DB_UPDATED_INTENT);
            updaterService.sendBroadcast(intent, RECEIVE_ZTEDB_NOTIFICATION);
        }
    }
}
