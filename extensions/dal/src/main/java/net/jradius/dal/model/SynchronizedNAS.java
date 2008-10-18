package net.jradius.dal.model;

import com.coova.dal.Record;
import com.coova.dal.SynchronizedRecord;

public class SynchronizedNAS extends NAS implements SynchronizedRecord {

    public SynchronizedNAS(NAS o) {
        super.setId(o.getId());
        super.setNasname(o.getNasname());
        super.setNastype(o.getNastype());
        super.setCalledstationid(o.getCalledstationid());
        super.setShortname(o.getShortname());
        super.setPorts(o.getPorts());
        super.setSecret(o.getSecret());
        super.setCommunity(o.getCommunity());
        super.setDescription(o.getDescription());
        super.setLatitude(o.getLatitude());
        super.setLongitude(o.getLongitude());
    }

    public synchronized Long getId() {
        return super.getId();
    }

    public synchronized void setId(Long id) {
        super.setId(id);
    }

    public synchronized String getNasname() {
        return super.getNasname();
    }

    public synchronized void setNasname(String nasname) {
        super.setNasname(nasname);
    }

    public synchronized String getNastype() {
        return super.getNastype();
    }

    public synchronized void setNastype(String nastype) {
        super.setNastype(nastype);
    }

    public synchronized String getCalledstationid() {
        return super.getCalledstationid();
    }

    public synchronized void setCalledstationid(String calledstationid) {
        super.setCalledstationid(calledstationid);
    }

    public synchronized String getShortname() {
        return super.getShortname();
    }

    public synchronized void setShortname(String shortname) {
        super.setShortname(shortname);
    }

    public synchronized Integer getPorts() {
        return super.getPorts();
    }

    public synchronized void setPorts(Integer ports) {
        super.setPorts(ports);
    }

    public synchronized String getSecret() {
        return super.getSecret();
    }

    public synchronized void setSecret(String secret) {
        super.setSecret(secret);
    }

    public synchronized String getCommunity() {
        return super.getCommunity();
    }

    public synchronized void setCommunity(String community) {
        super.setCommunity(community);
    }

    public synchronized String getDescription() {
        return super.getDescription();
    }

    public synchronized void setDescription(String description) {
        super.setDescription(description);
    }

    public synchronized Double getLatitude() {
        return super.getLatitude();
    }

    public synchronized void setLatitude(Double latitude) {
        super.setLatitude(latitude);
    }

    public synchronized Double getLongitude() {
        return super.getLongitude();
    }

    public synchronized void setLongitude(Double longitude) {
        super.setLongitude(longitude);
    }

    public Record synchronizedRecord() {
        return this;
    }
}