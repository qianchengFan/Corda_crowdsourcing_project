package com.template.states;

import com.template.contracts.workContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
@BelongsToContract(workContract.class)
public class workTask implements LinearState {
    private Party worker;
    private UniqueIdentifier taskId;
    private String task;
    private Party sender;
    private BigInteger result;
    private List<Party> participantParties;
    private UniqueIdentifier dataId;

    public workTask(String task, UniqueIdentifier taskId,Party sender,List<Party> participantParties,Party worker,BigInteger result){
        this.task = task;
        this.taskId = taskId;
        this.sender = sender;
        this.worker = worker;
        this.participantParties = participantParties;
        this.result = result;
        this.dataId = null;
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

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return taskId;
    }

    public Party getSender() {
        return sender;
    }

    public workTask putResult(BigInteger result){
        workTask output = new workTask(this.task,this.taskId,this.sender,this.participantParties,this.worker,result);
        return output;
    }

    public workTask putId(UniqueIdentifier id){
        workTask output = new workTask(this.task,this.taskId,this.sender,this.participantParties,this.worker,this.result);
        output.dataId = id;
        return output;
    }

    public String getTask() {
        return task;
    }

    public UniqueIdentifier getDataId() {
        return dataId;
    }

    public workTask setWorker(Party worker){
        workTask output = new workTask(this.task,this.taskId,this.sender,this.participantParties,worker,this.result);
        return output;
    }

    public Party getWorker() {
        return worker;
    }

    public BigInteger getResult() {
        return result;
    }
}
