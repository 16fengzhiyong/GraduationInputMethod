#!/usr/bin/env python3
"""
build_all.py - 一键构建完整简体中文词库
自动从 Rime + THUOCL 下载数据, 繁转简, 合并, 构建所有 SQLite 词库

用法:
  python build_all.py [--output-dir <assets_dir>]
默认输出到 app/src/main/assets/

数据源:
  1. THUOCL (清华大学开放中文词库) - 12个分类, 简体中文, 含DF值词频
  2. Rime essay.txt - 繁体基础词库, 通过OpenCC转简体作为补充
"""

import os
import sys
import urllib.request
import gzip
import re
import sqlite3
from collections import defaultdict
from typing import List, Tuple, Dict, Optional

HAS_PYPINYIN = False
HAS_OPENCC = False

try:
    from pypinyin import lazy_pinyin, Style
    HAS_PYPINYIN = True
except ImportError:
    pass

try:
    from opencc import OpenCC
    HAS_OPENCC = True
except ImportError:
    pass

# ============================================================
# 数据源配置
# ============================================================

THUOCL_BASE = "https://raw.githubusercontent.com/thunlp/THUOCL/master/data"

THUOCL_FILES = {
    "it":           f"{THUOCL_BASE}/THUOCL_IT.txt",            # 16000条
    "medical":      f"{THUOCL_BASE}/THUOCL_medical.txt",       # 18749条
    "legal":        f"{THUOCL_BASE}/THUOCL_law.txt",           # 9896条
    "finance":      f"{THUOCL_BASE}/THUOCL_caijing.txt",       # 3830条
    "food":         f"{THUOCL_BASE}/THUOCL_food.txt",          # 8974条
    "geography":    f"{THUOCL_BASE}/THUOCL_diming.txt",        # 44805条
    "idiom":        f"{THUOCL_BASE}/THUOCL_chengyu.txt",       # 8519条
    "poetry":       f"{THUOCL_BASE}/THUOCL_poem.txt",          # 13703条
    "history":      f"{THUOCL_BASE}/THUOCL_lishimingren.txt",  # 13658条
    "animal":       f"{THUOCL_BASE}/THUOCL_animal.txt",        # 17287条
    "car":          f"{THUOCL_BASE}/THUOCL_car.txt",           # 1752条
}

SPORTS_WORDS = [
    ("足球", 5000), ("篮球", 4800), ("排球", 4600), ("网球", 4400),
    ("乒乓球", 4200), ("羽毛球", 4000), ("游泳", 3800), ("跑步", 3600),
    ("马拉松", 3400), ("滑雪", 3200), ("滑冰", 3000), ("体操", 2800),
    ("举重", 2600), ("拳击", 2400), ("击剑", 2200), ("射击", 2100),
    ("射箭", 2000), ("摔跤", 1900), ("柔道", 1800), ("跆拳道", 1700),
    ("高尔夫", 1600), ("台球", 1500), ("保龄球", 1400), ("橄榄球", 1300),
    ("棒球", 1200), ("垒球", 1100), ("冰球", 1000), ("水球", 990),
    ("跳水", 980), ("蹦床", 970), ("攀岩", 960), ("冲浪", 950),
    ("赛艇", 940), ("帆船", 930), ("皮划艇", 920), ("自行车", 910),
    ("摩托车", 900), ("赛车", 890), ("跳高", 880), ("跳远", 870),
    ("铅球", 860), ("铁饼", 850), ("标枪", 840), ("跨栏", 830),
    ("接力", 820), ("短跑", 810), ("中长跑", 800), ("竞走", 790),
    ("体操", 780), ("吊环", 770), ("单杠", 760), ("双杠", 750),
    ("跳马", 740), ("平衡木", 730), ("武术", 720), ("太极拳", 710),
    ("散打", 700), ("拳法", 690), ("健身", 680), ("瑜伽", 670),
    ("健美", 660), ("肚皮舞", 650), ("广场舞", 640), ("跳绳", 630),
    ("踢毽子", 620), ("拔河", 610), ("放风筝", 600), ("飞盘", 590),
    ("NBA", 580), ("CBA", 570), ("世界杯", 560), ("奥运会", 550),
    ("亚运会", 540), ("锦标赛", 530), ("联赛", 520), ("决赛", 510),
    ("冠军", 500), ("亚军", 490), ("季军", 480), ("金牌", 470),
    ("银牌", 460), ("铜牌", 450), ("记录", 440), ("比分", 430),
    ("防守", 420), ("进攻", 410), ("传球", 400), ("投球", 390),
    ("扣篮", 380), ("三分球", 370), ("罚球", 360), ("篮板", 350),
    ("助攻", 340), ("抢断", 330), ("盖帽", 320), ("失误", 310),
    ("裁判", 300), ("教练", 290), ("替补", 280), ("首发", 270),
    ("加时赛", 260), ("点球", 250), ("角球", 240), ("任意球", 230),
    ("越位", 220), ("红牌", 210), ("黄牌", 200), ("犯规", 190),
]

