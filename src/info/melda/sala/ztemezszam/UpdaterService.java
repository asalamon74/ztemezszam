package info.melda.sala.ztemezszam;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 *
 * @author salamon
 */
public class UpdaterService extends Service {

    private static final String TAG = "UpdaterService";
    static final int UPDATER_FAIL    = -1;
    static final int UPDATER_NONEED  = 0;
    static final int UPDATER_SUCCESS = 1;
    static final int UPDATER_OBSOLETE = 2;

    public static final String DB_UPDATED_INTENT = "info.melda.sala.DB_UPDATED";
    private Updater updater;
//    private ZTEMezszamApplication application;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        this.application = (ZTEMezszamApplication) getApplication();
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
    private class Updater extends AsyncTask<Void,String,Integer> {

        static final String RECEIVE_ZTEDB_NOTIFICATION = "info.melda.sala.RECEIVE_ZTEDB_UPDATED_NOTIFICATION";

        private DbHelper dbHelper;
        private SQLiteDatabase db;
        private Intent intent;

        Updater() {
            dbHelper = new DbHelper(UpdaterService.this);
            db = dbHelper.getWritableDatabase();
        }

        BufferedReader readURL(String fileName) throws IOException {
            HttpGet httppost = new HttpGet("http://sala.melda.info/mezszam/"+fileName);
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 1000 );
            HttpConnectionParams.setSoTimeout(httpParameters, 2000);
            DefaultHttpClient httpclient = new DefaultHttpClient( httpParameters);
            Log.d(TAG, "before execute ");
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity ht = response.getEntity();
            Log.d(TAG, "after execute ");

            BufferedHttpEntity buf = new BufferedHttpEntity(ht);
            InputStream is = buf.getContent();

            return new BufferedReader(new InputStreamReader(is));
        }

        protected Integer doInBackground(Void... params) {
            Log.d(TAG, "Updater running");
            try {
                BufferedReader r = readURL("conf.csv");
                db.beginTransaction();
                if( DbHelper.processConf(db, r) ) {
                    r = readURL("seasons.csv");
                    DbHelper.processSeasons(db, r);
                    r = readURL("players.csv");
                    DbHelper.processPlayers( db, r );
                    r = readURL("shirts.csv");
                    DbHelper.processShirts( db, r );
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    return UPDATER_SUCCESS;
                } else {
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    return UPDATER_OBSOLETE;
                }
            } catch( Exception e) {
                Log.d(TAG, e.getMessage(), e);
                return UPDATER_FAIL;
            } finally {
                Log.d(TAG, "Updater ran");
            }
        }

        // Called once the background activity has completed
        @Override
        protected void onPostExecute(Integer result) {
            UpdaterService updaterService = UpdaterService.this;
            intent = new Intent( DB_UPDATED_INTENT );
            Bundle b = new Bundle();
            b.putInt("result", result);
            intent.putExtras(b);
            updaterService.sendBroadcast(intent, RECEIVE_ZTEDB_NOTIFICATION);
        }
    }
}
