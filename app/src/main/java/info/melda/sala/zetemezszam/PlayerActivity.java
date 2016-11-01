package info.melda.sala.zetemezszam;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PlayerActivity extends BaseActivity {

    //private static final String TAG = "PlayerActivity";
    private TextView titlePlayer;
    private static final String[] FROM = { "season_name", "shirt_number" };
    private static final int[] TO = { R.id.playerRowSeasonName, R.id.playerRowShirtNumber };
    private int playerIdIndex;
    private final List<Integer> playerIds = new ArrayList<>();

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
        c.close();
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
            if( playerDob == null ) {
                playerDob = "";
            }
        } else {
            playerName = "????";
            playerDob = " ????";
        }
        cursor.close();
        Resources res = getResources();
        String titlePlayerText=String.format(res.getString(R.string.titlePlayer), playerName, playerDob);
        titlePlayer.setText(titlePlayerText);
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