GAMING_WORDS = [
    ("游戏", 5000), ("玩家", 4800), ("角色", 4600), ("技能", 4400),
    ("装备", 4200), ("道具", 4000), ("金币", 3800), ("钻石", 3600),
    ("副本", 3400), ("BOSS", 3200), ("小怪", 3000), ("经验值", 2800),
    ("等级", 2600), ("升级", 2400), ("任务", 2200), ("主线", 2100),
    ("支线", 2000), ("日常", 1900), ("活动", 1800), ("奖励", 1700),
    ("成就", 1600), ("排行榜", 1500), ("竞技场", 1400), ("PVP", 1300),
    ("PVE", 1200), ("公会", 1100), ("战队", 1000), ("组队", 990),
    ("匹配", 980), ("排位", 970), ("段位", 960), ("青铜", 950),
    ("白银", 940), ("黄金", 930), ("铂金", 920), ("钻石", 910),
    ("王者", 900), ("吃鸡", 890), ("落地成盒", 880), ("毒圈", 870),
    ("空投", 860), ("舔包", 850), ("伏地魔", 840), ("狙击", 830),
    ("步枪", 820), ("冲锋枪", 810), ("霰弹枪", 800), ("手雷", 790),
    ("烟雾弹", 780), ("闪光弹", 770), ("燃烧弹", 760), ("倍镜", 750),
    ("三级头", 740), ("三级甲", 730), ("医疗包", 720), ("能量饮料", 710),
    ("英雄", 700), ("法师", 690), ("战士", 680), ("刺客", 670),
    ("射手", 660), ("辅助", 650), ("坦克", 640), ("ADC", 630),
    ("AP", 620), ("打野", 610), ("上单", 600), ("中单", 590),
    ("下单", 580), ("补刀", 570), ("推塔", 560), ("团战", 550),
    ("GANK", 540), ("AOE", 530), ("CD", 520), ("BUFF", 510),
    ("DEBUFF", 500), ("红BUFF", 490), ("蓝BUFF", 480), ("大龙", 470),
    ("小龙", 460), ("纳什男爵", 450), ("峡谷先锋", 440), ("水晶", 430),
    ("高地塔", 420), ("兵线", 410), ("野区", 400), ("河道", 390),
    ("草丛", 380), ("插眼", 370), ("排眼", 360), ("视野", 350),
    ("闪现", 340), ("点燃", 330), ("治疗", 320), ("虚弱", 310),
    ("净化", 300), ("传送", 290), ("惩戒", 280), ("疾跑", 270),
    ("皮肤", 260), ("限定", 250), ("传说", 240), ("史诗", 230),
    ("开箱", 220), ("抽卡", 210), ("SSR", 200), ("保底", 190),
    ("欧气", 180), ("非酋", 170), ("氪金", 160), ("白嫖", 150),
    ("肝帝", 140), ("大佬", 130), ("萌新", 120), ("菜鸟", 110),
    ("路人", 100), ("队友", 99), ("对手", 98), ("人头", 97),
    ("击杀", 96), ("助攻", 95), ("死亡", 94), ("KDA", 93),
    ("MVP", 92), ("五杀", 91), ("超神", 90), ("挂机", 89),
    ("延迟", 88), ("卡顿", 87), ("掉线", 86), ("外挂", 85),
    ("开挂", 84), ("封号", 83), ("代练", 82), ("工作室", 81),
    ("电竞", 80), ("战队", 79), ("俱乐部", 78), ("转会", 77),
    ("直播", 76), ("弹幕", 75), ("主播", 74), ("自媒体", 73),
    ("LOL", 72), ("王者荣耀", 71), ("绝地求生", 70), ("和平精英", 69),
    ("原神", 68), ("星穹铁道", 67), ("崩坏", 66), ("明日方舟", 65),
    ("魔兽世界", 64), ("守望先锋", 63), ("CSGO", 62), ("DOTA2", 61),
    ("我的世界", 60), ("网易我的世界", 59), ("光与夜之恋", 58), ("第五人格", 57),
    ("碧蓝航线", 56), ("阴阳师", 55), ("命运冠位指定", 54), ("FGO", 53),
    ("三国杀", 52), ("狼人杀", 51), ("剧本杀", 50), ("密室逃脱", 49),
    ("手柄", 48), ("键盘鼠标", 47), ("机械键盘", 46), ("游戏耳机", 45),
    ("电竞椅", 44), ("显卡", 43), ("RTX", 42), ("GTX", 41),
    ("帧数", 40), ("画质", 39), ("分辨率", 38), ("4K", 37),
    ("2K", 36), ("1080P", 35), ("144Hz", 34), ("240Hz", 33),
    ("G-Sync", 32), ("FreeSync", 31), ("固态硬盘", 30), ("内存", 29),
]

