package me.rahimklaber.swap

import arrow.core.Either
import arrow.core.Eval
import arrow.core.computations.nullable
import io.ktor.util.encodeBase64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.stellar.sdk.*
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.responses.TransactionResponse
import org.stellar.sdk.xdr.SignerKey
import shadow.com.google.common.base.Optional
import java.security.SecureRandom
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


}



/**
 * Handles the case where the user has the token on Stellar but wants a version
 * on Polygon.
 *
 *
 */
fun CoroutineScope.stellarToPolygonServer(
    settings: StellarToPolygonSettings,
    random : SecureRandom,
    messagesFromClient: Channel<ClientMessage>
) = produce {

    val kp = KeyPair.fromSecretSeed(settings.stellarPrivkey)

    val account = withContext(Dispatchers.IO) {
        settings.stellarServer.accounts().account(kp.accountId)
    }

    //1. send our half of the hash, by connecting to us the client implicitly requests it.

    send(ServerResponse.Hash(random.asKotlinRandom().nextBytes(settings.preimageSize/2).encodeBase64()))

    //4. client has locked funds and sends info to us

    val stellarlock = messagesFromClient.receive()
    check(stellarlock is ClientMessage.StellarHashTimeLock) { "The first message from the client should be the timelock." }
    //check whether there is actual tokens locked on stellar
    settings.checkStellarLock(stellarlock)

    //lock tokens on polygon




}

class StellarToPolygonSettings(
    val preimageSize: Int = 64,
    val stellarServer: Server,
    val stellarNetwork : Network = Network.PUBLIC,
    val stellarPrivkey: String
){
    private val mutableOperationFlow = MutableSharedFlow<TransactionResponse>(0,100)
    val operationSharedFlow = mutableOperationFlow.asSharedFlow()

    init {
        stellarServer.transactions()
            .stream(object : EventListener<TransactionResponse>{
                override fun onEvent(p0: TransactionResponse) {
                    mutableOperationFlow.tryEmit(p0)
                }

                override fun onFailure(p0: Optional<Throwable>?, p1: Optional<Int>?) {
                    TODO("Not yet implemented")
                }

            })
    }

    suspend fun waitForTxWithHashSigner(hash : String): TransactionResponse {
        val flow = operationSharedFlow.filter {
            it.signatures.first() == hash
        }
        return flow.first()
    }
}

/**
 * Function showing how to client stellar lock tx would look like
 *
 */
suspend fun createStellarTx()  {
    val tx = TransactionBuilder(Account("",0), Network.TESTNET)
        .addOperation(
            SetOptionsOperation.Builder()
                .setSigner(Signer.preAuthTx("claimtx".toByteArray()),10)
                .setSigner(Signer.preAuthTx("reclaimtx".toByteArray()),10)
                .setSigner(Signer.ed25519PublicKey(KeyPair.fromPublicKey(byteArrayOf())),0)
                .build()
        )

    // claimtx
    val claimtx = TransactionBuilder(Account("",1),Network.TESTNET)
        .addOperation(
            AccountMergeOperation.Builder("dest")
                .build()
        )

    val reclaimTx = TransactionBuilder(Account("",1), Network.TESTNET)
        .addOperation(
            SetOptionsOperation.Builder()
                .se
        )

}


suspend fun StellarToPolygonSettings.checkStellarLock(htlc: ClientMessage.StellarHashTimeLock) {
    val accountResponse = stellarServer
        .accounts()
        .account(htlc.lockedAccount)

    val signers = accountResponse.signers

    require(signers.size == 2) {
        "There should be two signers on the account."
    }

    require(accountResponse
        .signers
        .all { it.type == "preauth_tx" }) {"The signers should be preautherized"}
    fun checkHashTx(txXdr : String){
        val tx = Transaction.fromEnvelopeXdr(txXdr,stellarNetwork)
//        require(tx.toEnvelopeXdr().v1.tx.)
    }





}
