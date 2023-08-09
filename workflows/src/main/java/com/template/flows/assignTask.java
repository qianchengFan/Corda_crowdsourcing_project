package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.sendContract;
import com.template.contracts.workContract;
import com.template.states.data;
import com.template.states.request;
import com.template.states.workTask;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.checkerframework.checker.units.qual.K;

import java.util.*;
import java.util.stream.Collectors;

public class assignTask {

    @InitiatingFlow
    @StartableByRPC
    public static class assignTaskInitiator extends FlowLogic<String>{
        private Party sender;
        private List<Party> receiver;
        private UniqueIdentifier taskId;

        public assignTaskInitiator(List<Party> receivers, UniqueIdentifier taskId){
            this.receiver = receivers;
            this.taskId = taskId;
        }

        @Override
        @Suspendable
        public String call() throws FlowException{
            long startTime = System.nanoTime();


            this.sender = getOurIdentity();
            final workTask output1;
            final workTask output2;
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));

            List<Party> participants = receiver;

//            System.out.println(0);

            QueryCriteria.LinearStateQueryCriteria outputCriteria = new QueryCriteria.LinearStateQueryCriteria()
                    .withUuid(Arrays.asList(UUID.fromString(taskId.toString())))
                    .withStatus(Vault.StateStatus.UNCONSUMED);
            StateAndRef task = getServiceHub().getVaultService().queryBy(data.class, outputCriteria).getStates().get(0);

            data original = (data) task.getState().getData();
//            System.out.println(original.getOwner()+"get");
//            System.out.println(getOurIdentity()+"our");
            if (!original.getOwner().toString().equals(getOurIdentity().toString())){
                return ("this task is not yours");
            }

            output1 = new workTask("work1",new UniqueIdentifier(),sender,participants,null,null);
            output2 = new workTask("work1",new UniqueIdentifier(),sender,participants,null,null);

            System.out.println("query success");
            output1.putId(taskId);
            output2.putId(taskId);
//            final data output = new data(msg,new UniqueIdentifier(),sender,participants);

            // Step 3. Create a new TransactionBuilder object.
            final TransactionBuilder builder = new TransactionBuilder(notary);

            // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
            builder.addOutputState(output1);
            builder.addOutputState(output2);
            builder.addCommand(new workContract.Commands.Send(), Arrays.asList(this.sender.getOwningKey()));
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
            subFlow(new FinalityFlow(selfsignedTransaction, sessions));
            long endTime   = System.nanoTime();
            double totalTime = (double) (endTime - startTime) / 1_000_000_000;

            return ("assign success.\ntask 1 is: "+output1.getLinearId()+"\ntask2 is: "+output2.getLinearId()+"\n total time taken is: "+totalTime);
        }
    }

    @InitiatedBy(assignTaskInitiator.class)
    public static class assignResponder extends FlowLogic<SignedTransaction>{
        //private variable
        private FlowSession counterSession;

        public assignResponder(FlowSession counterSession) {
            this.counterSession = counterSession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return subFlow(new ReceiveFinalityFlow(counterSession));
        }
    }
}