CATEGORY_FALLBACK: Dict[str, List[Tuple[str, int]]] = {
    "sports": SPORTS_WORDS,
    "gaming": GAMING_WORDS,
}

RIME_ESSAY_URL = "https://raw.githubusercontent.com/rime/rime-essay/master/essay.txt"

# 额外补充的常用现代词汇 (简体中文)
MODERN_WORDS = [
    ("微信", 500000), ("抖音", 450000), ("淘宝", 400000), ("京东", 350000),
    ("外卖", 300000), ("支付宝", 280000), ("拼多多", 260000), ("微博", 240000),
    ("知乎", 220000), ("小红书", 200000), ("快手", 190000), ("美团", 180000),
    ("滴滴", 170000), ("头条", 160000), ("腾讯", 150000), ("百度", 140000),
    ("华为", 130000), ("小米", 120000), ("OPPO", 110000), ("vivo", 100000),
    ("网易", 95000), ("爱奇艺", 90000), ("优酷", 85000), ("豆瓣", 80000),
    ("饿了么", 75000), ("菜鸟", 70000), ("顺丰", 65000), ("京东物流", 60000),
    ("区块链", 55000), ("比特币", 50000), ("人工智能", 48000), ("元宇宙", 45000),
    ("直播", 42000), ("带货", 40000), ("网红", 38000), ("打卡", 36000),
    ("健康码", 34000), ("核酸", 32000), ("封控", 30000), ("居家", 28000),
    ("上网课", 26000), ("远程办公", 24000), ("裁员", 22000), ("内卷", 20000),
    ("躺平", 18000), ("摆烂", 16000), ("韭菜", 14000), ("割韭菜", 12000),
    ("双十一", 400000), ("618", 350000), ("双十二", 200000),
    ("5G", 100000), ("WiFi", 80000), ("蓝牙", 70000),
    ("新冠", 150000), ("疫苗", 120000), ("疫情", 180000),
    ("特斯拉", 80000), ("比亚迪", 70000), ("蔚来", 60000),
    ("扫地机器人", 30000), ("智能音箱", 25000),
    ("共享单车", 35000), ("充电宝", 30000),
    ("表情包", 25000), ("梗", 20000), ("破防", 18000),
    ("上头", 16000), ("绝绝子", 14000), ("yyds", 12000),
    ("EMO", 10000), ("芭比Q", 9000), ("栓Q", 8000),
]

# ============================================================
# 工具函数
# ============================================================

def download_file(url: str, dest: str) -> bool:
    try:
        print(f"  [下载] {url}")
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=180) as resp:
            content = resp.read()
            if url.endswith('.gz'):
                content = gzip.decompress(content)
            with open(dest, 'wb') as f:
                f.write(content)
        size_kb = len(content) / 1024
        print(f"    -> {dest} ({size_kb:.0f} KB)")
        return True
    except Exception as e:
        print(f"    [失败] {e}")
        return False


def normalize_pinyin(raw: str) -> str:
    pinyin = raw.strip().lower()
    pinyin = re.sub(r'[1-5]', '', pinyin)
    pinyin = re.sub(r"[''\u02c8\u02cc\u0304\u0301\u030c\u0300]", '', pinyin)
    pinyin = re.sub(r'\s+', ' ', pinyin).strip()
    return pinyin


def generate_pinyin(hanzi: str) -> str:
    if not HAS_PYPINYIN:
        return ""
    pinyins = lazy_pinyin(hanzi, style=Style.TONE3, errors='ignore')
    return normalize_pinyin(' '.join(pinyins))


def parse_line(line: str) -> Optional[Tuple[str, str, int]]:
    line = line.strip()
    if not line or line.startswith('#'):
        return None
    parts = line.split('\t')
    hanzi = parts[0].strip()
    if not hanzi:
        return None
    freq = 1
    pinyin = ""
    if len(parts) == 3:
        pinyin = parts[1].strip().lower()
        freq = int(parts[2].strip()) if parts[2].strip().isdigit() else 1
    elif len(parts) == 2:
        freq = int(parts[1].strip()) if parts[1].strip().isdigit() else 1
    else:
        return None
    return (hanzi, pinyin, freq)


