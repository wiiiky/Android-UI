package com.wiky.customui.test;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.action.ViewActions;
import android.test.ActivityInstrumentationTestCase2;

import com.wiky.customui.MainActivity;
import com.wiky.customui.MenuAdapter;
import com.wiky.customui.R;

import org.junit.Before;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Created by wiky on 5/25/15.
 */
public class MyEspressoTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mActivity;

    public MyEspressoTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();
    }

    public void testPageViewActivity_nextActivity() {
        DataInteraction item = onData(allOf(instanceOf(MenuAdapter.MenuItem.class))).inAdapterView(withId(R.id.list_view)).atPosition(0);
        item.perform(ViewActions.click());
        onView(withId(R.id.page_view)).check(matches(isDisplayed()));
    }
}
