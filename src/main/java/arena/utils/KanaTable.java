/*
 * Generator Runtime Servlet Framework
 * Copyright (C) 2004 Rick Knowles
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package arena.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Used to convert between different kinds of kana and romaji
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: KanaTable.java,v 1.8 2008/06/26 07:05:06 dareya Exp $
 */
public class KanaTable {
    public static final int ROMAJI = 0;
    public static final int ZENKAKU = 1;
    public static final int HIRAGANA = 2;
    public static final int HANKAKU = 3;
    public static final int MIXED = 4;
    private static Map<String,String[]> keyedByRomaji;
    private static Map<String,String[]> keyedByZenkaku;
    private static Map<String,String[]> keyedByHiragana;
    private static Map<String,String[]> keyedByHankaku;
    static final String[][] kanaSets = {
            { " ", "　", "　", "　" },
            { " ", " ", " ", " " },
            { "-", "ー", "ー", "ー" },
            

            { "a", "ア", "あ", "ｱ" },
            { "i", "イ", "い", "ｲ" },
            { "u", "ウ", "う", "ｳ" },
            { "e", "エ", "え", "ｴ" },
            { "o", "オ", "お", "ｵ" },
            

            { "sa", "サ", "さ", "ｻ" },
            { "si", "シ", "し", "ｼ" },
            { "shi", "シ", "し", "ｼ" },
            { "su", "ス", "す", "ｽ" },
            { "se", "セ", "せ", "ｾ" },
            { "so", "ソ", "そ", "ｿ" },
            { "ta", "タ", "た", "ﾀ" },
            { "ti", "チ", "ち", "ﾁ" },
            { "chi", "チ", "ち", "ﾁ" },
            { "tsu", "ツ", "つ", "ﾂ" },
            { "te", "テ", "て", "ﾃ" },
            { "to", "ト", "と", "ﾄ" },
            { "na", "ナ", "な", "ﾅ" },
            { "ni", "ニ", "に", "ﾆ" },
            { "nu", "ヌ", "ぬ", "ﾇ" },
            { "ne", "ネ", "ね", "ﾈ" },
            { "no", "ノ", "の", "ﾉ" },
            { "ka", "カ", "か", "ｶ" },
            { "ki", "キ", "き", "ｷ" },
            { "ku", "ク", "く", "ｸ" },
            { "ke", "ケ", "け", "ｹ" },
            { "ko", "コ", "こ", "ｺ" },
            { "ha", "ハ", "は", "ﾊ" },
            { "hi", "ヒ", "ひ", "ﾋ" },
            { "hu", "フ", "ふ", "ﾌ" },
            { "he", "ヘ", "へ", "ﾍ" },
            { "ho", "ホ", "ほ", "ﾎ" },
            { "ma", "マ", "ま", "ﾏ" },
            { "mi", "ミ", "み", "ﾐ" },
            { "mu", "ム", "む", "ﾑ" },
            { "me", "メ", "め", "ﾒ" },
            { "mo", "モ", "も", "ﾓ" },
            { "ya", "ヤ", "や", "ﾔ" },
            { "yu", "ユ", "ゆ", "ﾕ" },
            { "yo", "ヨ", "よ", "ﾖ" },
            { "ra", "ラ", "ら", "ﾗ" },
            { "ri", "リ", "り", "ﾘ" },
            { "ru", "ル", "る", "ﾙ" },
            { "re", "レ", "れ", "ﾚ" },
            { "ro", "ロ", "ろ", "ﾛ" },
            { "wa", "ワ", "わ", "ﾜ" },
            { "wo", "ヲ", "を", "ｦ" },
            { "nn", "ン", "ん", "ﾝ" },

            { "ga", "ガ", "が", "ｶﾞ" },
            { "gi", "ギ", "ぎ", "ｷﾞ" },
            { "gu", "グ", "ぐ", "ｸﾞ" },
            { "ge", "ゲ", "げ", "ｹﾞ" },
            { "go", "ゴ", "ご", "ｺﾞ" },
            { "za", "ザ", "ざ", "ｻﾞ" },
            { "ja", "ザ", "ざ", "ｻﾞ" },
            { "zi", "ジ", "じ", "ｼﾞ" },
            { "ji", "ジ", "じ", "ｼﾞ" },
            { "zu", "ズ", "ず", "ｽﾞ" },
            { "ju", "ズ", "ず", "ｽﾞ" },
            { "ze", "ゼ", "ぜ", "ｾﾞ" },
            { "je", "ゼ", "ぜ", "ｾﾞ" },
            { "zo", "ゾ", "ぞ", "ｿﾞ" },
            { "jo", "ゾ", "ぞ", "ｿﾞ" },
            { "da", "ダ", "だ", "ﾀﾞ" },
            { "di", "ヂ", "ぢ", "ﾁﾞ" },
            { "du", "ヅ", "づ", "ﾂﾞ" },
            { "de", "デ", "で", "ﾃﾞ" },
            { "do", "ド", "ど", "ﾄﾞ" },
            { "ba", "バ", "ば", "ﾊﾞ" },
            { "bi", "ビ", "び", "ﾋﾞ" },
            { "bu", "ブ", "ぶ", "ﾌﾞ" },
            { "be", "ベ", "べ", "ﾍﾞ" },
            { "bo", "ボ", "ぼ", "ﾎﾞ" },
            { "pa", "パ", "ぱ", "ﾊﾟ" },
            { "pi", "ピ", "ぴ", "ﾋﾟ" },
            { "pu", "プ", "ぷ", "ﾌﾟ" },
            { "pe", "ペ", "ぺ", "ﾍﾟ" },
            { "po", "ポ", "ぽ", "ﾎﾟ" },

            { "kka", "ッカ", "っか", "ｯｶ" },
            { "kki", "ッキ", "っき", "ｯｷ" },
            { "kku", "ック", "っく", "ｯｸ" },
            { "kke", "ッケ", "っけ", "ｯｹ" },
            { "kko", "ッコ", "っこ", "ｯｺ" },
            { "ssa", "ッサ", "っさ", "ｯｻ" },
            { "ssi", "ッシ", "っし", "ｯｼ" },
            { "sshi", "ッシ", "っし", "ｯｼ" },
            { "ssu", "ッス", "っす", "ｯｽ" },
            { "sse", "ッセ", "っせ", "ｯｾ" },
            { "sso", "ッソ", "っそ", "ｯｿ" },
            { "tta", "ッタ", "った", "ｯﾀ" },
            { "tti", "ッチ", "っち", "ｯﾁ" },
            { "cchi", "ッチ", "っち", "ｯﾁ" },
            { "ttu", "ッツ", "っつ", "ｯﾂ" },
            { "tte", "ッテ", "って", "ｯﾃ" },
            { "tto", "ット", "っと", "ｯﾄ" },
            { "hha", "ッハ", "っは", "ｯﾊ" },
            { "hhi", "ッヒ", "っひ", "ｯﾋ" },
            { "hhu", "ッフ", "っふ", "ｯﾌ" },
            { "hhe", "ッヘ", "っへ", "ｯﾍ" },
            { "hho", "ッホ", "っほ", "ｯﾎ" },
            { "mma", "ッマ", "っま", "ｯﾏ" },
            { "mmi", "ッミ", "っみ", "ｯﾐ" },
            { "mmu", "ッム", "っむ", "ｯﾑ" },
            { "mme", "ッメ", "っめ", "ｯﾒ" },
            { "mmo", "ッモ", "っも", "ｯﾓ" },
            { "yya", "ッヤ", "っや", "ｯﾔ" },
            { "yyu", "ッユ", "っゆ", "ｯﾕ" },
            { "yyo", "ッヨ", "っよ", "ｯﾖ" },
            { "rra", "ッラ", "っら", "ｯﾗ" },
            { "rri", "ッリ", "っり", "ｯﾘ" },
            { "rru", "ッル", "っる", "ｯﾙ" },
            { "rre", "ッレ", "っれ", "ｯﾚ" },
            { "rro", "ッロ", "っろ", "ｯﾛ" },
            { "wwa", "ッワ", "っわ", "ｯﾜ" },
            { "wwo", "ッヲ", "っを", "ｯｦ" },
            

            { "gga", "ッガ", "っが", "ｯｶﾞ" },
            { "ggi", "ッギ", "っぎ", "ｯｷﾞ" },
            { "ggu", "ッグ", "っぐ", "ｯｸﾞ" },
            { "gge", "ッゲ", "っげ", "ｯｹﾞ" },
            { "ggo", "ッゴ", "っご", "ｯｺﾞ" },
            { "zza", "ッザ", "っざ", "ｯｻﾞ" },
            { "zzi", "ッジ", "っじ", "ｯｼﾞ" },
            { "jji", "ッジ", "っじ", "ｯｼﾞ" },
            { "zzu", "ッズ", "っず", "ｯｽﾞ" },
            { "zze", "ッゼ", "っぜ", "ｯｾﾞ" },
            { "zzo", "ッゾ", "っぞ", "ｯｿﾞ" },
            { "dda", "ッダ", "っだ", "ｯﾀﾞ" },
            { "ddi", "ッヂ", "っぢ", "ｯﾁﾞ" },
            { "ddu", "ッヅ", "っづ", "ｯﾂﾞ" },
            { "dde", "ッデ", "っで", "ｯﾃﾞ" },
            { "ddo", "ッド", "っど", "ｯﾄﾞ" },
            { "bba", "ッバ", "っば", "ｯﾊﾞ" },
            { "bbi", "ッビ", "っび", "ｯﾋﾞ" },
            { "bbu", "ッブ", "っぶ", "ｯﾌﾞ" },
            { "bbe", "ッベ", "っべ", "ｯﾍﾞ" },
            { "bbo", "ッボ", "っぼ", "ｯﾎﾞ" },
            

            { "ppa", "ッパ", "っぱ", "ｯﾊﾟ" },
            { "ppi", "ッピ", "っぴ", "ｯﾋﾟ" },
            { "ppu", "ップ", "っぷ", "ｯﾌﾟ" },
            { "ppe", "ッペ", "っぺ", "ｯﾍﾟ" },
            { "ppo", "ッポ", "っぽ", "ｯﾎﾟ" },
            

            { "xa", "ァ", "ぁ", "ｧ" },
            { "xi", "ィ", "ぃ", "ｨ" },
            { "xu", "ゥ", "ぅ", "ｩ" },
            { "xe", "ェ", "ぇ", "ｪ" },
            { "xo", "ォ", "ぉ", "ｫ" },
            { "la", "ァ", "ぁ", "ｧ" },
            { "li", "ィ", "ぃ", "ｨ" },
            { "lu", "ゥ", "ぅ", "ｩ" },
            { "le", "ェ", "ぇ", "ｪ" },
            { "lo", "ォ", "ぉ", "ｫ" },
            { "xka", "ヵ", "ヵ", "ヵ" },
            { "xwa", "ヮ", "ゎ", "ゎ" },
            { "xyu", "ュ", "ゅ", "ｭ" },
            { "xyo", "ョ", "ょ", "ｮ" },
            { "xtu", "ッ", "っ", "ｯ" },
            

            { "ffa", "ッファ", "っふぁ", "ｯﾌｧ" },
            { "ffi", "ッフィ", "っふぃ", "ｯﾌｨ" },
            { "ffe", "ッフェ", "っふぇ", "ｯﾌｪ" },
            { "ffo", "ッフォ", "っふぉ", "ｯﾌｫ" },
            

            { "sha", "シャ", "しゃ", "ｼｬ" },
            { "shu", "シュ", "しゅ", "ｼｭ" },
            { "she", "シェ", "しぇ", "ｼｪ" },
            { "sho", "ショ", "しょ", "ｼｮ" },
            
            
            { "cha", "チャ", "ちゃ", "ﾁｬ" },
            { "che", "チェ", "ちぇ", "ﾁｪ" },
            { "cho", "チョ", "ちょ", "ﾁｮ" },
            { "chu", "チュ", "ちゅ", "ﾁｭ" },
            
            { "ja", "ジャ", "じゃ", "ｼﾞｬ" },
            { "je", "ジェ", "じぇ", "ｼﾞｪ" },
            { "jo", "ジョ", "じょ", "ｼﾞｮ" },
            { "ju", "ジュ", "じゅ", "ｼﾞｭ" },            

            { "gya", "ギャ", "ぎゃ", "ｷﾞｬ" },
            { "gye", "ギェ", "ぎぇ", "ｷﾞｪ" },
            { "gyo", "ギョ", "ぎょ", "ｷﾞｮ" },
            { "gyu", "ギュ", "ぎゅ", "ｷﾞｭ" },      
            
            { "kya", "キャ", "きゃ", "ｷｬ" },
            { "kye", "キェ", "きぇ", "ｷｪ" },
            { "kyo", "キョ", "きょ", "ｷｮ" },
            { "kyu", "キュ", "きゅ", "ｷｭ" },
            
            { "kkya", "ッキャ", "っきゃ", "ｯｷｬ" },
            { "kkye", "ッキェ", "っきぇ", "ｯｷｪ" },
            { "kkyo", "ッキョ", "っきょ", "ｯｷｮ" },
            { "kkyu", "ッキュ", "っきゅ", "ｯｷｭ" },
            

            { "ssya", "ッシャ", "っしゃ", "ｯｼｬ" },
            { "ssha", "ッシャ", "っしゃ", "ｯｼｬ" },
            { "ssyi", "ッシィ", "っしぃ", "ｯｼｨ" },
            { "sshi", "ッシィ", "っしぃ", "ｯｼｨ" },
            { "ssyu", "ッシュ", "っしゅ", "ｯｼｭ" },
            { "sshu", "ッシュ", "っしゅ", "ｯｼｭ" },
            { "ssye", "ッシェ", "っしぇ", "ｯｼｪ" },
            { "sshe", "ッシェ", "っしぇ", "ｯｼｪ" },
            { "ssyo", "ッショ", "っしょ", "ｯｼｮ" },
            { "ssho", "ッショ", "っしょ", "ｯｼｮ" },
            { "ttya", "ッチャ", "っちゃ", "ｯﾁｬ" },
            { "ccha", "ッチャ", "っちゃ", "ｯﾁｬ" },
            { "ttyi", "ッチィ", "っちぃ", "ｯﾁｨ" },
            { "ttyu", "ッチュ", "っちゅ", "ｯﾁｭ" },
            { "cchu", "ッチュ", "っちゅ", "ｯﾁｭ" },
            { "ttye", "ッチェ", "っちぇ", "ｯﾁｪ" },
            { "cche", "ッチェ", "っちぇ", "ｯﾁｪ" },
            { "ttyo", "ッチョ", "っちょ", "ｯﾁｮ" },
            { "ccho", "ッチョ", "っちょ", "ｯﾁｮ" },
            

            { "ppya", "ッピャ", "っぴゃ", "ｯﾋﾟｬ" },
            { "ppyi", "ッピィ", "っぴぃ", "ｯﾋﾟｨ" },
            { "ppyu", "ッピュ", "っぴゅ", "ｯﾋﾟｭ" },
            { "ppye", "ッピェ", "っぴぇ", "ｯﾋﾟｪ" },
            { "ppyo", "ッピョ", "っぴょ", "ｯﾋﾟｮ" },
            

            { "jja", "ッジャ", "っじゃ", "ｯｼﾞｬ" },
            { "jjya", "ッジャ", "っじゃ", "ｯｼﾞｬ" },
            { "jjyi", "ッジィ", "っじぃ", "ｯｼﾞｨ" },
            { "jju", "ッジュ", "っじゅ", "ｯｼﾞｭ" },
            { "jjyu", "ッジュ", "っじゅ", "ｯｼﾞｭ" },
            { "jje", "ッジェ", "っじぇ", "ｯｼﾞｪ" },
            { "jjye", "ッジェ", "っじぇ", "ｯｼﾞｪ" },
            { "jjo", "ッジョ", "っじょ", "ｯｼﾞｮ" },
            { "jjyo", "ッジョ", "っじょ", "ｯｼﾞｮ" },
            

            { "ddya", "ッヂャ", "っぢゃ", "ｯﾁﾞｬ" },
            { "ddyi", "ッヂィ", "っぢぃ", "ｯﾁﾞｨ" },
            { "ddyu", "ッヂュ", "っぢゅ", "ｯﾁﾞｭ" },
            { "ddye", "ッヂェ", "っぢぇ", "ｯﾁﾞｪ" },
            { "ddyo", "ッヂョ", "っぢょ", "ｯﾁﾞｮ" },
            
            { "cha", "チャ", "ちゃ", "ﾁｬ" },
            { "che", "チェ", "ちぇ", "ﾁｪ" },
            { "cho", "チョ", "ちょ", "ﾁｮ" },
            { "chu", "チュ", "ちゅ", "ﾁｭ" },

            { "va", "ヴァ", "ヴぁ", "ｳﾞｧ" },
            { "vi", "ヴィ", "ヴぃ", "ｳﾞｨ" },
            { "vu", "ヴ", "ヴ", "ｳﾞ" },
            { "ve", "ヴェ", "ヴぇ", "ｳﾞｪ" },
            { "vo", "ヴォ", "ヴぉ", "ｳﾞｫ" },
            { "vvya", "ッヴャ", "っヴゃ", "ｯｳﾞｬ" },
            { "vvyi", "ッヴィ", "っヴぃ", "ｯｳﾞｨ" },
            { "vvyu", "ッヴュ", "っヴゅ", "ｯｳﾞｭ" },
            { "vvye", "ッヴェ", "っヴぇ", "ｯｳﾞｪ" },
            { "vvyo", "ッヴョ", "っヴょ", "ｯｳﾞｮ" }
        };

