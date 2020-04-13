package com.nuc.omeletteinputmethod.entityclass;


/**
 * 用于候选框输入显示
 */
public class CandidatesEntity {
    private int id;
    private String candidates;
    private int allcishu;
    private int usercishu;
    public CandidatesEntity(int id, String candidates) {
        this.id = id;
        this.candidates = candidates;
    }

    /**
     * @param id id
     * @param candidates 显示文本
     * @param allcishu 次数
     */
    public CandidatesEntity(int id, String candidates, int allcishu,int usercishu) {
        this.id = id;
        this.candidates = candidates;
        this.allcishu = allcishu;
        this.usercishu = usercishu;
    }


    public int getUsercishu() {
        return usercishu;
    }

    public void setUsercishu(int usercishu) {
        this.usercishu = usercishu;
    }

    public int getAllcishu() {
        return allcishu;
    }

    public void setAllcishu(int allcishu) {
        this.allcishu = allcishu;
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
