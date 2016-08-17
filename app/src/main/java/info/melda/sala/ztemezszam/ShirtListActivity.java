package info.melda.sala.ztemezszam;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 *
 * @author salamon
 */
public class ShirtListActivity extends BaseActivity {

    //private static final String TAG = "ShirtListActivity";
    private static final String[] FROM = { "shirt_number" };
    private static final int[] TO = { R.id.shirtListShirtNumber };


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        list.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                //Log.d(TAG, "click pos"+" "+pos);
                Intent intent = new Intent(ShirtListActivity.this, ShirtActivity.class);
                Bundle b = new Bundle();
                int columnIndex =  getCursor().getColumnIndex("shirt_number");
                //Log.d( TAG, "columnIndex:"+columnIndex);
                int shirtNumber = ((Cursor)adapter.getItem(pos)).getInt( columnIndex );
                //Log.d( TAG, "shirtNumber:"+shirtNumber);
                b.putInt("shirtNumber", shirtNumber);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.shirt_grid;
    }

    @Override
    protected int getListId() {
        return R.id.gridShirtList;
    }

    @Override
    protected Cursor getCursor() {
        return db.rawQuery("select distinct shirt_number _id, shirt_number from shirt order by shirt_number", null);
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
        return R.layout.shirt_grid_row;
    }

}
