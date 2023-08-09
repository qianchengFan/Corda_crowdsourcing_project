package com.template;

import com.google.common.collect.ImmutableList;
import com.template.flows.createToken;
import com.template.states.CustomTokenState;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.driver.DriverParameters;
import net.corda.testing.driver.NodeHandle;
import net.corda.testing.driver.NodeParameters;
import org.junit.Test;

import java.security.Timestamp;
import java.util.List;

import static net.corda.testing.driver.Driver.driver;
import static org.junit.Assert.assertEquals;

public class DriverBasedTest {
    private final TestIdentity user = new TestIdentity(new CordaX500Name("user", "", "GB"));
    private final TestIdentity platform = new TestIdentity(new CordaX500Name("platform", "", "US"));

    @Test
    public void nodeTest() {
        driver(new DriverParameters().withIsDebug(true).withStartNodesInProcess(true), dsl -> {
            // Start a pair of nodes and wait for them both to be ready.
            List<CordaFuture<NodeHandle>> handleFutures = ImmutableList.of(
                    dsl.startNode(new NodeParameters().withProvidedName(user.getName())),
                    dsl.startNode(new NodeParameters().withProvidedName(platform.getName()))
            );

            try {
                NodeHandle partyAHandle = handleFutures.get(0).get();
                NodeHandle partyBHandle = handleFutures.get(1).get();

                // From each node, make an RPC call to retrieve another node's name from the network map, to verify that the
                // nodes have started and can communicate.

                // This is a very basic test: in practice tests would be starting flows, and verifying the states in the vault
                // and other important metrics to ensure that your CorDapp is working as intended.
                Party party_User = partyAHandle.getNodeInfo().getLegalIdentities().get(0);
                Party party_Platform = partyBHandle.getNodeInfo().getLegalIdentities().get(0);

                System.out.println(System.currentTimeMillis()+" time 1");
                partyAHandle.getRpc()
                        .startTrackedFlowDynamic(createToken.class,"valid",4)
                        .getReturnValue().get()
                        ;
                System.out.println(System.currentTimeMillis()+" time 2");

                Vault.Page<CustomTokenState> tokenStates_A = partyAHandle.getRpc().vaultQuery(CustomTokenState.class);
                assertEquals(1, tokenStates_A.getStates().size());

                CustomTokenState tokenState_A = tokenStates_A.getStates().get(0).getState().getData();
                assertEquals(party_User, tokenState_A.getIssuer());

                assertEquals(partyAHandle.getRpc().wellKnownPartyFromX500Name(platform.getName()).getName(), platform.getName());
                assertEquals(partyBHandle.getRpc().wellKnownPartyFromX500Name(user.getName()).getName(), user.getName());
            } catch (Exception e) {
                throw new RuntimeException("Caught exception during test: ", e);
            }

            return null;
        });
    }
}