    static {
        keyedByRomaji = Collections.synchronizedMap(new HashMap<String,String[]>());
        keyedByZenkaku = Collections.synchronizedMap(new HashMap<String,String[]>());
        keyedByHiragana = Collections.synchronizedMap(new HashMap<String,String[]>());
        keyedByHankaku = Collections.synchronizedMap(new HashMap<String,String[]>());

        for (int n = 0; n < kanaSets.length; n++) {
            keyedByRomaji.put(kanaSets[n][ROMAJI], kanaSets[n]);
            keyedByZenkaku.put(kanaSets[n][ZENKAKU], kanaSets[n]);
            keyedByHiragana.put(kanaSets[n][HIRAGANA], kanaSets[n]);
            keyedByHankaku.put(kanaSets[n][HANKAKU], kanaSets[n]);
        }
    }

    /**
     * Converts a string to be output in a specific kind of kana
     */
    public static String convert(String input, int toType) {
        return convert(input, MIXED, toType);
    }
    
    /**
     * Converts a string to be output in a specific kind of kana
     * @param input The string we want to convert
     * @param fromType The type of characters to filter for conversion. 
     *                 MIXED means all
     * @param toType The type of characters to convert to if recognized. 
     *               MIXED means no conversion
     */
    public static String convert(String input, int fromType, int toType) {
        if (input == null) {
            return null;
        } else if (toType == MIXED) {
            return input;
        }

        // Iterate through the input string, checking for matches in clumps
        // of 4 chars, then 3, 2, 1. If none, write it first char untouched
        StringBuffer out = new StringBuffer();
        int charPos = 0;

        while (charPos < input.length()) {
            boolean found = false;

            for (int n = Math.min(4, input.length() - charPos); (n > 0) && !found; n--) {
                String current = input.substring(charPos, charPos + n);

                if (((fromType == ZENKAKU) || (fromType == MIXED)) && 
                        keyedByZenkaku.containsKey(current)) {
                    String[] matches = (String[]) keyedByZenkaku.get(current);

                    out.append(matches[toType]);
                    charPos += n;
                    found = true;
                } else if (((fromType == HIRAGANA) || (fromType == MIXED)) && 
                        keyedByHiragana.containsKey(current)) {
                    String[] matches = (String[]) keyedByHiragana.get(current);

                    out.append(matches[toType]);
                    charPos += n;
                    found = true;
                } else if (((fromType == HANKAKU) || (fromType == MIXED)) && 
                        keyedByHankaku.containsKey(current)) {
                    String[] matches = (String[]) keyedByHankaku.get(current);

                    out.append(matches[toType]);
                    charPos += n;
                    found = true;
                } else if (((fromType == ROMAJI) || (fromType == MIXED)) && 
                        keyedByRomaji.containsKey(current)) {
                    String[] matches = (String[]) keyedByRomaji.get(current);

                    out.append(matches[toType]);
                    charPos += n;
                    found = true;
                }
            }

            // If no matches, copy the char through unchanged and moved to next 
            if (!found) {
                out.append(input.charAt(charPos));
                charPos++;
            }
        }

        return out.toString();
    }

    /**
     * Returns true if all the characters in the input string match the type
     * specified. The mechanism is to convert to that type and confirm that
     * there are no changes
     */
    public static boolean isAllOneKanaType(String input, int type) {
        if (input == null) {
            return false;
        } else if (type == MIXED) {
            return true;
        }

        // Iterate through the input string, checking for matches in clumps
        // of 4 chars, then 3, 2, 1. If none, return false
        int charPos = 0;

        while (charPos < input.length()) {
            boolean found = false;

            for (int n = Math.min(4, input.length() - charPos); (n > 0) && !found; n--) {
                String current = input.substring(charPos, charPos + n);

                if ((type == ZENKAKU) && keyedByZenkaku.containsKey(current)) {
                    found = true;
                    charPos += n;
                } else if ((type == HANKAKU) && keyedByHankaku.containsKey(current)) {
                    found = true;
                    charPos += n;
                } else if ((type == HIRAGANA) && keyedByHiragana.containsKey(current)) {
                    found = true;
                    charPos += n;
                } else if ((type == ROMAJI) && keyedByRomaji.containsKey(current)) {
                    found = true;
                    charPos += n;
                }
            }

            // If no matches, return false
            if (!found) {
                return false;
            }
        }

        return true;
    }
}
