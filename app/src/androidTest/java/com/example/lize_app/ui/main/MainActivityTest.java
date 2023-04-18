package com.example.lize_app.ui.main;

import androidx.test.espresso.intent.Intents;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.mikepenz.aboutlibraries.ui.LibsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.example.lize_app.R;
import com.example.lize_app.ui.central.CentralActivity;
import com.example.lize_app.ui.peripheral.PeripheralActivity;
import com.example.lize_app.utils.BLEIntents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by suzhenxi on 9/19/2016.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    @Test
    public void testDisplayed() {
        onView(withId(R.id.Central_Button))
                .check(matches(isDisplayed()));

        onView(withId(R.id.Peripheral_Button))
                .check(matches(isDisplayed()));

        onView(withId(R.id.About_Button))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testAboutIntent() {
        Intents.init();

        onView(withId(R.id.About_Button))
                .check(matches(isDisplayed()))
                .perform(click());

        Intents.intended(hasComponent(LibsActivity.class.getName()));

        Intents.release();
    }

    @Test
    public void testCentralIntent() {
        Intents.init();

        onView(withId(R.id.Central_Button))
                .check(matches(isDisplayed()))
                .perform(click());

        Intents.intended(hasAction(BLEIntents.ACTION_CENTRAL_MODE));
        // Intents.intended(hasComponent(CentralActivity.class.getName()));

        Intents.release();
    }

    @Test
    public void testPeripheralIntent() {
        Intents.init();

        onView(withId(R.id.Peripheral_Button))
                .check(matches(isDisplayed()))
                .perform(click());

        Intents.intended(hasAction(BLEIntents.ACTION_PERIPHERAL_MODE));
        // Intents.intended(hasComponent(PeripheralActivity.class.getName()));

        Intents.release();
    }
}
