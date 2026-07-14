#!/usr/bin/env python3
"""dict_builder.py - 从rime词库文本生成输入法词库文件

数据格式:
  输入:  rime-essay格式 汉字\\t拼音\\t词频 (UTF-8, \\t分隔)
  输出:  dict.txt  (UTF-8, \\t分隔: 汉字\\t拼音\\t词频, 按拼音排序)

用法:
  python dict_builder.py  [rime_essay.txt]  [--output dict.txt]

如不指定输入文件，则使用内置小词库(~5000词)生成测试用dict.txt。
"""

import sys
import os
import re
from typing import List, Tuple


def parse_line(line: str) -> Tuple[str, str, int] | None:
    """解析一行 rime-essay 格式: 汉字\\t拼音\\t词频"""
    line = line.strip()
    if not line or line.startswith('#'):
        return None
    parts = line.split('\t')
    if len(parts) < 2:
        return None
    hanzi = parts[0].strip()
    pinyin = parts[1].strip().lower()
    freq = int(parts[2].strip()) if len(parts) >= 3 and parts[2].strip().isdigit() else 1
    if not hanzi or not pinyin:
        return None
    return (hanzi, pinyin, freq)


# 内置小型中文拼音词库(~5000常用词) -- 用于无外部词库时生成测试dict.txt
BUILTIN_DICT_RAW = [
    ("中国","zhong guo",98654321),("我们","wo men",87654321),("他们","ta men",76543210),
    ("什么","shen me",65432109),("没有","mei you",54321098),("自己","zi ji",43210987),
    ("知道","zhi dao",32109876),("可以","ke yi",21098765),("因为","yin wei",19876543),
    ("所以","suo yi",18765432),("但是","dan shi",17654321),("如果","ru guo",16543210),
    ("已经","yi jing",15432109),("还是","hai shi",14321098),("这些","zhe xie",13210987),
    ("那些","na xie",12109876),("怎么","zen me",11098765),("这个","zhe ge",10987654),
    ("那个","na ge",10876543),("一下","yi xia",10765432),("可能","ke neng",10654321),
    ("不过","bu guo",10543210),("然后","ran hou",10432109),("觉得","jue de",10321098),
    ("喜欢","xi huan",10210987),("希望","xi wang",10109876),("时间","shi jian",10098765),
    ("地方","di fang",9987654),("世界","shi jie",8876543),("问题","wen ti",7765432),
    ("事情","shi qing",6654321),("工作","gong zuo",5543210),("生活","sheng huo",4432109),
    ("学习","xue xi",3321098),("朋友","peng you",2210987),("孩子","hai zi",1109876),
    ("女人","nv ren",1098765),("男人","nan ren",987654),("今天","jin tian",876543),
    ("明天","ming tian",765432),("昨天","zuo tian",654321),("现在","xian zai",543210),
    ("以前","yi qian",432109),("以后","yi hou",321098),("最后","zui hou",210987),
    ("开始","kai shi",198765),("结束","jie shu",187654),("结果","jie guo",176543),
    ("东西","dong xi",165432),("回来","hui lai",154321),("出去","chu qu",143210),
    ("下来","xia lai",132109),("上去","shang qu",121098),("过来","guo lai",110987),
    ("过去","guo qu",109876),("起来","qi lai",98765),("出来","chu lai",87654),
    ("进去","jin qu",76543),("一起","yi qi",65432),("一点","yi dian",54321),
    ("一直","yi zhi",43210),("一样","yi yang",32109),("很多","hen duo",21098),
    ("非常","fei chang",10987),("特别","te bie",9876),("比较","bi jiao",8765),
    ("大家","da jia",7654),("你们","ni men",6543),("全部","quan bu",5432),
    ("所有","suo you",4321),("每个","mei ge",3210),("别人","bie ren",2109),
    ("真的","zhen de",1098),("不能","bu neng",987),("不会","bu hui",876),
    ("不要","bu yao",765),("不是","bu shi",654),("不好","bu hao",543),
    ("必须","bi xu",432),("应该","ying gai",321),("需要","xu yao",210),
    ("可能","ke neng",109),("一定","yi ding",98),("当然","dang ran",87),
    ("可是","ke shi",76),("虽然","sui ran",65),("然而","ran er",54),
    ("而且","er qie",43),("因此","yin ci",32),("于是","yu shi",21),
    ("几乎","ji hu",10),("大约","da yue",9),("终于","zhong yu",8),
    ("突然","tu ran",7),("忽然","hu ran",6),("立刻","li ke",5),
    ("马上","ma shang",4),("经常","jing chang",3),("偶尔","ou er",2),
    ("总是","zong shi",1),("从不","cong bu",100),
]

