package me.rahimklaber.swap

import arrow.core.Either
import arrow.core.computations.either
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.stellar.sdk.*

class StellarSettings(
    val server: Server,
    val account: TransactionBuilderAccount,
    val amount: String,
    val network: Network,
    private val keyPair: KeyPair,
) {

    fun sign(tx: Transaction) {
        tx.sign(keyPair)
    }
}


sealed interface StellarLockError {
    val txHash: String

    data class FailedCreateHtlcAccount(override val txHash: String) : StellarLockError
    data class FailedSetHtlcOptions(override val txHash: String) : StellarLockError
}

sealed interface StellarClaimError {
    val txHash: String

    data class FailedClaim(override val txHash: String) : StellarClaimError
}

data class ClaimOnStellarResult(val txHash: String)

data class LockOnStellarResult(
    val htlcKeyPair: KeyPair,
    val claimTx: Transaction,
    val reclaimTx: Transaction,
    val setOptionsHash: String,
)

suspend fun StellarSettings.lock(
    claimAddress: String,
    hash: ByteArray,
) = either {
    // account representing the htlc
    val htlcKeyPair = KeyPair.random()
    val createHtlcAccountTx = TransactionBuilder(account, Network.TESTNET)
        .addOperation(CreateAccountOperation.Builder(htlcKeyPair.accountId, amount).build())
        .addOperation(
            PaymentOperation.Builder(htlcKeyPair.accountId, AssetTypeNative(), "2").build()
        )
        .setTimeout(0)
        .setBaseFee(100000)
        .build()

    sign(createHtlcAccountTx)

    val createHtlcAccountTxResult = Either.catch { server.submitTransaction(createHtlcAccountTx,true) }
        .mapLeft { StellarLockError.FailedCreateHtlcAccount(createHtlcAccountTx.hashHex()) }
        .bind()

//    println("create account hash: ${createHtlcAccountTx.hash()}")

    val htlcAccount = server.accounts().account(htlcKeyPair.accountId)

    val claimTx = TransactionBuilder(htlcAccount, network)
        .addOperation(
            PaymentOperation.Builder(claimAddress, AssetTypeNative(), amount)
                .build()
        )
        .addPreconditions(
            TransactionPreconditions.builder()
                .timeBounds(TimeBounds(0, 0))
//                .extraSigner(
//                    Signer.ed25519PublicKey(KeyPair.fromAccountId(claimAddress)))
                .extraSigner(
                    Signer.sha256Hash(hash)
                )
                .build()
        )
        .setBaseFee(100000)
        .build()

    val reclaimTx =
        TransactionBuilder(Account(htlcAccount.accountId, htlcAccount.sequenceNumber - 1), network)
            .addOperation(
                PaymentOperation.Builder(account.accountId, AssetTypeNative(), amount)
                    .build()
            )
            .addPreconditions(
                TransactionPreconditions.builder()
                    .timeBounds(
                        TimeBounds(
                            (System.currentTimeMillis() / 1000) + 300,
                            0
                        )
                    ) // claimable after 5 mins
                    .build()
            )
            .setBaseFee(100000)
            .build()

    val setOptionsTxHtlc = TransactionBuilder(account, network)
        .addOperation(
            SetOptionsOperation.Builder()
                .setSigner(Signer.preAuthTx(claimTx.hash()), 1)
                .setMasterKeyWeight(0)
                .setSourceAccount(htlcAccount.accountId)
                .build()
        ).addOperation(
            SetOptionsOperation.Builder()
                .setSigner(Signer.preAuthTx(reclaimTx.hash()), 1)
                .setSourceAccount(htlcAccount.accountId)
                .build()
        )
        .setTimeout(0)
        .setBaseFee(10000)
        .build()

    sign(setOptionsTxHtlc)
    setOptionsTxHtlc.sign(htlcKeyPair)

    val setOptionsHtlcResult = Either.catch { server.submitTransaction(setOptionsTxHtlc,true) }
        .mapLeft { StellarLockError.FailedSetHtlcOptions(setOptionsTxHtlc.hashHex()) }
        .bind()

//    println("SETOPTIONS is suceess: ${setOptionsHtlcResult.isSuccess}")
//    println("setoptions result : ${setOptionsHtlcResult.resultXdr}")
//    println("setoptions hash: ${setOptionsHtlcResult.hash}")


    return@either LockOnStellarResult(
        htlcKeyPair,
        claimTx,
        reclaimTx,
        setOptionsTxHtlc.hashHex()
    )

}

suspend fun StellarSettings.claim(
    claimTx: Transaction,
    secret: ByteArray,
) = either {

    claimTx.sign(secret)
//    sign(claimTx)

    val claimTxResult = Either.catch {
        withContext(Dispatchers.IO) { server.submitTransaction(claimTx,true) }
    }.mapLeft {
        StellarClaimError.FailedClaim(claimTx.hashHex())
    }.bind()

//    println()
//    println("claim is success: ${claimTxResult.isSuccess}")
//    println("claimresult xdr: ${claimTxResult.resultXdr}")

    return@either ClaimOnStellarResult(claimTx.hashHex())
}