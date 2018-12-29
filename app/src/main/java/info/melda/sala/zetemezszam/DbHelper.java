package info.melda.sala.zetemezszam;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbHelper";
    private static final String DB_NAME = "ztemezszam.db";
    private static final int DB_VERSION = 3;
    private final Context context;
    private static final SimpleDateFormat idf = new SimpleDateFormat("yyyy-MMM-dd", Locale.US);

    DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d( TAG, "onCreate" );
        db.execSQL("create table conf ( conf_id int primary key, csv_version int )");
        db.execSQL("create table season ( season_id int primary key, season_name text )");
        db.execSQL("create table player ( player_id int primary key, player_name text, player_dob real, player_mlsz_photo_id int, player_photo blob )");
        db.execSQL("create table shirt ( shirt_id int primary key, player_id int, season_id int, shirt_number int )");
        // read data from resource files
        try {
            Resources res = context.getResources();
            InputStream in_s = res.openRawResource(R.raw.shirts);
            BufferedReader reader = new BufferedReader( new InputStreamReader(in_s));
            processShirts( db, reader );

            in_s = res.openRawResource(R.raw.seasons);
            reader = new BufferedReader( new InputStreamReader(in_s));
            processSeasons( db, reader );

            in_s = res.openRawResource(R.raw.players);
            reader = new BufferedReader( new InputStreamReader(in_s));
            processPlayers( db, reader );

            in_s = res.openRawResource(R.raw.conf);
            reader = new BufferedReader( new InputStreamReader(in_s));
            processConf( db, reader );

        } catch ( IOException e ) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
        
    static void processShirts(SQLiteDatabase db, BufferedReader reader) throws IOException {
        db.execSQL("delete from shirt");
        String line;
        StringTokenizer st;
        while ((line = reader.readLine()) != null) {
            //Log.d( TAG, "shirtline: "+line);
            st = new StringTokenizer(line, ",");
            //Log.d( TAG, "tokennum: "+st.countTokens());
            String[] shirt = new String[3];
            shirt[0] = st.nextToken();
            shirt[1] = st.nextToken();
            shirt[2] = st.nextToken();
            int pos = shirt[1].indexOf("-");
            if (pos == -1) {
                //Log.d( TAG, "single shirt insert");
                db.execSQL("insert into shirt (player_id, season_id, shirt_number) values (?,?,?)", shirt);
            } else {
                int seasonIdFrom = Integer.parseInt(shirt[1].substring(0, pos));
                int seasonIdTo = Integer.parseInt(shirt[1].substring(pos + 1));
                int seasonId = seasonIdFrom;
                while (seasonId <= seasonIdTo) {
                    shirt[1] = "" + seasonId;
                    //Log.d( TAG, "multi shirt insert");
                    db.execSQL("insert into shirt (player_id, season_id, shirt_number) values (?,?,?)", shirt);
                    ++seasonId;
                }
            }
        }
    }
    
    // http://stackoverflow.com/a/466348/21348
    private static double getJulianFromUnix( int unixSecs ) {
        return ( unixSecs / 86400.0 ) + 2440587.5;
    }

    static void processPlayers(SQLiteDatabase db, BufferedReader reader) throws IOException {
        //db.execSQL("delete from player");
        String line;
        StringTokenizer st;
        Set<Integer> unprocessedIds = getAllPlayerIds(db);
        while ((line = reader.readLine()) != null) {
            // Log.d( TAG, "playerline: "+line);
            st = new StringTokenizer(line, ",");            
            String playerId = st.nextToken();
            String playerName = st.nextToken();
            // dob and mlsz_photo_id are optional
            Double playerDob;
            if( st.countTokens() == 0 ) {
                playerDob = null;
            } else {
                playerDob = getDateOfBirth(st.nextToken());
            }
            String playerMlszPhotoId = null;
            if (st.countTokens() > 0) {
                playerMlszPhotoId = st.nextToken();
            }
            mergeInto(db, playerId, playerName, playerDob, playerMlszPhotoId);
            unprocessedIds.remove(Integer.parseInt(playerId));
        }
        deletePlayersDefinedByIds(db, unprocessedIds);
    }

    private static void deletePlayersDefinedByIds(SQLiteDatabase db, Set<Integer> unprocessedIds) {
        for (Integer playerId : unprocessedIds) {
            db.execSQL("delete from player where player_id = ?", new Object[]{playerId});
        }
    }

    private static Set<Integer> getAllPlayerIds(SQLiteDatabase db) {
        Set<Integer> allIds = new HashSet<>();
        Cursor cursor = db.rawQuery("select player_id from player", null);
        while (cursor.moveToNext()) {
            allIds.add(cursor.getInt(0));
        }
        cursor.close();
        return allIds;
    }

    private static void mergeInto(SQLiteDatabase db, String playerId, String playerName, Double playerDob, String playerMlszPhotoId) {
        Cursor cursor = db.rawQuery("select player_id, player_photo, player_mlsz_photo_id from player where player_id = ?", new String[]{playerId});
        boolean hasPlayerInfo = cursor.moveToNext();
        boolean hasPhoto;
        int oldPlayerMlszPhotoId;
        if (hasPlayerInfo) {
            hasPhoto = cursor.getBlob(1) != null;
            oldPlayerMlszPhotoId = cursor.getInt(2);
        } else {
            hasPhoto = false;
            oldPlayerMlszPhotoId = 0;
        }
        cursor.close();
        Object[] player = new Object[4];
        if (hasPlayerInfo) {
            boolean noLongerHasMlszPhotoId = oldPlayerMlszPhotoId > 0 && playerMlszPhotoId != null;
            int newPlayerMlszPhotoId = playerMlszPhotoId == null ? 0 : Integer.parseInt(playerMlszPhotoId);
            boolean changedMlszPhotoId = oldPlayerMlszPhotoId != newPlayerMlszPhotoId;
            boolean needToDeletePhoto = hasPhoto && (noLongerHasMlszPhotoId || changedMlszPhotoId);
            if (needToDeletePhoto) {
                player[0] = playerName;
                player[1] = playerDob;
                player[2] = playerMlszPhotoId;
                player[3] = playerId;
                db.execSQL("update player set player_name = ?, player_dob = ?, player_mlsz_photo_id = ?, player_photo = null where player_id = ?");
            } else {
                player[0] = playerName;
                player[1] = playerDob;
                player[2] = playerMlszPhotoId;
                player[3] = playerId;
                db.execSQL("update player set player_name = ?, player_dob = ?, player_mlsz_photo_id = ? where player_id = ?");
            }
        } else {
            player[0] = playerId;
            player[1] = playerName;
            player[2] = playerDob;
            player[3] = playerMlszPhotoId;
            db.execSQL("insert into player (player_id, player_name, player_dob, player_mlsz_photo_id) values (?, ?, ?, ?)", player);
        }
    }

    private static Double getDateOfBirth(String token) {
        Double dob;
        try {
            Date dobDate = idf.parse(token);
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(dobDate);
            long millis = gc.getTime().getTime();
            dob = getJulianFromUnix ( (int)(millis / 1000) );
        } catch( ParseException e ) {
            Log.e( TAG, "dob parse exception", e);
            dob = null;
        }
        return dob;
    }

    static void processSeasons(SQLiteDatabase db, BufferedReader reader) throws IOException {
        db.execSQL("delete from season");
        String line;
        StringTokenizer st;
        while ((line = reader.readLine()) != null) {
            Log.d( TAG, "seasonline: "+line);
            st = new StringTokenizer(line, ",");
            Object[] season = new Object[2];
            season[0] = st.nextToken();
            season[1] = st.nextToken();
            db.execSQL("insert into season (season_id, season_name) values (?,?)", season);
        }
    }

    static boolean processConf(SQLiteDatabase db, BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if( line == null ) {
            return false;
        }

        StringTokenizer st = new StringTokenizer(line, ",");
        Object[] conf = new Object[1];
        conf[0] = st.nextToken();
        int newCsvVersion = Integer.parseInt((String)conf[0]);

        Cursor currentConf = db.rawQuery("select csv_version from conf", null);
        boolean success;
        if( !currentConf.moveToFirst() ) {
            db.execSQL("insert into conf (conf_id, csv_version) values (1, ?)", conf);
            success = true;
        } else {
            int currentCsvVersion = currentConf.getInt(0);
            if( currentCsvVersion == newCsvVersion ) {
                db.execSQL("delete from conf");
                db.execSQL("insert into conf (conf_id, csv_version) values (1, ?)", conf);
                success = true;
            } else {
                success = false;
            }
        }
        currentConf.close();
        return success;
    }

    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d( TAG, "upgrading db:"+oldVersion+" -> "+newVersion);
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 2:
                    db.execSQL("alter table player add column player_dob real");
                    break;
                case 3:
                    db.execSQL("alter table player add column player_mlsz_photo_id int");
                    db.execSQL("alter table player add column player_photo blob");
                default:
                    throw new IllegalStateException("onUpgrade() with unknown oldVersion" + oldVersion);
            }
            upgradeTo++;
        }
    }
}
