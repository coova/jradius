package net.jradius.dal.model;

import com.coova.dal.Record;
import com.coova.dal.SynchronizedRecord;
import java.util.Date;

public class SynchronizedRadPostAuth extends RadPostAuth implements SynchronizedRecord {

    public SynchronizedRadPostAuth(RadPostAuth o) {
        super.setId(o.getId());
        super.setUsername(o.getUsername());
        super.setPass(o.getPass());
        super.setReply(o.getReply());
        super.setAuthdate(o.getAuthdate());
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

    public synchronized String getPass() {
        return super.getPass();
    }

    public synchronized void setPass(String pass) {
        super.setPass(pass);
    }

    public synchronized String getReply() {
        return super.getReply();
    }

    public synchronized void setReply(String reply) {
        super.setReply(reply);
    }

    public synchronized Date getAuthdate() {
        return super.getAuthdate();
    }

    public synchronized void setAuthdate(Date authdate) {
        super.setAuthdate(authdate);
    }

    public Record synchronizedRecord() {
        return this;
    }
}