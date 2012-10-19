/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.melda.sala.ztemezszam;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 *
 * @author salamon
 */
public class SeasonActivity extends Activity {

    private static final String TAG = "SeasonActivity";
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private ListView listSeason;
    private TextView titleSeason;
    private Cursor cursor;
    private SimpleCursorAdapter adapter;
    private static final String[] FROM = { "shirt_number", "player_name" };
    private static final int[] TO = { R.id.textId, R.id.textName };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.season);

        listSeason = (ListView) findViewById(R.id.listSeason); 
        titleSeason = (TextView) findViewById(R.id.titleSeason);
        titleSeason.setText("Szezon");

        // Connect to database
        dbHelper = new DbHelper(this);
        db = dbHelper.getReadableDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d( TAG, "before query");
        // Get the data from the database
        //cursor = db.rawQuery("select rowid _id, * from season", null);
        cursor = db.rawQuery("select shirt_id _id, * from shirt, player where shirt.player_id=player.player_id and season_id=64 order by shirt_number", null);
        //cursor = db.rawQuery("select 1 _id, 1 season_id, 'alma' season_name", null);
        Log.d( TAG, "after query");
        // Create the adapter
        adapter = new SimpleCursorAdapter(this, R.layout.season_row, cursor, FROM, TO);
        listSeason.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close the database
        db.close();
    }

    // Called first time user clicks on the menu button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemPrefs:
                startActivity(new Intent(this, PrefsActivity.class));
                break;
            }
        return true;
    }
}
