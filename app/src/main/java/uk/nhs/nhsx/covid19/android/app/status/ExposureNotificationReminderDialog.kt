package uk.nhs.nhsx.covid19.android.app.status

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_exposure_notification_reminder.cancel
import kotlinx.android.synthetic.main.dialog_exposure_notification_reminder.hours_12
import kotlinx.android.synthetic.main.dialog_exposure_notification_reminder.hours_4
import kotlinx.android.synthetic.main.dialog_exposure_notification_reminder.hours_8
import kotlinx.android.synthetic.main.dialog_exposure_notification_reminder.minute_1
import uk.nhs.nhsx.covid19.android.app.ExposureApplication
import uk.nhs.nhsx.covid19.android.app.R
import uk.nhs.nhsx.covid19.android.app.R.plurals
import uk.nhs.nhsx.covid19.android.app.util.viewutils.setOnSingleClickListener
import java.time.Duration

class ExposureNotificationReminderDialog(
    context: Context,
    private val scheduleExposureNotification: (duration: Duration) -> Unit
) : BottomSheetDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_exposure_notification_reminder)

        initializeView()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun initializeView() {
        minute_1.isVisible = ExposureApplication.isTestBuild
        minute_1.setOnSingleClickListener {
            onRadioButtonClick(Duration.ofMinutes(1))
        }

        updateResumeRadioButton(hours_4, 4)
        updateResumeRadioButton(hours_8, 8)
        updateResumeRadioButton(hours_12, 12)

        cancel.setOnSingleClickListener { dismiss() }
    }

    private fun updateResumeRadioButton(
        radioButton: RadioButton,
        hours: Int
    ) {
        radioButton.text =
            context.resources.getQuantityString(plurals.resume_contact_tracing_hours, hours, hours)
        radioButton.setOnSingleClickListener {
            onRadioButtonClick(Duration.ofHours(hours.toLong()))
        }
    }

    private fun onRadioButtonClick(duration: Duration) {
        scheduleExposureNotification(duration)
        showNotificationReminderConfirmationDialog(duration)
        dismiss()
    }

    private fun showNotificationReminderConfirmationDialog(duration: Duration) {
        val builder = AlertDialog.Builder(context)
        val hours = duration.toHours().toInt().toString()

        builder.setTitle(
            context.getString(
                R.string.contact_tracing_notification_reminder_dialog_title,
                hours
            )
        )
        builder.setMessage(context.getString(R.string.contact_tracing_notification_reminder_dialog_message))
        builder.setPositiveButton(
            context.getString(R.string.okay)
        ) { _, _ ->
        }

        builder.show()
    }
}
