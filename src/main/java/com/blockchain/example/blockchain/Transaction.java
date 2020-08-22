package com.blockchain.example.blockchain;

public class Transaction {

    private String sender;
    private String receiver;
    private String data;

    public Transaction(String sender, String receiver, String data) {
        this.sender = sender;
        this.receiver = receiver;
        this.data = data;
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
    
}