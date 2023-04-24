package me.rahimklaber.swap

import arrow.core.Either
import arrow.core.computations.either
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.rahimklaber.swap.ethereum.AtomicSwapContract
import org.web3j.protocol.Web3j
import java.math.BigInteger
import java.security.MessageDigest

class EthereumSettings(
    val server: Web3j,
    val swapContract: AtomicSwapContract,
)

data class EthereumLockResult(val txHash: String)
data class EthereumClaimResult(val txHash: String)
sealed interface EthereumLockError{
    val cause: Throwable
    data class FailedLock(override val cause: Throwable) : EthereumLockError
}
sealed interface EthereumClaimError{
    val cause: Throwable
    data class FailedClaim(override val cause: Throwable) : EthereumLockError
}

suspend fun EthereumSettings.lock(
    claimAddress: String,
    hash: ByteArray,
) = either{
    return@either withContext(Dispatchers.IO){
        Either.catch {
            swapContract.addSwap(claimAddress,hash, BigInteger.TEN, BigInteger.TEN).send()
        }
            .map { EthereumLockResult(it.transactionHash) }
            .mapLeft { EthereumLockError.FailedLock(it) }
            .bind()
    }
}

suspend fun EthereumSettings.claim(
    secret: ByteArray
) = either{
    return@either withContext(Dispatchers.IO){
        Either.catch {
            val hash = MessageDigest.getInstance("SHA-256").digest(secret)
            swapContract.claim(secret,hash).send()
        }.map { EthereumClaimResult(it.transactionHash) }
            .mapLeft { EthereumClaimError.FailedClaim(it) }
            .bind()
    }
}