package uk.nhs.nhsx.covid19.android.app.testhelpers.robots

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import kotlinx.android.synthetic.main.activity_symptoms_advice_isolate.stateExplanation
import org.hamcrest.Matchers.not
import uk.nhs.nhsx.covid19.android.app.R
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.IsolationSymptomAdvice
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.IsolationSymptomAdvice.IndexCaseThenHasSymptomsDidUpdateIsolation
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.IsolationSymptomAdvice.IndexCaseThenHasSymptomsNoEffectOnIsolation
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.IsolationSymptomAdvice.IndexCaseThenNoSymptoms
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.IsolationSymptomAdvice.NoIndexCaseThenIsolationDueToSelfAssessment
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.IsolationSymptomAdvice.NoIndexCaseThenSelfAssessmentNoImpactOnIsolation
import uk.nhs.nhsx.covid19.android.app.testhelpers.matcher.containsStringResourceAt
import uk.nhs.nhsx.covid19.android.app.testhelpers.matcher.withStateColor
import uk.nhs.nhsx.covid19.android.app.testhelpers.matcher.withStateStringResource

class SymptomsAdviceIsolateRobot {

    fun checkActivityIsDisplayed() {
        onView(withId(R.id.symptomsAdviceIsolateContainer))
            .check(matches(isDisplayed()))
    }

    fun checkViewState(isolationSymptomAdvice: IsolationSymptomAdvice) {
        when (isolationSymptomAdvice) {
            is IndexCaseThenHasSymptomsDidUpdateIsolation ->
                checkIndexCaseThenHasSymptomsDidUpdateIsolationIsDisplayed(isolationSymptomAdvice.remainingDaysInIsolation)
            IndexCaseThenHasSymptomsNoEffectOnIsolation ->
                checkIndexCaseThenHasSymptomsNoEffectOnIsolationIsDisplayed()
            IndexCaseThenNoSymptoms ->
                checkIndexCaseThenNoSymptomsIsDisplayed()
            is NoIndexCaseThenIsolationDueToSelfAssessment ->
                checkNoIndexCaseThenIsolationDueToSelfAssessmentIsDisplayed(isolationSymptomAdvice.remainingDaysInIsolation)
            is NoIndexCaseThenSelfAssessmentNoImpactOnIsolation ->
                checkNoIndexCaseThenSelfAssessmentNoImpactOnIsolationIsDisplayed(isolationSymptomAdvice.remainingDaysInIsolation)
        }
    }

    private fun checkIndexCaseThenHasSymptomsDidUpdateIsolationIsDisplayed(remainingDaysInIsolation: Int) {
        checkCloseIconInToolbarIsNotDisplayed()
        checkPreDaysTextViewIsDisplayed(R.string.self_isolate_for)
        checkDaysUntilExpirationTextViewIsDisplayed(
            context.resources.getQuantityString(
                R.plurals.state_isolation_days,
                remainingDaysInIsolation,
                remainingDaysInIsolation
            )
        )
        checkPostDaysTextViewIsNotDisplayed()
        checkExposureLinkIsNotDisplayed()
        checkStateInfo(R.string.symptoms_advice_isolate_info_continue_isolation, R.color.amber)
        checkExplanationText(R.string.symptoms_advice_isolate_paragraphs_continue_isolation)
        checkBottomActionButtonIsDisplayedWithText(R.string.continue_button)
    }

    private fun checkIndexCaseThenHasSymptomsNoEffectOnIsolationIsDisplayed() {
        checkCloseIconInToolbarIsNotDisplayed()
        checkPreDaysTextViewIsDisplayed(R.string.symptoms_advice_isolate_heading_continue_isolation_no_change)
        checkDaysUntilExpirationTextViewIsNotDisplayed()
        checkPostDaysTextViewIsNotDisplayed()
        checkExposureLinkIsNotDisplayed()
        checkStateInfo(R.string.symptoms_advice_isolate_info_continue_isolation_no_change, R.color.error_red)
        checkExplanationText(R.string.symptoms_advice_isolate_paragraphs_continue_isolation_no_change)
        checkBottomActionButtonIsDisplayedWithText(R.string.continue_button)
    }

    private fun checkIndexCaseThenNoSymptomsIsDisplayed() {
        checkCloseIconInToolbarIsNotDisplayed()
        checkPreDaysTextViewIsDisplayed(R.string.symptoms_advice_isolate_heading_continue_isolation_no_symptoms)
        checkDaysUntilExpirationTextViewIsNotDisplayed()
        checkPostDaysTextViewIsNotDisplayed()
        checkExposureLinkIsNotDisplayed()
        checkStateInfo(R.string.symptoms_advice_isolate_info_continue_isolation_no_symptoms, R.color.error_red)
        checkExplanationText(R.string.symptoms_advice_isolate_paragraphs_continue_isolation_no_symptoms)
        checkBottomActionButtonIsDisplayedWithText(R.string.continue_button)
    }

