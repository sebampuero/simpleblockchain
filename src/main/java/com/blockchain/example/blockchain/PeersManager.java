package com.blockchain.example.blockchain;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class PeersManager {

    private Set<String> peers;

    public PeersManager() {
        this.peers = new HashSet<>();
    }

    public Set<String> getPeers() {
        return peers;
    }

    public void addPeer(String address) {
        this.peers.add(address);
    }

    public Boolean deletePeer(String address) {
        if(!this.peers.contains(address))
            return false;
        this.peers.remove(address);
        return true;
    }
    
}