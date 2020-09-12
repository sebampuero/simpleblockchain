package com.blockchain.example.blockchain.rest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.blockchain.example.blockchain.Block;
import com.blockchain.example.blockchain.Blockchain;
import com.blockchain.example.blockchain.Transaction;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        if (!added)
            return "The block was discarded by the node";
        return "Block added to the chain";
    }

    @PostMapping("/new_transaction")
    public String newTransaction(@RequestBody Transaction transaction, 
                                    @RequestHeader("public-key") String pubKey) {
        transaction.setTimestamp(System.currentTimeMillis());
        try {
            blockchain.addNewTransaction(transaction, pubKey);
            return "Transaction added";
        } catch (Exception e) {
            e.printStackTrace();
            return "Could not add transaction: " + e.getMessage();
        }
    }

    @GetMapping("/chain")
    public Blockchain getChain() {
        return this.blockchain;
    }

    @GetMapping("/mine")
    public String mineUnconfirmedTransactions() {
        try {
            int result = blockchain.mine();
            if(result != -1){
                int chainLength = blockchain.getChain().size();
                blockchain.consensus().join();
                if(chainLength == blockchain.getChain().size()){
                    blockchain.announceNewBlock(blockchain.getLastBlock());
                }
                return "Block " + blockchain.getLastBlock().getIndex() + " is mined";
            }
            else
                return "No results to mine";
        } catch(Exception e) {
            return "Aborting mining process: " + e.getMessage();
        }
    }

    @PostMapping("/register_node")
    public Blockchain registerNode(@RequestBody String nodeAddress) {
        blockchain.addPeer(nodeAddress);
        return getChain();
    }

    @PostMapping("/register_with")
    public String registerWith(@RequestBody String nodeAddress, HttpServletRequest request) {
        //InetAddress IP = InetAddress.getLocalHost();
        String hostUrl = "http://10.8.0.3:8080";//"http://" + IP.getHostAddress() + ":" + request.getLocalPort();
        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(nodeAddress + "/api/register_node"))
                .POST(HttpRequest.BodyPublishers.ofString(hostUrl))
                .build();
        try {
            HttpResponse<String> httpResponse =  client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
            .orTimeout(5, TimeUnit.SECONDS)
            .whenComplete((response, throwable) -> {
                try {
                    if (throwable != null)
                        throw new Exception(throwable.getMessage());
                    Blockchain peerBlockchain = new Gson().fromJson(response.body(), Blockchain.class);
                    this.blockchain = Blockchain.createChainFromDump(peerBlockchain.getChain(), this.blockchain.getPeersAddresses());
                    Set<String> peers = peerBlockchain.getPeersAddresses();
                    peers = peers.stream().filter( peer -> !hostUrl.equals(peer)).collect(Collectors.toSet());
                    this.blockchain.updatePeers(peers);
                } catch (Exception e) {
                    throw new CompletionException(e.getMessage(), e);
                }
            }).join();
            if(httpResponse.statusCode() == 200) 
                return "Registration successful";
            else
                return "Registration failed";
        } catch (CompletionException e) {
            return "Registration failed because " + e.getMessage();
        }
    }

}