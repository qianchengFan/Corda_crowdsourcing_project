package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.workflows.utilities.QueryUtilities;
import com.template.contracts.requestContract;
import com.template.states.CustomTokenState;
import com.template.states.request;
import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.*;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class requestFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class requestFlowInitiator extends FlowLogic<String> {

        private final String task;
        //private variables
        private Party sender ;
        private Party receiver;

        private Party viewer;
        private UniqueIdentifier id;
        private UniqueIdentifier tokenId;
        //public constructor
        public requestFlowInitiator(String task,UniqueIdentifier tokenId) {

            this.task = task;
            this.tokenId = tokenId;
        }

        @Override
        @Suspendable
        public String call() throws FlowException {
            long startTime = System.nanoTime();

            //Hello World message
            this.sender = getOurIdentity();
            this.id = new UniqueIdentifier();
//

            // Step 1. Get a reference to the notary service on our network and our key pair.
            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
            this.receiver = getServiceHub().getNetworkMapCache().getPeerByLegalName(CordaX500Name.parse("O=platform,L=London,C=GB"));
            //Compose the State that carries the Hello World message
            final request output = new request(sender,receiver,task,id,tokenId);

            // Step 3. Create a new TransactionBuilder object.
            final TransactionBuilder builder = new TransactionBuilder(notary);

            // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
            builder.addOutputState(output);
            builder.addCommand(new requestContract.Commands.Send(), this.sender.getOwningKey(),this.receiver.getOwningKey());

            // Step 5. Verify and sign it with our KeyPair.
            builder.verify(getServiceHub());
            final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);

            // Step 6. Collect the other party's signature using the SignTransactionFlow.
            List<Party> otherParties = output.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
            otherParties.remove(getOurIdentity());
            List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());

            SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, sessions));
            // Step 7. Assuming no exceptions, we can now finalise the transaction
            long endTime   = System.nanoTime();
            double totalTime = (double) (endTime - startTime) / 1_000_000_000;

            subFlow(new FinalityFlow(stx, sessions));
            return("request success, task id is: "+id+"\n total time taken is: "+totalTime);
        }
    }

    @InitiatedBy(requestFlowInitiator.class)
    public static class TemplateFlowResponder extends FlowLogic<Void>{
        //private variable
        private FlowSession counterpartySession;
        private int sum = 0;

        //Constructor
        public TemplateFlowResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    ContractState output = stx.getTx().getOutputs().get(0).getData();
                    Set<CustomTokenState> evolvableTokenTypeSet = getServiceHub().getVaultService().
                            queryBy(CustomTokenState.class).getStates().stream()
                            .map(StateAndRef::getState)
                            .map(TransactionState::getData).collect(Collectors.toSet());
                    if (evolvableTokenTypeSet.isEmpty()) {
                        System.out.println("you have no token");
                    }
                    List<String> stockAmountsAndNames = new ArrayList<>();

                    // The set will have multiple elements, because we retrieve all
                    for (CustomTokenState evolvableTokenType : evolvableTokenTypeSet) {
                        // get the pointer to the stock state
                        TokenPointer<CustomTokenState> tokenPointer = evolvableTokenType.toPointer(CustomTokenState.class);

                        // query balance or each different Token
                        Amount<TokenType> amount = QueryUtilities.tokenBalance(getServiceHub().getVaultService(), tokenPointer);

                        sum += amount.getQuantity();
                    }
                    // System.out.println(result);
                    if (sum<=4){
                        System.out.println("request failed,please send more");
                    }
                    else{
                        System.out.println("request success");
                    }
                    requireThat(require -> {
                        require.using("the input token should be at least 4", sum == 4);
                        return null;
                    });
                }
            });
            //Stored the transaction into data base.
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }
}
