package de.henku.jpaillier;

import java.math.BigInteger;
import java.util.Random;

public class key1 {
    private static int bits = 1024;
    private static BigInteger n = new BigInteger("86144539297035571547091956002867287822364311869207845357626785075826263163866927903693597632646960298092994120840973578165432007524410726056717838967189374540495539052363758798482233236595058034926899521500398650314339211013461104851728062806382061511275048424253136999214290828095081173618471285004508574393");
    private static BigInteger g = new BigInteger("116290379215154210249965486883221331657798551869037995940179392042782613538638490711487604540492402552319836078427915786463309323353438926641176184224194989388781271293327354140300666724774783531088186238028941536771456951319925408508551386253356646842531338836719833203128707190131373317389797313397837352589");
    private static BigInteger nSquared = new BigInteger("7420881650698505843281637445258154756222508346168212365529323585477160840627887551521209732952360430620147026603208629020342160570481451589215372904970458352876064597616261819508720506912954259900255851327551897788011507810197955499895504968454621658381022518198899712060559522073427689974313518918415109777213092571669805229546447120561571471706533438895715143100036516495737808541407973488393705861135511784345008376776165465476188811368101459297278845331439164843585054741535844167394692274510953586244416168888712453137444586829312658083132866373612023168861182339570665366149836266816478574733937253057215318449");
    private static BigInteger lambda = new BigInteger("43072269648517785773545978001433643911182155934603922678813392537913131581933463951846798816323480149046497060420486789082716003762205363028358919483594677851245053356010850870163751006612316218601270524543078369512304339450492114338719266250917723846632483594246381887291195033987135572170709169820034608250");
    private static BigInteger u = new BigInteger("1521431225684206680303896418430421223901433673415602835866990525590304719645931007634882625368039959475349001460490277785340971171850458814870947609760524646217274885747293074774166905792169006381127988967006426629276938130602494528902466758693617409507775213787777877425702077433079945872153170751853781990");
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
        System.out.println(bits);
        System.out.println("N  "+publicKey.getN());
        System.out.println(n);
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
//        BigInteger c = encrypt(new BigInteger("50"),bits,n,nSquared,g);
//        BigInteger d = encrypt(new BigInteger("60"),bits,n,nSquared,g);
////        BigInteger output = decrypt(c,n,nSquared,lambda,u,upperBound);
//
//        BigInteger encryptedProduct = c.multiply(d).mod(nSquared);
//        BigInteger output = decrypt(encryptedProduct,n,nSquared,lambda,u,upperBound).divide(new BigInteger("2"));
////        key_pair1();
//        System.out.println(output);

        BigInteger b1 = new BigInteger("10664939547371224077514995172733242116130763821443462652848185077357300565954825602335438888023045156740800650510014664874075714574312907459819786530386834343642633211022607452166825883324241808496425122005023343396176242404629305221139124709942029053975372969892938596077454842793075972720137786027863781532773899882287492209678954624910035420063262266648543405762474714595347822330962833362584189352871533258644181722033433288940216899910964048031274615056677193743521014293729417793389056916636970720555089569076956979693306829334690417355905438749235461751909611534142459564020810378715123796590701392757073300526");
        System.out.println(b1.intValue());
        //        BigInteger b2 = new BigInteger("123");
//
//        // apply compareTo() method
//        int comparevalue = b1.compareTo(b2);
//        System.out.println(comparevalue);

    }
}
