package de.henku.jpaillier;

import java.math.BigInteger;
import java.util.Random;

public class key3 {
    private static int bits = 1024;
    private static BigInteger n = new BigInteger("88677334260296706347527372755737712129755952640135952819920440845690843759727095648029271893365779135563956110637018668306946235413873883398851984140625710212209415091545804640847664776628581524461245918007298668892936794148929600025467113987284317959105749361763363760183685118347563905771848943613172703913");
    private static BigInteger g = new BigInteger("174109714274362141581211037372076266844072920259730659905712090869500782439251777276009524417342991257874743714581951783762329515444694434286606488103158647225933734288853976159156514007053475854185636655031081407537062896990090371650554188380798283927869359091421286763258123727813002335219589658085337479007");
    private static BigInteger nSquared = new BigInteger("7863669611512392003513585151377003896933052850723940106112145374878239056604528733346954514181890337200038913988408749382961846400199970958926461161476543570801867840635060484280152327106663025619063766462471330952659738825018825828785285238037118289610150954876136715656535282560004336412957540400255376231165052792906810624624501761575433378274489796003694110894674224624159669966259639337522383511008447344509407883273394194827481708437056674870609853993006774417342657117637269124325234750971899444631868810990635995530083181612403436261047997398274218083329477929693426125665954047592776689382792741979565511569");
    private static BigInteger lambda = new BigInteger("22169333565074176586881843188934428032438988160033988204980110211422710939931773912007317973341444783890989027659254667076736558853468470849712996035156422826874559925121124264341910496039607406132625695712008643249808019031581866442233689265833393303263233355864233306465053106190551544192153485184167558692");
    private static BigInteger u = new BigInteger("63306382696771165892506034490788881274921659222429874037637140834478118444557429954424610401741691200592451074797108252086346420484096233188716586783347169806184966677032321766742538662949004356959803393654479416938926293081538256818214520496719062502632880361898752948753270014511174058610086457900174434141");
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
        BigInteger c = encrypt(new BigInteger("50"),bits,n,nSquared,g);
        BigInteger d = encrypt(new BigInteger("60"),bits,n,nSquared,g);
//        BigInteger output = decrypt(c,n,nSquared,lambda,u,upperBound);

        BigInteger encryptedProduct = c.multiply(d).mod(nSquared);
        BigInteger output = decrypt(encryptedProduct,n,nSquared,lambda,u,upperBound).divide(new BigInteger("2"));
//        key_pair1();
        System.out.println(output);
    }
}