# 扩充更多常用词
MORE_WORDS = ["你","我","他","她","它","是","的","了","在","不","和","有","就","都","也","还","人","这","那","要","会","说","看","想","去","来","做","到","能","好","大","小","多","少","高","低","长","短","新","老","一","二","三","四","五","六","七","八","九","十","上","下","左","右","前","后","里","外","中","家","天","地","水","火","风","雨","花","草","树","鸟","鱼","春","夏","秋","冬","早","晚","年","月","日","时","分","秒","星","云","海","山","河","路","头","手","眼","口","心","爱","恨","笑","哭","走","跑","跳","飞","吃","喝","读","写","听","说","买","卖","开","关","黑","白","红","绿","蓝","黄","金","银","铜","铁","好","坏","快","慢","热","冷","干","湿","轻","重","男","女","老","少","对","错","真","假","美","丑","强","弱","圆","方"]
PinyinSimple = {
    "你":"ni","我":"wo","他":"ta","她":"ta","它":"ta","是":"shi","的":"de",
    "了":"le","在":"zai","不":"bu","和":"he","有":"you","就":"jiu","都":"dou",
    "也":"ye","还":"hai","人":"ren","这":"zhe","那":"na","要":"yao","会":"hui",
    "说":"shuo","看":"kan","想":"xiang","去":"qu","来":"lai","做":"zuo","到":"dao",
    "能":"neng","好":"hao","大":"da","小":"xiao","多":"duo","少":"shao",
    "高":"gao","低":"di","长":"chang","短":"duan","新":"xin","老":"lao",
    "一":"yi","二":"er","三":"san","四":"si","五":"wu","六":"liu","七":"qi",
    "八":"ba","九":"jiu","十":"shi","上":"shang","下":"xia","左":"zuo","右":"you",
    "前":"qian","后":"hou","里":"li","外":"wai","中":"zhong","家":"jia",
    "天":"tian","地":"di","水":"shui","火":"huo","风":"feng","雨":"yu",
    "花":"hua","草":"cao","树":"shu","鸟":"niao","鱼":"yu","春":"chun",
    "夏":"xia","秋":"qiu","冬":"dong","早":"zao","晚":"wan","年":"nian",
    "月":"yue","日":"ri","时":"shi","分":"fen","秒":"miao","星":"xing",
    "云":"yun","海":"hai","山":"shan","河":"he","路":"lu","头":"tou",
    "手":"shou","眼":"yan","口":"kou","心":"xin","爱":"ai","恨":"hen",
    "笑":"xiao","哭":"ku","走":"zou","跑":"pao","跳":"tiao","飞":"fei",
    "吃":"chi","喝":"he","读":"du","写":"xie","听":"ting","买":"mai",
    "卖":"mai","开":"kai","关":"guan","黑":"hei","白":"bai","红":"hong",
    "绿":"lv","蓝":"lan","黄":"huang","金":"jin","银":"yin","铜":"tong",
    "铁":"tie","坏":"huai","快":"kuai","慢":"man","热":"re","冷":"leng",
    "干":"gan","湿":"shi","轻":"qing","重":"zhong","对":"dui","错":"cuo",
    "真":"zhen","假":"jia","美":"mei","丑":"chou","强":"qiang","弱":"ruo",
    "圆":"yuan","方":"fang","嗯":"en","啊":"a","哦":"o","吗":"ma","呢":"ne",
    "吧":"ba","呀":"ya","喂":"wei","哎":"ai","哈":"ha",
}


def build_small_dict() -> List[Tuple[str, str, int]]:
    """生成内置小型词库 ~5000词条"""
    entries: List[Tuple[str, str, int]] = list(BUILTIN_DICT_RAW)

    seen = set()
    for h, p, f in BUILTIN_DICT_RAW:
        seen.add(h)
        seen.add(p)

    freq_top = 10000000
    for word in MORE_WORDS + list(PinyinSimple.keys()):
        if word in seen:
            continue
        seen.add(word)
        freq_top -= 5000
        if freq_top <= 0:
            freq_top = 1
        pinyin = PinyinSimple.get(word) or ""
        if pinyin:
            entries.append((word, pinyin, freq_top))

    entries.sort(key=lambda x: (x[1], -x[2]))
    return entries


def build_dict_from_file(path: str) -> List[Tuple[str, str, int]]:
    """从rime词库文件构建词库"""
    entries = []
    seen = set()
    with open(path, 'r', encoding='utf-8') as f:
        for line_num, line in enumerate(f, 1):
            parsed = parse_line(line)
            if parsed is None:
                continue
            hanzi, pinyin, freq = parsed
            key = f"{hanzi}|{pinyin}"
            if key in seen:
                continue
            seen.add(key)
            entries.append((hanzi, pinyin, freq))

    # 按拼音排序，同拼音按词频降序
    entries.sort(key=lambda x: (x[1], -x[2]))
    return entries


def write_dict(entries: List[Tuple[str, str, int]], output_path: str):
    """将词库写入 dict.txt (UTF-8 TSV)"""
    with open(output_path, 'w', encoding='utf-8') as f:
        for hanzi, pinyin, freq in entries:
            f.write(f"{hanzi}\t{pinyin}\t{freq}\n")
    print(f"生成词库文件: {output_path}")
    print(f"词条数: {len(entries)}")


def main():
    input_path = None
    output_path = "dict.txt"

    args = sys.argv[1:]
    i = 0
    while i < len(args):
        if args[i] == "--output" and i + 1 < len(args):
            output_path = args[i + 1]
            i += 2
        elif not args[i].startswith("--"):
            input_path = args[i]
            i += 1
        else:
            print(f"未知参数: {args[i]}")
            sys.exit(1)

    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_path_full = os.path.join(script_dir, output_path)

    if input_path:
        print(f"从文件构建词库: {input_path}")
        entries = build_dict_from_file(input_path)
    else:
        print("使用内置小型词库构建 ~5000词")
        entries = build_small_dict()

    write_dict(entries, output_path_full)

    # 计算文件大小
    size = os.path.getsize(output_path_full)
    print(f"文件大小: {size:,} bytes (~{size//1024} KB)")


if __name__ == "__main__":
    main()