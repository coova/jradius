package net.jradius.dal.model;

import com.coova.dal.Record;
import com.coova.dal.SynchronizedRecord;

public class SynchronizedUIHelp extends UIHelp implements SynchronizedRecord {

    public SynchronizedUIHelp(UIHelp o) {
        super.setId(o.getId());
        super.setKeyid(o.getKeyid());
        super.setHelptext(o.getHelptext());
    }

    public synchronized Long getId() {
        return super.getId();
    }

    public synchronized void setId(Long id) {
        super.setId(id);
    }

    public synchronized String getKeyid() {
        return super.getKeyid();
    }

    public synchronized void setKeyid(String keyid) {
        super.setKeyid(keyid);
    }

    public synchronized String getHelptext() {
        return super.getHelptext();
    }

    public synchronized void setHelptext(String helptext) {
        super.setHelptext(helptext);
    }

    public Record synchronizedRecord() {
        return this;
    }
}