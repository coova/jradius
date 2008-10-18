package net.jradius.dal.model;

import com.coova.dal.Record;
import com.coova.dal.SynchronizedRecord;

public class SynchronizedRadUserGroup extends RadUserGroup implements SynchronizedRecord {

    public SynchronizedRadUserGroup(RadUserGroup o) {
        super.setId(o.getId());
        super.setUsername(o.getUsername());
        super.setGroupname(o.getGroupname());
        super.setPriority(o.getPriority());
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

    public synchronized String getGroupname() {
        return super.getGroupname();
    }

    public synchronized void setGroupname(String groupname) {
        super.setGroupname(groupname);
    }

    public synchronized Integer getPriority() {
        return super.getPriority();
    }

    public synchronized void setPriority(Integer priority) {
        super.setPriority(priority);
    }

    public Record synchronizedRecord() {
        return this;
    }
}