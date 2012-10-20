/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.melda.sala.ztemezszam;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 *
 * @author salamon
 */
public class SeasonActivity extends BaseActivity {

    private static final String TAG = "SeasonActivity";
    private TextView titleSeason;
    private static final String[] FROM = { "shirt_number", "player_name" };
    private static final int[] TO = { R.id.seasonRowShirtNumber, R.id.seasonRowPlayerName };
    private int seasonId=64;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.season);
    }

    protected int getListId() {
        return R.id.listSeason;
    }

    protected Cursor getCursor() {
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
        list.setAdapter(adapter);
    }

}
