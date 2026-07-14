#!/usr/bin/env python3
"""
build_dictionary.py - 从Rime词库构建主流输入法词库

支持两种输入格式:
  1. 完整格式: 汉字\\t拼音\\t词频
  2. Rime essay格式: 汉字\\t词频 (无拼音, 自动用pypinyin生成)

生成物:
  dict.db          - SQLite词库 (word, pinyin, freq, id, 索引)
  pinyin_keys.txt  - 拼音→候选ID列表映射文件 (供C++ Trie构建使用)
  bigram.db        - 2-gram统计表 (从词库词内相邻字推断 + 可选语料库)

用法:
  python build_dictionary.py <essay.txt> [--output-dir <dir>] [--corpus <corpus.txt>]
  python build_dictionary.py --mode full [--output-dir <dir>] [--download-source <url>]
  python build_dictionary.py --merge-celldict <celldict_dir> <essay.txt> [--output-dir <dir>]
"""

import sqlite3
import sys
import os
import re
import json
from collections import defaultdict
from typing import List, Tuple, Dict

try:
    from pypinyin import lazy_pinyin, Style
    HAS_PYPINYIN = True
except ImportError:
    HAS_PYPINYIN = False

try:
    import urllib.request
    import gzip
    HAS_URLLIB = True
except ImportError:
    HAS_URLLIB = False


def parse_line(line: str) -> Tuple[str, str, int] | None:
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


def normalize_pinyin(raw: str) -> str:
    pinyin = raw.strip().lower()
    pinyin = re.sub(r'[1-5]', '', pinyin)
    pinyin = re.sub(r"[''\u02c8\u02cc\u0304\u0301\u030c\u0300]", '', pinyin)
    pinyin = re.sub(r'\s+', ' ', pinyin).strip()
    return pinyin


def generate_pinyin_for_hanzi(hanzi: str) -> str:
    if not HAS_PYPINYIN:
        return ""
    pinyins = lazy_pinyin(hanzi, style=Style.TONE3, errors='ignore')
    joined = ' '.join(pinyins)
    return normalize_pinyin(joined)


def download_source(url: str, dest_path: str) -> bool:
    if not HAS_URLLIB:
        print("[下载] urllib 不可用，跳过下载")
        return False
    try:
        print(f"[下载] 从 {url} 下载词库数据...")
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=120) as resp:
            content = resp.read()
            if url.endswith('.gz'):
                content = gzip.decompress(content)
            with open(dest_path, 'wb') as f:
                f.write(content)
        print(f"[下载] 保存到 {dest_path} ({len(content):,} bytes)")
        return True
    except Exception as e:
        print(f"[下载] 失败: {e}")
        return False


def load_celldict_files(celldict_dir: str) -> Dict[str, List[Tuple[str, str, int]]]:
    cat_entries: Dict[str, List[Tuple[str, str, int]]] = defaultdict(list)
    if not os.path.isdir(celldict_dir):
        print(f"[Celldict] 目录不存在: {celldict_dir}")
        return cat_entries

    for fname in sorted(os.listdir(celldict_dir)):
        if not fname.endswith('.txt'):
            continue
        category = fname.replace('.txt', '')
        fpath = os.path.join(celldict_dir, fname)
        print(f"[Celldict] 加载分类: {category} <- {fpath}")
        with open(fpath, 'r', encoding='utf-8') as f:
            for line in f:
                parsed = parse_line(line)
                if parsed:
                    cat_entries[category].append(parsed)
        print(f"  -> {len(cat_entries[category])} 条")
    return cat_entries


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
        c.execute(
            "INSERT INTO dict (id, word, pinyin, freq) VALUES (?, ?, ?, ?)",
            (idx, hanzi, pinyin, freq)
        )
    c.execute("COMMIT")
    conn.close()
    print(f"[SQLite] 写入 {len(entries)} 条记录 -> {output_path}")


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
        c.execute(
            "INSERT INTO category_dict (id, word, pinyin, freq, category) VALUES (?, ?, ?, ?, ?)",
            (idx, hanzi, pinyin, freq, category)
        )
    c.execute("COMMIT")
    conn.close()
    print(f"[Celldict SQLite] {category}: {len(entries)} 条 -> {output_path}")


