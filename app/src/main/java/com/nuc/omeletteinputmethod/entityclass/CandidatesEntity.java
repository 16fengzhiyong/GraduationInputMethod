package com.nuc.omeletteinputmethod.entityclass;


/**
 * 用于候选框输入显示
 */
public class CandidatesEntity {
    private int id;
    private String candidates;

    public CandidatesEntity(int id, String candidates) {
        this.id = id;
        this.candidates = candidates;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCandidates() {
        return candidates;
    }

    public void setCandidates(String candidates) {
        this.candidates = candidates;
    }
}