# ============================================================
# 构建逻辑 (从 build_dictionary.py 提取)
# ============================================================

def build_sqlite_dict(entries: List[Tuple[str, str, int]], output_path: str):
    if os.path.exists(output_path):
        os.remove(output_path)
    conn = sqlite3.connect(output_path)
    c = conn.cursor()
    c.execute("PRAGMA journal_mode=OFF")
    c.execute("PRAGMA synchronous=OFF")
    c.execute("PRAGMA cache_size=8000")
    c.execute("PRAGMA page_size=4096")
    c.execute("""
        CREATE TABLE dict (
            id INTEGER PRIMARY KEY,
            word TEXT NOT NULL,
            pinyin TEXT NOT NULL,
            freq INTEGER DEFAULT 0
        )
    """)
    c.execute("CREATE INDEX idx_pinyin ON dict(pinyin)")
    c.execute("CREATE INDEX idx_freq ON dict(freq DESC)")
    c.execute("CREATE INDEX idx_word ON dict(word)")
    c.execute("BEGIN TRANSACTION")
    for idx, (hanzi, pinyin, freq) in enumerate(entries):
        c.execute("INSERT INTO dict (id, word, pinyin, freq) VALUES (?, ?, ?, ?)",
                  (idx, hanzi, pinyin, freq))
    c.execute("COMMIT")
    conn.close()
    print(f"  [dict.db] {len(entries):,} 条 -> {output_path}")


def build_category_sqlite(category: str, entries: List[Tuple[str, str, int]], output_path: str):
    if os.path.exists(output_path):
        os.remove(output_path)
    conn = sqlite3.connect(output_path)
    c = conn.cursor()
    c.execute("PRAGMA journal_mode=OFF")
    c.execute("PRAGMA synchronous=OFF")
    c.execute("""
        CREATE TABLE category_dict (
            id INTEGER PRIMARY KEY,
            word TEXT NOT NULL,
            pinyin TEXT NOT NULL,
            freq INTEGER DEFAULT 0,
            category TEXT NOT NULL
        )
    """)
    c.execute("CREATE INDEX idx_cat_pinyin ON category_dict(pinyin)")
    c.execute("CREATE INDEX idx_cat_freq ON category_dict(freq DESC)")
    c.execute("BEGIN TRANSACTION")
    for idx, (hanzi, pinyin, freq) in enumerate(entries):
        c.execute("INSERT INTO category_dict (id, word, pinyin, freq, category) VALUES (?, ?, ?, ?, ?)",
                  (idx, hanzi, pinyin, freq, category))
    c.execute("COMMIT")
    conn.close()
    print(f"  [celldict/{category}.db] {len(entries):,} 条")


def build_bigram_db(entries: List[Tuple[str, str, int]], output_path: str, top_n: int = 80000):
    char_bigram: Dict[Tuple[str, str], int] = defaultdict(int)
    for hanzi, pinyin, freq in entries:
        if len(hanzi) < 2:
            continue
        for i in range(len(hanzi) - 1):
            w1 = hanzi[i]
            w2 = hanzi[i + 1]
            char_bigram[(w1, w2)] += freq
    sorted_bigrams = sorted(char_bigram.items(), key=lambda x: -x[1])[:top_n]
    if os.path.exists(output_path):
        os.remove(output_path)
    conn = sqlite3.connect(output_path)
    c = conn.cursor()
    c.execute("PRAGMA journal_mode=OFF")
    c.execute("PRAGMA synchronous=OFF")
    c.execute("""
        CREATE TABLE bigram (
            prev_word TEXT NOT NULL,
            next_word TEXT NOT NULL,
            freq INTEGER DEFAULT 0,
            PRIMARY KEY (prev_word, next_word)
        )
    """)
    c.execute("CREATE INDEX idx_bigram_prev ON bigram(prev_word)")
    c.execute("BEGIN TRANSACTION")
    for (prev, next_w), freq in sorted_bigrams:
        c.execute("INSERT OR IGNORE INTO bigram (prev_word, next_word, freq) VALUES (?, ?, ?)",
                  (prev, next_w, freq))
    c.execute("COMMIT")
    conn.close()
    print(f"  [bigram.db] {len(sorted_bigrams):,} 条 -> {output_path}")


