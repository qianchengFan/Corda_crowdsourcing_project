package com.template.states;

import com.template.contracts.requestContract;
import com.template.contracts.sendContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


@BelongsToContract(sendContract.class)
public class data implements LinearState {
    private Party user;
    private UniqueIdentifier taskId;
    private Party owner;
    private String task;
    private Party sender;

    private List<Party> participantParties;

    private BigInteger result;

    private UniqueIdentifier requestId;

    private List<UniqueIdentifier> workIdList;
    private UniqueIdentifier tokenId;

    public data(String task, UniqueIdentifier taskId,Party sender,List<Party> participantParties,Party owner,BigInteger result,UniqueIdentifier tokenId){
        this.task = task;
        this.taskId = taskId;
        this.sender = sender;
        this.owner = owner;
        this.user = null;
        this.participantParties = participantParties;
        this.result = result;
        this.requestId = null;
        this.workIdList = null;
        this.tokenId = tokenId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> allParties = new ArrayList<>(participantParties);
        allParties.add(sender);
        return allParties;
    }

    public List<Party> getParties() {
        return participantParties;
    }

    public data setTokenId(UniqueIdentifier tokenId) {
        data output = new data(this.task,this.taskId,this.sender,this.participantParties,this.owner,result,tokenId);
        return output;
    }

    public UniqueIdentifier getTokenId() {
        return tokenId;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return taskId;
    }

    public List<Party> getPeople() {
        return participantParties;
    }

    public Party getSender() {
        return sender;
    }

    public String getTask() {
        return task;
    }
    public data setResult(BigInteger result){
        data output = new data(this.task,this.taskId,this.sender,this.participantParties,this.owner,result,this.tokenId);
        return output;
    }

    public void setWorkIdList(List<UniqueIdentifier> workIdList) {
        this.workIdList = workIdList;
    }

    public List<UniqueIdentifier> getWorkIdList() {
        return workIdList;
    }

    public data setId(UniqueIdentifier id){
        data output = new data(this.task,this.taskId,this.sender,this.participantParties,this.owner,this.result,this.tokenId);
        output.requestId = id;
        return output;
    }

    public UniqueIdentifier getRequestId() {
        return requestId;
    }

    public BigInteger getResult() {
        return result;
    }

    public data changeOwner(Party owner){
        data output = new data(this.task,this.taskId,this.sender,this.participantParties,owner,this.result,this.tokenId);
        return output;
    }

    public Party getOwner() {
        return owner;
    }
}
