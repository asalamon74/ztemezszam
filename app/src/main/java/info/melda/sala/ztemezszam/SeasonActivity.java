/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
 */
public class SeasonActivity extends BaseActivity {

    //private static final String TAG = "SeasonActivity";
    private static final String[] FROM = { "shirt_number", "player_name" };
    private static final int[] TO = { R.id.seasonRowShirtNumber, R.id.seasonRowPlayerName };
    private int seasonIdIndex;
    private final List<Integer> seasonIds = new ArrayList<>();
    
    protected int getLayoutId() {
        return R.layout.season;
    }

    protected int getListId() {
        return R.id.listSeason;
    }

    @Override
    protected void initDB() {
        Cursor c = db.rawQuery("select season_id from season order by season_id", null);
        seasonIds.clear();
        while( c.moveToNext() ) {
            seasonIds.add( c.getInt(0));
        }
        c.close();
        seasonIdIndex = seasonIds.size()-1;
    }

    private String getSeasonId() {
        String seasonId;
        if( seasonIdIndex != -1 ) {
            seasonId = String.valueOf(seasonIds.get( seasonIdIndex ));
        } else {
            seasonId = "-1";
        }
        return seasonId;
    }

    protected Cursor getCursor() {
        return db.rawQuery("select shirt_id _id, * from shirt, player where shirt.player_id=player.player_id and season_id=? order by shirt_number", new String [] { getSeasonId()} );
    }

    protected String[] getAdapterFrom() {
        return FROM;
    }

    protected int[] getAdapterTo() {
        return TO;
    }

    protected int getAdapterLayoutRow() {
        return R.layout.season_row;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor seasonNameCursor = db.rawQuery("select season_name from season where season_id=?", new String[] { getSeasonId() });
        String seasonName;
        if( seasonNameCursor.moveToFirst() ) {
            seasonName = seasonNameCursor.getString(0);
        } else {
            seasonName = "????";
        }
        seasonNameCursor.close();
        TextView titleSeason = (TextView) findViewById(R.id.titleSeason);
        titleSeason.setText(seasonName);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate( icicle );

        Bundle b = getIntent().getExtras();
        if( b != null ) {
            int foundSeasonIdIndex = seasonIds.indexOf(b.getInt("seasonId"));
            if( foundSeasonIdIndex != -1 ) {
                seasonIdIndex = foundSeasonIdIndex;
            }
        }

        list.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                //Log.d(TAG, "click pos"+" "+pos);
                Intent intent = new Intent(SeasonActivity.this, PlayerActivity.class);
                Bundle b = new Bundle();
                int columnIndex =  getCursor().getColumnIndex("player_id");
                //Log.d( TAG, "columnIndex:"+columnIndex);
                int playerId = ((Cursor)adapter.getItem(pos)).getInt( columnIndex );
                //Log.d( TAG, "playerId:"+playerId);
                b.putInt("playerId", playerId);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
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

    @Override
    protected void longPressAction() {
        startActivity( new Intent( this, SeasonListActivity.class) );
    }
}
