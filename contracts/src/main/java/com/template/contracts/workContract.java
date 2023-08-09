package com.template.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class workContract implements Contract {
    public static final String ID = "com.template.contracts.workContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) {

        /* We can use the requireSingleCommand function to extract command data from transaction.
         * However, it is possible to have multiple commands in a signle transaction.*/
        //final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final CommandData commandData = tx.getCommands().get(0).getValue();
//
        if (commandData instanceof sendContract.Commands.Send) {
            requireThat(require -> {
                require.using("There should be 1 output state", tx.getOutputStates().size()==1);
                return null;
            });
        }
    }
    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Send implements requestContract.Commands {}
    }
}

