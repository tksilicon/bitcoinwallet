package com.tksilicon.crypto.bitcoinwallet.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SeedPhraseGenerator {

    static String path = System.getProperty("user.dir") + "/english.txt";

    public static void main(String... args){
        System.out.println("Your seedPhrase is: " +generateSeedPhrase());
    }

    public static String generateSeedPhrase() {
        final File bip39Dictionary = new File(path);
        List<String> seedPhraseWords = readSeedPhrases(bip39Dictionary);
        List<Integer> random12 = generate12RandomNumbers();
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (true) {
            if (i >= random12.size()) break;
            Integer integer = random12.get(i);
            builder.append(seedPhraseWords.get(integer));
            builder.append(" ");
            i++;
        }
        return builder.toString();
    }

    public static List<Integer> generate12RandomNumbers() {
        List<Integer> twelveRandomNumbers = new ArrayList<>();
        SecureRandom rand = new SecureRandom();
        // Setting the upper bound to generate
        // the random numbers between the specific range
        int upperbound = 2048;
        // using nextInt()
        for (int i = 0; i < 12; i++) {
            twelveRandomNumbers.add(rand.nextInt(upperbound));
        }
        return twelveRandomNumbers;
    }


    public static List<String> readSeedPhrases(File file) {
        List<String> seedPhraseWords = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                seedPhraseWords.add(line.trim());
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return seedPhraseWords;
    }

}
