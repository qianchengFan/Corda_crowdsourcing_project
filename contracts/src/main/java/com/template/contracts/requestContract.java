package com.template.contracts;

import com.template.states.TemplateState;
import com.template.states.request;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;
import java.util.List;

// ************
// * Contract *
// ************
public class requestContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.requestContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {

        /* We can use the requireSingleCommand function to extract command data from transaction.
         * However, it is possible to have multiple commands in a signle transaction.*/
        //final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final CommandData commandData = tx.getCommands().get(0).getValue();

        if (commandData instanceof Commands.Send) {
            //Retrieve the output state of the transaction
            request output = tx.outputsOfType(request.class).get(0);

            //Using Corda DSL function requireThat to replicate conditions-checks
            requireThat(require -> {
                require.using("No inputs should be consumed.", tx.getInputStates().size() == 0);
                require.using("There should be 1 output state", tx.getOutputStates().size()==1);
                return null;
            });
        }
        if (commandData instanceof Commands.change) {
            request output = tx.outputsOfType(request.class).get(0);
            requireThat(require -> {
                require.using("This transaction should only output one state", tx.getOutputs().size() == 1);
                require.using("The output state should have clear description of task", !output.getTask().equals(""));
                return null;
            });
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command.
        class Send implements Commands {}
        class change implements Commands{}
    }
}