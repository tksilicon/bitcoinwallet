package com.tksilicon.crypto.bitcoinwallet.controllers;

import com.tksilicon.crypto.bitcoinwallet.service.MyWallet;
import org.bitcoinj.core.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    @Autowired
    MyWallet myWallet;

    @RequestMapping
    public String index() {
        return "Greetings from MyWallet Spring Boot!";
    }


    @GetMapping("/transactionHistory")
    public List<Pair<String, String>> getTxnHistory() {
        return myWallet.txHistory();
    }


    @GetMapping("/mybalance")
    public String getBalance() {
        return myWallet.walletBalance();
    }

    @PostMapping("/send")
    public String send(@RequestParam String amount, @RequestParam String address) {
        myWallet.send(amount, address);
        return "Done!";
    }

    @GetMapping("/myaddresses")
    public List<String> changedAddresses(){
       return myWallet.getChangeAddresses();
    }

}

