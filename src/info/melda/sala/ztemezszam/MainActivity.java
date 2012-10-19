package info.melda.sala.ztemezszam;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d( TAG, "onCreate" );
        setContentView(R.layout.main);
        dbHelper = new DbHelper( this );
        db = dbHelper.getWritableDatabase();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // close the database
        db.close();
    }
}
