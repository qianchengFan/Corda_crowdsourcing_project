package com.template.states;

import com.template.contracts.sendContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@BelongsToContract(sendContract.class)
public class subtask implements ContractState {
    private final List<Party> participants;
    private String task;
    private UniqueIdentifier taskId;
    private Party sender;
    private Party user;
    private Party owner;

    public subtask(String task, UniqueIdentifier taskId, Party sender, Party user) {
        this.task = task;
        this.taskId = taskId;
        this.sender = sender;
        this.owner = null;
        this.user = user;
        this.participants = Arrays.asList(sender,user);
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return new ArrayList<>(participants);
    }

    public String getTask() {
        return task;
    }

    public UniqueIdentifier getTaskId() {
        return taskId;
    }

    public Party getSender() {
        return sender;
    }

    public Party getUser() {
        return user;
    }

    public Party getOwner() {
        return owner;
    }

    public void assignOwner(Party owner){
        this.owner = owner;
    }
}
