package info.melda.sala.zetemezszam;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 *
 */
public class PrefsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsPreferenceFragment()).commit();
    }

    public static class PrefsPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
        }
    }
}
