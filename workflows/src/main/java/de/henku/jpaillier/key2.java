package de.henku.jpaillier;

import java.math.BigInteger;
import java.util.Random;

public class key2 {
    private static int bits = 1024;
    private static BigInteger n = new BigInteger("79104394751515190885999914993342402954993902225543810179538926347169739878886315734608569099310585837709296025116493969679959694635672448223851452120750792032005858571848701569394644147083604811030072395445934602408725770475876370975430232708223361869035046840954254016781871429512801111377126858008985857639");
    private static BigInteger g = new BigInteger("31804276110119301204365257125626635015872757033811857013534526362359882141221799102632273299972411755118273717535883777712152133199033025644816776753425994843266116634320764736910908516428189671383723748441646305602261747145129643459922171816355099208543212131053964329337012715715998472502055000296371444332");
    private static BigInteger nSquared = new BigInteger("6257505269003544078437774851620619337924182570261360777233890650121179689663065071232879301073929101495682096998751672176149581818799233167962731266296544120309321724908691712949470135834821403115073299916284207909401951138687938427739738824716705574198576990540207025314927829612124451369499549377491462671489701525608659578006062326231715301833479429964487809451125732059813543197264940375868480833966719783123121452961958643661110419248812795204297796596853153979981230556797230949242445167014933374598561846040332342117289487102771638323766702212481532758356307599588255846110050854863854373743482161508374654321");
    private static BigInteger lambda = new BigInteger("13184065791919198480999985832223733825832317037590635029923154391194956646481052622434761516551764306284882670852748994946659949105945408037308575353458462373426136882023692300728339862003546213625352039439397405659690256625580950175821760011584507991356615577396019997106279150508921266923835278792534487176");
    private static BigInteger u = new BigInteger("62130039368932929944049082778272227457754921413298691679866250517971846487282334948318425105949602393286935853698871013973367350170422139393011378952982823912509405503230506365391866589224231700837641744885118670408608099473506052456255389208875456995883674922302410926020051093611351040442648931007110816260");
    private static BigInteger upperBound = null;
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
        BigInteger c = encrypt(new BigInteger("50"),bits,n,nSquared,g);
        BigInteger d = encrypt(new BigInteger("60"),bits,n,nSquared,g);
//        BigInteger output = decrypt(c,n,nSquared,lambda,u,upperBound);

        BigInteger encryptedProduct = c.multiply(d).mod(nSquared);
        BigInteger output = decrypt(encryptedProduct,n,nSquared,lambda,u,upperBound).divide(new BigInteger("2"));
//        key_pair1();
        System.out.println(output);
    }
}
