package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.sendContract;
import com.template.states.data;
import com.template.states.request;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class spread {
    @InitiatingFlow
    @StartableByRPC
    public static class spreadInitiator extends FlowLogic<SignedTransaction> {

        //private variables
        private Party sender;
        private UniqueIdentifier id;
        private UniqueIdentifier tokenId;

        //public constructor
        public spreadInitiator(UniqueIdentifier id) {
            this.id = id;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            long startTime = System.nanoTime();


            this.sender = getOurIdentity();
            final data output1;
            final data output2;
            // Step 1. Get a reference to the notary service on our network and our key pair.
            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
            final Party worker = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=worker1_sup1,L=London,C=GB"));

            //Compose the State that carries the Hello World message
            List<Party> participants = getServiceHub().getNetworkMapCache().getAllNodes().stream()
                    .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                    .collect(Collectors.toList());
            participants.remove(worker);
            participants.remove(sender);
            participants.remove(notary);
//            System.out.println(0);

            QueryCriteria.LinearStateQueryCriteria outputCriteria = new QueryCriteria.LinearStateQueryCriteria()
                    .withUuid(Arrays.asList(UUID.fromString(id.toString())))
                    .withStatus(Vault.StateStatus.UNCONSUMED);
            StateAndRef task = getServiceHub().getVaultService().queryBy(request.class, outputCriteria).getStates().get(0);

            request original = (request) task.getState().getData();
            System.out.println(original.getTask());
            this.tokenId = original.getTokenId();
            if (Objects.equals(original.getTask(), "valid")){
                output1 = new data("task1",new UniqueIdentifier(),sender,participants,null,new BigInteger("0"),tokenId);
                output1.setTokenId(tokenId);
//                System.out.println("tokenid 1 is: "+output1.getTokenId());
                output2 = new data("task2",new UniqueIdentifier(),sender,participants,null,new BigInteger("0"),tokenId);
                output2.setTokenId(tokenId);
//                System.out.println("tokenid 2 is: "+output2.getTokenId());
            }
            else{
                return null;
            }
            System.out.println("query success");
//            System.out.println(1);
            // Step 3. Create a new TransactionBuilder object.
            final TransactionBuilder builder = new TransactionBuilder(notary);

            // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
            output2.setId(id);
            output1.setId(id);
            builder.addOutputState(output1);
            builder.addOutputState(output2);
            builder.addCommand(new sendContract.Commands.Send(), Arrays.asList(this.sender.getOwningKey()));
//            System.out.println(2);
            // Step 5. Verify and sign it with our KeyPair.
            builder.verify(getServiceHub());
            final SignedTransaction selfsignedTransaction = getServiceHub().signInitialTransaction(builder);
//            System.out.println(3);
            // Call finality Flow to notarise the transaction and record it in all participants ledger.
            List<FlowSession> sessions = new ArrayList<>();
            for (Party party : participants) {
                sessions.add(initiateFlow(party));
            }
//            System.out.println(4);
            System.out.println("task1: "+output1.getLinearId()+"\n task2: "+output2.getLinearId());
            long endTime   = System.nanoTime();
            double totalTime = (double) (endTime - startTime) / 1_000_000_000;
            System.out.println("time taken: "+totalTime);
            return subFlow(new FinalityFlow(selfsignedTransaction, sessions));
        }
    }

    @InitiatedBy(spreadInitiator.class)
    public static class spreadResponder extends FlowLogic<SignedTransaction>{
        //private variable
        private FlowSession counterSession;

        public spreadResponder(FlowSession counterSession) {
            this.counterSession = counterSession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return subFlow(new ReceiveFinalityFlow(counterSession));
        }
    }
}