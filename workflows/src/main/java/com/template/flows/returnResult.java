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

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
public class returnResult {
    @InitiatingFlow
    @StartableByRPC
    public static class returnResultInitiator extends FlowLogic<String>{
        private Party receiver;
        private UniqueIdentifier id;
        private String result1;
        private String result2;

        private BigInteger encrypted_result;
        private Party sender;
        private de.henku.jpaillier.PublicKey pk;

        de.henku.jpaillier.PublicKey key1 = new de.henku.jpaillier.PublicKey(de.henku.jpaillier.key1.getN(), de.henku.jpaillier.key1.getnSquared(),
                de.henku.jpaillier.key1.getG(), de.henku.jpaillier.key1.getBits());
        de.henku.jpaillier.PublicKey key2 = new de.henku.jpaillier.PublicKey(de.henku.jpaillier.key2.getN(), de.henku.jpaillier.key2.getnSquared(),
                de.henku.jpaillier.key2.getG(), de.henku.jpaillier.key2.getBits());
        de.henku.jpaillier.PublicKey key3 = new de.henku.jpaillier.PublicKey(de.henku.jpaillier.key3.getN(), de.henku.jpaillier.key3.getnSquared(),
                de.henku.jpaillier.key3.getG(), de.henku.jpaillier.key3.getBits());

        public returnResultInitiator(Party receiver, UniqueIdentifier id, String result1,String result2,String key_name){
            this.receiver=receiver;
            this.id=id;
            this.result1=result1;
            this.result2=result2;
            switch (key_name){
                case "key2": this.pk = key2;
                    break;
                case "key3": this.pk = key3;
                    break;
                default:
                    this.pk = key1;
            }
        }

        @Override
        @Suspendable
        public String call() throws FlowException {
            long startTime = System.nanoTime();

            this.sender = getOurIdentity();
            // Step 1. Get a reference to the notary service on our network and our key pair.
            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
//            System.out.println("here");

            QueryCriteria.LinearStateQueryCriteria outputCriteria = new QueryCriteria.LinearStateQueryCriteria()
                    .withUuid(Arrays.asList(UUID.fromString(id.toString())))
                    .withStatus(Vault.StateStatus.UNCONSUMED)
                    .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT);

            StateAndRef work = getServiceHub().getVaultService().queryBy(workTask.class, outputCriteria).getStates().get(0);
            workTask original = (workTask) work.getState().getData();
            System.out.println("query success");

            BigInteger e_r1 = this.pk.encrypt(new BigInteger(result1));
            BigInteger e_r2 = this.pk.encrypt(new BigInteger(result2));
            BigInteger encryptedProduct = e_r1.multiply(e_r2).mod(pk.getnSquared());
            this.encrypted_result = encryptedProduct;
            workTask output = original.putResult(encrypted_result);
            List<AbstractParty> participants = output.getParticipants();
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

            final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);

            List<FlowSession> sessions = new ArrayList<>();
            for (AbstractParty party : participants) {
                sessions.add(initiateFlow(party));
            }

            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(ptx, sessions));
            System.out.println("sessions added");
            // Step 7. Assuming no exceptions, we can now finalise the transaction
            subFlow(new FinalityFlow(fullySignedTx, sessions));
            long endTime   = System.nanoTime();
            double totalTime = (double) (endTime - startTime) / 1_000_000_000;
            return "success, the result is: "+ output.getResult()+"\ntime taken: "+totalTime;
        }
    }
    @InitiatedBy(returnResultInitiator.class)
    public static class returnResultResponder extends FlowLogic<Void> {
        private final FlowSession counterpartySession;

        public returnResultResponder(FlowSession counterpartySession) {
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
