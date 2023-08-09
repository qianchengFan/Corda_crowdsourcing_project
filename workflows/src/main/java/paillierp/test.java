//package paillierp;
//
//import paillierp.Paillier;
//import paillierp.PaillierThreshold;
//import java.math.BigInteger;
//import java.util.Random;
//import paillierp.key.KeyGen;
//import paillierp.key.PaillierPrivateThresholdKey;
//import paillierp.zkp.DecryptionZKP;
//public class test {
//    public static void main (String[] args){
//        System.out.println(" Create new keypairs.");
//        Random rnd = new Random();
//        PaillierPrivateThresholdKey [] keys = KeyGen.PaillierThresholdKey (128,3,2,rnd.nextLong());
//        System.out.println (" 3 keys are generated , with a threshold of 2.") ;
//        System . out . println (" 3 people use their keys : p1 , p2 , p3");
//        PaillierThreshold p1 = new PaillierThreshold ( keys [0]) ;
//        PaillierThreshold p2 = new PaillierThreshold ( keys [1]) ;
//        PaillierThreshold p3 = new PaillierThreshold ( keys [2]) ;
//        Paillier alice = new Paillier (keys[0].getPublicKey());
//
//        BigInteger msg = BigInteger.valueOf (135819283) ;
//        BigInteger Emsg = alice.encrypt ( msg ) ;
//        System . out . println (" Alice encrypts the message "+ msg +" and sends "+Emsg +" to everyone .") ;
//
//        System . out . println (" p1 receives the message and tries to decrypt all alone:");
//        BigInteger p1decrypt = p1 . decryptOnly ( Emsg ) ;
//        if ( p1decrypt . equals ( msg ) ) {
//            System . out . println (" p1 succeeds decrypting the message all alone .") ;
//        } else {
//            System . out . println (" p1 fails decrypting the message all alone . :(") ;
//        }
//        System . out . println (" p2 and p3 receive the message and " + " create a partial decryptions .") ;
//        DecryptionZKP p2share = p2.decryptProof ( Emsg ) ;
//        DecryptionZKP p3share = p3.decryptProof ( Emsg ) ;
//        // p2 sends the partial decryption to p3
//        // p3 sends the partial decryption to p2
//        System . out . println (" p2 receives the partial p3 ’ s partial decryption " +
//                " and attempts to decrypt the whole message using its own " +
//                " share twice ") ;
//
//        try {
//            BigInteger p2decrypt = p2.combineShares(p2share,p3share,p2share ) ;
//            if ( p2decrypt . equals ( msg ) ) {
//                System . out . println (" p2 succeeds decrypting the message with p3 .") ;
//            } else {
//                System . out . println (" p2 fails decrypting the message with p3 . :(") ;
//            }
//        } catch ( IllegalArgumentException e ) {
//            System . out . println (" p2 fails decrypting and throws an error ") ;
//        }
//
//    }
//}
