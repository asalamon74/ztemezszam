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
import android.widget.Toast;

/**
 *
 * @author salamon
 */
public abstract class BaseActivity extends Activity {
    private DbHelper dbHelper;
    protected SQLiteDatabase db;
    private UpdateReceiver receiver;
    private IntentFilter filter;
    protected SimpleCursorAdapter adapter;
    protected ListView list;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // Connect to database
        dbHelper = new DbHelper(this);
        db = dbHelper.getReadableDatabase();
        receiver = new UpdateReceiver();
        filter = new IntentFilter( UpdaterService.DB_UPDATED_INTENT );
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, filter );
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

    protected abstract Cursor getCursor();

    class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.changeCursor(getCursor());
            adapter.notifyDataSetChanged();
            Toast.makeText( BaseActivity.this, "Adatbázis frissítve", Toast.LENGTH_LONG).show();
            Log.d("UpdateReceiver", "onReceived");
            //titleSeason = (TextView) findViewById(R.id.titleSeason);
            //titleSeason.setText("aaaaaa");
        }
    }

}
