/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.melda.sala.ztemezszam;

import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author salamon
 */
public class SeasonActivity extends BaseActivity {

    private static final String TAG = "SeasonActivity";
    private TextView titleSeason;
    private static final String[] FROM = { "shirt_number", "player_name" };
    private static final int[] TO = { R.id.seasonRowShirtNumber, R.id.seasonRowPlayerName };
    private int seasonIdIndex;
    private List<Integer> seasonIds = new ArrayList<Integer>();
    
    protected int getLayoutId() {
        return R.layout.season;
    }

    protected int getListId() {
        return R.id.listSeason;
    }

    protected void initDB() {
        Cursor c = db.rawQuery("select season_id from season order by season_id", null);
        seasonIds.clear();
        while( c.moveToNext() ) {
            seasonIds.add( c.getInt(0));
        }
        seasonIdIndex = seasonIds.size()-1;
    }

    protected Cursor getCursor() {
        return db.rawQuery("select shirt_id _id, * from shirt, player where shirt.player_id=player.player_id and season_id=? order by shirt_number", new String [] { String.valueOf(seasonIds.get(seasonIdIndex))} );
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor seasonNameCursor = db.rawQuery("select season_name from season where season_id="+seasonIds.get(seasonIdIndex), null);
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

    @Override
    protected void swipeRightAction() {
        if( seasonIdIndex > 0 ) {
            --seasonIdIndex;
        }
        onResume();
    }

    @Override
    protected void swipeLeftAction() {
        if( seasonIdIndex < seasonIds.size()-1 ) {
            ++seasonIdIndex;
        }
        onResume();
    }

}
