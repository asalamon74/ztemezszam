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
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 *
 * @author salamon
 */
public abstract class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";
    private DbHelper dbHelper;
    protected SQLiteDatabase db;
    private UpdateReceiver receiver;
    private IntentFilter filter;
    protected SimpleCursorAdapter adapter;
    protected ListView list;
    private static final String SEND_ZTEDB_NOTIFICATION = "info.melda.sala.SEND_ZTEDB_UPDATED_NOTIFICATION";

    private SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {

        
        @Override
        public void onLongPress(MotionEvent e) {
            Log.d( TAG, "onLongPress");
            longPressAction();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d( TAG, "onFling");
            String swipe = "";
            float sensitvity = 50;
            if ((e1.getX() - e2.getX()) > sensitvity) {
                swipe += "Swipe Left\n";
                swipeLeftAction();
            } else if ((e2.getX() - e1.getX()) > sensitvity) {
                swipe += "Swipe Right\n";
                swipeRightAction();
            } else {
                 swipe += "\n";
            }
/*
            if ((e1.getY() - e2.getY()) > sensitvity) {
                swipe += "Swipe Up\n";
            } else if ((e2.getY() - e1.getY()) > sensitvity) {
                swipe += "Swipe Down\n";
            } else {
                swipe += "\n";
            }*/
            Log.d( TAG, "swipe: "+swipe);
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d( TAG, "onDown");
            return true;
        }

    };


    protected GestureDetector gestureDetector  = new GestureDetector(simpleOnGestureListener);

    protected abstract int getLayoutId();
    protected abstract int getListId();

    protected abstract String[] getAdapterFrom();
    protected abstract int[] getAdapterTo();
    protected abstract int getAdapterLayoutRow();

    protected void initDB() {
        // basic implementation does nothing
    }

    protected void swipeRightAction() {
    }
    
    protected void swipeLeftAction() {
    }

    protected void longPressAction() {
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(getLayoutId());
        Log.d( TAG, "onCreate");
        Log.d( TAG, "id:"+getListId());
        list = (ListView) findViewById( getListId() );
        Log.d( TAG, "list:"+list);
        // Connect to database
        dbHelper = new DbHelper(this);
        db = dbHelper.getReadableDatabase();
        initDB();
        receiver = new UpdateReceiver();
        filter = new IntentFilter(UpdaterService.DB_UPDATED_INTENT);

        View swipeView = getWindow().getDecorView();
        Log.d(TAG, "swipView:" + swipeView);
        swipeView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                //Log.d(TAG, "onTouch");
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter = new SimpleCursorAdapter(this, getAdapterLayoutRow(), getCursor(), getAdapterFrom(), getAdapterTo());
        Log.d( TAG, "list:"+list);
        list.setAdapter(adapter);
        registerReceiver(receiver, filter, SEND_ZTEDB_NOTIFICATION, null );
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
                Toast.makeText( this, "Adatbázis frissítése", Toast.LENGTH_SHORT).show();
                startService(new Intent(this, UpdaterService.class));
                break;
            case R.id.itemPlayerList:
                startActivity(new Intent(this, PlayerListActivity.class));
                break;
            case R.id.itemSeasonList:
                startActivity(new Intent(this, SeasonListActivity.class));
                break;
        }
        return true;
    }

    protected abstract Cursor getCursor();

    class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            initDB();
            onResume();
            //adapter.changeCursor(getCursor());
            //adapter.notifyDataSetChanged();
            Toast.makeText( BaseActivity.this, "Adatbázis frissítve", Toast.LENGTH_LONG).show();
            Log.d("UpdateReceiver", "onReceived");
            //titleSeason = (TextView) findViewById(R.id.titleSeason);
            //titleSeason.setText("aaaaaa");
        }
    }


}
