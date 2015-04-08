package datamodels;

import java.io.Serializable;

/**
 * Created by Ahmed on 29-Jun-14.
 */
public class ProviderObject implements Serializable {
    int id;
    String name;
    String slug;
    int dock;

    public int isDock() {
        return dock;
    }

    public void setDock(int dock) {
        this.dock = dock;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
