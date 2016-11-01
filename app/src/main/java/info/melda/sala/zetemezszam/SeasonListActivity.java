package info.melda.sala.zetemezszam;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 *
 */
public class SeasonListActivity extends BaseActivity {

    //private static final String TAG = "SeasonListActivity";
    private static final String[] FROM = { "season_name" };
    private static final int[] TO = { R.id.seasonListSeasonName };


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        list.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                //Log.d(TAG, "click pos"+" "+pos);
                Intent intent = new Intent(SeasonListActivity.this, SeasonActivity.class);
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

    @Override
    protected int getLayoutId() {
        return R.layout.season_list;
    }

    @Override
    protected int getListId() {
        return R.id.listSeasonList;
    }

    @Override
    protected Cursor getCursor() {
        return db.rawQuery("select season_id _id, * from season order by season_id", null);
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
        return R.layout.season_list_row;
    }

}
