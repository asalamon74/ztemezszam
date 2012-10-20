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

        private DbHelper dbHelper;
        private SQLiteDatabase db;
        private Intent intent;

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
                db.execSQL("delete from season");
                while ((line = r.readLine()) != null) {
                    //Log.d( TAG, "seasonline: "+line);
                    StringTokenizer st = new StringTokenizer( line, "," );
                    Object[] season = new Object[2];
                    season[0] = st.nextToken();
                    season[1] = st.nextToken();
                    db.execSQL("insert into season (season_id, season_name) values (?,?)",season);
                }
                r = readURL("players.csv");
                db.execSQL("delete from player");
                while ((line = r.readLine()) != null) {
                    //Log.d( TAG, "playerline: "+line);
                    StringTokenizer st = new StringTokenizer( line, "," );
                    //Log.d( TAG, "tokennum: "+st.countTokens());
                    Object[] player = new Object[2];
                    player[0] = st.nextToken();
                    player[1] = st.nextToken();
                    db.execSQL("insert into player (player_id, player_name) values (?, ?)", player);
                }
                r = readURL("shirts.csv");
                db.execSQL("delete from shirt");
                while ((line = r.readLine()) != null) {
                    //Log.d( TAG, "shirtline: "+line);
                    StringTokenizer st = new StringTokenizer( line, "," );
                    //Log.d( TAG, "tokennum: "+st.countTokens());
                    String[] shirt = new String[3];
                    shirt[0] = st.nextToken();
                    shirt[1] = st.nextToken();
                    shirt[2] = st.nextToken();
                    int pos = shirt[1].indexOf("-");
                    if( pos == -1 ) {
                        //Log.d( TAG, "single shirt insert");
                        db.execSQL("insert into shirt (player_id, season_id, shirt_number) values (?,?,?)", shirt);
                    } else {
                        int seasonIdFrom = Integer.parseInt(shirt[1].substring(0, pos));
                        int seasonIdTo = Integer.parseInt(shirt[1].substring(pos+1));
                        int seasonId = seasonIdFrom;
                        while( seasonId <= seasonIdTo ) {
                            shirt[1] = ""+seasonId;
                            //Log.d( TAG, "multi shirt insert");
                            db.execSQL("insert into shirt (player_id, season_id, shirt_number) values (?,?,?)", shirt);
                            ++seasonId;
                        }
                    }
                }
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
            updaterService.sendBroadcast(intent);
        }
    }
}
