package com.acuman.domain;

import com.acuman.CbDocType;
import com.acuman.util.StringUtils;

/**
 * Created by hopeng on 8/05/2016.
 */
public class ZhEnWord extends Auditable {

    // word Id
    protected String mid;

    protected String type = CbDocType.ZhEnWord;

    // traditional chinese
    protected String cc;

    // simplified chinese
    protected String cs;

    // pinyin with accent
    protected String py1;

    // pinyin
    protected String py3;

    protected String pic;

    // english
    protected String eng1;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getType() {
        return type;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getCs() {
        return cs;
    }

    public void setCs(String cs) {
        this.cs = cs;
    }

    public String getPy1() {
        return py1;
    }

    public void setPy1(String py1) {
        this.py1 = py1;
    }

    public String getPy3() {
        return py3;
    }

    public void setPy3(String py3) {
        this.py3 = py3;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getEng1() {
        return eng1;
    }

    public void setEng1(String eng1) {
        this.eng1 = eng1;
    }

    public void trimFields() {
        cs = StringUtils.trimNonBreaking(cs);
        cc = StringUtils.trimNonBreaking(cc);
        eng1 = StringUtils.trimNonBreaking(eng1);
    }
}
