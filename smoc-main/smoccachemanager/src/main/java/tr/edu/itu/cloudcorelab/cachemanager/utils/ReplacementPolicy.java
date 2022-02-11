package tr.edu.itu.cloudcorelab.cachemanager.utils;

public enum ReplacementPolicy {
    RANDOM, // Use Java Random Generator
    FIFO, //First in first out
    LRU, //Least recently used
    LFU, //Least frequently used
}
