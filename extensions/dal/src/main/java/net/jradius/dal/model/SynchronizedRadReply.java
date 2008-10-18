package net.jradius.dal.model;

import com.coova.dal.Record;
import com.coova.dal.SynchronizedRecord;

public class SynchronizedRadReply extends RadReply implements SynchronizedRecord {

    public SynchronizedRadReply(RadReply o) {
        super.setId(o.getId());
        super.setUsername(o.getUsername());
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

    public synchronized String getUsername() {
        return super.getUsername();
    }

    public synchronized void setUsername(String username) {
        super.setUsername(username);
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