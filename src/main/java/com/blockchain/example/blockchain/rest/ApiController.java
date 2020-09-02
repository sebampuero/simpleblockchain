package com.blockchain.example.blockchain.rest;

import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.blockchain.example.blockchain.Block;
import com.blockchain.example.blockchain.Blockchain;
import com.blockchain.example.blockchain.Transaction;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;


@RestController // makes all methods ResponseBody annotated. All methods get parsed to JSON
                // automatically when sending response
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private Blockchain blockchain;

    @GetMapping("/test")
    public String test() {
        return "OK";
    }

    @PostMapping("/add_block")
    public String addBlock(@RequestBody Block block) {
        String proof = block.getHash();
        boolean added = blockchain.addBlock(block, proof);
        if(!added)
            return "The block was discarded by the node";
        return "Block added to the chain";
    }

    @PostMapping("/new_transaction")
    public void newTransaction(@RequestBody Transaction transaction) {
        transaction.setTimestamp(System.currentTimeMillis());
        blockchain.addNewTransaction(transaction);
    }

    @GetMapping("/chain")
    public Blockchain getChain() {
        return this.blockchain;
    }

    @GetMapping("/mine")
    public String mineUnconfirmedTransactions() {
        int result = blockchain.mine();
        if(result != -1){
            int chainLength = blockchain.getChain().size();
            blockchain.consensus();
            if(chainLength == blockchain.getChain().size()){
                blockchain.announceNewBlock(blockchain.getLastBlock());
            }
            return "Block " + blockchain.getLastBlock().getIndex() + " is mined";
        }
        else
            return "No results to mine";
    }

    @PostMapping("/register_node")
    public Blockchain registerNode(@RequestBody String nodeAddress) {
        blockchain.addPeer(nodeAddress);
        return getChain();
    }

    @PostMapping("/register_with")
    public void registerWith(@RequestBody String nodeAddress, HttpServletRequest request) {
        //InetAddress IP = InetAddress.getLocalHost();
        String hostUrl = "http://10.8.0.3:8080";//"http://" + IP.getHostAddress() + ":" + request.getLocalPort();
        WebClient client = WebClient.builder().baseUrl(nodeAddress).build();
        client.post()
            .uri("/api/register_node")
            .bodyValue(hostUrl)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(response -> {
                try {
                    Blockchain peerBlockchain = new Gson().fromJson(response, Blockchain.class);
                    this.blockchain = Blockchain.createChainFromDump(peerBlockchain.getChain(), this.blockchain.getPeersAddresses());
                    Set<String> peers = peerBlockchain.getPeersAddresses();
                    peers = peers.stream().filter( peer -> !hostUrl.equals(peer)).collect(Collectors.toSet());
                    this.blockchain.updatePeers(peers);
                }catch(Exception e) {
                    System.out.println(e.toString());
                }
            });
    }

}