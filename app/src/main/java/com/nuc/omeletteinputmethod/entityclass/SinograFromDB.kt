package com.nuc.omeletteinputmethod.entityclass

class SinograFromDB {
    var wenzi1: String? = null
    var wenzi2: String? = null
    var pinyin: String? = null
    var jisheng: Int = 0
    var id: Int = 0
    var allcishu: Int = 0
    var usercishu: Int = 0
    //    public SinograFromDB(String wenzi1, String wenzi2, int jisheng, int id) {
    //        this.wenzi1 = wenzi1;
    //        this.wenzi2 = wenzi2;
    //        this.jisheng = jisheng;
    //        this.id = id;
    //    }


    /**
     *
     * @param wenzi1 文字
     * @param pinyin 拼音
     * @param allcishu 相当于频率
     * @param id id
     */
    constructor(wenzi1: String, pinyin: String, allcishu: Int, id: Int, usercishu: Int) {
        this.wenzi1 = wenzi1
        this.pinyin = pinyin
        this.id = id
        this.allcishu = allcishu
        this.usercishu = usercishu
    }

    constructor(wenzi1: String, wenzi2: String, jisheng: Int, id: Int, allcishu: Int, usercishu: Int) {
        this.wenzi1 = wenzi1
        this.wenzi2 = wenzi2
        this.jisheng = jisheng
        this.id = id
        this.allcishu = allcishu
        this.usercishu = usercishu
    }
}
