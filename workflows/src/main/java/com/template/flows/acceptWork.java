package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.workContract;
import com.template.states.workTask;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class acceptWork {
    @InitiatingFlow
    @StartableByRPC
    public static class acceptWorkInitiator extends FlowLogic<SignedTransaction> {

        //private variables
        private Party sender ;
        private Party receiver;

        private UniqueIdentifier id;

        //public constructor
        public acceptWorkInitiator(UniqueIdentifier id, Party receiver) {
            this.id = id;
            this.receiver=receiver;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            long startTime = System.nanoTime();

            this.sender = getOurIdentity();
            // Step 1. Get a reference to the notary service on our network and our key pair.
            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
            System.out.println("here");

            QueryCriteria.LinearStateQueryCriteria outputCriteria = new QueryCriteria.LinearStateQueryCriteria()
                    .withUuid(Arrays.asList(UUID.fromString(id.toString())))
                    .withStatus(Vault.StateStatus.UNCONSUMED)
                    .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT);

            StateAndRef work = getServiceHub().getVaultService().queryBy(workTask.class, outputCriteria).getStates().get(0);
            workTask original = (workTask) work.getState().getData();

            System.out.println("query success");

            workTask output = original.setWorker(sender);
//            System.out.println("parties are: "+output.getParticipants());
            List<AbstractParty> participants = output.getParticipants();
//            System.out.println("get success");

//            System.out.println("worker is "+output.getWorker());
            // Step 3. Create a new TransactionBuilder object.
            final TransactionBuilder builder = new TransactionBuilder(notary);

            List<PublicKey> keys = new ArrayList<>();
            for (AbstractParty parties: participants){
                keys.add(parties.getOwningKey());
            }
            participants.remove(getOurIdentity());
            // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
            builder.addInputState(work);
            builder.addOutputState(output, workContract.ID);
            builder.addCommand(new workContract.Commands.Send(), keys);

            // Step 5. Verify and sign it with our KeyPair.
            builder.verify(getServiceHub());

            System.out.println("verify Success");
            final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);
            System.out.println("transaction created");

            List<FlowSession> sessions = new ArrayList<>();
            for (AbstractParty party : participants) {
                sessions.add(initiateFlow(party));
            }

            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(ptx, sessions));
            System.out.println("sessions added");
            long endTime   = System.nanoTime();
            double totalTime = (double) (endTime - startTime) / 1_000_000_000;
            System.out.println("total time taken is: "+ totalTime);
            // Step 7. Assuming no exceptions, we can now finalise the transaction
            return subFlow(new FinalityFlow(fullySignedTx, sessions));
        }
    }

    @InitiatedBy(acceptWorkInitiator.class)
    public static class acceptWorkResponder extends FlowLogic<Void> {
        private final FlowSession counterpartySession;

        public acceptWorkResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                }
            });
            //Stored the transaction into data base.
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }
}
