package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.sendContract;
import com.template.states.data;
import com.template.states.workTask;
import de.henku.jpaillier.PrivateKey;
import de.henku.jpaillier.key_platform;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.*;

public class returnTaskResult {
    @InitiatingFlow
    @StartableByRPC
    public static class returnTaskInitiator extends FlowLogic<String> {
        private UniqueIdentifier id;
        private Party receiver;
        private Party sender;
        private List<UniqueIdentifier> works;
        private BigInteger sum;

        de.henku.jpaillier.PublicKey pkey1 = new de.henku.jpaillier.PublicKey(de.henku.jpaillier.key1.getN(), de.henku.jpaillier.key1.getnSquared(),
                de.henku.jpaillier.key1.getG(), de.henku.jpaillier.key1.getBits());
        de.henku.jpaillier.PublicKey pkey2 = new de.henku.jpaillier.PublicKey(de.henku.jpaillier.key2.getN(), de.henku.jpaillier.key2.getnSquared(),
                de.henku.jpaillier.key2.getG(), de.henku.jpaillier.key2.getBits());
        de.henku.jpaillier.PublicKey pkey3 = new de.henku.jpaillier.PublicKey(de.henku.jpaillier.key3.getN(), de.henku.jpaillier.key3.getnSquared(),
                de.henku.jpaillier.key3.getG(), de.henku.jpaillier.key3.getBits());

        de.henku.jpaillier.PublicKey key_p = new de.henku.jpaillier.PublicKey(key_platform.getN(), key_platform.getnSquared(),
                key_platform.getG(), key_platform.getBits());
        private PrivateKey key1 = new PrivateKey(de.henku.jpaillier.key1.getLambda(),de.henku.jpaillier.key1.getU());
        private PrivateKey key2 = new PrivateKey(de.henku.jpaillier.key2.getLambda(),de.henku.jpaillier.key2.getU());
        private PrivateKey key3 = new PrivateKey(de.henku.jpaillier.key3.getLambda(),de.henku.jpaillier.key3.getU());
        private PrivateKey pk;
        private de.henku.jpaillier.PublicKey pubk;
        public returnTaskInitiator(Party receiver, UniqueIdentifier id, List<UniqueIdentifier> works,String caller){
            this.receiver=receiver;
            this.works = works;
            this.id=id;
            this.sum = new BigInteger("0");
            switch (caller){
                case "key2": this.pk = key2;this.pubk = pkey2;

                    break;
                case "key3": this.pk = key3;this.pubk = pkey3;
                    break;
                default:
                    this.pk = key1;this.pubk = pkey1;
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


            this.sender = getOurIdentity();
            // Step 1. Get a reference to the notary service on our network and our key pair.
            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
            List<workTask> workList = new ArrayList<>();
            List<StateAndRef> workRefList = new ArrayList<>();
            Integer temp_sum = 0;
            for (UniqueIdentifier id : works){
                QueryCriteria.LinearStateQueryCriteria outputCriteria = new QueryCriteria.LinearStateQueryCriteria()
                        .withUuid(Arrays.asList(UUID.fromString(id.toString())))
                        .withStatus(Vault.StateStatus.UNCONSUMED)
                        .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT);

                StateAndRef work = getServiceHub().getVaultService().queryBy(workTask.class, outputCriteria).getStates().get(0);
                workRefList.add(work);
                workTask original = (workTask) work.getState().getData();
                workList.add(original);

                BigInteger encrypted_result = original.getResult();
                BigInteger decrypted_result = decrypt(encrypted_result,pubk.getN(),pubk.getnSquared(),pk.getLambda(),pk.getPreCalculatedDenominator(),null)
                        .divide(new BigInteger("2"));
                if (decrypted_result.intValue()>=60) {
                    temp_sum+=decrypted_result.intValue();
                }
            }
            sum = new BigInteger(temp_sum.toString());
            System.out.println("sum is: "+sum.intValue());
            if (sum.intValue() >= 120){
                QueryCriteria.LinearStateQueryCriteria outputCriteria = new QueryCriteria.LinearStateQueryCriteria()
                        .withUuid(Arrays.asList(UUID.fromString(id.toString())))
                        .withStatus(Vault.StateStatus.UNCONSUMED)
                        .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT);

                StateAndRef inputSR = getServiceHub().getVaultService().queryBy(data.class, outputCriteria).getStates().get(0);
                data input = (data) inputSR.getState().getData();

                BigInteger encrypted_result = key_p.encrypt(sum);
                data output = input.setResult(encrypted_result);
                output.setWorkIdList(works);
                List<AbstractParty> participants = output.getParticipants();

                final TransactionBuilder builder = new TransactionBuilder(notary);

                List<PublicKey> keys = new ArrayList<>();
                for (AbstractParty parties: participants){
                    keys.add(parties.getOwningKey());
                }
                participants.remove(getOurIdentity());
                // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
                builder.addInputState(inputSR);
                builder.addOutputState(output, sendContract.ID);
                builder.addCommand(new sendContract.Commands.Send(), keys);

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
                return ("valid, result "+output.getResult()+ "sent, works include are: "+output.getWorkIdList().get(0)+" and "+output.getWorkIdList().get(1)
                +" \ntime taken: "+totalTime);
                //-------------------------------------------------------------------------------------------
//
//
//
//                final TransactionBuilder builder2 = new TransactionBuilder(notary);
//                List<PublicKey> keys2 = new ArrayList<>();
//                workTask task1 = workList.get(0);
//                List<AbstractParty> participants2 = task1.getParticipants();
//                for (AbstractParty parties: participants2){
//                    keys2.add(parties.getOwningKey());
//                }
//                participants2.remove(getOurIdentity());
//                for (StateAndRef ref : workRefList) {
//                    builder2.addInputState(ref);
//                }
//                builder2.addCommand(new workContract.Commands.Send(), keys2);
//
//                builder2.verify(getServiceHub());
//
//                final SignedTransaction ptx2 = getServiceHub().signInitialTransaction(builder2);
//                List<FlowSession> sessions2 = new ArrayList<>();
//                for (AbstractParty party : participants2) {
//                    sessions2.add(initiateFlow(party));
//                }
//                final SignedTransaction fullySignedTx2 = subFlow(
//                        new CollectSignaturesFlow(ptx2, sessions2));
//                System.out.println("sessions2 added");
//                subFlow(new FinalityFlow(fullySignedTx2, sessions2));
//                return "everything finish, work consumed";
            }
            else{
                return "invalid, sub-work haven't finish";
            }

//            long validNumber2 = getServiceHub().getVaultService().queryBy(workTask.class,pageSpec).getStates().stream()
//                    .filter(x -> (x.getState().getData().getDataId().toString().equals(id.toString()))).count();
//            System.out.println(validNumber2+" number1");
//            long validNumber = getServiceHub().getVaultService().queryBy(workTask.class,pageSpec).getStates().stream()
//                    .filter(x -> (x.getState().getData().getDataId().toString().equals(id.toString()) && x.getState().getData().getResult().equals(1))).count();
//            System.out.println(validNumber+" number2");
//            if (validNumber >= 2){
//                return "true";
//            }
//            else{
//                return "false";
//            }
        }
    }

    @InitiatedBy(returnTaskInitiator.class)
    public static class returnTaskResponder extends FlowLogic<Void> {
        private final FlowSession counterpartySession;

        public returnTaskResponder(FlowSession counterpartySession) {
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
//            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }

}
