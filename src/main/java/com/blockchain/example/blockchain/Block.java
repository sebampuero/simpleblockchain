package com.blockchain.example.blockchain;

import java.util.List;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.Gson;

public class Block {

    private int index;
    private List<Transaction> transactions;
    private long timestamp;
    private String previousHash;
    private String hash;
    private int nonce;

    public Block(int index, List<Transaction> transactions, long timestamp, String previousHash) {
        this.index = index;
        this.timestamp = timestamp;
        this.transactions = transactions;
        this.previousHash = previousHash;
        this.hash = this.calculateHash();
    }

    public String calculateHash() {
        String jsonString = new Gson().toJson(this);
        return BCrypt.withDefaults().hash(12, jsonString.toCharArray()).toString();
    }

    public int getIndex() {
        return index;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }
}