package net.jradius.dal.model;

import com.coova.dal.Record;
import com.coova.dal.SynchronizedRecord;
import java.util.Date;

public class SynchronizedRadAcct extends RadAcct implements SynchronizedRecord {

    public SynchronizedRadAcct(RadAcct o) {
        super.setId(o.getId());
        super.setAcctsessionid(o.getAcctsessionid());
        super.setAcctuniqueid(o.getAcctuniqueid());
        super.setUsername(o.getUsername());
        super.setGroupname(o.getGroupname());
        super.setRealm(o.getRealm());
        super.setNasipaddress(o.getNasipaddress());
        super.setNasportid(o.getNasportid());
        super.setNasporttype(o.getNasporttype());
        super.setAcctstarttime(o.getAcctstarttime());
        super.setAcctstoptime(o.getAcctstoptime());
        super.setAcctsessiontime(o.getAcctsessiontime());
        super.setAcctauthentic(o.getAcctauthentic());
        super.setConnectinfoStart(o.getConnectinfoStart());
        super.setConnectinfoStop(o.getConnectinfoStop());
        super.setAcctinputoctets(o.getAcctinputoctets());
        super.setAcctoutputoctets(o.getAcctoutputoctets());
        super.setCalledstationid(o.getCalledstationid());
        super.setCallingstationid(o.getCallingstationid());
        super.setAcctterminatecause(o.getAcctterminatecause());
        super.setServicetype(o.getServicetype());
        super.setFramedprotocol(o.getFramedprotocol());
        super.setFramedipaddress(o.getFramedipaddress());
        super.setAcctstartdelay(o.getAcctstartdelay());
        super.setAcctstopdelay(o.getAcctstopdelay());
        super.setXascendsessionsvrkey(o.getXascendsessionsvrkey());
    }

    public synchronized Long getId() {
        return super.getId();
    }

    public synchronized void setId(Long id) {
        super.setId(id);
    }

    public synchronized String getAcctsessionid() {
        return super.getAcctsessionid();
    }

    public synchronized void setAcctsessionid(String acctsessionid) {
        super.setAcctsessionid(acctsessionid);
    }

    public synchronized String getAcctuniqueid() {
        return super.getAcctuniqueid();
    }

    public synchronized void setAcctuniqueid(String acctuniqueid) {
        super.setAcctuniqueid(acctuniqueid);
    }

    public synchronized String getUsername() {
        return super.getUsername();
    }

    public synchronized void setUsername(String username) {
        super.setUsername(username);
    }

    public synchronized String getGroupname() {
        return super.getGroupname();
    }

    public synchronized void setGroupname(String groupname) {
        super.setGroupname(groupname);
    }

    public synchronized String getRealm() {
        return super.getRealm();
    }

    public synchronized void setRealm(String realm) {
        super.setRealm(realm);
    }

    public synchronized String getNasipaddress() {
        return super.getNasipaddress();
    }

    public synchronized void setNasipaddress(String nasipaddress) {
        super.setNasipaddress(nasipaddress);
    }

    public synchronized String getNasportid() {
        return super.getNasportid();
    }

    public synchronized void setNasportid(String nasportid) {
        super.setNasportid(nasportid);
    }

    public synchronized String getNasporttype() {
        return super.getNasporttype();
    }

    public synchronized void setNasporttype(String nasporttype) {
        super.setNasporttype(nasporttype);
    }

    public synchronized Date getAcctstarttime() {
        return super.getAcctstarttime();
    }

    public synchronized void setAcctstarttime(Date acctstarttime) {
        super.setAcctstarttime(acctstarttime);
    }

    public synchronized Date getAcctstoptime() {
        return super.getAcctstoptime();
    }

    public synchronized void setAcctstoptime(Date acctstoptime) {
        super.setAcctstoptime(acctstoptime);
    }

    public synchronized Integer getAcctsessiontime() {
        return super.getAcctsessiontime();
    }

    public synchronized void setAcctsessiontime(Integer acctsessiontime) {
        super.setAcctsessiontime(acctsessiontime);
    }

    public synchronized String getAcctauthentic() {
        return super.getAcctauthentic();
    }

    public synchronized void setAcctauthentic(String acctauthentic) {
        super.setAcctauthentic(acctauthentic);
    }

    public synchronized String getConnectinfoStart() {
        return super.getConnectinfoStart();
    }

    public synchronized void setConnectinfoStart(String connectinfoStart) {
        super.setConnectinfoStart(connectinfoStart);
    }

    public synchronized String getConnectinfoStop() {
        return super.getConnectinfoStop();
    }

    public synchronized void setConnectinfoStop(String connectinfoStop) {
        super.setConnectinfoStop(connectinfoStop);
    }

    public synchronized Long getAcctinputoctets() {
        return super.getAcctinputoctets();
    }

    public synchronized void setAcctinputoctets(Long acctinputoctets) {
        super.setAcctinputoctets(acctinputoctets);
    }

    public synchronized Long getAcctoutputoctets() {
        return super.getAcctoutputoctets();
    }

    public synchronized void setAcctoutputoctets(Long acctoutputoctets) {
        super.setAcctoutputoctets(acctoutputoctets);
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

    public synchronized String getAcctterminatecause() {
        return super.getAcctterminatecause();
    }

    public synchronized void setAcctterminatecause(String acctterminatecause) {
        super.setAcctterminatecause(acctterminatecause);
    }

    public synchronized String getServicetype() {
        return super.getServicetype();
    }

    public synchronized void setServicetype(String servicetype) {
        super.setServicetype(servicetype);
    }

    public synchronized String getFramedprotocol() {
        return super.getFramedprotocol();
    }

    public synchronized void setFramedprotocol(String framedprotocol) {
        super.setFramedprotocol(framedprotocol);
    }

    public synchronized String getFramedipaddress() {
        return super.getFramedipaddress();
    }

    public synchronized void setFramedipaddress(String framedipaddress) {
        super.setFramedipaddress(framedipaddress);
    }

    public synchronized Integer getAcctstartdelay() {
        return super.getAcctstartdelay();
    }

    public synchronized void setAcctstartdelay(Integer acctstartdelay) {
        super.setAcctstartdelay(acctstartdelay);
    }

    public synchronized Integer getAcctstopdelay() {
        return super.getAcctstopdelay();
    }

    public synchronized void setAcctstopdelay(Integer acctstopdelay) {
        super.setAcctstopdelay(acctstopdelay);
    }

    public synchronized String getXascendsessionsvrkey() {
        return super.getXascendsessionsvrkey();
    }

    public synchronized void setXascendsessionsvrkey(String xascendsessionsvrkey) {
        super.setXascendsessionsvrkey(xascendsessionsvrkey);
    }

    public Record synchronizedRecord() {
        return this;
    }
}