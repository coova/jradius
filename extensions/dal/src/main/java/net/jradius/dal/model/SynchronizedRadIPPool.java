package net.jradius.dal.model;

import com.coova.dal.Record;
import com.coova.dal.SynchronizedRecord;
import java.util.Date;

public class SynchronizedRadIPPool extends RadIPPool implements SynchronizedRecord {

    public SynchronizedRadIPPool(RadIPPool o) {
        super.setId(o.getId());
        super.setPoolName(o.getPoolName());
        super.setFramedipaddress(o.getFramedipaddress());
        super.setNasipaddress(o.getNasipaddress());
        super.setCalledstationid(o.getCalledstationid());
        super.setCallingstationid(o.getCallingstationid());
        super.setExpiryTime(o.getExpiryTime());
        super.setUsername(o.getUsername());
        super.setPoolKey(o.getPoolKey());
    }

    public synchronized Long getId() {
        return super.getId();
    }

    public synchronized void setId(Long id) {
        super.setId(id);
    }

    public synchronized String getPoolName() {
        return super.getPoolName();
    }

    public synchronized void setPoolName(String poolName) {
        super.setPoolName(poolName);
    }

    public synchronized String getFramedipaddress() {
        return super.getFramedipaddress();
    }

    public synchronized void setFramedipaddress(String framedipaddress) {
        super.setFramedipaddress(framedipaddress);
    }

    public synchronized String getNasipaddress() {
        return super.getNasipaddress();
    }

    public synchronized void setNasipaddress(String nasipaddress) {
        super.setNasipaddress(nasipaddress);
    }

    public synchronized String getCalledstationid() {
        return super.getCalledstationid();
    }

    public synchronized void setCalledstationid(String calledstationid) {
        super.setCalledstationid(calledstationid);
    }

    public synchronized String getCallingstationid() {
        return super.getCallingstationid();
    }

    public synchronized void setCallingstationid(String callingstationid) {
        super.setCallingstationid(callingstationid);
    }

    public synchronized Date getExpiryTime() {
        return super.getExpiryTime();
    }

    public synchronized void setExpiryTime(Date expiryTime) {
        super.setExpiryTime(expiryTime);
    }

    public synchronized String getUsername() {
        return super.getUsername();
    }

    public synchronized void setUsername(String username) {
        super.setUsername(username);
    }

    public synchronized String getPoolKey() {
        return super.getPoolKey();
    }

    public synchronized void setPoolKey(String poolKey) {
        super.setPoolKey(poolKey);
    }

    public Record synchronizedRecord() {
        return this;
    }
}