package com.template.contracts;

import com.template.states.data;
import com.template.states.request;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class sendContract implements Contract {
    public static final String ID = "com.template.contracts.sendContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) {

        /* We can use the requireSingleCommand function to extract command data from transaction.
         * However, it is possible to have multiple commands in a signle transaction.*/
        //final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final CommandData commandData = tx.getCommands().get(0).getValue();
//
        if (commandData instanceof sendContract.Commands.Send) {

            //Using Corda DSL function requireThat to replicate conditions-checks
            requireThat(require -> {
                return null;
            });
        }
        }
    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command.
        class Send implements requestContract.Commands {}
    }
}
