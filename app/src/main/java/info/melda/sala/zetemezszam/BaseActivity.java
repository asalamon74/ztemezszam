package info.melda.sala.zetemezszam;

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
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    protected static final String UNKNOWN = "????";
    private static final String TAG = "BaseActivity";
    public static final String BUNDLE_KEY_PLAYER_ID = "playerId";
    private static final float SWIPE_SENSITIVITY = 50;
    SQLiteDatabase db;
    private UpdateReceiver receiver;
    private IntentFilter filter;
    SimpleCursorAdapter adapter;
    AbsListView list;
    private static final String SEND_ZTEDB_NOTIFICATION = "info.melda.sala.SEND_ZTEDB_UPDATED_NOTIFICATION";

    private final SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {
        
        @Override
        public void onLongPress(MotionEvent e) {
            Log.d( TAG, "onLongPress");
            longPressAction();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d( TAG, "onFling");
            String swipe = "";
            boolean ret = false;
            float xDiff = e1.getX() - e2.getX();
            float yDiff = e1.getY() - e2.getY();
            if (Math.abs(xDiff) > Math.abs(yDiff)) {
                if (xDiff > SWIPE_SENSITIVITY) {
                    swipe += "Swipe Left\n";
                    swipeLeftAction();
                    ret = true;
                } else if (xDiff < -SWIPE_SENSITIVITY) {
                    swipe += "Swipe Right\n";
                    swipeRightAction();
                    ret = true;
                } else {
                    swipe += "\n";
                }
            } else {
                if (yDiff > SWIPE_SENSITIVITY) {
                    swipe += "Swipe Up\n";
                    swipeUpAction();
                    ret = true;
                } else if (yDiff < -SWIPE_SENSITIVITY) {
                    swipe += "Swipe Down\n";
                    swipeDownAction();
                    ret = true;
                } else {
                    swipe += "\n";
                }
            }
            Log.d( TAG, "swipe: "+swipe);
            return ret;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d( TAG, "onDown");
            return true;
        }

    };

    protected abstract int getLayoutId();
    protected abstract int getListId();

    protected abstract String[] getAdapterFrom();
    protected abstract int[] getAdapterTo();
    protected abstract int getAdapterLayoutRow();

    void initDB() {
    }

    void swipeRightAction() {
    }

    void swipeLeftAction() {
    }

    void swipeUpAction() {
    }

    void swipeDownAction() {
        updateDatabase();
    }

    void longPressAction() {
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(getLayoutId());
        if( getSupportActionBar() != null ) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setLogo(R.drawable.zteicon);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setTitle(" "+getResources().getString(R.string.app_name));
        }
        Log.d( TAG, "onCreate");
        Log.d( TAG, "id:"+getListId());
        list = findViewById( getListId() );
        Log.d( TAG, "list:"+list);
        // Connect to database
        DbHelper dbHelper = new DbHelper(this);
        db = dbHelper.getReadableDatabase();
        initDB();
        receiver = new UpdateReceiver();
        filter = new IntentFilter(UpdaterService.DB_UPDATED_INTENT);

        View swipeView = getWindow().getDecorView();
        Log.d(TAG, "swipView:" + swipeView);
        final GestureDetector gestureDetector  = new GestureDetector(this, simpleOnGestureListener);
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

    private void addClickListener(final TextView view, final int position, final String columnName, final String bundleName, final Class<? extends BaseActivity> activityClass) {
        if( view != null ) {
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.d( TAG, "onClick "+view.getText()+" "+position);
                    int columnIndex =  getCursor().getColumnIndex(columnName);
                    Log.d( TAG, "columnIndex:"+columnIndex);
                    int shirtNumber = ((Cursor)adapter.getItem(position)).getInt( columnIndex );
                    Log.d( TAG, "shirtNumber: "+shirtNumber);
                    Intent intent = new Intent(BaseActivity.this, activityClass);
                    Bundle b = new Bundle();
                    b.putInt(bundleName, shirtNumber);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter = new SimpleCursorAdapter(this, getAdapterLayoutRow(), getCursor(), getAdapterFrom(), getAdapterTo()) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                Log.d(TAG, "position:"+position+" convertView:"+convertView+" parent:"+parent);
                View row = super.getView( position, convertView, parent );
                TextView seasonTextView = row.findViewWithTag("season");
                addClickListener( seasonTextView, position, "season_id", "seasonId", SeasonActivity.class);
                TextView shirtTextView = row.findViewWithTag("shirt");
                addClickListener( shirtTextView, position, "shirt_number", "shirtNumber", ShirtActivity.class);
                TextView playerTextView = row.findViewWithTag("player");
                addClickListener( playerTextView, position, "player_id", BUNDLE_KEY_PLAYER_ID, PlayerActivity.class);
                return row;

            }
        };
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
                updateDatabase();
                break;
            case R.id.itemPlayerList:
                startActivity(new Intent(this, PlayerListActivity.class));
                break;
            case R.id.itemSeasonList:
                startActivity(new Intent(this, SeasonListActivity.class));
                break;
            case R.id.itemShirtList:
                startActivity(new Intent(this, ShirtListActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void updateDatabase() {
        Toast.makeText( this, "Adatbázis frissítése", Toast.LENGTH_SHORT).show();
        startService(new Intent(this, UpdaterService.class));
    }

    protected abstract Cursor getCursor();

    class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("UpdateReceiver", "onReceived");
            Integer result = UpdaterService.UPDATER_FAIL;
            if (intent.getExtras() != null) {
                result = (Integer) intent.getExtras().get("result");
            }
            if( result == null ) {
                result = UpdaterService.UPDATER_FAIL;
            }
            switch( result ) {
                case UpdaterService.UPDATER_FAIL:
                    Toast.makeText( BaseActivity.this, R.string.updateFail, Toast.LENGTH_LONG).show();
                    break;
                case UpdaterService.UPDATER_SUCCESS:
                    initDB();
                    onResume();
                    Toast.makeText( BaseActivity.this, R.string.updateSuccess, Toast.LENGTH_LONG).show();
                    break;
                case UpdaterService.UPDATER_OBSOLETE:
                    Toast.makeText( BaseActivity.this, R.string.updateObsolete, Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText( BaseActivity.this, R.string.updateUnknown, Toast.LENGTH_LONG).show();
            }
        }
    }
}
