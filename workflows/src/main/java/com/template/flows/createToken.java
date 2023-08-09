package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import com.r3.corda.lib.tokens.workflows.utilities.FungibleTokenBuilder;
import com.template.states.CustomTokenState;
import com.template.states.tokenState;
import net.corda.core.contracts.TransactionState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@StartableByRPC
public class createToken extends FlowLogic<String> {
    private String msg;

    private int amount;

    public createToken(String msg,int amount) {
        this.msg = msg;
        this.amount = amount;
    }
    @Override
    @Suspendable
    public String call() throws FlowException {
        long startTime = System.nanoTime();
        final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));



        List<Party> observers = getServiceHub().getNetworkMapCache().getAllNodes().stream()
                .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                .collect(Collectors.toList());
        observers.remove(getOurIdentity());
        observers.remove(notary);

        final TokenPointer<CustomTokenState> tokenStatePointer;

        UniqueIdentifier uuid = new UniqueIdentifier();
        CustomTokenState tokenState = new CustomTokenState(msg,getOurIdentity(),0,uuid);

        //warp it with transaction state specifying the notary
        TransactionState<CustomTokenState> transactionState = new TransactionState<>(tokenState,notary);
        subFlow(new CreateEvolvableTokens(transactionState,observers));

        tokenStatePointer = tokenState.toPointer();

        FungibleToken token = new FungibleTokenBuilder()
                .ofTokenType(tokenStatePointer)
                .withAmount(amount)
                .issuedBy(getOurIdentity())
                .heldBy(getOurIdentity())
                .buildFungibleToken();
        long endTime   = System.nanoTime();
        double totalTime = (double) (endTime - startTime) / 1_000_000_000;

        SignedTransaction stx = subFlow(new IssueTokens(Arrays.asList(token),observers));
        return "\nMessage: "+ tokenState.getMessage() + "\nToken Id is: "+tokenState.getLinearId()+"\nStorage Node is: "+tokenState.getMaintainers()
                +"\namout is: "+token.getAmount()+"\n time taken is: "+totalTime;
    }
}