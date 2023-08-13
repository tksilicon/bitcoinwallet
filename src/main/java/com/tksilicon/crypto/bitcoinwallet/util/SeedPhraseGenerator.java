package com.tksilicon.crypto.bitcoinwallet.util;

import org.web3j.crypto.MnemonicUtils;

import java.security.SecureRandom;
import java.util.List;

public class SeedPhraseGenerator {

    public static String generateSeedPhrase() {
        byte[] initialEntropy = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(initialEntropy);
        return MnemonicUtils.generateMnemonic(initialEntropy);
    }
}


