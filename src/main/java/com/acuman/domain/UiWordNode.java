package com.acuman.domain;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Used to build word tree for UI display, do not persist to DB as the tree can be big
 */
public class UiWordNode extends ZhEnWord {

    // not persisted to DB
    private Set<UiWordNode> children = new LinkedHashSet<>();

    public UiWordNode() {
    }

    public static UiWordNode fromWord(ZhEnWord zhEnWord) {
        UiWordNode result = new UiWordNode();
        try {
            BeanUtils.copyProperties(result, zhEnWord);
        } catch (IllegalAccessException|InvocationTargetException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return result;
    }

    public Set<UiWordNode> getChildren() {
        return children;
    }

    public void setChildren(Set<UiWordNode> children) {
        this.children = children;
    }

    public void addChild(UiWordNode word) {
        children.add(word);
    }
}
