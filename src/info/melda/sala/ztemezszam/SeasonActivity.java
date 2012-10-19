/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.melda.sala.ztemezszam;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

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
    private SimpleCursorAdapter adapter;
    private static final String[] FROM = { "shirt_number", "player_name" };
    private static final int[] TO = { R.id.textId, R.id.textName };
    private UpdateReceiver receiver;
    private IntentFilter filter;
    private int seasonId=64;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.season);

        listSeason = (ListView) findViewById(R.id.listSeason); 
        
        // Connect to database
        dbHelper = new DbHelper(this);
        db = dbHelper.getReadableDatabase();

        receiver = new UpdateReceiver();
        filter = new IntentFilter( UpdaterService.DB_UPDATED_INTENT );
    }

    private Cursor getCursor() {
        return db.rawQuery("select shirt_id _id, * from shirt, player where shirt.player_id=player.player_id and season_id="+seasonId+" order by shirt_number", null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor seasonNameCursor = db.rawQuery("select season_name from season where season_id="+seasonId, null);
        String seasonName;
        if( seasonNameCursor.moveToFirst() ) {
            seasonName = seasonNameCursor.getString(0);
        } else {
            seasonName = "????";
        }
        titleSeason = (TextView) findViewById(R.id.titleSeason);
        titleSeason.setText(seasonName);
        adapter = new SimpleCursorAdapter(this, R.layout.season_row, getCursor(), FROM, TO);
        listSeason.setAdapter(adapter);
        registerReceiver(receiver, filter );
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
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
            case R.id.itemManualSync:
                startService(new Intent(this, UpdaterService.class));
                break;
            }
        return true;
    }

    class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.changeCursor(getCursor());
            adapter.notifyDataSetChanged();
            Toast.makeText( SeasonActivity.this, "Adatbázis frissítve", Toast.LENGTH_LONG).show();
            Log.d("UpdateReceiver", "onReceived");
            //titleSeason = (TextView) findViewById(R.id.titleSeason);
            //titleSeason.setText("aaaaaa");
        }
    }

}
