package net.jradius.dal.model;

import com.coova.dal.Record;

public class NAS extends Record {
    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database column nas.id
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    private Long id;

    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database column nas.nasname
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    private String nasname;

    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database column nas.nastype
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    private String nastype = "other";

    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database column nas.calledstationid
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    private String calledstationid;

    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database column nas.shortname
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    private String shortname;

    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database column nas.ports
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    private Integer ports;

    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database column nas.secret
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    private String secret;

    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database column nas.community
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    private String community;

    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database column nas.description
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    private String description = "RADIUS Client";

    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database column nas.latitude
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    private Double latitude;

    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database column nas.longitude
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    private Double longitude;

    /**
     * This method was generated by Abator for iBATIS.
     * This method returns the value of the database column nas.id
     *
     * @return the value of nas.id
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method sets the value of the database column nas.id
     *
     * @param id the value for nas.id
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method returns the value of the database column nas.nasname
     *
     * @return the value of nas.nasname
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public String getNasname() {
        return nasname;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method sets the value of the database column nas.nasname
     *
     * @param nasname the value for nas.nasname
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void setNasname(String nasname) {
        this.nasname = nasname == null ? null : nasname.trim();
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method returns the value of the database column nas.nastype
     *
     * @return the value of nas.nastype
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public String getNastype() {
        return nastype;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method sets the value of the database column nas.nastype
     *
     * @param nastype the value for nas.nastype
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void setNastype(String nastype) {
        this.nastype = nastype == null ? null : nastype.trim();
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method returns the value of the database column nas.calledstationid
     *
     * @return the value of nas.calledstationid
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public String getCalledstationid() {
        return calledstationid;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method sets the value of the database column nas.calledstationid
     *
     * @param calledstationid the value for nas.calledstationid
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void setCalledstationid(String calledstationid) {
        this.calledstationid = calledstationid == null ? null : calledstationid.trim();
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method returns the value of the database column nas.shortname
     *
     * @return the value of nas.shortname
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public String getShortname() {
        return shortname;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method sets the value of the database column nas.shortname
     *
     * @param shortname the value for nas.shortname
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void setShortname(String shortname) {
        this.shortname = shortname == null ? null : shortname.trim();
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method returns the value of the database column nas.ports
     *
     * @return the value of nas.ports
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public Integer getPorts() {
        return ports;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method sets the value of the database column nas.ports
     *
     * @param ports the value for nas.ports
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void setPorts(Integer ports) {
        this.ports = ports;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method returns the value of the database column nas.secret
     *
     * @return the value of nas.secret
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public String getSecret() {
        return secret;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method sets the value of the database column nas.secret
     *
     * @param secret the value for nas.secret
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void setSecret(String secret) {
        this.secret = secret == null ? null : secret.trim();
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method returns the value of the database column nas.community
     *
     * @return the value of nas.community
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public String getCommunity() {
        return community;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method sets the value of the database column nas.community
     *
     * @param community the value for nas.community
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void setCommunity(String community) {
        this.community = community == null ? null : community.trim();
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method returns the value of the database column nas.description
     *
     * @return the value of nas.description
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method sets the value of the database column nas.description
     *
     * @param description the value for nas.description
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method returns the value of the database column nas.latitude
     *
     * @return the value of nas.latitude
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method sets the value of the database column nas.latitude
     *
     * @param latitude the value for nas.latitude
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method returns the value of the database column nas.longitude
     *
     * @return the value of nas.longitude
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method sets the value of the database column nas.longitude
     *
     * @param longitude the value for nas.longitude
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Record synchronizedRecord() {
        return new SynchronizedNAS(this);
    }
}