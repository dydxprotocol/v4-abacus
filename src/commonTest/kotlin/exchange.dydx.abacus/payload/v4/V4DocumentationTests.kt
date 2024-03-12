package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.Documentation
import exchange.dydx.abacus.output.FAQ
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class V4DocumentationTests {
    @Test
    fun testDecodeDocumentation() {
        val jsonString = """
            {
                "tradingRewardsFAQs": [
                    {
                        "questionLocalizationKey": "APP.TRADING_REWARDS.FAQ_WHO_IS_ELIGIBLE_QUESTION",
                        "answerLocalizationKey": "APP.TRADING_REWARDS.FAQ_WHO_IS_ELIGIBLE_ANSWER"
                    },
                    {
                        "questionLocalizationKey": "APP.TRADING_REWARDS.FAQ_HOW_DO_TRADING_REWARDS_WORK_QUESTION",
                        "answerLocalizationKey": "APP.TRADING_REWARDS.FAQ_HOW_DO_TRADING_REWARDS_WORK_ANSWER"
                    }
                ]
            }
        """.trimIndent()

        val expectedFAQs = listOf(
            FAQ("APP.TRADING_REWARDS.FAQ_WHO_IS_ELIGIBLE_QUESTION", "APP.TRADING_REWARDS.FAQ_WHO_IS_ELIGIBLE_ANSWER"),
            FAQ("APP.TRADING_REWARDS.FAQ_HOW_DO_TRADING_REWARDS_WORK_QUESTION", "APP.TRADING_REWARDS.FAQ_HOW_DO_TRADING_REWARDS_WORK_ANSWER"),
            // ... Add more FAQs as needed ...
        )

        val decodedDocumentation = Json.decodeFromString<Documentation>(jsonString)

        assertEquals(expectedFAQs.size, decodedDocumentation.tradingRewardsFAQs.size)
        for (i in expectedFAQs.indices) {
            assertEquals(expectedFAQs[i].questionLocalizationKey, decodedDocumentation.tradingRewardsFAQs[i].questionLocalizationKey)
            assertEquals(expectedFAQs[i].answerLocalizationKey, decodedDocumentation.tradingRewardsFAQs[i].answerLocalizationKey)
        }
    }
}
