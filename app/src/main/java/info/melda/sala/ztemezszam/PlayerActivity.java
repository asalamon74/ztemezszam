package info.melda.sala.ztemezszam;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author salamon
 */
public class PlayerActivity extends BaseActivity {

    //private static final String TAG = "PlayerActivity";
    private TextView titlePlayer;
    private static final String[] FROM = { "season_name", "shirt_number" };
    private static final int[] TO = { R.id.playerRowSeasonName, R.id.playerRowShirtNumber };
    private int playerIdIndex;
    private final List<Integer> playerIds = new ArrayList<Integer>();

    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        Bundle b = getIntent().getExtras();
        if( b != null ) {
            int foundPlayerIdIndex = playerIds.indexOf(b.getInt("playerId"));
            if( foundPlayerIdIndex != -1 ) {
                playerIdIndex = foundPlayerIdIndex;
            }
        }

        titlePlayer = (TextView) findViewById(R.id.titlePlayer);
        
        list.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                //Log.d(TAG, "click pos"+" "+pos);
                Intent intent = new Intent(PlayerActivity.this, SeasonActivity.class);
                Bundle b = new Bundle();
                int columnIndex =  getCursor().getColumnIndex("season_id");
                //Log.d( TAG, "columnIndex:"+columnIndex);
                int seasonId = ((Cursor)adapter.getItem(pos)).getInt( columnIndex );
                //Log.d( TAG, "seasonId:"+seasonId);
                b.putInt("seasonId", seasonId);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }
    
    protected int getLayoutId() {
        return R.layout.player;
    }

    protected int getListId() {
        return R.id.listPlayer;
    }

    @Override
    protected void initDB() {
        Cursor c = db.rawQuery("select player_id from player order by player_name", null);
        playerIds.clear();
        while( c.moveToNext() ) {
            playerIds.add( c.getInt(0));
        }
        playerIdIndex = playerIds.size()-1;
    }

    protected Cursor getCursor() {
        return db.rawQuery("select shirt_id _id, * from shirt, season where shirt.season_id=season.season_id and player_id=? order by season_id", new String [] { String.valueOf(playerIds.get(playerIdIndex))} );
    }

    protected String[] getAdapterFrom() {
        return FROM;
    }

    protected int[] getAdapterTo() {
        return TO;
    }

    protected int getAdapterLayoutRow() {
        return R.layout.player_row;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor cursor = db.rawQuery("select player_name, strftime('%Y.%m.%d.',player_dob) from player where player_id="+playerIds.get(playerIdIndex), null);
        String playerName;
        String playerDob;
        if( cursor.moveToFirst() ) {
            playerName = cursor.getString(0);
            playerDob = cursor.getString(1);
            if( playerDob != null ) {
                playerDob = " "+playerDob;
            } else {
                playerDob = "";
            }
        } else {
            playerName = "????";
            playerDob = " ????";
        }
        titlePlayer.setText(playerName+playerDob);
    }

     @Override
    protected void swipeRightAction() {
        if( playerIdIndex > 0 ) {
            --playerIdIndex;
        }
        onResume();
    }

    @Override
    protected void swipeLeftAction() {
        if( playerIdIndex < playerIds.size()-1 ) {
            ++playerIdIndex;
        }
        onResume();
    }

    @Override
    protected void longPressAction() {
        startActivity( new Intent( this, PlayerListActivity.class) );
    }
}
