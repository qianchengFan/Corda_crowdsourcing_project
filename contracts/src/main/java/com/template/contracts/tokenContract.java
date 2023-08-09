package com.template.contracts;

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract;
import com.r3.corda.lib.tokens.contracts.commands.EvolvableTokenTypeCommand;
import com.template.states.tokenState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class tokenContract implements Contract {
    public static String ID = "com.template.contracts.tokenContract";
//
@Override
public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {

    if(tx.getCommands().size() !=1)
        throw new IllegalArgumentException("One Command Expected");

    if(tx.getCommand(0).getValue() instanceof Commands.Issue)
        verifyIssue(tx);
    else if(tx.getCommand(0).getValue() instanceof Commands.Transfer)
        verifyTransfer(tx);
    else
        throw new IllegalArgumentException("Unsupported Command");

}

    private void verifyIssue(LedgerTransaction tx){
        if(tx.getInputs().size() != 0)
            throw new IllegalArgumentException("Zero Inputs Expected");

        if(tx.getOutputs().size() != 1)
            throw new IllegalArgumentException("One Output Expected");

        if(!(tx.getOutput(0) instanceof tokenState))
            throw new IllegalArgumentException("Output of type tokenState Expected");

        tokenState tokenState = (tokenState)tx.getOutput(0);
        if(tokenState.getAmount() < 1)
            throw new IllegalArgumentException("Positive amount expected");

        if(!(tx.getCommand(0).getSigners()
                .contains(tokenState.getIssuer().getOwningKey())))
            throw new IllegalArgumentException("Issuer must sign");
    }

    private void verifyTransfer(LedgerTransaction tx){
        // Inputs must be greater than zero
        if(tx.getInputs().size() < 1)
            throw new IllegalArgumentException("More than 0 inputs expected");

        // Output must be equal to either 1 or 2
        if(!(tx.getOutputs().size() == 1 || tx.getOutputs().size() == 2))
            throw new IllegalArgumentException("Output count must either be one or two");

        // Input amount must be equal to output amount
        AtomicInteger inputSum = new AtomicInteger();
        tx.getInputs().forEach(contractStateStateAndRef -> {
            tokenState inputState = (tokenState)contractStateStateAndRef.getState().getData();
            inputSum.set(inputSum.get() + inputState.getAmount());
        });

        AtomicInteger outputSum = new AtomicInteger();
        tx.getOutputs().forEach(contractStateTransactionState -> {
            outputSum.set(outputSum.get() + ((tokenState)contractStateTransactionState.getData()).getAmount());
        });

        if(inputSum.get() != outputSum.get())
            throw new IllegalArgumentException("Incorrect Spending");

        // Owner must sign
        if(!(tx.getCommand(0).getSigners().contains(((tokenState)tx.getInput(0)).getOwner().getOwningKey())))
            throw new IllegalArgumentException("Owner must Sign");
    }

    public interface Commands extends CommandData {
        class Issue implements Commands { }
        class Transfer implements Commands { }
    }
}