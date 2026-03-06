package org.example;

import java.util.Map;
import java.util.Random;

public class CacheMemory {

    public enum ReplacementPolicy { LRU, FIFO, RANDOM }
    public enum MappingType { DIRECT, SET_ASSOCIATIVE, FULLY_ASSOCIATIVE }
    public enum WritePolicy { WRITE_BACK, WRITE_THROUGH }

    private CacheLine[] cache;
    private int cacheSize;
    private int ways = 1;
    private long counter = 0;
    private Random random = new Random();

    private ReplacementPolicy replacementPolicy = ReplacementPolicy.LRU;
    private MappingType mappingType = MappingType.DIRECT;
    private WritePolicy writePolicy = WritePolicy.WRITE_BACK;

    public CacheMemory(int size) {
        this.cacheSize = size;
        this.cache = new CacheLine[size];
        for (int i = 0; i < size; i++) {
            cache[i] = new CacheLine();
        }
    }

    public void setReplacementPolicy(ReplacementPolicy policy) {
        this.replacementPolicy = policy;
    }

    public void setMappingType(MappingType type, int ways) {
        this.mappingType = type;
        this.ways = ways;
    }

    public void setWritePolicy(WritePolicy policy) {
        this.writePolicy = policy;
    }

    public int getCacheSize() { return cacheSize; }
    public CacheLine[] getCache() { return cache; }
    public ReplacementPolicy getReplacementPolicy() { return replacementPolicy; }
    public MappingType getMappingType() { return mappingType; }
    public WritePolicy getWritePolicy() { return writePolicy; }

    public int getIndex(int address) {
        if(mappingType == MappingType.DIRECT) {
            return address % cacheSize;
        } else if(mappingType == MappingType.SET_ASSOCIATIVE) {
            int numSets = cacheSize / ways;
            return (address % numSets) * ways;
        } else {
            return -1;
        }
    }

    public int getTag(int address) {
        if(mappingType == MappingType.DIRECT) {
            return address / cacheSize;
        } else if(mappingType == MappingType.SET_ASSOCIATIVE) {
            int numSets = cacheSize / ways;
            return address / numSets;
        } else {
            return address;
        }
    }


    public int checkHit(int address) {
        if(mappingType == MappingType.FULLY_ASSOCIATIVE) {
            for(int i = 0; i < cacheSize; i++) {
                if(cache[i].valid && cache[i].tag == getTag(address)) {
                    cache[i].lastUsed = counter++;
                    return i;
                }
            }
            return -1;
        } else if(mappingType == MappingType.SET_ASSOCIATIVE) {
            int start = getIndex(address);
            for(int i = start; i < start + ways; i++) {
                if(cache[i].valid && cache[i].tag == getTag(address)) {
                    cache[i].lastUsed = counter++;
                    return i;
                }
            }
            return -1;
        } else {
            int index = getIndex(address);
            if(cache[index].valid && cache[index].tag == getTag(address)) {
                return index;
            }
            return -1;
        }
    }

    public boolean isHit(int address) {
        return checkHit(address) != -1;
    }

    public int selectLineToReplace(int startIndex) {
        if(mappingType == MappingType.FULLY_ASSOCIATIVE) {
            return selectByPolicy(0, cacheSize);
        } else if(mappingType == MappingType.SET_ASSOCIATIVE) {
            return selectByPolicy(startIndex, startIndex + ways);
        } else {
            return startIndex;
        }
    }

    private int selectByPolicy(int start, int end) {
        switch (replacementPolicy) {
            case LRU:
                long minUsed = Long.MAX_VALUE;
                int lruIndex = start;
                for(int i = start; i < end; i++) {
                    if(!cache[i].valid) return i;
                    if(cache[i].lastUsed < minUsed) {
                        minUsed = cache[i].lastUsed;
                        lruIndex = i;
                    }
                }
                return lruIndex;

            case FIFO:
                long minOrder = Long.MAX_VALUE;
                int fifoIndex = start;
                for(int i = start; i < end; i++) {
                    if(!cache[i].valid) return i;
                    if(cache[i].insertionOrder < minOrder) {
                        minOrder = cache[i].insertionOrder;
                        fifoIndex = i;
                    }
                }
                return fifoIndex;

            case RANDOM:
                for(int i = start; i < end; i++) {
                    if(!cache[i].valid) return i;
                }
                return start + random.nextInt(end - start);

            default:
                return start;
        }
    }


    public void placeLine(int address, String data, Map<Integer,String> mainMemory) {
        int startIndex = (mappingType == MappingType.FULLY_ASSOCIATIVE) ? 0 : getIndex(address);
        int replaceIndex = selectLineToReplace(startIndex);
        CacheLine line = cache[replaceIndex];

        if(line.valid && line.dirty && writePolicy == WritePolicy.WRITE_BACK) {
            int oldAddress = reconstructAddress(line.tag, replaceIndex);
            mainMemory.put(oldAddress, line.data);
        }

        line.tag = getTag(address);
        line.data = data;
        line.valid = true;
        line.dirty = false;
        line.lastUsed = counter++;
        line.insertionOrder = counter++;
    }


    public void writeLine(int address, String data, Map<Integer,String> mainMemory) {
        int hitIndex = checkHit(address);

        if(hitIndex != -1) {
            CacheLine line = cache[hitIndex];
            line.data = data;
            line.dirty = (writePolicy == WritePolicy.WRITE_BACK);
            line.lastUsed = counter++;

            if(writePolicy == WritePolicy.WRITE_THROUGH) {
                mainMemory.put(address, data);
            }
        } else {
            int startIndex = (mappingType == MappingType.FULLY_ASSOCIATIVE) ? 0 : getIndex(address);
            int replaceIndex = selectLineToReplace(startIndex);
            CacheLine line = cache[replaceIndex];

            if(line.valid && line.dirty && writePolicy == WritePolicy.WRITE_BACK) {
                int oldAddress = reconstructAddress(line.tag, replaceIndex);
                mainMemory.put(oldAddress, line.data);
            }

            line.tag = getTag(address);
            line.data = data;
            line.valid = true;
            line.dirty = (writePolicy == WritePolicy.WRITE_BACK);
            line.lastUsed = counter++;
            line.insertionOrder = counter++;

            if(writePolicy == WritePolicy.WRITE_THROUGH) {
                mainMemory.put(address, data);
            }
        }
    }

    private int reconstructAddress(int tag, int index) {
        if(mappingType == MappingType.DIRECT) {
            return tag * cacheSize + index;
        } else if(mappingType == MappingType.SET_ASSOCIATIVE) {
            int numSets = cacheSize / ways;
            int setIndex = index / ways;
            return tag * numSets + setIndex;
        } else {
            return tag;
        }
    }


    public String getData(int address) {
        if(mappingType == MappingType.FULLY_ASSOCIATIVE) {
            for(CacheLine line : cache) {
                if(line.valid && line.tag == getTag(address)) {
                    return line.data;
                }
            }
            return null;
        } else if(mappingType == MappingType.SET_ASSOCIATIVE) {
            int start = getIndex(address);
            for(int i = start; i < start + ways; i++) {
                if(cache[i].valid && cache[i].tag == getTag(address)) {
                    return cache[i].data;
                }
            }
            return null;
        } else {
            int index = getIndex(address);
            return cache[index].valid ? cache[index].data : null;
        }
    }
}