def process_entries(raw_entries: List[Tuple[str, str, int]], label: str) -> List[Tuple[str, str, int]]:
    """处理原始词条: 生成拼音, 去重, 返回有效词条"""
    seen = set()
    result = []
    skip_no_pinyin = 0
    skip_dup = 0
    for hanzi, raw_pinyin, freq in raw_entries:
        if raw_pinyin:
            pinyin = normalize_pinyin(raw_pinyin)
        else:
            pinyin = generate_pinyin(hanzi) if HAS_PYPINYIN else ""
        if not pinyin:
            skip_no_pinyin += 1
            continue
        key = f"{hanzi}|{pinyin}"
        if key in seen:
            skip_dup += 1
            continue
        seen.add(key)
        result.append((hanzi, pinyin, freq))
    result.sort(key=lambda x: (x[1], -x[2]))
    if skip_no_pinyin or skip_dup:
        print(f"  [{label}] 有效 {len(result):,} / 跳过无拼音 {skip_no_pinyin} / 重复 {skip_dup}")
    else:
        print(f"  [{label}] {len(result):,} 条")
    return result


# ============================================================
# 单字词生成 (THUOCL 没有单字词, 需要为拼音输入提供基础单字)
# ============================================================

COMMON_SINGLE_CHARS = [
    ("的", 5000000), ("一", 4800000), ("是", 4500000), ("了", 4000000),
    ("我", 3800000), ("不", 3700000), ("在", 3600000), ("人", 3500000),
    ("有", 3400000), ("这", 3300000), ("个", 3200000), ("上", 3100000),
    ("们", 3000000), ("来", 2900000), ("到", 2800000), ("说", 2700000),
    ("大", 2600000), ("地", 2500000), ("出", 2400000), ("子", 2300000),
    ("和", 2200000), ("你", 2100000), ("就", 2000000), ("可", 1900000),
    ("也", 1800000), ("他", 1700000), ("她", 1600000), ("会", 1500000),
    ("要", 1400000), ("里", 1300000), ("得", 1200000), ("过", 1100000),
    ("那", 1000000), ("下", 990000), ("能", 980000), ("多", 970000),
    ("去", 960000), ("对", 950000), ("都", 940000), ("小", 930000),
    ("年", 920000), ("很", 910000), ("开", 900000), ("想", 890000),
    ("么", 880000), ("看", 870000), ("用", 860000), ("中", 850000),
    ("好", 840000), ("时", 830000), ("做", 820000), ("把", 810000),
    ("前", 800000), ("为", 790000), ("回", 780000), ("没", 770000),
    ("给", 760000), ("学", 750000), ("家", 740000), ("然", 730000),
    ("生", 720000), ("其", 710000), ("如", 700000), ("发", 690000),
    ("所", 680000), ("成", 670000), ("而", 660000), ("当", 650000),
    ("点", 640000), ("于", 630000), ("只", 620000), ("方", 610000),
    ("行", 600000), ("现", 590000), ("经", 580000), ("种", 570000),
    ("还", 560000), ("自", 550000), ("从", 540000), ("已", 530000),
    ("应", 520000), ("动", 510000), ("面", 500000), ("最", 490000),
    ("天", 480000), ("长", 470000), ("起", 460000), ("后", 450000),
    ("打", 440000), ("两", 430000), ("问", 420000), ("气", 410000),
    ("明", 400000), ("定", 390000), ("新", 380000), ("知", 370000),
    ("手", 360000), ("头", 350000), ("三", 340000), ("力", 330000),
    ("日", 320000), ("十", 310000), ("事", 300000), ("文", 290000),
    ("口", 280000), ("老", 270000), ("再", 260000), ("正", 250000),
    ("心", 240000), ("本", 230000), ("妈", 220000), ("真", 210000),
    ("见", 200000), ("月", 190000), ("全", 180000), ("外", 170000),
    ("水", 160000), ("间", 150000), ("高", 140000), ("走", 130000),
    ("吃", 120000), ("买", 110000), ("睡", 100000), ("写", 99000),
    ("书", 98000), ("钱", 97000), ("电", 96000), ("车", 95000),
    ("话", 94000), ("少", 93000), ("果", 92000), ("白", 91000),
    ("花", 90000), ("爱", 89000), ("机", 88000), ("关", 87000),
    ("孩", 86000), ("王", 85000), ("平", 84000), ("民", 83000),
    ("飞", 82000), ("器", 81000), ("士", 80000), ("品", 79000),
    ("部", 78000), ("战", 77000), ("通", 76000), ("活", 75000),
    ("物", 74000), ("声", 73000), ("道", 72000), ("门", 71000),
    ("放", 70000), ("望", 69000), ("第", 68000), ("重", 67000),
    ("节", 66000), ("四", 65000), ("色", 64000), ("空", 63000),
    ("科", 62000), ("原", 61000), ("连", 60000), ("网", 59000),
    ("笑", 58000), ("安", 57000), ("信", 56000), ("船", 55000),
    ("乐", 54000), ("完", 53000), ("路", 52000), ("半", 51000),
    ("被", 50000), ("记", 49000), ("命", 48000), ("加", 47000),
    ("片", 46000), ("感", 45000), ("计", 44000), ("军", 43000),
    ("领", 42000), ("早", 41000), ("算", 40000), ("主", 39000),
    ("带", 38000), ("鱼", 37000), ("玩", 36000), ("石", 35000),
    ("立", 34000), ("教", 33000), ("号", 32000), ("太", 31000),
    ("万", 30000), ("世", 29000), ("字", 28000), ("星", 27000),
    ("市", 26000), ("钱", 25000), ("金", 24000), ("难", 23000),
    ("总", 22000), ("百", 21000), ("合", 20000), ("男", 19000),
    ("女", 18000), ("讲", 17000), ("听", 16000), ("快", 15000),
    ("光", 14000), ("风", 13000), ("别", 12000), ("工", 11000),
    ("山", 10000), ("马", 9800), ("六", 9600), ("千", 9500),
    ("七", 9400), ("八", 9300), ("九", 9200), ("五", 9100),
    ("先", 9000), ("红", 8900), ("东", 8800), ("南", 8700),
    ("西", 8600), ("北", 8500), ("必", 8400), ("取", 8300),
    ("强", 8200), ("意", 8100), ("怎", 8000), ("服", 7900),
    ("房", 7800), ("答", 7700), ("告", 7600), ("解", 7500),
    ("友", 7400), ("无", 7300), ("变", 7200), ("美", 7100),
    ("穿", 7000), ("提", 6900), ("济", 6800), ("每", 6700),
    ("选", 6600), ("度", 6500), ("父", 6400), ("层", 6300),
    ("助", 6200), ("求", 6100), ("院", 6000), ("台", 5900),
    ("片", 5800), ("何", 5700), ("体", 5600), ("名", 5500),
    ("利", 5400), ("常", 5300), ("热", 5200), ("界", 5100),
    ("跑", 5000), ("较", 4900), ("简", 4800), ("备", 4700),
    ("目", 4600), ("消", 4500), ("容", 4400), ("易", 4300),
    ("欢", 4200), ("近", 4100), ("准", 4000), ("收", 3900),
    ("步", 3800), ("设", 3700), ("苦", 3600), ("底", 3500),
    ("终", 3400), ("周", 3300), ("交", 3200), ("思", 3100),
    ("停", 3000), ("图", 2900), ("转", 2800), ("系", 2700),
    ("拉", 2600), ("共", 2500), ("冷", 2400), ("场", 2300),
    ("达", 2200), ("相", 2100), ("研", 2000), ("练", 1900),
    ("管", 1800), ("胜", 1700), ("办", 1600), ("失", 1500),
    ("留", 1400), ("流", 1300), ("投", 1200), ("般", 1100),
    ("接", 1000), ("觉", 990), ("期", 980), ("近", 970),
    ("视", 960), ("喜", 950), ("局", 940), ("随", 930),
    ("紧", 920), ("够", 910), ("甚", 900), ("师", 890),
    ("直", 880), ("满", 870), ("切", 860), ("系", 850),
    ("改", 840), ("据", 830), ("极", 820), ("影", 810),
    ("疑", 800), ("角", 790), ("油", 780), ("刚", 770),
    ("吗", 760), ("哪", 750), ("喂", 740), ("请", 730),
    ("坐", 720), ("次", 710), ("位", 700), ("找", 690),
    ("员", 680), ("元", 670), ("圆", 660), ("假", 650),
    ("夫", 640), ("妻", 630), ("妹", 620), ("弟", 610),
    ("哥", 600), ("姐", 590), ("婆", 580), ("公", 570),
    ("祖", 560), ("孙", 550), ("脚", 540), ("腿", 530),
    ("双", 520), ("条", 510), ("杯", 500), ("碗", 490),
    ("筷", 480), ("勺", 470), ("叉", 460), ("锅", 450),
    ("盖", 440), ("盘", 430), ("桌", 420), ("椅", 410),
    ("床", 400), ("灯", 390), ("窗", 380), ("墙", 370),
    ("楼", 360), ("树", 350), ("草", 340), ("花", 330),
    ("叶", 320), ("根", 310), ("果", 300), ("菜", 290),
    ("肉", 280), ("蛋", 270), ("奶", 260), ("茶", 250),
    ("酒", 240), ("饭", 230), ("面", 220), ("汤", 210),
    ("糖", 200), ("盐", 190), ("醋", 180), ("油", 170),
    ("米", 160), ("豆", 150), ("瓜", 140), ("椒", 130),
    ("葱", 120), ("蒜", 110), ("姜", 100), ("辣", 90),
    ("甜", 88), ("咸", 86), ("苦", 84), ("酸", 82),
    ("烫", 80), ("冷", 78), ("冰", 76), ("凉", 74),
    ("热", 72), ("温", 70), ("烧", 68), ("煮", 66),
    ("炒", 64), ("烤", 62), ("蒸", 60), ("炖", 58),
    ("炸", 56), ("拌", 54), ("腌", 52), ("切", 50),
    ("削", 48), ("剁", 46), ("撕", 44), ("掰", 42),
]


