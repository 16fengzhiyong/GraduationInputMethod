package com.nuc.omeletteinputmethod.entityclass


/**
 * 用于候选框输入显示
 */
class CandidatesEntity {
    var id: Int = 0
    var candidates: String? = null
    var allcishu: Int = 0
    var usercishu: Int = 0

    constructor(id: Int, candidates: String) {
        this.id = id
        this.candidates = candidates
    }

    /**
     * @param id id
     * @param candidates 显示文本
     * @param allcishu 次数
     */
    constructor(id: Int, candidates: String, allcishu: Int, usercishu: Int) {
        this.id = id
        this.candidates = candidates
        this.allcishu = allcishu
        this.usercishu = usercishu
    }
}