def build_pinyin_prefix_index(entries: List[Tuple[str, str, int]], output_path: str):
    groups: Dict[str, Dict[str, any]] = {}

    for idx, (hanzi, pinyin, freq) in enumerate(entries):
        if pinyin not in groups:
            groups[pinyin] = {"start": idx, "end": idx, "top_ids": [idx], "top_freq": freq}
        else:
            g = groups[pinyin]
            g["end"] = idx
            if len(g["top_ids"]) < 5:
                g["top_ids"].append(idx)
            elif freq > g["top_freq"]:
                g["top_ids"][-1] = idx
                g["top_freq"] = freq

    sorted_pinyins = sorted(groups.keys())

    syllable_prefix_map: Dict[str, List[str]] = defaultdict(list)
    for py in sorted_pinyins:
        syllables = py.split(' ')
        if not syllables:
            continue
        for seg_end in range(1, len(syllables) + 1):
            prefix = ' '.join(syllables[:seg_end])
            syllable_prefix_map[prefix].append(py)

    with open(output_path, 'w', encoding='utf-8') as f:
        f.write("# pinyin_keys.txt - 拼音索引文件\n")
        f.write("# 格式: pinyin <tb> start_id <tb> end_id <tb> top_ids\n")
        f.write(f"# 词条数: {len(entries)}, 拼音种类: {len(sorted_pinyins)}\n")

        for py in sorted_pinyins:
            g = groups[py]
            top_ids = ','.join(str(x) for x in g["top_ids"])
            f.write(f"{py}\t{g['start']}\t{g['end']}\t{top_ids}\n")

    prefix_path = output_path.replace('.txt', '_prefix.txt')
    with open(prefix_path, 'w', encoding='utf-8') as f:
        f.write("# pinyin_prefix.txt - 音节前缀→完整拼音列表\n")
        f.write("# 格式: prefix <tab> full_pinyin <tab> end_id\n")
        f.write(f"# 前缀种类: {len(syllable_prefix_map)}\n")

        for prefix in sorted(syllable_prefix_map.keys()):
            pinyin_list = syllable_prefix_map[prefix]
            referenced_end_ids = []
            for py in pinyin_list:
                if py in groups:
                    referenced_end_ids.append(groups[py]["end"])
            if referenced_end_ids:
                max_end = max(referenced_end_ids)
                f.write(f"{prefix}\t{max_end}\n")

    print(f"[索引] 拼音前缀: {len(sorted_pinyins)} 种 -> {output_path}")
    print(f"[索引] 音节前缀: {len(syllable_prefix_map)} 种 -> {prefix_path}")


def build_bigram_db_from_entries(entries: List[Tuple[str, str, int]], output_path: str, top_n: int = 80000):
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
    c.execute("CREATE INDEX idx_bigram_next ON bigram(next_word)")

    c.execute("BEGIN TRANSACTION")
    for (prev, next_w), freq in sorted_bigrams:
        c.execute(
            "INSERT OR IGNORE INTO bigram (prev_word, next_word, freq) VALUES (?, ?, ?)",
            (prev, next_w, freq)
        )
    c.execute("COMMIT")
    conn.close()
    print(f"[Bigram] 从词库推断 {len(sorted_bigrams)} 条字级2-gram -> {output_path}")


