package com.blockchain.example.blockchain;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

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
        this.nonce = 0;
    }

    public String calculateHash() {
        String repr = String.valueOf(this.index) + 
            String.valueOf(this.timestamp) + 
            this.transactions + 
            this.previousHash + String.valueOf(this.nonce);
        return DigestUtils.sha256Hex(repr);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
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

    @Override
    public String toString() {
        return "Block [hash=" + hash + ", index=" + index + ", nonce=" + nonce + ", previousHash=" + previousHash
                + ", timestamp=" + timestamp + ", transactions=" + transactions + "]";
    }

    
}