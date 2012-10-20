/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.melda.sala.ztemezszam;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 *
 * @author salamon
 */
public class PlayerActivity extends BaseActivity {

    private static final String TAG = "PlayerActivity";
    private TextView titlePlayer;
    private static final String[] FROM = { "season_name", "shirt_number" };
    private static final int[] TO = { R.id.playerRowSeasonName, R.id.playerRowShirtNumber };
    private int playerId=2;
    
    protected int getLayoutId() {
        return R.layout.player;
    }

    protected int getListId() {
        return R.id.listPlayer;
    }

    protected Cursor getCursor() {
        return db.rawQuery("select shirt_id _id, * from shirt, season where shirt.season_id=season.season_id and player_id="+playerId+" order by season_id", null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor cursor = db.rawQuery("select player_name from player where player_id="+playerId, null);
        String playerName;
        if( cursor.moveToFirst() ) {
            playerName = cursor.getString(0);
        } else {
            playerName = "????";
        }
        titlePlayer = (TextView) findViewById(R.id.titlePlayer);
        titlePlayer.setText(playerName);
        adapter = new SimpleCursorAdapter(this, R.layout.player_row, getCursor(), FROM, TO);
        Log.d( TAG, "list:"+list);
        list.setAdapter(adapter);
    }
}