package me.rahimklaber

import io.ktor.server.application.*
import me.rahimklaber.plugins.*
import me.rahimklaber.swap.EthereumSettings
import me.rahimklaber.swap.ethereum.AtomicSwapContract
import okio.ByteString.Companion.decodeHex
import org.stellar.sdk.*
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.gas.DefaultGasProvider


//fun main(args: Array<String>): Unit =
//    io.ktor.server.netty.EngineMain.main(args)

//fun main() {
//    val server = Server("https://horizon-testnet.stellar.org")
//    val serverKeyPair = KeyPair.fromSecretSeed("SDHA5STCIW2I3EHDUGLLDPY4DGCG2DCDZAGJ55AV4WRRIID43Q5E3TOP")
//    val clientKeyPair = KeyPair.fromSecretSeed("SCPMV6TCIF44UPASE5MC5L565OE4SQ3FOOEI5SGKSPEZZMST7K5GA2D6")
//    val lockKeyPair = KeyPair.random()
//
//    val clientAccount = server.accounts().account(clientKeyPair.accountId)
//    // the account that represents the hash-timelock
//    val serverAccount = server.accounts().account(serverKeyPair.accountId)
//    val amount = "10"
//
//    val secret = Random.nextBytes(32)
//    val digest = MessageDigest.getInstance("SHA-256")
//
//    val hash = digest.digest(secret)
//
//    val createLockTx = TransactionBuilder(clientAccount, Network.TESTNET)
//        .addOperation(CreateAccountOperation.Builder(lockKeyPair.accountId, "20").build())
//        .setTimeout(0)
//        .setBaseFee(100000)
//        .build()
//
//    createLockTx.sign(clientKeyPair)
//
//    val result = server.submitTransaction(createLockTx)
//
//    println("created lockaccount. hash: ${result.hash}. success: ${result.isSuccess}")
//
//    val lockAccount = server.accounts().account(lockKeyPair.accountId)
//
//
//    val claimTx = TransactionBuilder(lockAccount, Network.TESTNET)
//        .addOperation(
//            PaymentOperation.Builder(serverAccount.accountId, AssetTypeNative(), amount)
//                .build()
//        )
//        .addPreconditions(
//            TransactionPreconditions.builder()
//                .timeBounds(TimeBounds(0,0))
//                .extraSigner(
//                    Signer.ed25519PublicKey(serverKeyPair))
//                .extraSigner(
//                    Signer.sha256Hash(hash)
//                )
//                .build()
//            )
//        .setBaseFee(100000)
//        //toddo fees
//        .build()
//
//
//    claimTx.hash()
//
//    val tx = TransactionBuilder(clientAccount, Network.TESTNET)
////        .addOperation(CreateAccountOperation.Builder(account.accountId, "20").build())
//        .addOperation(
//            SetOptionsOperation.Builder()
//                .setSigner(Signer.preAuthTx(claimTx.hash()), 10)
////                .setSigner(Signer.preAuthTx(claimTx.), 10)
////                .setSigner(Signer.preAuthTx("reclaimtx".toByteArray()), 10)
////                .setSigner(Signer.ed25519PublicKey(KeyPair.fromPublicKey(byteArrayOf())), 0)
//                .setSourceAccount(lockAccount.accountId)
//                .build()
//        )
//        .setTimeout(0)
//        .setBaseFee(100000)
//        .build()
//
//    tx.sign(clientKeyPair)
//    tx.sign(lockKeyPair)
//
//    val lockAccountTxResult = server.submitTransaction(tx)
//
////    println("xdr: ${lockAccountTxResult.envelopeXdr.get()}")
//
//    println("locked Account. Hash : ${lockAccountTxResult.hash}. success: ${lockAccountTxResult.isSuccess}. xdrres : ${lockAccountTxResult.resultXdr.get()} ")
//
//    claimTx.sign(serverKeyPair)
//    claimTx.sign(secret)
//
//    val claimtxResult = server.submitTransaction(claimTx)
//
//    println("claimed tx: ${claimtxResult.hash}. success: ${claimtxResult.isSuccess}. xdrres : ${claimtxResult.resultXdr.get()}")
//
//}

suspend fun main(){
//    val server = Server("https://horizon-testnet.stellar.org")
//
//
//    val serverKeyPair = KeyPair.fromSecretSeed("SDHA5STCIW2I3EHDUGLLDPY4DGCG2DCDZAGJ55AV4WRRIID43Q5E3TOP")
//    println("accountid: ${serverKeyPair.accountId}")
//    val clientKeyPair = KeyPair.fromSecretSeed("SCPMV6TCIF44UPASE5MC5L565OE4SQ3FOOEI5SGKSPEZZMST7K5GA2D6")
//
//    val account = server.accounts().account(serverKeyPair.accountId)
//
//    val settings = StellarSettings(
//        server,
//        account,
//        "11",
//        Network.TESTNET,
//        serverKeyPair
//    )
//
//        val secret = Random.nextBytes(32)
//    val digest = MessageDigest.getInstance("SHA-256")
//
//    val hash = digest.digest(secret)
//
//    val lockResult = settings.lockOnStellar(account.accountId,hash)
//    if(lockResult !is Either.Right)
//        throw RuntimeException()
//
//    println(lockResult.value)
//
//    val claimResult = settings.claimOnStellar(lockResult.value.claimTx,secret)
//    if(claimResult !is Either.Right)
//    throw RuntimeException()
//
//    println(claimResult.value)
//
//    val serverRes = server.transactions().transaction(claimResult.value.txHash)
//
//    println(serverRes.hash)

//    val web3j = Web3j.build(HttpService())
//
//    println(web3j.ethBlockNumber().send().blockNumber)
//
//    val credentials = Credentials.create("85405fabcd42cb13da8b5706914e39d40278f616165b9cac11fdd18790f8aa81")
//
////    println(credentials.address)
//
//    println(web3j.ethGetBalance(credentials.address, DefaultBlockParameter.valueOf("latest")).send().balance)
//    val txManager = RawTransactionManager(web3j,credentials,1337)
//    val  contract = AtomicSwapContract.load("0x8cb9c881c4b5c6f7054ba3d5ac6d3c6e7312e48c",web3j,credentials,DefaultGasProvider())
//
//
//    val ethSettings = EthereumSettings(
//        server = web3j,
//        contract
//    )
    val keyPair = KeyPair.fromPublicKey("acf972fb32c26cb5b286a9833580c2ae7249f4e98ea5cfdc37e220b78e55fb3e".decodeHex().toByteArray())
    val result = keyPair.verify("hi".toByteArray(),"7f39753b4ba2207b69ffff27251d155c0f8f11c6e7a3568b6752cbb449940a58b6fcf6e6562b24b5f673f10c412f5860da1ebcb943ded10cb93becaf6e04870b".decodeHex().toByteArray())
    print(result)
}

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureRouting()
    configureHTTP()
    configureTemplating()
    configureSerialization()
    configureSockets()
}