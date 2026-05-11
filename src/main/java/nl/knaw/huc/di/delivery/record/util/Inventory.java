package nl.knaw.huc.di.delivery.record.util;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.List;

public class Inventory implements Serializable {
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

    @JsonIgnore
    public String getFirstChildUnitId() {
        if (children.isEmpty())
            return null;

        Inventory child = children.getFirst();

        String firstUnitId = child.getFirstChildUnitId();
        if (firstUnitId != null)
            return firstUnitId;

        if (child.getUnitId() != null)
            return child.getUnitId();

        return null;
    }

    @JsonIgnore
    public String getLastChildUnitId() {
        if (children.isEmpty())
            return null;

        Inventory child = children.getLast();

        String lastUnitId = child.getLastChildUnitId();
        if (lastUnitId != null)
            return lastUnitId;

        if (child.getUnitId() != null)
            return child.getUnitId();

        return null;
    }
}
