package me.rahimklaber.swap

import arrow.core.valid
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.stellar.sdk.*
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.responses.TransactionResponse
import org.stellar.sdk.xdr.SignerKey
import org.stellar.sdk.xdr.Uint256
import shadow.com.google.common.base.Optional
import java.security.SecureRandom
import java.util.Base64
import kotlin.random.asKotlinRandom

@Serializable
sealed interface ServerResponse {

    /**
     *
     *
     * @property hash Base64 encoded bytes.
     */
    @Serializable
    @SerialName("HASH_SERVER")
    data class Hash(val hash: String)

}

@Serializable
sealed interface ClientMessage {


    @Serializable()
    @SerialName("LOCKSTELLAR")
    // here hash is the base64 encoded hash
    data class StellarHashTimeLock(
        val hash: String,
        val hashTxXdr: String,
        val timeTxXdr: String,
        val lockedAccount: String
        ) : ClientMessage

    @Serializable
    @SerialName("HASH")
    data class Hash(val hash: String) : ClientMessage

    @Serializable
    data class InitInfo(val ethAddress: String) : ClientMessage
}



/**
 * Handles the case where the user has the token on Stellar but wants a version
 * on Polygon.
 *
 *
 */
fun CoroutineScope.stellarToPolygonServer(
    stellarSettings: StellarSettings,
    evmSettings: EthereumSettings,
    random : SecureRandom,
    messagesFromClient: Channel<ClientMessage>
) = produce {

    // receive init info from client
    val clientInfo = messagesFromClient.receive()
    require(clientInfo is ClientMessage.InitInfo)

    // client locks message
    val clientLock = messagesFromClient.receive()
    require(clientLock is ClientMessage.StellarHashTimeLock){"1st message from client is lock."}

    // we verify client lock info...
    //....

    // we lock on poly
    evmSettings.lock(clientInfo.ethAddress,Base64.getDecoder().decode(clientLock.hash))
    send("")

}
//
//class StellarToPolygonSettings(
//    val preimageSize: Int = 64,
//    val stellarServer: Server,
//    val stellarNetwork : Network = Network.PUBLIC,
//    val stellarPrivkey: String
//){
//    private val mutableOperationFlow = MutableSharedFlow<TransactionResponse>(0,100)
//    val operationSharedFlow = mutableOperationFlow.asSharedFlow()
//
//    init {
//        stellarServer.transactions()
//            .stream(object : EventListener<TransactionResponse>{
//                override fun onEvent(p0: TransactionResponse) {
//                    mutableOperationFlow.tryEmit(p0)
//                }
//
//                override fun onFailure(p0: Optional<Throwable>?, p1: Optional<Int>?) {
//                    TODO("Not yet implemented")
//                }
//
//            })
//    }
//
//    suspend fun waitForTxWithHashSigner(hash : String): TransactionResponse {
//        val flow = operationSharedFlow.filter {
//            it.signatures.first() == hash
//        }
//        return flow.first()
//    }
//}
//
///**
// * Function showing how to client stellar lock tx would look like
// *
// */
//suspend fun createStellarTx() {
//    val clientAccount = ""
//    // the account that represents the hash-timelock
//    val lockAccount = ""
//    val serverAccount = ""
//    val amount = ""
//
//    val claimTx = TransactionBuilder(Account(lockAccount, 0), Network.TESTNET)
//        .addOperation(
//            PaymentOperation.Builder(serverAccount, AssetTypeNative(), amount)
//                .build()
//        )
//        .addPreconditions(
//            TransactionPreconditions.builder().extraSigner(
//                SignerKey.Builder().hashX(
//                    Uint256(byteArrayOf())
//                ).build()
//            ).build()
//        )
//        //toddo fees
//        .build()
//
//    val tx = TransactionBuilder(Account(clientAccount, 0), Network.TESTNET)
//        .addOperation(CreateAccountOperation.Builder(lockAccount, "2").build())
//        .addOperation(
//            SetOptionsOperation.Builder()
//                .setSigner(Signer.preAuthTx("claimtx".toByteArray()), 10)
//                .setSigner(Signer.preAuthTx("reclaimtx".toByteArray()), 10)
//                .setSigner(Signer.ed25519PublicKey(KeyPair.fromPublicKey(byteArrayOf())), 0)
//                .setSourceAccount(lockAccount)
//                .build()
//        )
//
//    // claimtx
//    val claimtx = TransactionBuilder(Account("",1),Network.TESTNET)
//        .addOperation(
//            AccountMergeOperation.Builder("dest")
//                .build()
//        )
//
//    val reclaimTx = TransactionBuilder(Account("", 1), Network.TESTNET)
////        .addOperation(
////            SetOptionsOperation.Builder()
////                .se
////        )
//        .addTimeBounds(TimeBounds(/*time lock time*/1, 0))
//
//}
//
//
//suspend fun StellarToPolygonSettings.checkStellarLock(htlc: ClientMessage.StellarHashTimeLock) {
//    val accountResponse = stellarServer
//        .accounts()
//        .account(htlc.lockedAccount)
//
//    val signers = accountResponse.signers
//
//    require(signers.size == 2) {
//        "There should be two signers on the account."
//    }
//
//    require(accountResponse
//        .signers
//        .all { it.type == "preauth_tx" }) {"The signers should be preautherized"}
//    fun checkHashTx(txXdr : String){
//        val tx = Transaction.fromEnvelopeXdr(txXdr,stellarNetwork)
////        require(tx.toEnvelopeXdr().v1.tx.)
//    }
//
//
//
//
//
//}
