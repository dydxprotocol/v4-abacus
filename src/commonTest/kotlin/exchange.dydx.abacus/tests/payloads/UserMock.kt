package exchange.dydx.abacus.tests.payloads

internal class UserMock {
    internal val call = """
        {
          "user": {
            "publicId": "ODPJEHUJ",
            "ethereumAddress": "0x41da92325bd0c8f7a61b4dfda87d2b5a77e29d2c",
            "isRegistered": false,
            "email": "johnqh@yahoo.com",
            "username": "johnqh",
            "userData": {
              "preferences": {
                "userTradeOptions": {
                  "TAKE_PROFIT": {
                    "goodTilTimeInput": "28",
                    "goodTilTimeTimescale": "DAYS",
                    "selectedTimeInForceOption": "GTT"
                  },
                  "lastPlacedTradeType": "TAKE_PROFIT"
                }
              },
              "notifications": {
                "trade": {
                  "push": true
                },
                "deposit": {
                  "push": true
                },
                "transfer": {
                  "push": true
                },
                "withdrawal": {
                  "push": true
                },
                "liquidation": {
                  "push": true
                },
                "funding_payment": {
                  "push": true
                }
              }
            },
            "makerFeeRate": "0.000200",
            "takerFeeRate": "0.000500",
            "referralDiscountRate": null,
            "makerVolume30D": "314",
            "takerVolume30D": "630216.8782",
            "fees30D": "252.202934",
            "referredByAffiliateLink": null,
            "isSharingUsername": null,
            "isSharingAddress": null,
            "dydxTokenBalance": "0",
            "stakedDydxTokenBalance": "0",
            "activeStakedDydxTokenBalance": "0",
            "isEmailVerified": false,
            "country": "CK",
            "languageCode": "en",
            "hedgiesHeld": []
          }
        }
    """.trimIndent()
}
