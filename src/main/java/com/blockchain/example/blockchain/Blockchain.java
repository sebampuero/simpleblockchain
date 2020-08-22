package com.blockchain.example.blockchain;

import java.lang.reflect.InaccessibleObjectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class Blockchain {

    private List<Block> chain;
    private List<Transaction> unconfirmedTransactions;
    private int difficulty = 2;

    public Blockchain() {
        this.chain = new ArrayList<>();
        this.unconfirmedTransactions = new ArrayList<>();
        this.generateGenesisBlock();
    }

    private void generateGenesisBlock() {
        Block genesisBlock = new Block(0, Collections.emptyList(), System.currentTimeMillis(), "0");
        this.chain.add(genesisBlock);
    }

    public String proofOfWork(Block block) {
        block.setNonce(0);

        String hash = block.calculateHash();
        while(!hash.startsWith("00")) { // check later
            block.setNonce(block.getNonce() + 1);
            hash = block.calculateHash();
        }

        return hash;
    }

    public Boolean addBlock(Block block, String proof) {
        String previousHash = this.getLastBlock().getHash();
        if(previousHash != block.getPreviousHash())
            return false;
        if(!this.isValidProof(block, proof))
            return false;
        
        block.setHash(proof);
        this.chain.add(block);
        return true;
    }

    private Boolean isValidProof(Block block, String block_hash) {
        return (block_hash.startsWith("00") && block_hash == block.calculateHash());
    }

    public  Block getLastBlock() {
        if(this.chain.size() == 0)
            throw new InaccessibleObjectException();
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
                                    this.unconfirmedTransactions, 
                                    System.currentTimeMillis(), 
                                    lastBlock.getPreviousHash());
        String proof = this.proofOfWork(newBlock);
        this.addBlock(newBlock, proof);
        this.unconfirmedTransactions.clear();
        return newBlock.getIndex();
    }
    
}