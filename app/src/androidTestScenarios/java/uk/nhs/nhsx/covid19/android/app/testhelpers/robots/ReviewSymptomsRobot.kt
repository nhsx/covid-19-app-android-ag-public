package uk.nhs.nhsx.covid19.android.app.testhelpers.robots

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import uk.nhs.nhsx.covid19.android.app.R
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.adapter.ReviewSymptomViewHolder
import uk.nhs.nhsx.covid19.android.app.testhelpers.NestedScrollViewScrollToAction

class ReviewSymptomsRobot {

    fun confirmReviewSymptomsScreenIsDisplayed() {
        checkActivityTitleIsDisplayed(R.string.questionnaire_review_symptoms)
    }

    fun selectCannotRememberDate() {
        onView(withId(R.id.checkboxNoDate))
            .perform(NestedScrollViewScrollToAction(), click())
    }

    fun clickSelectDate() {
        onView(withId(R.id.selectDateContainer))
            .perform(NestedScrollViewScrollToAction(), click())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    fun selectDayOfMonth(dayOfMonth: Int) {
        datePickerSelectDayOfMonth(dayOfMonth)
    }

    fun confirmSelection() {
        onView(withId(R.id.buttonConfirmSymptoms))
            .perform(NestedScrollViewScrollToAction(), click())
    }

    fun changeFirstNegativeSymptom() {
        onView(withId(R.id.listReviewSymptoms))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ReviewSymptomViewHolder>(
                    1,
                    clickOnViewChild(R.id.textChange)
                )
            )
    }

    fun checkReviewSymptomsErrorIsDisplayed() {
        onView(withText(R.string.questionnaire_input_date_error))
            .check(matches(isDisplayed()))
    }

    fun checkDoNotRememberDateIsChecked() {
        onView(withId(R.id.checkboxNoDate))
            .check(matches(isChecked()))
    }

    fun checkDoNotRememberDateIsNotChecked() {
        onView(withId(R.id.checkboxNoDate))
            .check(matches(isNotChecked()))
    }

    fun clickOnViewChild(viewId: Int) = object : ViewAction {
        override fun getConstraints() = null

        override fun getDescription() = "Click on a child view with specified id."

        override fun perform(uiController: UiController, view: View) =
            click().perform(uiController, view.findViewById(viewId))
    }
}
