package com.blockchain.example.blockchain;

public class Transaction {

    private String sender;
    private String receiver;
    private String data;
    private long timestamp;

    public Transaction(String sender, String receiver, String data, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.data = data;
        this.timestamp = timestamp;
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

    @Override
    public String toString() {
        return "Transaction [data=" + data + ", receiver=" + receiver + ", sender=" + sender + ", timestamp="
                + timestamp + "]";
    }

    
}