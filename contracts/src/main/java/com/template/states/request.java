package com.template.states;
import com.template.contracts.requestContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@BelongsToContract(requestContract.class)
public class request implements LinearState {
    public String task;
    private Party sender;
    private Party receiver;
    private Party executer;
    private List<Party> participantParties;
    private UniqueIdentifier id;

    private UniqueIdentifier tokenId;
    /* Constructor of your Corda state */
    public request(Party sender, Party receiver,String task,UniqueIdentifier id,UniqueIdentifier tokenId) {
        this.sender = sender;
        this.receiver = receiver;
        this.task = task;
        this.id = id;
        this.executer = null;
        this.participantParties = new ArrayList<>();
        this.tokenId = tokenId;
        participantParties.add(sender);
        participantParties.add(receiver);
    }

    //getters
    public Party getSender() { return sender; }
    public Party getReceiver() { return receiver; }
    public String getTask() {return task;}

    /* This method will indicate who are the participants and required signers when
     * this state is used in a transaction. */
    @Override
    public List<AbstractParty> getParticipants() {
        return new ArrayList<>(participantParties);
    }

    public request setExecuter(Party executer){
        request current = new request(this.sender,this.receiver,this.task,this.id,tokenId);
        current.executer = executer;
        return current;
    }

    public UniqueIdentifier getTokenId() {
        return tokenId;
    }

    public request addParticipants(Party party){
        request current = new request(this.sender,this.receiver,this.task,this.id,tokenId);
        current.participantParties.add(party);
        return current;
    }

    public Party getExecuter() {
        return executer;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.id;
    }
}