def main():
    input_path = None
    output_dir = "."
    corpus_path = None
    celldict_dir = None
    download_url = None
    mode = "normal"

    args = sys.argv[1:]
    i = 0
    while i < len(args):
        if args[i] == "--output-dir" and i + 1 < len(args):
            output_dir = args[i + 1]
            i += 2
        elif args[i] == "--corpus" and i + 1 < len(args):
            corpus_path = args[i + 1]
            i += 2
        elif args[i] == "--merge-celldict" and i + 1 < len(args):
            celldict_dir = args[i + 1]
            i += 2
        elif args[i] == "--download-source" and i + 1 < len(args):
            download_url = args[i + 1]
            i += 2
        elif args[i] == "--mode" and i + 1 < len(args):
            mode = args[i + 1]
            i += 2
        elif not args[i].startswith("--"):
            input_path = args[i]
            i += 1
        else:
            print(f"未知参数: {args[i]}")
            sys.exit(1)

    if mode == "full":
        print("[全量模式] 构建大规模词库 (目标 30万+ 词条)")
        os.makedirs(output_dir, exist_ok=True)
        combined_path = os.path.join(output_dir, "_combined_input.txt")
        with open(combined_path, 'w', encoding='utf-8') as fout:
            fout.write("# Combined full dictionary input\n")
            count = 0
            if input_path and os.path.exists(input_path):
                with open(input_path, 'r', encoding='utf-8') as fin:
                    for line in fin:
                        if line.strip() and not line.startswith('#'):
                            fout.write(line)
                            count += 1
                print(f"[全量] 基础词库: {count} 条")
            if download_url:
                downloaded = os.path.join(output_dir, "_downloaded.txt")
                if download_source(download_url, downloaded):
                    with open(downloaded, 'r', encoding='utf-8') as fin:
                        for line in fin:
                            if line.strip() and not line.startswith('#'):
                                fout.write(line)
                                count += 1
                    print(f"[全量] 下载词库: +{count} 条")
        input_path = combined_path
        print(f"[全量] 合并后输入: {input_path}")
    elif celldict_dir:
        print(f"[合并分类词库] 来源目录: {celldict_dir}")

    if not input_path:
        print("用法:")
        print("  python build_dictionary.py <word_list.txt> [--output-dir <dir>] [--corpus <corpus.txt>]")
        print("  python build_dictionary.py --mode full [<word_list.txt>] [--output-dir <dir>] [--download-source <url>]")
        print("  python build_dictionary.py --merge-celldict <celldict_dir> <word_list.txt> [--output-dir <dir>]")
        print("  输入文件支持两种格式:")
        print("    完整格式: 汉字\\t拼音\\t词频")
        print("    Rime essay格式: 汉字\\t词频 (自动用pypinyin生成拼音)")
        sys.exit(1)

    if not os.path.exists(input_path):
        print(f"文件不存在: {input_path}")
        sys.exit(1)

    if not HAS_PYPINYIN:
        print("警告: pypinyin 未安装. 对无拼音的词条, 拼音字段将为空!")
        print("      请执行 pip install pypinyin 后重新运行.")

    output_dir = os.path.abspath(output_dir)
    os.makedirs(output_dir, exist_ok=True)

    print(f"从词库文件读取: {input_path}")
    print(f"输出目录: {output_dir}")

    print("\n[Step 1] 解析词库 + 生成拼音...")
    raw_entries = []
    with open(input_path, 'r', encoding='utf-8') as f:
        for line_num, line in enumerate(f, 1):
            parsed = parse_line(line)
            if parsed is None:
                continue
            raw_entries.append(parsed)

    total_raw = len(raw_entries)
    print(f"  原始行数: {total_raw}")

    has_pinyin = any(entry[1] for entry in raw_entries[:1000])
    print(f"  格式检测: {'完整格式(含拼音)' if has_pinyin else 'Rime essay格式(无拼音, 将自动生成)'}")

    entries: List[Tuple[str, str, int]] = []
    seen = set()
    no_pinyin_count = 0
    skip_count = 0

    for hanzi, raw_pinyin, freq in raw_entries:
        if raw_pinyin:
            pinyin = normalize_pinyin(raw_pinyin)
        else:
            pinyin = generate_pinyin_for_hanzi(hanzi) if HAS_PYPINYIN else ""

        if not pinyin:
            no_pinyin_count += 1
            if no_pinyin_count <= 10:
                print(f"    跳过(无拼音): [{hanzi}]")
            skip_count += 1
            continue

        key = f"{hanzi}|{pinyin}"
        if key in seen:
            skip_count += 1
            continue
        seen.add(key)
        entries.append((hanzi, pinyin, freq))

    entries.sort(key=lambda x: (x[1], -x[2]))
    print(f"  有效词条: {len(entries)} 条 (跳过 {skip_count} 条重复/无拼音)")

    dict_db_path = os.path.join(output_dir, "dict.db")
    pinyin_idx_path = os.path.join(output_dir, "pinyin_keys.txt")
    bigram_db_path = os.path.join(output_dir, "bigram.db")

    print("\n[Step 2] 构建SQLite词库 dict.db ...")
    build_sqlite_dict(entries, dict_db_path)

    if celldict_dir:
        print("\n[Step 2b] 构建分类词库 SQLite ...")
        cat_entries = load_celldict_files(celldict_dir)
        celldict_out_dir = os.path.join(output_dir, "celldict")
        os.makedirs(celldict_out_dir, exist_ok=True)
        for cat, cat_ents in cat_entries.items():
            out_db = os.path.join(celldict_out_dir, f"{cat}.db")
            build_category_sqlite(cat, cat_ents, out_db)

    print("\n[Step 3] 构建拼音前缀索引...")
    build_pinyin_prefix_index(entries, pinyin_idx_path)

    print("\n[Step 4] 构建2-gram语言模型 bigram.db ...")
    build_bigram_db_from_entries(entries, bigram_db_path)

    pinyin_set = set(e[1] for e in entries)
    total_syllables = sum(len(e[1].split(' ')) for e in entries)

    print("\n========== 构建完成 ==========")
    print(f"  词条总数:     {len(entries):,}")
    print(f"  拼音种类:     {len(pinyin_set):,}")
    print(f"  平均音节数:   {total_syllables / len(entries):.1f}")
    print()
    print(f"  dict.db          ({os.path.getsize(dict_db_path):,} bytes)")
    print(f"  pinyin_keys.txt  ({os.path.getsize(pinyin_idx_path):,} bytes)")
    print(f"  bigram.db        ({os.path.getsize(bigram_db_path):,} bytes)")
    print()
    print("将这些文件放入 app/src/main/assets/ 即可.")


if __name__ == "__main__":
    main()