/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.melda.sala.ztemezszam;

import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

/**
 *
 * @author salamon
 */
public class SeasonActivity extends BaseActivity {

    private static final String TAG = "SeasonActivity";
    private ListView listSeason;
    private TextView titleSeason;
    private static final String[] FROM = { "shirt_number", "player_name" };
    private static final int[] TO = { R.id.textId, R.id.textName };
    private int seasonId=64;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.season);

        listSeason = (ListView) findViewById(R.id.listSeason); 
    }

    protected Cursor getCursor() {
        return db.rawQuery("select shirt_id _id, * from shirt, player where shirt.player_id=player.player_id and season_id="+seasonId+" order by shirt_number", null);
    }

    private static final ViewBinder VIEW_BINDER = new ViewBinder() {

        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view.getId() != R.id.textId) {
                return false;
            }

            String text = cursor.getString(columnIndex);
            if( text.length() == 1 ) {
                text = "\u0020"+text;
            }
            ((TextView) view).setText(text);
            return true;
        }
    };

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
        //adapter.setViewBinder(VIEW_BINDER);
        listSeason.setAdapter(adapter);
    }

}