# ============================================================
# 主流程
# ============================================================

def main():
    output_dir = os.path.abspath(sys.argv[1] if len(sys.argv) > 1 and not sys.argv[1].startswith("--")
                                  else "app/src/main/assets")
    if any(a.startswith("--output-dir") for a in sys.argv):
        for i, a in enumerate(sys.argv):
            if a == "--output-dir" and i + 1 < len(sys.argv):
                output_dir = os.path.abspath(sys.argv[i + 1])
                break

    work_dir = os.path.join(output_dir, "..", ".dict_build_tmp")
    os.makedirs(work_dir, exist_ok=True)
    os.makedirs(output_dir, exist_ok=True)
    celldict_dir = os.path.join(output_dir, "celldict")
    os.makedirs(celldict_dir, exist_ok=True)

    print("=" * 60)
    print("  煎蛋输入法 - 完整简中词库构建工具")
    print(f"  输出目录: {output_dir}")
    print(f"  临时目录: {work_dir}")
    print(f"  pypinyin: {'OK' if HAS_PYPINYIN else '缺失!'}")
    print(f"  OpenCC:   {'OK' if HAS_OPENCC else '缺失 (无法繁转简)'}")
    print("=" * 60)

    if not HAS_PYPINYIN:
        print("\n[错误] pypinyin 未安装! 请运行: pip install pypinyin")
        sys.exit(1)

    # ================================================================
    # Phase 1: 下载所有数据源
    # ================================================================
    print("\n" + "=" * 60)
    print("Phase 1: 下载数据源")
    print("=" * 60)

    # 1a. 下载 THUOCL 各分类
    dl_dir = os.path.join(work_dir, "thuocl")
    os.makedirs(dl_dir, exist_ok=True)
    thuocl_raw: Dict[str, List[Tuple[str, str, int]]] = {}

    for cat, url in THUOCL_FILES.items():
        fname = os.path.basename(url)
        dest = os.path.join(dl_dir, fname)
        if not os.path.exists(dest):
            download_file(url, dest)
        if os.path.exists(dest):
            entries = []
            with open(dest, 'r', encoding='utf-8') as f:
                for line in f:
                    parsed = parse_line(line)
                    if parsed:
                        entries.append(parsed)
            thuocl_raw[cat] = entries
            print(f"  [THUOCL/{cat}] {len(entries):,} 条")

    # 1b. 下载 Rime essay.txt (繁体)
    rime_path = os.path.join(work_dir, "rime_essay.txt")
    if not os.path.exists(rime_path):
        download_file(RIME_ESSAY_URL, rime_path)

    # ================================================================
    # Phase 2: 繁转简 + 解析
    # ================================================================
    print("\n" + "=" * 60)
    print("Phase 2: 处理词库数据")
    print("=" * 60)

    # Rime 繁转简
    rime_entries_raw = []
    if os.path.exists(rime_path):
        if HAS_OPENCC:
            print("  使用 OpenCC 将 Rime 繁体词库转换为简体...")
            cc = OpenCC('t2s')
            with open(rime_path, 'r', encoding='utf-8') as f:
                for line in f:
                    parsed = parse_line(line)
                    if parsed:
                        hanzi_simp = cc.convert(parsed[0])
                        rime_entries_raw.append((hanzi_simp, parsed[1], parsed[2]))
            print(f"  [Rime] 繁转简后 {len(rime_entries_raw):,} 条")
        else:
            print("  警告: OpenCC 未安装, Rime词库将以繁体读入")
            with open(rime_path, 'r', encoding='utf-8') as f:
                for line in f:
                    parsed = parse_line(line)
                    if parsed:
                        rime_entries_raw.append(parsed)
            print(f"  [Rime·繁体] {len(rime_entries_raw):,} 条")

    # 单字词
    single_entries_raw = [(hanzi, "", freq) for hanzi, freq in COMMON_SINGLE_CHARS]
    print(f"  [单字词] {len(single_entries_raw)} 条")

    # 现代词
    modern_entries_raw = [(hanzi, "", freq) for hanzi, freq in MODERN_WORDS]
    print(f"  [现代词] {len(modern_entries_raw)} 条")

    # ================================================================
    # Phase 3: 合并主词库
    # ================================================================
    print("\n" + "=" * 60)
    print("Phase 3: 合并主词库")
    print("=" * 60)

    all_main_raw = []
    all_main_raw.extend(rime_entries_raw)
    all_main_raw.extend(single_entries_raw)
    all_main_raw.extend(modern_entries_raw)
    for cat, entries in thuocl_raw.items():
        all_main_raw.extend(entries)

    main_entries = process_entries(all_main_raw, "主词库合并")

    # ================================================================
    # Phase 4: 构建 dict.db
    # ================================================================
    print("\n" + "=" * 60)
    print("Phase 4: 构建 dict.db + bigram.db")
    print("=" * 60)

    dict_db_path = os.path.join(output_dir, "dict.db")
    build_sqlite_dict(main_entries, dict_db_path)

    bigram_db_path = os.path.join(output_dir, "bigram.db")
    build_bigram_db(main_entries, bigram_db_path)

    # ================================================================
    # Phase 5: 构建分类词库
    # ================================================================
    print("\n" + "=" * 60)
    print("Phase 5: 构建分类词库 celldict/*.db")
    print("=" * 60)

    for cat in CellDictManager.CATEGORIES:
        thuocl_map = {
            "it": "it", "medical": "medical", "legal": "legal",
            "finance": "finance", "food": "food", "geography": "geography",
            "idiom": "idiom", "poetry": "poetry",
        }
        base_cat = thuocl_map.get(cat)
        if base_cat and base_cat in thuocl_raw:
            cat_entries = process_entries(thuocl_raw[base_cat], f"分类/{cat}")
        elif cat in CATEGORY_FALLBACK:
            fallback = CATEGORY_FALLBACK[cat]
            raw = [(hanzi, "", freq) for hanzi, freq in fallback]
            cat_entries = process_entries(raw, f"分类/{cat}")
        else:
            cat_entries = []

        db_path = os.path.join(celldict_dir, f"{cat}.db")
        build_category_sqlite(cat, cat_entries, db_path)

    # ================================================================
    # 构建额外分类 (非 CellDictManager 10个的补充)
    # ================================================================
    extra_cats = {
        "history": thuocl_raw.get("history", []),
        "animal": thuocl_raw.get("animal", []),
        "car": thuocl_raw.get("car", []),
    }
    for cat, raw in extra_cats.items():
        if raw:
            cat_entries = process_entries(raw, f"分类/{cat}")
            db_path = os.path.join(celldict_dir, f"{cat}.db")
            build_category_sqlite(cat, cat_entries, db_path)

    # ================================================================
    # 统计报告
    # ================================================================
    print("\n" + "=" * 60)
    print("构建完成!")
    print("=" * 60)
    print(f"  主词库:   {len(main_entries):,} 条 ({os.path.getsize(dict_db_path) / 1024 / 1024:.1f} MB)")
    print(f"  Bigram:   ({os.path.getsize(bigram_db_path) / 1024 / 1024:.1f} MB)")
    print(f"  分类词库: {len(os.listdir(celldict_dir))} 个")
    for fname in sorted(os.listdir(celldict_dir)):
        fpath = os.path.join(celldict_dir, fname)
        if fname.endswith('.db'):
            size_mb = os.path.getsize(fpath) / 1024 / 1024
            conn = sqlite3.connect(fpath)
            count = conn.execute("SELECT COUNT(*) FROM category_dict").fetchone()[0]
            conn.close()
            print(f"    {fname}: {count:,} 条 ({size_mb:.1f} MB)")

    # 验证: 抽样检查是否为简体中文
    conn = sqlite3.connect(dict_db_path)
    sample = conn.execute("SELECT word FROM dict ORDER BY freq DESC LIMIT 20").fetchall()
    conn.close()
    common_traditional = set("爲於臺灣體國學書長門開關無")
    has_trad = any(any(c in common_traditional for c in row[0]) for row in sample)
    print(f"\n  验证: 高频词 {'含繁体⚠️' if has_trad else '均为简体✓'}")
    print(f"  高频词示例: {', '.join(row[0] for row in sample[:10])}")


# 用于引用 CellDictManager 中定义的分类常量
class CellDictManager:
    CATEGORIES = [
        "medical", "legal", "it", "gaming", "geography",
        "idiom", "poetry", "food", "sports", "finance",
    ]


if __name__ == "__main__":
    main()