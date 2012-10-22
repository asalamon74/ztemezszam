package info.melda.sala.ztemezszam;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 *
 * @author salamon
 */
public class PlayerListActivity extends BaseActivity {

    private static final String TAG = "PlayerListActivity";
    private static final String[] FROM = { "player_name" };
    private static final int[] TO = { R.id.playerListPlayerName };


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        list.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                Log.d(TAG, "long click pos"+" "+pos);
                Intent intent = new Intent(PlayerListActivity.this, PlayerActivity.class);
                Bundle b = new Bundle();
                int columnIndex =  getCursor().getColumnIndex("player_id");
                Log.d( TAG, "columnIndex:"+columnIndex);
                int playerId = ((Cursor)adapter.getItem(pos)).getInt( columnIndex );
                Log.d( TAG, "playerId:"+playerId);
                b.putInt("playerId", playerId);
                intent.putExtras(b);
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.player_list;
    }

    @Override
    protected int getListId() {
        return R.id.listPlayers;
    }

    @Override
    protected Cursor getCursor() {
        return db.rawQuery("select player_id _id, * from player order by player_name", null);
    }

    @Override
    protected String[] getAdapterFrom() {
        return FROM;
    }

    @Override
    protected int[] getAdapterTo() {
        return TO;
    }

    @Override
    protected int getAdapterLayoutRow() {
        return R.layout.player_list_row;
    }

}
