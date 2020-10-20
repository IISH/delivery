package org.socialhistoryservices.delivery.record.util;

import java.util.List;

public class Inventory {
    private String unitId;
    private String title;
    private List<Inventory> children;

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Inventory> getChildren() {
        return children;
    }

    public void setChildren(List<Inventory> children) {
        this.children = children;
    }
}
