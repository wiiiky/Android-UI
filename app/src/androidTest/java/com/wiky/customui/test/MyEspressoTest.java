package com.wiky.customui.test;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.FailureHandler;
import android.support.test.espresso.action.ViewActions;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;

import com.wiky.customui.MainActivity;
import com.wiky.customui.MenuAdapter;
import com.wiky.customui.R;

import org.hamcrest.Matcher;
import org.junit.Before;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Created by wiky on 5/25/15.
 */
public class MyEspressoTest extends ActivityInstrumentationTestCase2<MainActivity> implements FailureHandler {
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
        onData(allOf(instanceOf(MenuAdapter.MenuItem.class))).inAdapterView(withId(R.id.list_view)).atPosition(0).perform(ViewActions.click());
        onView(withId(R.id.page_view)).check(matches(isDisplayed()));
        pressBack();
        onView(withId(R.id.list_view)).check(matches(isDisplayed()));
        onData(allOf(instanceOf(MenuAdapter.MenuItem.class))).inAdapterView(withId(R.id.list_view)).atPosition(1).perform(ViewActions.click());
        onView(withId(R.id.image_viewer)).check(matches(isDisplayed()));
    }

    @Override
    public void handle(Throwable throwable, Matcher<View> matcher) {
        Log.e("failure", throwable.getMessage());
    }
}
