package net.jradius.dal.model;

import com.coova.dal.Record;
import com.coova.dal.SynchronizedRecord;

public class SynchronizedRadGroupCheck extends RadGroupCheck implements SynchronizedRecord {

    public SynchronizedRadGroupCheck(RadGroupCheck o) {
        super.setId(o.getId());
        super.setGroupname(o.getGroupname());
        super.setAttribute(o.getAttribute());
        super.setOp(o.getOp());
        super.setValue(o.getValue());
    }

    public synchronized Long getId() {
        return super.getId();
    }

    public synchronized void setId(Long id) {
        super.setId(id);
    }

    public synchronized String getGroupname() {
        return super.getGroupname();
    }

    public synchronized void setGroupname(String groupname) {
        super.setGroupname(groupname);
    }

    public synchronized String getAttribute() {
        return super.getAttribute();
    }

    public synchronized void setAttribute(String attribute) {
        super.setAttribute(attribute);
    }

    public synchronized String getOp() {
        return super.getOp();
    }

    public synchronized void setOp(String op) {
        super.setOp(op);
    }

    public synchronized String getValue() {
        return super.getValue();
    }

    public synchronized void setValue(String value) {
        super.setValue(value);
    }

    public Record synchronizedRecord() {
        return this;
    }
}