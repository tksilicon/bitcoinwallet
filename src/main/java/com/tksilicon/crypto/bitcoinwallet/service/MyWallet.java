package com.tksilicon.crypto.bitcoinwallet.service;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class MyWallet {

    static NetworkParameters params = TestNet3Params.get();
    static String path = System.getProperty("user.home") + "/wallet";
    private static File walletFile = new File(path);
    private final static String APP_NAME = "MyWallet";
    private static WalletAppKit kit;

    public MyWallet() {
        kit = new WalletAppKit(params, walletFile, APP_NAME/*this is the filePrefix*/) {
            @Override
            protected Wallet createWallet() {
                Wallet wallet;
                try {
                    /**
                     *  //1 -> obsolete but still works
                     *  Wallet wallet = new Wallet(params);
                     *  The constructor above has been depreciated in favour of {@link Wallet#createBasic(params)}
                     *
                     *   We can create deterministic wallets and specify the script type, that is the type of bitcoin we
                     *   want to create. Read my article to learn more about the addresses
                     *
                     *   //2 -> different script types static method
                     *   Wallet wallet = Wallet.createDeterministic(params, Script.ScriptType.P2PKH);
                     *   Wallet wallet = Wallet.createDeterministic(params, Script.ScriptType.P2SH);
                     *   wallet = Wallet.createDeterministic(params, Script.ScriptType.P2WPKH);
                     *   Wallet wallet = Wallet.createDeterministic(params, Script.ScriptType.P2PK);
                     *
                     *
                     */

                    //3 -> SeedCode static method
                    // Here is the pudding. We will create a wallet with a seedPhrase or seedCode
                    // Instead of importing  EcKey. We can also get this EcKey to retrieve the private and public key
                    // The seedPhrase is a human-readable mnemonic that can be used to construct the private key
                    // This is what we find in cryptoCurrency wallet like trust wallet.
                    String seedPhrase = "yard impulse luxury drive today throw farm pepper survey wreck glass federal";
                    //long creationTime = 1409478661L; earliest epoch
                    //1691839946911 - Sat Aug 12 12:32:56 BST 2023
                    long creationTime = 1691839946911L;
                    DeterministicSeed seed = new DeterministicSeed(seedPhrase, null, "", creationTime);
                    //Using a seedPhrase will extend the 24 words seedPhrase
                    //DeterministicSeed seed = new DeterministicSeed(seedCode, null, "tango alpha", creationTime);

                    ChildNumber number = new ChildNumber(1);
                    //According to unchained, see link in references in article
                    //P2WPKH is the SegWit variant of P2PKH, which at a basic level, means that
                    //choosing this address type rather than older
                    //P2PKH addresses will help you save money on transaction fees when moving your bitcoin around.
                    //That is why I am using this script type. You can insect any other script type.
                    wallet = Wallet.fromSeed(params, seed, Script.ScriptType.P2WPKH, ImmutableList.of(number));

                    //wallet = new Wallet(params);
                } catch (UnreadableWalletException e) {
                    throw new RuntimeException(e);
                }


                System.out.println(wallet);
                return wallet;
            }

            //This listener is called when we are done creating the wallet
            @Override
            protected void onSetupCompleted() {
                super.onSetupCompleted();
                System.out.println(kit.wallet().currentReceiveAddress());
                System.out.println(kit.wallet().getTotalReceived().toFriendlyString());

            }
        };
        System.out.println("start syncing...");

        kit.startAsync();
        kit.awaitRunning();
        //If we start or restart, we see our balance
        System.out.println("kit.wallet().getBalance():" + kit.wallet().getBalance());
        //This listener listens to the bitcoin nodes and informs us when we receive coins
        kit.wallet().addCoinsReceivedEventListener(
                (wallet, tx, prevBalance, newBalance) -> {
                    Coin value = tx.getValueSentToMe(wallet);
                    System.out.println("Received tx for " + value.toFriendlyString());
                    Futures.addCallback(tx.getConfidence().getDepthFuture(6),
                            new FutureCallback<TransactionConfidence>() {
                                @Override
                                public void onSuccess(TransactionConfidence result) {
                                    System.out.println("Received tx " +
                                            value.toFriendlyString() + " is confirmed. ");
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                }
                            }, MoreExecutors.directExecutor());
                });
    }

    /**
     * We return the transaction history in a readable format see what we recieved and what we sent
     *
     * @return
     */

    public List<Pair<String, String>> txHistory() {
        List<Pair<String, String>> amounts = new ArrayList<>();
        List<Transaction> txx = kit.wallet().getTransactionsByTime();
        if (!txx.isEmpty()) {
            int i = 1;
            for (Transaction tx : txx) {
                System.out.println(i + "  ________________________");
                System.out.println("Date and Time: " + tx.getUpdateTime().toString());
                Double amountsTx = 0.00;
                amountsTx = Double.valueOf(String.valueOf(tx.getValueSentToMe(kit.wallet()).toFriendlyString()).replace("BTC", "").trim());
                if (amountsTx > 0) {
                    System.out.println("Amount Sent to me: " + tx.getValueSentToMe(kit.wallet()).toFriendlyString());
                    amounts.add(Pair.of("Received:", tx.getValueSentToMe(kit.wallet()).toFriendlyString()));
                }

                amountsTx = Double.valueOf(String.valueOf(tx.getValueSentFromMe(kit.wallet()).toFriendlyString()).replace("BTC", "").trim());

                if (amountsTx > 0) {
                    System.out.println("Amount Sent from me: " + tx.getValueSentFromMe(kit.wallet()).toFriendlyString());
                    amounts.add(Pair.of("Sent:", tx.getValueSentFromMe(kit.wallet()).toFriendlyString()));
                }

                long fee = (tx.getInputSum().getValue() > 0 ? tx.getInputSum().getValue() - tx.getOutputSum().getValue() : 0);
                System.out.println("Fee: " + Coin.valueOf(fee).toFriendlyString());
                System.out.println("Transaction Depth: " + tx.getConfidence().getDepthInBlocks());
                System.out.println("Transaction Blocks: " + tx.getConfidence().toString());
                System.out.println("Tx Hex: " + tx.getHashAsString());
                i++;
            }
        } else {

            System.err.println("No Transaction Found");
        }
        return amounts;
    }

    public String walletBalance() {
        return kit.wallet().getBalance().toFriendlyString();
    }

    public List<String> getChangeAddresses() {
        return kit.wallet().getIssuedReceiveAddresses().stream().map(sc->sc.toString()).collect(Collectors.toList());
    }

    /**
     * This method in the Library will try to send to a Legacy address ( eg Script.ScriptType.P2PKH) if it
     * fails, it will attempt to constructor a SegwitAddress (eg Script.ScriptType.P2WPKH)
     *
     * @param value amount to send
     * @param to    bitcoin address
     */
    public void send(String value, String to) {
        try {
            Address toAddress = Address.fromString(params, to);
            System.out.println("value" + value);
            SendRequest sendRequest = SendRequest.to(toAddress, Coin.parseCoin(value));
            System.out.println("sendRequest" + sendRequest);
            sendRequest.feePerKb = Coin.parseCoin("0.0000001");
            Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(), sendRequest);
            sendResult.broadcastComplete.addListener(() ->
                            System.out.println("Sent coins. Transaction hash is " + sendResult.tx.getTxId()),
                    MoreExecutors.directExecutor());
        } catch (InsufficientMoneyException e) {
            throw new RuntimeException(e);
        }
    }

}

