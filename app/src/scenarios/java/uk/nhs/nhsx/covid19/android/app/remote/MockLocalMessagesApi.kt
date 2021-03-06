package uk.nhs.nhsx.covid19.android.app.remote

import uk.nhs.nhsx.covid19.android.app.di.MockApiModule
import uk.nhs.nhsx.covid19.android.app.remote.data.ContentBlock
import uk.nhs.nhsx.covid19.android.app.remote.data.ContentBlockType.PARAGRAPH
import uk.nhs.nhsx.covid19.android.app.remote.data.LocalInformation.Notification
import uk.nhs.nhsx.covid19.android.app.remote.data.LocalMessagesResponse
import uk.nhs.nhsx.covid19.android.app.remote.data.NotificationMessage
import uk.nhs.nhsx.covid19.android.app.remote.data.TranslatableNotificationMessage
import java.time.Instant

class MockLocalMessagesApi : LocalMessagesApi {
    var response: LocalMessagesResponse = successResponse

    override suspend fun fetchLocalMessages() = MockApiModule.behaviour.invoke { response }

    companion object {
        val emptyResponse = LocalMessagesResponse(mapOf(), mapOf())
        val successResponse = LocalMessagesResponse(
            localAuthorities = mapOf(
                "E07000240" to listOf("message1")
            ),
            messages = mapOf(
                "message1" to Notification(
                    updated = Instant.parse("2021-05-19T14:59:13Z"),
                    contentVersion = 1,
                    translations = TranslatableNotificationMessage(
                        mapOf(
                            "en" to NotificationMessage(
                                head = "A new variant of concern is in your area",
                                body = "This is the body of the notification",
                                content = listOf(
                                    ContentBlock(
                                        type = PARAGRAPH,
                                        text = "There have been reported cases of a new variant in [postcode]. Here are some key pieces of information to help you stay safe in [local authority]."
                                    ),
                                    ContentBlock(
                                        type = PARAGRAPH,
                                        text = "There have been reported cases of a new variant in [postcode]. Here are some key pieces of information to help you stay safe",
                                        link = "http://example.com",
                                        linkText = "Click me"
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
