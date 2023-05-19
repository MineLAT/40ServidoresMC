package com.cadiducho.cservidoresmc.web.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class VoteResponse {

    public static final VoteResponse EMPTY = new VoteResponse("Voto único", "<empty>", VoteStatus.NOT_VOTED, "<empty>");

    @SerializedName("tipovoto") private final String voteType;
    private final String web;
    private final VoteStatus status;
    @SerializedName("mensaje") private final String msg;
}
