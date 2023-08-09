package com.template.flows;
import de.henku.jpaillier.*;
public class test2 {


    public static void main (String[] args){
        KeyPair keypair;
        PublicKey publicKey;
        KeyPairBuilder keygen = new KeyPairBuilder();
        keypair = keygen.generateKeyPair();
        publicKey = keypair.getPublicKey();
        System.out.println(publicKey.toString());
    }
}
