package com.xkcoding.kafkatrans;

public class KafkaTransBinaryHead {
    private byte magic;
    private byte headerLen;
    private byte ipVersion;    // 4 bits
    private byte reserve;      // 2 bits
    private boolean isFirst;   // 1 bit
    private boolean isDone;    // 1 bit
    private byte netlinkId;
    private int dataLen;
    private short sequence;
    private short pktCount;
    private byte[] ipaddr;     // variable length

    // Getters and setters
    public byte getMagic() { return magic; }
    public void setMagic(byte magic) { this.magic = magic; }
    
    public byte getHeaderLen() { return headerLen; }
    public void setHeaderLen(byte headerLen) { this.headerLen = headerLen; }
    
    public byte getIpVersion() { return ipVersion; }
    public void setIpVersion(byte ipVersion) { this.ipVersion = ipVersion; }
    
    public byte getReserve() { return reserve; }
    public void setReserve(byte reserve) { this.reserve = reserve; }
    
    public boolean isFirst() { return isFirst; }
    public void setFirst(boolean first) { isFirst = first; }
    
    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }
    
    public byte getNetlinkId() { return netlinkId; }
    public void setNetlinkId(byte netlinkId) { this.netlinkId = netlinkId; }
    
    public int getDataLen() { return dataLen; }
    public void setDataLen(int dataLen) { this.dataLen = dataLen; }
    
    public short getSequence() { return sequence; }
    public void setSequence(short sequence) { this.sequence = sequence; }
    
    public short getPktCount() { return pktCount; }
    public void setPktCount(short pktCount) { this.pktCount = pktCount; }
    
    public byte[] getIpaddr() { return ipaddr; }
    public void setIpaddr(byte[] ipaddr) { this.ipaddr = ipaddr; }
} 