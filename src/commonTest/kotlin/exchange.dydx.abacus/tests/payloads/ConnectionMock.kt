package exchange.dydx.abacus.tests.payloads

class ConnectionMock {
    internal val connectedMessage = """
        {"type":"connected","connection_id":"3afef1ce-92d1-4f61-9009-14b656142d7e","message_id":0}
    """.trimIndent()

    internal val pongMessage = """
        {"type":"pong","connection_id":"3afef1ce-92d1-4f61-9009-14b656142d7e","message_id":3}
    """.trimIndent()
}
