package com.acuman;

import com.luhuiguo.chinese.ChineseUtils;
import com.luhuiguo.chinese.pinyin.PinyinFormat;
import org.junit.Test;

public class ChineseUtilTest {

    @Test
    public void big5() {
        String result = ChineseUtils.toTraditional("出发, 头发, 皇后, 后面, 干燥, 精干, 干涉");
        System.out.println(result);

        result = ChineseUtils.toSimplified("出發, 頭髮, 皇后, 後面, 乾燥, 精幹, 幹涉");
        System.out.println(result);

        result = ChineseUtils.toTraditional("硬盘, 内存, 光驱, 硬件");
        System.out.println(result);

        result = ChineseUtils.toTraditional("解表法,黄子铭,黄靖媛");
        System.out.println(result);

        result = ChineseUtils.toPinyin("蓝山昨天下午有预防性烧山行动,大家请注意.", PinyinFormat.TONELESS_PINYIN_FORMAT);
        System.out.println(result);
    }
}
