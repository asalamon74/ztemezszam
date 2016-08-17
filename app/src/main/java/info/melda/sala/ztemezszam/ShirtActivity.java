package info.melda.sala.ztemezszam;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
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
public class ShirtActivity extends BaseActivity {

    private static final String TAG = "ShirtActivity";
    private TextView titleShirt;
    private static final String[] FROM = { "season_name", "player_name" };
    private static final int[] TO = { R.id.shirtRowSeasonName, R.id.shirtRowPlayerName };
    private int shirtNumberIndex;
    private List<Integer> shirtNumbers = new ArrayList<Integer>();

    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        Bundle b = getIntent().getExtras();
        if( b != null ) {
            int foundShirtNumberIndex = shirtNumbers.indexOf(b.getInt("shirtNumber"));
            if( foundShirtNumberIndex != -1 ) {
                shirtNumberIndex = foundShirtNumberIndex;
            }
        }

        titleShirt = (TextView) findViewById(R.id.titleShirt);

        list.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                //Log.d(TAG, "click pos"+" "+pos);
                Intent intent = new Intent(ShirtActivity.this, SeasonActivity.class);
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
        return R.layout.shirt;
    }

    protected int getListId() {
        return R.id.listShirt;
    }

    @Override
    protected void initDB() {
        Cursor c = db.rawQuery("select distinct shirt_number from shirt order by shirt_number", null);
        shirtNumbers.clear();
        while( c.moveToNext() ) {
            shirtNumbers.add( c.getInt(0));
        }
        shirtNumberIndex = shirtNumbers.size()-1;
    }

    protected Cursor getCursor() {
        return db.rawQuery("select shirt_id _id, * from shirt, season, player where shirt.season_id=season.season_id and shirt.player_id=player.player_id and shirt_number=? order by season_id", new String [] { String.valueOf(shirtNumbers.get(shirtNumberIndex))} );
    }

    protected String[] getAdapterFrom() {
        return FROM;
    }

    protected int[] getAdapterTo() {
        return TO;
    }

    protected int getAdapterLayoutRow() {
        return R.layout.shirt_row;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d( TAG, "shirtNumberIndex:"+shirtNumberIndex);
        Log.d( TAG, "shirtNumbers:"+shirtNumbers);
        //titleShirt.setText("AA");
        titleShirt.setText( String.valueOf(shirtNumbers.get( shirtNumberIndex )));
    }

     @Override
    protected void swipeRightAction() {
        if( shirtNumberIndex > 0 ) {
            --shirtNumberIndex;
        }
        onResume();
    }

    @Override
    protected void swipeLeftAction() {
        if( shirtNumberIndex < shirtNumbers.size()-1 ) {
            ++shirtNumberIndex;
        }
        onResume();
    }

    @Override
    protected void longPressAction() {
        startActivity( new Intent( this, PlayerListActivity.class) );
    }
}
