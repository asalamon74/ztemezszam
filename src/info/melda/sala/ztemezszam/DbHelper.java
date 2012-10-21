/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.melda.sala.ztemezszam;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 *
 * @author salamon
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbHelper";
    private static final String DB_NAME = "ztemezszam.db";
    private static final int DB_VERSION = 1;
    private Context context;

    // Constructor
    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d( TAG, "onCreate" );
        db.execSQL("create table season ( season_id int primary key, season_name text )");
        db.execSQL("create table player ( player_id int primary key, player_name text )");
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
            /*String line;
            while ( (line = reader.readLine()) != null) {
                Log.d( TAG, "line: "+line);
            }*/
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

    static void processPlayers(SQLiteDatabase db, BufferedReader reader) throws IOException {
        db.execSQL("delete from player");
        String line;
        StringTokenizer st;
        while ((line = reader.readLine()) != null) {
            //Log.d( TAG, "playerline: "+line);
            st = new StringTokenizer(line, ",");
            //Log.d( TAG, "tokennum: "+st.countTokens());
            Object[] player = new Object[2];
            player[0] = st.nextToken();
            player[1] = st.nextToken();
            db.execSQL("insert into player (player_id, player_name) values (?, ?)", player);
        }

    }

    static void processSeasons(SQLiteDatabase db, BufferedReader reader) throws IOException {
        db.execSQL("delete from season");
        String line;
        StringTokenizer st;
        while ((line = reader.readLine()) != null) {
            //Log.d( TAG, "seasonline: "+line);
            st = new StringTokenizer(line, ",");
            Object[] season = new Object[2];
            season[0] = st.nextToken();
            season[1] = st.nextToken();
            db.execSQL("insert into season (season_id, season_name) values (?,?)", season);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        // drop the database and recreate
        db.execSQL("drop table if exists season");
        onCreate(db);
    }

}
