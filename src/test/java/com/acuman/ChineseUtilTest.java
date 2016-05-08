package com.acuman;

import com.hankcs.hanlp.HanLP;
import org.junit.Test;

/**
 * Created by hopeng on 5/05/2016.
 */
public class ChineseUtilTest {

    @Test
    public void hanLPTest() {
        String result = HanLP.convertToPinyinString("this is not chinese, lala", " ", false);
        System.out.println(result);

        result = HanLP.convertToPinyinString("蓝山昨天下午有预防性烧山行动,大家请注意.", " ", false);
        System.out.println(result);

        result = HanLP.convertToTraditionalChinese("蓝山昨天下午有预防性烧山行动");
        System.out.println(result);

        result = HanLP.convertToTraditionalChinese("皇后的后面有条龙");
        System.out.println(result);

        result = HanLP.convertToSimplifiedChinese("皇后的後面有條龍");
        System.out.println(result);

        result = HanLP.convertToSimplifiedChinese("皇后的后面有條龍");
        System.out.println(result);

        result = HanLP.convertToTraditionalChinese("皇后的后面有条龙");
        System.out.println(result);

    }

}
