package com.template;

import net.corda.core.identity.CordaX500Name;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Future;

public class MockNetworkTestsTutorial {
    private StartedMockNode nodeA;
    private StartedMockNode nodeB;
    private final MockNetwork mockNet = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(Arrays.asList(
                    TestCordapp.findCordapp("com.template.contracts"),
                    TestCordapp.findCordapp("com.template.flows")))
            .withNotarySpecs(Arrays.asList(new MockNetworkNotarySpec(CordaX500Name.parse("O=Notary,L=London,C=GB")))));

    @Before
    public void setUp() {
        nodeA = mockNet.createNode();
        // We can optionally give the node a name.
        nodeB = mockNet.createNode(new CordaX500Name("Bank B", "London", "GB"));
    }

    @After
    public void cleanUp() {
        mockNet.stopNodes();
    }

    @Test
    public void dummyTest() {
        TemplateFlow.TemplateFlowInitiator flow = new TemplateFlow.TemplateFlowInitiator(nodeB.getInfo().getLegalIdentities().get(0));
        Future<SignedTransaction> future = nodeA.startFlow(flow);
        mockNet.runNetwork();

    }
}