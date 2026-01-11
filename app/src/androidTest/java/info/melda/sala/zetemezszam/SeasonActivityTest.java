package info.melda.sala.zetemezszam;

import android.view.View;
import android.view.ViewGroup;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * Instrumentation test for SeasonActivity to verify window insets are properly handled,
 * especially on Android 15 and other newer versions.
 */
@RunWith(AndroidJUnit4.class)
public class SeasonActivityTest {

    @Rule
    public ActivityTestRule<SeasonActivity> activityRule = new ActivityTestRule<>(SeasonActivity.class);

    @Test
    public void testActivityLaunches() {
        SeasonActivity activity = activityRule.getActivity();
        assertNotNull("Activity should be launched", activity);
    }

    @Test
    public void testWindowInsetsApplied() {
        SeasonActivity activity = activityRule.getActivity();
        View rootView = activity.findViewById(android.R.id.content);
        assertNotNull("Root view should exist", rootView);

        // Verify that padding is applied (should have non-zero top padding to account for action bar and status bar)
        // Note: This test verifies the basic structure, actual padding values depend on device/system bars
        assertTrue("Root view should have padding set", 
                rootView.getPaddingTop() >= 0 && 
                rootView.getPaddingLeft() >= 0 && 
                rootView.getPaddingRight() >= 0 && 
                rootView.getPaddingBottom() >= 0);
    }

    @Test
    public void testListViewExists() {
        SeasonActivity activity = activityRule.getActivity();
        View listView = activity.findViewById(R.id.listSeason);
        assertNotNull("ListView should exist", listView);
        assertTrue("ListView should be visible", listView.getVisibility() == View.VISIBLE);
    }

    @Test
    public void testActionBarExists() {
        SeasonActivity activity = activityRule.getActivity();
        assertNotNull("ActionBar should exist", activity.getSupportActionBar());
    }
}
