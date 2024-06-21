package exchange.dydx.abacus.tests.payloads

object SkipTrackMock {
    val error = """{
  "code": 5,
  "message": "chain id not supported",
  "details": []
}"""
    val success = """
        {
          "tx_hash": "0x897a7464fe7736def48f5eb77ffe06f11beacadc9805d3f9237c17767567c00f"
        }
    """
}
