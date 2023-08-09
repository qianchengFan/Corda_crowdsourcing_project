package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.workflows.flows.move.MoveTokensUtilities;
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokens;
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokensHandler;
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveNonFungibleTokens;
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveNonFungibleTokensHandler;
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount;
import com.r3.corda.lib.tokens.workflows.types.PartyAndToken;
import com.template.states.CustomTokenState;
import kotlin.Unit;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class sendToken{

    @InitiatingFlow
    @StartableByRPC
    public static class sendTokenIni extends FlowLogic<String>{
        private final UniqueIdentifier id;
        private final Party receivre;
        private final int amount;
        public sendTokenIni(UniqueIdentifier id,int amount,Party receiver){
            this.id = id;
            this.amount = amount;
            this.receivre = receiver;
        }

        @Override
        @Suspendable
        public String call() throws FlowException {
            long startTime = System.nanoTime();


            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));

            StateAndRef<CustomTokenState> tokenStateAndRef = getServiceHub().getVaultService().
                    queryBy(CustomTokenState.class).getStates().stream()
                    .filter(sf -> sf.getState().getData().getLinearId().equals(this.id)).findAny()
                    .orElseThrow(() -> new IllegalArgumentException("token id=\"" + this.id + "\" not found from vault"));

            List<Party> observers = getServiceHub().getNetworkMapCache().getAllNodes().stream()
                    .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                    .collect(Collectors.toList());
            observers.remove(getOurIdentity());
            observers.remove(notary);

            CustomTokenState tokenState = tokenStateAndRef.getState().getData();
            TokenPointer tokenPointer = tokenState.toPointer();
            // With the pointer, we can create an instance of transferring Amount
            Amount<TokenType> amount = new Amount(this.amount, tokenPointer);
            PartyAndAmount partyAndAmount = new PartyAndAmount(receivre, amount);

            SignedTransaction stx1 = (SignedTransaction) subFlow(new MoveFungibleTokens(partyAndAmount,observers));
            long endTime   = System.nanoTime();
            double totalTime = (double) (endTime - startTime) / 1_000_000_000;
            return "\nTransfer ownership of a token (token serial#: "+ this.id+ ") to "
                    + this.receivre.getName() + "\nTransaction IDs: "
                    + stx1.getId()
                    + "\n time taken: "+totalTime;
        }
    }

    @InitiatedBy(sendTokenIni.class)
    public static class sendTokenRes extends FlowLogic<Unit>{
        private FlowSession counterSession;

        public sendTokenRes(FlowSession counterSession) {
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
