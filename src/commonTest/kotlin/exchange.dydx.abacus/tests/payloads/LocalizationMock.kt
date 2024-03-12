package exchange.dydx.abacus.tests.payloads

class LocalizationMock {
    internal val appMock = """
       {
         "APP": {
           "GENERAL": {
             "TIME_STRINGS": {
                  "ALL_TIME": "all time"
             },
             "ABOUT": "About"
           }
           }
        }
    """.trimIndent()

    internal val appMock2 = """
       {
        "APP": {
          "GENERAL": {
             "TIME_STRINGS": {
                  "ALL_TIME2": "all time2, {PARAM1}"
             },
             "ABOUT2": "About2"
          }
          }
        }
    """.trimIndent()

    internal val appMock3 = """
       {
        "APP": {
          "GENERAL": {
             "TIME_STRINGS": {
                  "ALL_TIME2": "updated with appMock3"
             },
             "ABOUT2": "About2"
          }
          }
        }
    """.trimIndent()
}
