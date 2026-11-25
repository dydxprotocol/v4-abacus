package exchange.dydx.abacus.utils

import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Expected bytes come from cosmjs (v4-client-js) signing the same transfer with the same key
class InjectiveTxTests {
    @Test
    fun testTransferBodyMatchesCosmjs() {
        assertEquals(InjectiveTxVectors.BODY, body().toHex())
    }

    @Test
    fun testAuthInfoMatchesCosmjs() {
        assertEquals(InjectiveTxVectors.PLAIN_AUTH_INFO, plainAuthInfo().toHex())
        assertEquals(InjectiveTxVectors.GRANTED_AUTH_INFO, grantedAuthInfo().toHex())
    }

    @Test
    fun testSignDocMatchesCosmjs() {
        val signDoc = InjectiveTx.signDoc(body(), plainAuthInfo(), "injective-888", InjectiveTxVectors.ACCOUNT_NUMBER)
        assertEquals(InjectiveTxVectors.PLAIN_SIGN_DOC, signDoc.toHex())
    }

    @Test
    fun testTxRawMatchesCosmjs() {
        val signature = InjectiveTxVectors.PLAIN_SIGNATURE.decodeBase64Bytes()
        assertEquals(InjectiveTxVectors.PLAIN_TX_RAW, InjectiveTx.txRaw(body(), plainAuthInfo(), signature).encodeBase64())
    }

    @Test
    fun testEvmToInjectiveAddress() {
        assertEquals(InjectiveTxVectors.ADDRESS, InjectiveTxVectors.EVM_ADDRESS.evmToInjectiveAddress())
        assertEquals(InjectiveTxVectors.EVM_ADDRESS.lowercase(), InjectiveTxVectors.ADDRESS.toInjectiveEvmAddress())
        assertNull("0x1234".evmToInjectiveAddress())
        assertNull("0xzz9Fd6e51aad88F6F4ce6aB8827279cffFb92266".evmToInjectiveAddress())
    }

    private fun body(): ByteArray {
        return InjectiveTx.transferBody(
            channel = "channel-258",
            denom = "erc20:0xa00C59fF5a080D2b954d0c75e46E22a0c371235a",
            amount = "2500000",
            sender = InjectiveTxVectors.ADDRESS,
            receiver = "dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
            timeoutTimestampNs = 1_800_000_000_000_000_000,
        )
    }

    private fun plainAuthInfo(): ByteArray {
        return InjectiveTx.authInfo(
            publicKey = InjectiveTxVectors.PUBLIC_KEY.decodeBase64Bytes(),
            sequence = InjectiveTxVectors.SEQUENCE,
            feeDenom = "inj",
            feeAmount = "450000000000000",
            gasLimit = 450_000,
            feeGranter = null,
        )
    }

    private fun grantedAuthInfo(): ByteArray {
        return InjectiveTx.authInfo(
            publicKey = InjectiveTxVectors.PUBLIC_KEY.decodeBase64Bytes(),
            sequence = InjectiveTxVectors.SEQUENCE,
            feeDenom = "inj",
            feeAmount = "600000000000000",
            gasLimit = 500_000,
            feeGranter = "inj1granter",
        )
    }
}

// Derived from the mnemonic "test test test test test test test test test test test junk" at m/44'/60'/0'/0/0
internal object InjectiveTxVectors {
    const val ADDRESS = "inj17w0adeg64ky0daxwd2ugyuneellmjgnxf5vkec"
    const val EVM_ADDRESS = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266"
    const val PUBLIC_KEY = "A4MYU1tUEF1Keq5gwI/EX5aHGBtP38YlvRp1P6c5f+11"
    const val ACCOUNT_NUMBER = 173471L
    const val SEQUENCE = 3L

    const val BODY =
        "0ae7010a292f6962632e6170706c69636174696f6e732e7472616e736665722e76312e4d73675472616e7366657212b9010a087472616e73666572120b6368616e6e656c2d3235381a3b0a3065726332303a307861303043353966463561303830443262393534643063373565343645323261306333373132333561120732353030303030222a696e6a313777306164656736346b7930646178776432756779756e65656c6c6d6a676e786635766b65632a2b6479647831347a7a7565617a656830686a36376367686866396a7970736c6366397368326e356b366172743200388080d09de9ceb8fd18"
    const val PLAIN_AUTH_INFO =
        "0a5e0a540a2d2f696e6a6563746976652e63727970746f2e763162657461312e657468736563703235366b312e5075624b657912230a21038318535b54105d4a7aae60c08fc45f9687181b4fdfc625bd1a753fa7397fed7512040a0208011803121c0a160a03696e6a120f34353030303030303030303030303010d0bb1b"
    const val GRANTED_AUTH_INFO =
        "0a5e0a540a2d2f696e6a6563746976652e63727970746f2e763162657461312e657468736563703235366b312e5075624b657912230a21038318535b54105d4a7aae60c08fc45f9687181b4fdfc625bd1a753fa7397fed7512040a020801180312290a160a03696e6a120f36303030303030303030303030303010a0c21e220b696e6a316772616e746572"
    const val PLAIN_SIGN_DOC = "0aea01" + BODY + "127e" + PLAIN_AUTH_INFO + "1a0d696e6a6563746976652d383838209fcb0a"
    const val PLAIN_SIGNATURE = "hrnFP/Yawppx1mvExBw6pdF1iflF/H/zgQN5uJEfoPllo0OYDPNFz+ZQswzxMwdOmd5jlIdNjMCL3h6ifk8Mtw=="
    const val PLAIN_TX_RAW =
        "CuoBCucBCikvaWJjLmFwcGxpY2F0aW9ucy50cmFuc2Zlci52MS5Nc2dUcmFuc2ZlchK5AQoIdHJhbnNmZXISC2NoYW5uZWwtMjU4GjsKMGVyYzIwOjB4YTAwQzU5ZkY1YTA4MEQyYjk1NGQwYzc1ZTQ2RTIyYTBjMzcxMjM1YRIHMjUwMDAwMCIqaW5qMTd3MGFkZWc2NGt5MGRheHdkMnVneXVuZWVsbG1qZ254ZjV2a2VjKitkeWR4MTR6enVlYXplaDBoajY3Y2doaGY5anlwc2xjZjlzaDJuNWs2YXJ0MgA4gIDQnenOuP0YEn4KXgpUCi0vaW5qZWN0aXZlLmNyeXB0by52MWJldGExLmV0aHNlY3AyNTZrMS5QdWJLZXkSIwohA4MYU1tUEF1Keq5gwI/EX5aHGBtP38YlvRp1P6c5f+11EgQKAggBGAMSHAoWCgNpbmoSDzQ1MDAwMDAwMDAwMDAwMBDQuxsaQIa5xT/2GsKacdZrxMQcOqXRdYn5Rfx/84EDebiRH6D5ZaNDmAzzRc/mULMM8TMHTpneY5SHTYzAi94eon5PDLc="

    // Sign bytes always end with the auth info, chain id and account number
    const val SIGN_DOC_SUFFIX = "1a0d696e6a6563746976652d383838209fcb0a"
}

internal fun ByteArray.toHex(): String {
    return joinToString(separator = "") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
}
