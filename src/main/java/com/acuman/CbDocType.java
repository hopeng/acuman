package com.acuman;

import com.acuman.domain.WordNode;
import com.acuman.domain.ZhEnWord;

/**
 * Created by hopeng on 8/05/2016.
 */
public interface CbDocType {
    String ZhEnWord = ZhEnWord.class.getSimpleName();
    String WordNode = WordNode.class.getSimpleName();
    String Patient = "PATIENT";
    String Consult = "CONSULTATION";
}
