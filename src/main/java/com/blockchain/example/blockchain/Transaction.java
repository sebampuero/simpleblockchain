package com.blockchain.example.blockchain;

import java.security.PublicKey;

public class Transaction {

    private String sender;
    private String receiver;
    private String data;
    private long timestamp;
    private String signature;
    private PublicKey publicKey;

    public Transaction(String sender, String receiver, String data, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.data = data;
        this.timestamp = timestamp;
    }

    public Transaction(String sender, String receiver, String data, long timestamp, String signature, PublicKey pubKey) {
        this.sender = sender;
        this.receiver = receiver;
        this.data = data;
        this.timestamp = timestamp;
        this.signature = signature;
        this.publicKey = pubKey;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "Transaction [data=" + data + ", receiver=" + receiver + ", sender=" + sender + ", timestamp="
                + timestamp + "]";
    }

}