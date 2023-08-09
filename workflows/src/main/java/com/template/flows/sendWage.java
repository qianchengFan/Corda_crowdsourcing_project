package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokens;
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokensHandler;
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount;
import com.template.states.CustomTokenState;
import com.template.states.workTask;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class sendWage {
    @InitiatingFlow
    @StartableByRPC
    public static class sendWageIni extends FlowLogic<String> {
        private final UniqueIdentifier token_id;
        private final UniqueIdentifier task_id;

        public sendWageIni(UniqueIdentifier tokenId,UniqueIdentifier taskId){
            this.token_id = tokenId;
            this.task_id = taskId;
        }

        @Override
        @Suspendable
        public String call() throws FlowException {
            long startTime = System.nanoTime();

            Party receiver;
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));

            StateAndRef<CustomTokenState> tokenStateAndRef = getServiceHub().getVaultService().
                    queryBy(CustomTokenState.class).getStates().stream()
                    .filter(sf -> sf.getState().getData().getLinearId().equals(this.token_id)).findAny()
                    .orElseThrow(() -> new IllegalArgumentException("token id=\"" + this.token_id + "\" not found from vault"));

            List<Party> observers = getServiceHub().getNetworkMapCache().getAllNodes().stream()
                    .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                    .collect(Collectors.toList());
            observers.remove(getOurIdentity());
            observers.remove(notary);

            CustomTokenState tokenState = tokenStateAndRef.getState().getData();
            TokenPointer tokenPointer = tokenState.toPointer();
            System.out.println("token find");

            QueryCriteria.LinearStateQueryCriteria outputCriteria = new QueryCriteria.LinearStateQueryCriteria()
                    .withUuid(Arrays.asList(UUID.fromString(task_id.toString())))
                    .withStatus(Vault.StateStatus.UNCONSUMED)
                    .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT);

            StateAndRef work = getServiceHub().getVaultService().queryBy(workTask.class, outputCriteria).getStates().get(0);

            workTask original = (workTask) work.getState().getData();
            receiver = original.getWorker();
            System.out.println("worker find");
            // With the pointer, we can create an instance of transferring Amount
            Amount<TokenType> amount = new Amount(1, tokenPointer);
            PartyAndAmount partyAndAmount = new PartyAndAmount(receiver, amount);

            SignedTransaction stx1 = (SignedTransaction) subFlow(new MoveFungibleTokens(partyAndAmount,observers));
            long endTime   = System.nanoTime();
            double totalTime = (double) (endTime - startTime) / 1_000_000_000;
            return "\nTransfer ownership of a token (token serial#: "+ this.token_id + ") to "
                    + receiver.getName() + "\nTransaction IDs: "
                    + stx1.getId()
                    + "\n time taken: "+totalTime;
        }
    }

    @InitiatedBy(sendWage.sendWageIni.class)
    public static class sendWageRes extends FlowLogic<Unit>{
        private FlowSession counterSession;

        public sendWageRes(FlowSession counterSession) {
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
