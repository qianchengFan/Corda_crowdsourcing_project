package de.henku.jpaillier;

import java.math.BigInteger;
import java.util.Random;

public class key_platform {
    private static int bits = 1024;
    private static BigInteger n = new BigInteger("115321335833207002192152758177951964458348867318604555903854960052573473475484296991345551637495551884239730740084502036995151774639563979297115978795719680561559033448398537625591606669312995206423190809052642063153759617569730917774586118137004434799009635627133542993754738620106861248922759966162945127701");
    private static BigInteger g = new BigInteger("79541614779504122117102072776604175529193989909940010802342938969125854747124928971610192770402424893669192607678650172866770662815923231408142620135985641813518913615733449152207485729529569834086929624721314455688411887313620109809660104380458075639241375943820069861185988890661914901870030218695780181294");
    private static BigInteger nSquared = new BigInteger("13299010498355313342527873696061227386306046637936008221136256593130988049736590148915458128319458862764538808095921280698749943228261321683160735321608303300438847480960895697701815771688226423031269759700574901561619614213534311137549942312787488216251844923979905215130463407521355455622977889811843918726616026173567841337272252012959745767408018359569411011719864683562905360709325247015020034208460541044091228325862504705772610463078812235448659124356597512199749582746546765623561837478581047793540605474591334123281553651468327990824931932256259861644245256577385367848259155796336098717031950973495197545401");
    private static BigInteger lambda = new BigInteger("28830333958301750548038189544487991114587216829651138975963740013143368368871074247836387909373887971059932685021125509248787943659890994824278994698929914664571671999761332616660264965890724401123206083724351848407159833293578046178045989290574971179218145761051209266906520653291370992445824874589263714376");
    private static BigInteger u = new BigInteger("31854088699069799657676564073780547605783634648572730618599858723356715419976110431967761791944316852788943669243333830655726015696418071452582865873358425918175685875218053186125516916053513890709885736221362416341707265631637086324135614272570987357290980517250854901417814719431244822940425658697806897690");
    private static BigInteger upperBound = null;
    public static void key_pair1(){
        KeyPair keypair;
        PublicKey publicKey;
        PrivateKey privatekey;
        KeyPairBuilder keygen = new KeyPairBuilder();
        keypair = keygen.generateKeyPair();
        publicKey = keypair.getPublicKey();
        System.out.println("public key");
        System.out.println("bits  "+publicKey.getBits());
        System.out.println("N  "+publicKey.getN());
        System.out.println("G  "+publicKey.getG());
        System.out.println("nSquared  "+publicKey.getnSquared());
        privatekey = keypair.getPrivateKey();
        System.out.println("private key");
        System.out.println("lambda  "+privatekey.getLambda());
        System.out.println("preCalculatedDenominator  "+privatekey.getPreCalculatedDenominator());
        System.out.println("upper bound  "+keypair.getUpperBound());
    }
    public static BigInteger getN() {
        return n;
    }

    public static int getBits() {
        return bits;
    }

    public static BigInteger getG() {
        return g;
    }

    public static BigInteger getLambda() {
        return lambda;
    }

    public static BigInteger getnSquared() {
        return nSquared;
    }

    public static BigInteger getU() {
        return u;
    }

    public static BigInteger getUpperBound() {
        return upperBound;
    }

    public static BigInteger encrypt(BigInteger m,int bits,BigInteger n,BigInteger nSquared,BigInteger g) {
        BigInteger r;
        do {
            r = new BigInteger(bits, new Random());
        } while (r.compareTo(n) >= 0);
        BigInteger result = g.modPow(m, nSquared);
        BigInteger x = r.modPow(n, nSquared);

        result = result.multiply(x);
        result = result.mod(nSquared);

        return result;
    }

    public static final BigInteger decrypt(BigInteger c, BigInteger n, BigInteger nSquared, BigInteger lambda, BigInteger u, BigInteger upperBound) {
        BigInteger p = c.modPow(lambda, nSquared).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);

        if (upperBound != null && p.compareTo(upperBound) > 0) {
            p = p.subtract(n);
        }
        return p;
    }

    public static void main (String[] args){
//        BigInteger c = encrypt(new BigInteger("50"),bits,n,nSquared,g);
//        BigInteger d = encrypt(new BigInteger("60"),bits,n,nSquared,g);
////        BigInteger output = decrypt(c,n,nSquared,lambda,u,upperBound);
//
//        BigInteger encryptedProduct = c.multiply(d).mod(nSquared);
        BigInteger e = new BigInteger("8284744816800307872948692169031340195745056160021279600984752211096692019674280449432337557657838513806775608405308300713284742912536237668872228222957674898580006352554411710318617632872649138667670738900203442019116491523656688395069441051610510873909224950560002450268504590526987928896238724608109281077348697592801472108770348690804449367198281114912105078825341278535165920128858651276984750750651592293560170989644757200934753604002198628253536610642737308146009432376888578129594688982229104343079942231941602001265864996236592807994357869566331738100186587113488945329994289711187354262621487062541506047362");
        BigInteger output = decrypt(e,n,nSquared,lambda,u,upperBound);
//        key_pair1();
        System.out.println(output);
    }
}
