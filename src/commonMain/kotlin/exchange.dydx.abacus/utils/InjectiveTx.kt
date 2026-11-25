package exchange.dydx.abacus.utils

internal object InjectiveTx {
    private const val MSG_TRANSFER_TYPE_URL = "/ibc.applications.transfer.v1.MsgTransfer"
    private const val PUBKEY_TYPE_URL = "/injective.crypto.v1beta1.ethsecp256k1.PubKey"
    private const val SIGN_MODE_DIRECT = 1L

    fun transferBody(
        channel: String,
        denom: String,
        amount: String,
        sender: String,
        receiver: String,
        timeoutTimestampNs: Long,
    ): ByteArray {
        val transfer = stringField(1, "transfer") +
            stringField(2, channel) +
            bytesField(3, coin(denom, amount)) +
            stringField(4, sender) +
            stringField(5, receiver) +
            bytesField(6, ByteArray(0)) +
            uint64Field(7, timeoutTimestampNs)
        return bytesField(1, any(MSG_TRANSFER_TYPE_URL, transfer))
    }

    fun authInfo(
        publicKey: ByteArray,
        sequence: Long,
        feeDenom: String,
        feeAmount: String,
        gasLimit: Long,
        feeGranter: String?,
    ): ByteArray {
        val modeInfo = bytesField(1, uint64Field(1, SIGN_MODE_DIRECT))
        val signerInfo = bytesField(1, any(PUBKEY_TYPE_URL, bytesField(1, publicKey))) +
            bytesField(2, modeInfo) +
            uint64Field(3, sequence)
        val fee = bytesField(1, coin(feeDenom, feeAmount)) +
            uint64Field(2, gasLimit) +
            stringField(4, feeGranter.orEmpty())
        return bytesField(1, signerInfo) + bytesField(2, fee)
    }

    fun signDoc(body: ByteArray, authInfo: ByteArray, chainId: String, accountNumber: Long): ByteArray {
        return bytesField(1, body) + bytesField(2, authInfo) + stringField(3, chainId) + uint64Field(4, accountNumber)
    }

    fun txRaw(body: ByteArray, authInfo: ByteArray, signature: ByteArray): ByteArray {
        return bytesField(1, body) + bytesField(2, authInfo) + bytesField(3, signature)
    }

    private fun coin(denom: String, amount: String): ByteArray {
        return stringField(1, denom) + stringField(2, amount)
    }

    private fun any(typeUrl: String, value: ByteArray): ByteArray {
        return stringField(1, typeUrl) + bytesField(2, value)
    }

    private fun stringField(field: Int, value: String): ByteArray {
        return if (value.isEmpty()) ByteArray(0) else bytesField(field, value.encodeToByteArray())
    }

    private fun bytesField(field: Int, value: ByteArray): ByteArray {
        return varint((field shl 3 or 2).toLong()) + varint(value.size.toLong()) + value
    }

    private fun uint64Field(field: Int, value: Long): ByteArray {
        return if (value == 0L) ByteArray(0) else varint((field shl 3).toLong()) + varint(value)
    }

    private fun varint(value: Long): ByteArray {
        val bytes = mutableListOf<Byte>()
        var remaining = value
        while (remaining >= 0x80) {
            bytes.add((remaining and 0x7f or 0x80).toByte())
            remaining = remaining ushr 7
        }
        bytes.add(remaining.toByte())
        return bytes.toByteArray()
    }
}
