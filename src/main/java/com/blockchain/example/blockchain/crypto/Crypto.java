package com.blockchain.example.blockchain.crypto;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class Crypto {

    private static final String ALGORITHM = "RSA";

    public static PublicKey fromBytesToPubKey(byte[] pubkeyBytes) throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(pubkeyBytes);
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
        return kf.generatePublic(spec);
    }

    public static byte[] decrypt(byte[] pubkey, byte[] inputData) throws Exception {
        PublicKey pubKey = KeyFactory.getInstance(ALGORITHM)
            .generatePublic(new X509EncodedKeySpec(pubkey));
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        return cipher.doFinal(inputData);
    }
    
}
