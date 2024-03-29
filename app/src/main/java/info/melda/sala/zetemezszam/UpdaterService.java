package info.melda.sala.zetemezszam;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.NonNull;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class UpdaterService extends Service {

    private static final String TAG = "UpdaterService";
    static final int UPDATER_FAIL    = -1;
    //static final int UPDATER_NONEED  = 0;
    static final int UPDATER_SUCCESS = 1;
    static final int UPDATER_OBSOLETE = 2;

    public static final String DB_UPDATED_INTENT = "info.melda.sala.DB_UPDATED";
    private static final String URL_PLAYERINFO = "https://sala.melda.info/mezszam/";
    private static final String URL_MLSZ_PHOTO_TEMPLATE = "https://adatbank.mlsz.hu/img/SzemelyFoto/Foto/%d/%d.%s";
    private static final String[] PHOTO_EXTENSIONS = {"jpg", "png"};
    private Updater updater;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        this.updater = new Updater(this);
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
    private static class Updater extends AsyncTask<Void,String,Integer> {

        static final String RECEIVE_ZTEDB_NOTIFICATION = "info.melda.sala.RECEIVE_ZTEDB_UPDATED_NOTIFICATION";

        private final WeakReference<UpdaterService> updaterServiceReference;
        private final SQLiteDatabase db;

        Updater(UpdaterService updaterService) {
            this.updaterServiceReference = new WeakReference<>(updaterService);
            DbHelper dbHelper = new DbHelper(updaterService);
            db = dbHelper.getWritableDatabase();
        }

        BufferedReader readURL(String fileName) throws IOException {
            InputStream is = readURLStream(URL_PLAYERINFO + fileName);
            return new BufferedReader(new InputStreamReader(is));
        }

        @NonNull
        private InputStream readURLStream(String urlString) throws IOException {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(1000 );
            urlConnection.setReadTimeout(2000);

            return new BufferedInputStream(urlConnection.getInputStream());
        }

        byte[] readBytes(InputStream inputStream) throws IOException {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        }

        protected Integer doInBackground(Void... params) {
            Log.d(TAG, "Updater running");
            BufferedReader confReader = null;
            BufferedReader seasonsReader = null;
            BufferedReader playersReader = null;
            BufferedReader shirtsReader = null;
            try {
                confReader = readURL("conf.csv");
                db.beginTransaction();
                if( DbHelper.processConf(db, confReader) ) {
                    seasonsReader = readURL("seasons.csv");
                    DbHelper.processSeasons(db, seasonsReader);
                    playersReader = readURL("players.csv");
                    DbHelper.processPlayers( db, playersReader );
                    shirtsReader = readURL("shirts.csv");
                    DbHelper.processShirts( db, shirtsReader );
                    updatePhotos(db);
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
                closeReader(confReader);
                closeReader(seasonsReader);
                closeReader(playersReader);
                closeReader(shirtsReader);
                Log.d(TAG, "Updater ran");
            }
        }

        private void closeReader(Reader reader) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot close reader", e);
                }
            }
        }

        private void updatePhotos(SQLiteDatabase db) {
            List<Integer> playersWithMissingPhotos = getPlayersWithMissingPhoto(db);
            for (Integer playerMlszPhotoId : playersWithMissingPhotos) {
                Log.v(TAG, "Updating photo for "+playerMlszPhotoId);
                String[] urls = photoURLs(playerMlszPhotoId);
                for (String url : urls) {
                    Log.v(TAG, "url:"+url);
                    byte[] imageJpeg = getImage(url);
                    if (imageJpeg != null) {
                        writePlayerPhotoIntoDB(db, playerMlszPhotoId, imageJpeg);
                        break;
                    }
                }
            }
        }

        private byte[] getImage(String url) {
            try {
                InputStream is = readURLStream(url);
                byte[] imageJpeg = readBytes(is);
                Log.v(TAG, "Downloaded image size:"+imageJpeg.length);
                return imageJpeg;
            } catch (IOException e) {
                // missing photo, not a big problem
                Log.v(TAG, "Unable to download photo", e);
                return null;
            }
        }

        private void writePlayerPhotoIntoDB(SQLiteDatabase db, int playerMlszPhotoId, byte[] imageJpeg) {
            SQLiteStatement updateStmt = db.compileStatement("update player set player_photo = ? where player_mlsz_photo_id = ?");
            updateStmt.clearBindings();
            updateStmt.bindBlob(1, imageJpeg);
            updateStmt.bindLong(2, playerMlszPhotoId);
            int affectedRows = updateStmt.executeUpdateDelete();
            Log.v(TAG, "updated rows:"+affectedRows);
        }

        private List<Integer> getPlayersWithMissingPhoto(SQLiteDatabase db) {
            Log.v(TAG, "Getting list of players with missing photo");
            List<Integer> playerIds = new ArrayList<>();
            Cursor cursor = db.rawQuery("select player_mlsz_photo_id, player_photo from player where player_photo is null and coalesce(player_mlsz_photo_id,0) != 0", null);
            while (cursor.moveToNext()) {
                Log.v(TAG, "Checking "+cursor.getInt(0));
                // player_photo is null is not enough
                byte[] playerImageByteArray = cursor.getBlob(1);
                if (playerImageByteArray == null) {
                    Log.v(TAG, "photo is null");
                    playerIds.add(cursor.getInt(0));
                }
            }
            cursor.close();
            Log.v(TAG, String.format("Found %d players with missing photo", playerIds.size()));
            return playerIds;
        }

        @SuppressLint("DefaultLocale")
        private String[] photoURLs(int playerId) {
            int playerDir = (int)Math.ceil(0.001 * playerId);
            String[] urls = new String[PHOTO_EXTENSIONS.length];
            int urlIndex = 0;
            for (String extension : PHOTO_EXTENSIONS) {
                urls[urlIndex++] = String.format(URL_MLSZ_PHOTO_TEMPLATE, playerDir, playerId, extension);
            }
            return urls;
        }

        // Called once the background activity has completed
        @Override
        protected void onPostExecute(Integer result) {
            Intent intent = new Intent( DB_UPDATED_INTENT );
            Bundle b = new Bundle();
            b.putInt("result", result);
            intent.putExtras(b);
            UpdaterService updaterService = updaterServiceReference.get();
            updaterService.sendBroadcast(intent, RECEIVE_ZTEDB_NOTIFICATION);
        }
    }
}
