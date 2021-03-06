package uk.nhs.nhsx.covid19.android.app.testhelpers.base

import android.app.Activity
import android.content.Intent
import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkManager
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uk.nhs.nhsx.covid19.android.app.MockApiResponseType.ALWAYS_SUCCEED
import uk.nhs.nhsx.covid19.android.app.di.MockApiModule
import uk.nhs.nhsx.covid19.android.app.report.config.FontScale.DEFAULT
import uk.nhs.nhsx.covid19.android.app.report.config.FontScale.SMALL
import uk.nhs.nhsx.covid19.android.app.report.config.Orientation.LANDSCAPE
import uk.nhs.nhsx.covid19.android.app.report.config.Orientation.PORTRAIT
import uk.nhs.nhsx.covid19.android.app.report.config.TestConfiguration
import uk.nhs.nhsx.covid19.android.app.report.config.Theme.DARK
import uk.nhs.nhsx.covid19.android.app.report.config.Theme.LIGHT
import uk.nhs.nhsx.covid19.android.app.report.isRunningReporterTool
import uk.nhs.nhsx.covid19.android.app.testhelpers.AWAIT_AT_MOST_SECONDS
import uk.nhs.nhsx.covid19.android.app.testhelpers.TestApplicationContext
import uk.nhs.nhsx.covid19.android.app.testhelpers.retry.RetryRule
import uk.nhs.nhsx.covid19.android.app.testhelpers.setScreenOrientation
import uk.nhs.nhsx.covid19.android.app.util.ScreenshotTakingRule

@RunWith(AndroidJUnit4::class)
abstract class EspressoTest {

    var testAppContext: TestApplicationContext = TestApplicationContext()

    open val configuration: TestConfiguration? = null

    @get:Rule
    val screenshotRule = ScreenshotTakingRule()

    @get:Rule
    var rule = RetryRule(testAppContext, retryCount = 3)

    @Before
    fun setup() {
        testAppContext.reset()
    }

    @Before
    fun setUpMockApiBehaviour() {
        MockApiModule.behaviour.apply {
            delayMillis = 0
            responseType = ALWAYS_SUCCEED
        }
    }

    @After
    fun teardown() {
        WorkManager.getInstance(InstrumentationRegistry.getInstrumentation().targetContext)
            .cancelAllWork()
        testAppContext.encryptedStorage.sharedPreferences.edit(commit = true) { clear() }
    }

    protected fun runBackgroundTasks() {
        testAppContext.runBackgroundTasks()
    }

    protected inline fun <reified T : Activity> startTestActivity(
        config: Intent.() -> Unit = {}
    ): Activity? {
        val intent = Intent(testAppContext.app, T::class.java)
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) }
            .apply(config)

        return InstrumentationRegistry
            .getInstrumentation()
            .startActivitySync(intent).apply {
                configuration?.let {
                    setScreenOrientation(it.orientation)
                }
            }
    }

    fun waitFor(idleTime: Long = AWAIT_AT_MOST_SECONDS, assertion: () -> Unit) {
        uk.nhs.nhsx.covid19.android.app.testhelpers.waitFor(idleTime, assertion)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun configurations(): Iterable<TestConfiguration> {
            return if (isRunningReporterTool()) {
                listOf(
                    TestConfiguration(PORTRAIT, DEFAULT, LIGHT),
                    TestConfiguration(LANDSCAPE, SMALL, DARK),
                    TestConfiguration(
                        PORTRAIT,
                        SMALL,
                        LIGHT,
                        languageCode = "ar"
                    ),
                    TestConfiguration(
                        PORTRAIT,
                        SMALL,
                        LIGHT,
                        languageCode = "bn"
                    ),
                    TestConfiguration(
                        PORTRAIT,
                        SMALL,
                        LIGHT,
                        languageCode = "cy"
                    ),
                    TestConfiguration(
                        PORTRAIT,
                        SMALL,
                        LIGHT,
                        languageCode = "gu"
                    ),
                    TestConfiguration(
                        PORTRAIT,
                        SMALL,
                        LIGHT,
                        languageCode = "pa"
                    ),
                    TestConfiguration(
                        PORTRAIT,
                        SMALL,
                        LIGHT,
                        languageCode = "ro"
                    ),
                    TestConfiguration(
                        PORTRAIT,
                        SMALL,
                        LIGHT,
                        languageCode = "tr"
                    ),
                    TestConfiguration(
                        PORTRAIT,
                        SMALL,
                        LIGHT,
                        languageCode = "ur"
                    ),
                    TestConfiguration(
                        PORTRAIT,
                        SMALL,
                        LIGHT,
                        languageCode = "zh"
                    ),
                    TestConfiguration(
                        PORTRAIT,
                        SMALL,
                        LIGHT,
                        languageCode = "pl"
                    ),
                    TestConfiguration(
                        PORTRAIT,
                        SMALL,
                        LIGHT,
                        languageCode = "so"
                    )
                )
            } else {
                listOf(
                    TestConfiguration(
                        PORTRAIT,
                        DEFAULT,
                        LIGHT,
                        languageCode = "en"
                    )
                )
            }
        }
    }
}
