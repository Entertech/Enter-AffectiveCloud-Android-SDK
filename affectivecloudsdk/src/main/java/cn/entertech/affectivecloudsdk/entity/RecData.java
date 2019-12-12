package cn.entertech.affectivecloudsdk.entity;

import java.util.HashMap;
import java.util.List;

public class RecData {
    private float st;
    private float et;
    private HashMap<String, ?> tag;
    private List<String> note;

    public float getSt() {
        return st;
    }

    public void setSt(float st) {
        this.st = st;
    }

    public float getEt() {
        return et;
    }

    public void setEt(float et) {
        this.et = et;
    }

    public HashMap<String, ?> getTag() {
        return tag;
    }

    public void setTag(HashMap<String, ?> tag) {
        this.tag = tag;
    }

    public List<String> getNote() {
        return note;
    }

    public void setNote(List<String> note) {
        this.note = note;
    }
}
