package com.github.czipperz.gitgui;

import java.util.Objects;

public class Ref {
    public boolean isHead;
    public boolean isTag;
    public boolean isTrackingOrigin;
    public String name;

    public Ref(String name) {
        this.name = name;
        removePrefixes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ref ref = (Ref) o;
        return isHead == ref.isHead &&
                isTag == ref.isTag &&
                isTrackingOrigin == ref.isTrackingOrigin &&
                Objects.equals(name, ref.name);
    }

    public void removePrefixes() {
        if (name.startsWith("HEAD -> ")) {
            isHead = true;
            name = name.substring("HEAD -> ".length());
        }

        if (name.startsWith("tag: ")) {
            isTag = true;
            name = name.substring("tag: ".length());
        }
    }

    public boolean isFilteredOut() {
        return name.equals("origin/HEAD");
    }
}
