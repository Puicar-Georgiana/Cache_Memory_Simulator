package org.example;

public class CacheLine {
    public int tag;
    public String data;
    public boolean valid;
    public boolean dirty;
    public long lastUsed;
    public long insertionOrder;

    public CacheLine() {
        this.valid = false;
        this.dirty = false;
        this.tag = -1;
        this.data = "";
        this.lastUsed = 0;
        this.insertionOrder = 0;
    }
}
