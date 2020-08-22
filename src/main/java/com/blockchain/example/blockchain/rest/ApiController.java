package com.blockchain.example.blockchain.rest;

import com.blockchain.example.blockchain.PeersManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    @Autowired
    private PeersManager peersManager;

}