package com.blockchain.example.blockchain;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

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
        if(peers.size() > 0)
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
        while(!hash.startsWith("00")) { // maybe use a thread for this?
            block.setNonce(block.getNonce() + 1);
            hash = block.calculateHash();
        }

        return hash;
    }

    public Boolean addBlock(Block block, String proof) {
        String previousHash = this.getLastBlock().getHash();
        if(!previousHash.equals(block.getPreviousHash()))
            return false;
        if(!Blockchain.isValidProof(block, proof))
            return false;
        
        block.setHash(proof);
        this.chain.add(block);
        return true;
    }

    private static Boolean isValidProof(Block block, String block_hash) {
        return (block_hash.startsWith("00") && block_hash.equals(block.calculateHash()));
    }

    public  Block getLastBlock() {
        return this.chain.get(this.chain.size() - 1);
    }

    public void addNewTransaction(Transaction transaction) {
        this.unconfirmedTransactions.add(transaction);
    }

    public int mine() {
        if(this.unconfirmedTransactions.size() == 0)
            return -1;

        Block lastBlock = this.getLastBlock();
        Block newBlock = new Block(lastBlock.getIndex() + 1, 
                                    new ArrayList<>(this.unconfirmedTransactions), 
                                    System.currentTimeMillis(), 
                                    lastBlock.getHash());
        String proof = this.proofOfWork(newBlock);
        this.addBlock(newBlock, proof);
        this.unconfirmedTransactions.clear();
        return newBlock.getIndex();
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

    public boolean consensus() {
        List<Block> longestChain = null;
        int currentLength = this.chain.size();
        for ( String peer : this.peersAddresses) {
            WebClient client = WebClient.builder().baseUrl(peer).build();
            try {
                Mono<String> responseMono = client.get().uri("/api/chain").accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
                String chainJson = responseMono.block(Duration.ofSeconds(1));
                Blockchain blockchain = new Gson().fromJson(chainJson, Blockchain.class);
                if(blockchain.getChain().size() > currentLength && Blockchain.checkChainValidity(blockchain.getChain())){
                    longestChain = blockchain.getChain();
                    currentLength = blockchain.getChain().size();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                System.out.println("Problem connecting to peer or parsing its response " + peer);
            }
        }
        if(longestChain != null){
            System.out.println("Updating the chain");
            this.chain = longestChain;
            return true;
        }
        return false;
    }

    public void announceNewBlock(Block block) {
        for(String peer : this.peersAddresses) {
            WebClient client = WebClient.builder().baseUrl(peer).build();
            try {
                client.post().uri("/api/add_block").bodyValue(block).retrieve().bodyToMono(Void.class).block(Duration.ofSeconds(1));
                System.out.println("Announcing new block");
            }
            catch(Exception e) {
                e.printStackTrace();
                System.out.println("Problem sending new block to peer " + peer);
            }
        }
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