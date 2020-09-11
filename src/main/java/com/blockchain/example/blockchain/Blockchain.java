package com.blockchain.example.blockchain;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.blockchain.example.blockchain.crypto.Crypto;
import com.google.gson.Gson;

import org.springframework.stereotype.Component;

/**
 * Blockchain class. Contains the chain of blocks, and exposes several methods
 * to mine blocks, check unconfirmed transactions and apply consensus.
 */
@Component
public class Blockchain {

    private List<Block> chain;
    private List<Transaction> unconfirmedTransactions;
    private Set<String> peersAddresses;

    public Blockchain() {
        this.chain = new ArrayList<>();
        this.unconfirmedTransactions = new ArrayList<>();
        this.peersAddresses = new HashSet<>();
        this.generateGenesisBlock();
    }

    public void addPeer(String peer) {
        this.peersAddresses.add(peer);
    }

    public void updatePeers(Set<String> peers) {
        if (peers.size() > 0)
            this.peersAddresses.addAll(peers);
    }

    private void generateGenesisBlock() {
        Block genesisBlock = new Block(0, Collections.emptyList(), 0, "0");
        genesisBlock.setHash(genesisBlock.calculateHash());
        this.chain.add(genesisBlock);
    }

    public String proofOfWork(Block block) {
        block.setNonce(0);

        String hash = block.calculateHash();
        while (!hash.startsWith("00")) { // maybe use a thread for this?
            block.setNonce(block.getNonce() + 1);
            hash = block.calculateHash();
        }

        return hash;
    }

    public Boolean addBlock(Block block, String proof) {
        String previousHash = this.getLastBlock().getHash();
        if (!previousHash.equals(block.getPreviousHash()))
            return false;
        if (!Blockchain.isValidProof(block, proof))
            return false;

        block.setHash(proof);
        this.chain.add(block);
        return true;
    }

    private static Boolean isValidProof(Block block, String block_hash) {
        return (block_hash.startsWith("00") && block_hash.equals(block.calculateHash()));
    }

    public Block getLastBlock() {
        return this.chain.get(this.chain.size() - 1);
    }

    public void addNewTransaction(Transaction transaction, String publicKey) throws Exception {
        byte[] pubkey = publicKey.getBytes();
        transaction.setPublicKey(Crypto.fromBytesToPubKey(pubkey));
        this.unconfirmedTransactions.add(transaction);
    }

    public int mine() {
        if(this.unconfirmedTransactions.size() == 0)
            return -1;

        Block lastBlock = this.getLastBlock();
        Block newBlock = new Block(lastBlock.getIndex() + 1, 
                                    new ArrayList<>(this.verifyTransactions()), 
                                    System.currentTimeMillis(), 
                                    lastBlock.getHash());
        String proof = this.proofOfWork(newBlock);
        this.addBlock(newBlock, proof);
        this.unconfirmedTransactions.clear();
        return newBlock.getIndex();
    }

    private ArrayList<Transaction> verifyTransactions() {
        for(Transaction tr : this.unconfirmedTransactions) {
            // verify
            // if there is an inconsistency, throw an exception
        }

        return null;
    }

    public List<Block> getChain() {
        return this.chain;
    }

    public static  boolean checkChainValidity(List<Block> chain) {
        boolean result = true;
        String prevHash = "0";

        for (Block block : chain) {
            String blockHash = block.getHash();
            if(!Blockchain.isValidProof(block, blockHash) || prevHash != block.getPreviousHash()){
                result = false;
                break;
            }
            prevHash = block.getHash();
        }

        return result;
    }

    public CompletableFuture<Void> consensus() {
        List<CompletableFuture<CompletableFuture<HttpResponse<String>>>> futures = this.peersAddresses.stream()
            .map(peer -> applyConsensus(peer))
            .collect(Collectors.toList());
        return CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[futures.size()])
        );
    }

    private CompletableFuture<CompletableFuture<HttpResponse<String>>> applyConsensus(final String peer) {
        return CompletableFuture.supplyAsync(() -> {
            final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(peer + "/api/chain"))
                .header("Content-Type", "application/json")
                .GET()
                .build();
            final HttpClient client = HttpClient.newHttpClient();
            return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .orTimeout(5, TimeUnit.SECONDS)
                .whenComplete((response, throwable) -> {
                   if(throwable != null || response.statusCode() != 200){
                       throw new CompletionException("Could not connect to peer " + peer + ", aborting consensus!", throwable);
                   } else {
                        Blockchain blockchain = new Gson().fromJson(response.body(), Blockchain.class);
                        if(blockchain.getChain().size() > this.chain.size() && Blockchain.checkChainValidity(blockchain.getChain())){
                            this.chain = blockchain.getChain();
                        }
                   }
                });
        });
    }

    public void announceNewBlock(Block block) {
        List<CompletableFuture<Void>> futures =  this.peersAddresses.stream()
                .map(peer -> announceBlockRequest(peer, block))
                .collect(Collectors.toList());
        CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[futures.size()])
        ).join();
    }

    private CompletableFuture<Void> announceBlockRequest(final String peer, final Block block) {
        return CompletableFuture.runAsync(() -> {
            final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(peer + "/api/add_block"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(block)))
                .build();
            final HttpClient client = HttpClient.newHttpClient();
            client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .orTimeout(5, TimeUnit.SECONDS)
                .thenApply(HttpResponse::statusCode)
                .whenComplete((statusCode, throwable) -> {
                    if (throwable != null || statusCode != 200) {
                        if (throwable != null) 
                            System.out.println(throwable.getMessage());
                        else
                            System.out.println("Could not announce new block");
                    } else
                        System.out.println("Announced new block");
                });
        });
    }

    public static Blockchain createChainFromDump(List<Block> dump, Set<String> ourPeers) throws Exception {
        Blockchain generatedChain = new Blockchain();
        generatedChain.setPeersAddresses(ourPeers);
        for(int i = 1; i < dump.size(); i++) {
            Block block = new Block(dump.get(i).getIndex(),
                dump.get(i).getTransactions(), 
                dump.get(i).getTimestamp(), 
                dump.get(i).getPreviousHash());
            String proof = dump.get(i).getHash();
            boolean added = generatedChain.addBlock(block, proof);
            if(!added)
                throw new Exception("Chain dump is tampered");
        }
        return generatedChain;
    }

    public Set<String> getPeersAddresses() {
        return peersAddresses;
    }

    public void setPeersAddresses(Set<String> peersAddresses) {
        this.peersAddresses = peersAddresses;
    }
    
}