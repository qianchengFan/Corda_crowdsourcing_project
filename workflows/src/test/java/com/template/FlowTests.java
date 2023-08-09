package com.template;

import com.template.states.TemplateState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Future;

public class FlowTests {
    private MockNetwork network;
    private StartedMockNode a;
    private StartedMockNode b;
//
//    private final TestIdentity partyAIdentity = new TestIdentity(new CordaX500Name("PartyA", "London", "GB"));
//    private final TestIdentity partyBIdentity = new TestIdentity(new CordaX500Name("PartyB", "New York", "US"));
//    private final TestIdentity partyCIdentity = new TestIdentity(new CordaX500Name("PartyC", "Mumbai", "IN"));
    @Before
    public void setup() {
        network = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(Arrays.asList(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows")))
                .withNotarySpecs(Arrays.asList(new MockNetworkNotarySpec(CordaX500Name.parse("O=Notary,L=London,C=GB")))));
        a = network.createPartyNode(null);
        b = network.createPartyNode(null);
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void dummyTest() {
        TemplateFlow.TemplateFlowInitiator flow = new TemplateFlow.TemplateFlowInitiator(b.getInfo().getLegalIdentities().get(0));
        Future<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();

        //successful query means the state is stored at node b's vault. Flow went through.
        QueryCriteria inputCriteria = new QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.UNCONSUMED);
        TemplateState state = b.getServices().getVaultService().queryBy(TemplateState.class,inputCriteria)
                .getStates().get(0).getState().getData();
    }
}
