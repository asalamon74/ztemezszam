/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.melda.sala.ztemezszam;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
        db.execSQL("insert into season ( season_id, season_name) values ( '64', '2012/13 ősz' )");
        db.execSQL("create table player ( player_id int primary key, player_name text )");
        db.execSQL("insert into player ( player_id, player_name) values ( '2', 'Kocsárdi Gergely' )");
        db.execSQL("insert into player ( player_id, player_name) values ( '85', 'Vlaszák Géza' )");
        db.execSQL("create table shirt ( shirt_id int primary key, player_id int, season_id int, shirt_number int )");
        db.execSQL("insert into shirt ( shirt_id, player_id, season_id, shirt_number) values ( 1, 2, 64, 2)");
        db.execSQL("insert into shirt ( shirt_id, player_id, season_id, shirt_number) values ( 2, 85, 64, 1)");
        //db.execSQL("create table player ( player_id int primary key, player_name text )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        // drop the database and recreate
        db.execSQL("drop table if exists season");
        onCreate(db);
    }

}