    private fun checkNoIndexCaseThenIsolationDueToSelfAssessmentIsDisplayed(remainingDaysInIsolation: Int) {
        checkCloseIconInToolbarIsDisplayed()
        checkPreDaysTextViewIsDisplayed(R.string.self_isolate_for)
        checkDaysUntilExpirationTextViewIsDisplayed(
            context.resources.getQuantityString(
                R.plurals.state_isolation_days,
                remainingDaysInIsolation,
                remainingDaysInIsolation
            )
        )
        checkPostDaysTextViewIsDisplayed(R.string.state_and_book_a_test)
        checkExposureLinkIsDisplayed()
        checkStateInfo(R.string.state_index_info, R.color.amber)
        checkExplanationText(R.string.isolate_after_corona_virus_symptoms, R.string.exposure_faqs_title)
        checkBottomActionButtonIsDisplayedWithText(R.string.book_free_test)
    }

    private fun checkNoIndexCaseThenSelfAssessmentNoImpactOnIsolationIsDisplayed(remainingDaysInIsolation: Int) {
        checkCloseIconInToolbarIsDisplayed()
        checkPreDaysTextViewIsDisplayed(R.string.self_isolate_for)
        checkDaysUntilExpirationTextViewIsDisplayed(
            context.resources.getQuantityString(
                R.plurals.state_isolation_days,
                remainingDaysInIsolation,
                remainingDaysInIsolation
            )
        )
        checkPostDaysTextViewIsNotDisplayed()
        checkExposureLinkIsNotDisplayed()
        checkStateInfo(R.string.you_do_not_appear_to_have_symptoms, R.color.nhs_button_green)
        checkExplanationText(R.string.isolate_after_no_corona_virus_symptoms)
        checkBottomActionButtonIsDisplayedWithText(R.string.back_to_home)
    }

    private fun checkCloseIconInToolbarIsDisplayed() {
        onView(withContentDescription(R.string.close))
            .check(matches(isDisplayed()))
    }

    private fun checkCloseIconInToolbarIsNotDisplayed() {
        onView(withContentDescription(R.string.close))
            .check(doesNotExist())
    }

    private fun checkPreDaysTextViewIsDisplayed(@StringRes textResId: Int) {
        onView(withId(R.id.preDaysTextView))
            .check(matches(isDisplayed()))
            .check(matches(withText(textResId)))
    }

    private fun checkDaysUntilExpirationTextViewIsDisplayed(text: String) {
        onView(withId(R.id.daysUntilExpirationTextView))
            .check(matches(isDisplayed()))
            .check(matches(withText(text)))
    }

    private fun checkDaysUntilExpirationTextViewIsNotDisplayed() {
        onView(withId(R.id.daysUntilExpirationTextView))
            .check(matches(not(isDisplayed())))
    }

    private fun checkPostDaysTextViewIsDisplayed(@StringRes textResId: Int) {
        onView(withId(R.id.postDaysTextView))
            .check(matches(isDisplayed()))
            .check(matches(withText(textResId)))
    }

    private fun checkPostDaysTextViewIsNotDisplayed() {
        onView(withId(R.id.postDaysTextView))
            .check(matches(not(isDisplayed())))
    }

    private fun checkExposureLinkIsDisplayed() {
        onView(withId(R.id.exposureFaqsLinkTextView))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    private fun checkExposureLinkIsNotDisplayed() {
        onView(withId(R.id.stateActionButton))
            .perform(scrollTo())
        onView(withId(R.id.exposureFaqsLinkTextView))
            .check(matches(not(isDisplayed())))
    }

    private fun checkStateInfo(@StringRes textResId: Int, @ColorRes colorResId: Int) {
        onView(withId(R.id.stateInfoView)).apply {
            check(matches(withStateStringResource(textResId)))
            check(matches(withStateColor(colorResId)))
        }
    }

    private fun checkExplanationText(@StringRes vararg stringResIds: Int) {
        with(onView(withId(R.id.stateExplanation))) {
            stringResIds.forEachIndexed { index, stringResourceId ->
                check(matches(containsStringResourceAt(stringResourceId, index)))
            }
        }
    }

    private fun checkBottomActionButtonIsDisplayedWithText(@StringRes textResId: Int) {
        onView(withId(R.id.stateActionButton))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(withText(textResId)))
    }

    fun clickBottomActionButton() {
        onView(withId(R.id.stateActionButton))
            .perform(scrollTo(), click())
    }
}
