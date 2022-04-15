package info.melda.sala.zetemezszam;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PlayerActivity extends BaseActivity {

    private static final String TAG = "PlayerActivity";
    private TextView titlePlayer;
    private TextView dobPlayer;
    private ImageView photoPlayer;
    private static final String[] FROM = { "season_name", "shirt_number" };
    private static final int[] TO = { R.id.playerRowSeasonName, R.id.playerRowShirtNumber };
    private int playerId;
    private int playerIdIndex;
    private final List<Integer> playerIds = new ArrayList<>();

    @Override
    public void onCreate( Bundle icicle ) {
        Log.d(TAG, "onCreate "+icicle);
        super.onCreate( icicle );
        findPlayerByBundle( icicle );
        titlePlayer = findViewById(R.id.titlePlayer);
        dobPlayer = findViewById(R.id.dobPlayer);
        photoPlayer = findViewById(R.id.photoPlayer);
    }

    private void findPlayerByBundle(Bundle b) {
        if( b != null ) {
            playerId = b.getInt(BUNDLE_KEY_PLAYER_ID);
            findPlayerIdIndex();
        }
    }

    private void findPlayerIdIndex() {
        int foundPlayerIdIndex = playerIds.indexOf(playerId);
        if( foundPlayerIdIndex != -1 ) {
            playerIdIndex = foundPlayerIdIndex;
        }
    }

    protected int getLayoutId() {
        return R.layout.player;
    }

    protected int getListId() {
        return R.id.listPlayer;
    }

    @Override
    protected void initDB() {
        Log.d(TAG, "initDB "+playerIdIndex+" "+playerId);
        Cursor c = db.rawQuery("select player_id from player order by player_name", null);
        playerIds.clear();
        while( c.moveToNext() ) {
            playerIds.add( c.getInt(0));
        }
        c.close();
        Log.d(TAG, "playerNum:"+playerIds.size());
        Bundle bundle = getIntent().getExtras();
        Log.d(TAG, "bundle: "+bundle);
        if (playerId == 0) {
            findPlayerByBundle(bundle);
        }
        Log.d(TAG, "playerIdIndex:"+playerIdIndex);
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
        Log.d(TAG, "onResume "+playerIdIndex+" "+playerId);
        Cursor cursor = db.rawQuery("select player_name, strftime('%Y.%m.%d.',player_dob), player_photo from player where player_id="+playerId, null);
        String playerName;
        String playerDob;
        Bitmap playerImageBitmap;
        if( cursor.moveToFirst() ) {
            playerName = cursor.getString(0);
            playerDob = cursor.getString(1);
            if( playerDob == null ) {
                playerDob = "";
            }
            byte []playerImageByteArray = cursor.getBlob(2);
            if (playerImageByteArray != null) {
                Log.v(TAG, "DB image size"+playerImageByteArray.length);
                playerImageBitmap = convertByteArrayToBitmap(playerImageByteArray);
            } else {
                Log.v(TAG, "null image");
                playerImageBitmap = null;
            }
        } else {
            playerName = UNKNOWN;
            playerDob = UNKNOWN;
            playerImageBitmap = null;
        }
        cursor.close();
        titlePlayer.setText(playerName);
        dobPlayer.setText(playerDob);
        photoPlayer.setImageBitmap(playerImageBitmap);
    }

    private static Bitmap convertByteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    @Override
    protected void swipeRightAction() {
        if( playerIdIndex > 0 ) {
            --playerIdIndex;
            playerId = playerIds.get(playerIdIndex);
        }
        onResume();
    }

    @Override
    protected void swipeLeftAction() {
        if( playerIdIndex < playerIds.size()-1 ) {
            ++playerIdIndex;
            playerId = playerIds.get(playerIdIndex);
        }
        onResume();
    }

    @Override
    protected void longPressAction() {
        startActivity( new Intent( this, PlayerListActivity.class) );
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        Log.d(TAG, "onSaveInstanceState "+playerId);
        super.onSaveInstanceState(state);
        state.putInt(BUNDLE_KEY_PLAYER_ID, playerId);
    }
}
