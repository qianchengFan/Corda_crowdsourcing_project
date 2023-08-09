package com.template.states;

import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.template.contracts.CustomTokenContract;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearPointer;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


@BelongsToContract(CustomTokenContract.class)
public class CustomTokenState extends EvolvableTokenType {

    private String message;
    private Party issuer;
    private int fractionDigits;
    private UniqueIdentifier linearId;

    public CustomTokenState(String message, Party issuer, int fractionDigits, UniqueIdentifier linearId) {
        this.message = message;
        this.issuer = issuer;
        this.fractionDigits = fractionDigits;
        this.linearId = linearId;
    }


    public String getMessage() {
        return message;
    }

    public Party getIssuer() {
        return issuer;
    }

    @Override
    public int getFractionDigits() {
        return fractionDigits;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    public List<Party> getMaintainers() {
        List<Party> maintainers = new ArrayList<Party>();
        maintainers.add(this.issuer);
        return maintainers;
    }

    /* This method returns a TokenPointer by using the linear Id of the evolvable state */
    public TokenPointer<CustomTokenState> toPointer(){
        LinearPointer<CustomTokenState> linearPointer = new LinearPointer<>(linearId, CustomTokenState.class);
        return new TokenPointer<>(linearPointer, fractionDigits);
    }
}
