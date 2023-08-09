package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokens;
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokensHandler;
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount;
import com.template.states.CustomTokenState;
import com.template.states.data;
import com.template.states.workTask;
import de.henku.jpaillier.PrivateKey;
import de.henku.jpaillier.PublicKey;
import de.henku.jpaillier.key_platform;
import kotlin.Unit;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class verify {
    @InitiatingFlow
    @StartableByRPC
    public static class verifyInitiator extends FlowLogic<String> {
        private UniqueIdentifier id;
        private Party verifier;
        private Party owner;
        private Integer sum;
        private UniqueIdentifier tokenId;

        private PublicKey key_p;

        private PrivateKey privateKey;



        public verifyInitiator(UniqueIdentifier id,String caller) {
            this.id = id;
            this.sum = 0;
            if(caller == "platform"){
                key_p = new de.henku.jpaillier.PublicKey(key_platform.getN(), key_platform.getnSquared(),
                        key_platform.getG(), key_platform.getBits());
                privateKey = new PrivateKey(de.henku.jpaillier.key_platform.getLambda(),de.henku.jpaillier.key_platform.getU());
            }
            else{
                System.err.println("invalid call");
            }
        }

        public static final BigInteger decrypt(BigInteger c, BigInteger n, BigInteger nSquared, BigInteger lambda, BigInteger u, BigInteger upperBound) {
            BigInteger p = c.modPow(lambda, nSquared).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);

            if (upperBound != null && p.compareTo(upperBound) > 0) {
                p = p.subtract(n);
            }
            return p;
        }

        @Override
        @Suspendable
        public String call() throws FlowException {
            long startTime = System.nanoTime();


            this.verifier = getOurIdentity();
            // Step 1. Get a reference to the notary service on our network and our key pair.
            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));

            QueryCriteria.LinearStateQueryCriteria outputCriteria = new QueryCriteria.LinearStateQueryCriteria()
                    .withUuid(Arrays.asList(UUID.fromString(id.toString())))
                    .withStatus(Vault.StateStatus.UNCONSUMED)
                    .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT);

            StateAndRef task = getServiceHub().getVaultService().queryBy(data.class, outputCriteria).getStates().get(0);
            data original = (data) task.getState().getData();
            System.out.println("query1 success");
            BigInteger result = decrypt(original.getResult(),key_platform.getN(),key_platform.getnSquared(),key_platform.getLambda(),key_platform.getU(),key_platform.getUpperBound());
            System.out.println("result is: "+result.intValue());
            this.sum = result.intValue();

            if (sum>=120) {
                this.owner = original.getOwner();
                this.tokenId = original.getTokenId();
                System.out.println("token id is: "+ tokenId);
                System.out.println("if pass");
                StateAndRef<CustomTokenState> tokenStateAndRef = getServiceHub().getVaultService().
                        queryBy(CustomTokenState.class).getStates().stream()
                        .filter(sf -> sf.getState().getData().getLinearId().equals(this.tokenId)).findAny()
                        .orElseThrow(() -> new IllegalArgumentException("token id=\"" + this.tokenId + "\" not found from vault"));
                System.out.println("query2 success");
                List<Party> observers = getServiceHub().getNetworkMapCache().getAllNodes().stream()
                        .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                        .collect(Collectors.toList());

                observers.remove(getOurIdentity());
                observers.remove(notary);
                CustomTokenState tokenState = tokenStateAndRef.getState().getData();
                TokenPointer tokenPointer = tokenState.toPointer();

                // With the pointer, we can create an instance of transferring Amount
                Amount<TokenType> amount = new Amount(2, tokenPointer);
                PartyAndAmount partyAndAmount = new PartyAndAmount(this.owner, amount);
                subFlow(new MoveFungibleTokens(partyAndAmount, observers));
                long endTime   = System.nanoTime();
                double totalTime = (double) (endTime - startTime) / 1_000_000_000;
                return "verify success, token sent to " + this.owner+"\n time taken: "+totalTime;
            } else {
                long endTime   = System.nanoTime();
                double totalTime = (double) (endTime - startTime) / 1_000_000_000;
                System.out.println("sum is: "+sum.intValue());
                return "verify fail, time taken: "+ totalTime;

            }
        }
    }

        @InitiatedBy(verifyInitiator.class)
        public static class verifyRes extends FlowLogic<Unit>{
            private FlowSession counterSession;

            public verifyRes(FlowSession counterSession) {
                this.counterSession = counterSession;
            }

            @Suspendable
            @Override
            public Unit call() throws FlowException {
                // Simply use the MoveFungibleTokensHandler as the responding flow
                return subFlow(new MoveFungibleTokensHandler(counterSession));
            }
        }
    }
