package net.jradius.dal.model;

import com.coova.dal.Example;
import com.coova.dal.ExampleCriteria;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RadIPPoolExample extends Example {
    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    protected String orderByClause;

    protected Integer startRow;

    protected Integer rowCount;

    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    protected List<Criteria> oredCriteria;

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public RadIPPoolExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    protected RadIPPoolExample(RadIPPoolExample example) {
        this.orderByClause = example.orderByClause;
        this.oredCriteria = example.oredCriteria;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public void setStartRow(Integer startRow) {
        this.startRow = startRow;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    public Integer getStartRow() {
        return startRow;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public Criteria appendCriteria() {
        if (oredCriteria.size() == 0) {
            return createCriteria();
        }
        return oredCriteria.get(0);
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void clear() {
        oredCriteria.clear();
    }

    /**
     * This class was generated by Abator for iBATIS.
     * This class corresponds to the database table radippool
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public static class Criteria extends ExampleCriteria {
        protected List<String> criteriaWithoutValue;

        protected List<Map<String, Object>> criteriaWithSingleValue;

        protected List<Map<String, Object>> criteriaWithListValue;

        protected List<Map<String, Object>> criteriaWithBetweenValue;

        protected Criteria() {
            super();
            criteriaWithoutValue = new ArrayList<String>();
            criteriaWithSingleValue = new ArrayList<Map<String, Object>>();
            criteriaWithListValue = new ArrayList<Map<String, Object>>();
            criteriaWithBetweenValue = new ArrayList<Map<String, Object>>();
        }

        public boolean isValid() {
            return criteriaWithoutValue.size() > 0
                || criteriaWithSingleValue.size() > 0
                || criteriaWithListValue.size() > 0
                || criteriaWithBetweenValue.size() > 0;
        }

        public List<String> getCriteriaWithoutValue() {
            return criteriaWithoutValue;
        }

        public List<Map<String, Object>> getCriteriaWithSingleValue() {
            return criteriaWithSingleValue;
        }

        public List<Map<String, Object>> getCriteriaWithListValue() {
            return criteriaWithListValue;
        }

        public List<Map<String, Object>> getCriteriaWithBetweenValue() {
            return criteriaWithBetweenValue;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteriaWithoutValue.add(condition);
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("condition", condition);
            map.put("value", value);
            criteriaWithSingleValue.add(map);
        }

        protected void addCriterion(String condition, List<? extends Object> values, String property) {
            if (values == null || values.size() == 0) {
                throw new RuntimeException("Value list for " + property + " cannot be null or empty");
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("condition", condition);
            map.put("values", values);
            criteriaWithListValue.add(map);
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            List<Object> list = new ArrayList<Object>();
            list.add(value1);
            list.add(value2);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("condition", condition);
            map.put("values", list);
            criteriaWithBetweenValue.add(map);
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return this;
        }

        public Criteria andPoolNameIsNull() {
            addCriterion("pool_name is null");
            return this;
        }

        public Criteria andPoolNameIsNotNull() {
            addCriterion("pool_name is not null");
            return this;
        }

        public Criteria andPoolNameEqualTo(String value) {
            addCriterion("pool_name =", value, "poolName");
            return this;
        }

        public Criteria andPoolNameNotEqualTo(String value) {
            addCriterion("pool_name <>", value, "poolName");
            return this;
        }

        public Criteria andPoolNameGreaterThan(String value) {
            addCriterion("pool_name >", value, "poolName");
            return this;
        }

        public Criteria andPoolNameGreaterThanOrEqualTo(String value) {
            addCriterion("pool_name >=", value, "poolName");
            return this;
        }

        public Criteria andPoolNameLessThan(String value) {
            addCriterion("pool_name <", value, "poolName");
            return this;
        }

        public Criteria andPoolNameLessThanOrEqualTo(String value) {
            addCriterion("pool_name <=", value, "poolName");
            return this;
        }

        public Criteria andPoolNameLike(String value) {
            addCriterion("pool_name like", value, "poolName");
            return this;
        }

        public Criteria andPoolNameNotLike(String value) {
            addCriterion("pool_name not like", value, "poolName");
            return this;
        }

        public Criteria andPoolNameIn(List<String> values) {
            addCriterion("pool_name in", values, "poolName");
            return this;
        }

        public Criteria andPoolNameNotIn(List<String> values) {
            addCriterion("pool_name not in", values, "poolName");
            return this;
        }

        public Criteria andPoolNameBetween(String value1, String value2) {
            addCriterion("pool_name between", value1, value2, "poolName");
            return this;
        }

        public Criteria andPoolNameNotBetween(String value1, String value2) {
            addCriterion("pool_name not between", value1, value2, "poolName");
            return this;
        }

        public Criteria andFramedipaddressIsNull() {
            addCriterion("framedipaddress is null");
            return this;
        }

        public Criteria andFramedipaddressIsNotNull() {
            addCriterion("framedipaddress is not null");
            return this;
        }

        public Criteria andFramedipaddressEqualTo(String value) {
            addCriterion("framedipaddress =", value, "framedipaddress");
            return this;
        }

        public Criteria andFramedipaddressNotEqualTo(String value) {
            addCriterion("framedipaddress <>", value, "framedipaddress");
            return this;
        }

        public Criteria andFramedipaddressGreaterThan(String value) {
            addCriterion("framedipaddress >", value, "framedipaddress");
            return this;
        }

        public Criteria andFramedipaddressGreaterThanOrEqualTo(String value) {
            addCriterion("framedipaddress >=", value, "framedipaddress");
            return this;
        }

        public Criteria andFramedipaddressLessThan(String value) {
            addCriterion("framedipaddress <", value, "framedipaddress");
            return this;
        }

        public Criteria andFramedipaddressLessThanOrEqualTo(String value) {
            addCriterion("framedipaddress <=", value, "framedipaddress");
            return this;
        }

        public Criteria andFramedipaddressLike(String value) {
            addCriterion("framedipaddress like", value, "framedipaddress");
            return this;
        }

        public Criteria andFramedipaddressNotLike(String value) {
            addCriterion("framedipaddress not like", value, "framedipaddress");
            return this;
        }

        public Criteria andFramedipaddressIn(List<String> values) {
            addCriterion("framedipaddress in", values, "framedipaddress");
            return this;
        }

        public Criteria andFramedipaddressNotIn(List<String> values) {
            addCriterion("framedipaddress not in", values, "framedipaddress");
            return this;
        }

        public Criteria andFramedipaddressBetween(String value1, String value2) {
            addCriterion("framedipaddress between", value1, value2, "framedipaddress");
            return this;
        }

        public Criteria andFramedipaddressNotBetween(String value1, String value2) {
            addCriterion("framedipaddress not between", value1, value2, "framedipaddress");
            return this;
        }

        public Criteria andNasipaddressIsNull() {
            addCriterion("nasipaddress is null");
            return this;
        }

        public Criteria andNasipaddressIsNotNull() {
            addCriterion("nasipaddress is not null");
            return this;
        }

        public Criteria andNasipaddressEqualTo(String value) {
            addCriterion("nasipaddress =", value, "nasipaddress");
            return this;
        }

        public Criteria andNasipaddressNotEqualTo(String value) {
            addCriterion("nasipaddress <>", value, "nasipaddress");
            return this;
        }

        public Criteria andNasipaddressGreaterThan(String value) {
            addCriterion("nasipaddress >", value, "nasipaddress");
            return this;
        }

        public Criteria andNasipaddressGreaterThanOrEqualTo(String value) {
            addCriterion("nasipaddress >=", value, "nasipaddress");
            return this;
        }

        public Criteria andNasipaddressLessThan(String value) {
            addCriterion("nasipaddress <", value, "nasipaddress");
            return this;
        }

        public Criteria andNasipaddressLessThanOrEqualTo(String value) {
            addCriterion("nasipaddress <=", value, "nasipaddress");
            return this;
        }

        public Criteria andNasipaddressLike(String value) {
            addCriterion("nasipaddress like", value, "nasipaddress");
            return this;
        }

        public Criteria andNasipaddressNotLike(String value) {
            addCriterion("nasipaddress not like", value, "nasipaddress");
            return this;
        }

        public Criteria andNasipaddressIn(List<String> values) {
            addCriterion("nasipaddress in", values, "nasipaddress");
            return this;
        }

        public Criteria andNasipaddressNotIn(List<String> values) {
            addCriterion("nasipaddress not in", values, "nasipaddress");
            return this;
        }

        public Criteria andNasipaddressBetween(String value1, String value2) {
            addCriterion("nasipaddress between", value1, value2, "nasipaddress");
            return this;
        }

        public Criteria andNasipaddressNotBetween(String value1, String value2) {
            addCriterion("nasipaddress not between", value1, value2, "nasipaddress");
            return this;
        }

        public Criteria andCalledstationidIsNull() {
            addCriterion("calledstationid is null");
            return this;
        }

        public Criteria andCalledstationidIsNotNull() {
            addCriterion("calledstationid is not null");
            return this;
        }

        public Criteria andCalledstationidEqualTo(String value) {
            addCriterion("calledstationid =", value, "calledstationid");
            return this;
        }

        public Criteria andCalledstationidNotEqualTo(String value) {
            addCriterion("calledstationid <>", value, "calledstationid");
            return this;
        }

        public Criteria andCalledstationidGreaterThan(String value) {
            addCriterion("calledstationid >", value, "calledstationid");
            return this;
        }

        public Criteria andCalledstationidGreaterThanOrEqualTo(String value) {
            addCriterion("calledstationid >=", value, "calledstationid");
            return this;
        }

        public Criteria andCalledstationidLessThan(String value) {
            addCriterion("calledstationid <", value, "calledstationid");
            return this;
        }

        public Criteria andCalledstationidLessThanOrEqualTo(String value) {
            addCriterion("calledstationid <=", value, "calledstationid");
            return this;
        }

        public Criteria andCalledstationidLike(String value) {
            addCriterion("calledstationid like", value, "calledstationid");
            return this;
        }

        public Criteria andCalledstationidNotLike(String value) {
            addCriterion("calledstationid not like", value, "calledstationid");
            return this;
        }

        public Criteria andCalledstationidIn(List<String> values) {
            addCriterion("calledstationid in", values, "calledstationid");
            return this;
        }

        public Criteria andCalledstationidNotIn(List<String> values) {
            addCriterion("calledstationid not in", values, "calledstationid");
            return this;
        }

        public Criteria andCalledstationidBetween(String value1, String value2) {
            addCriterion("calledstationid between", value1, value2, "calledstationid");
            return this;
        }

        public Criteria andCalledstationidNotBetween(String value1, String value2) {
            addCriterion("calledstationid not between", value1, value2, "calledstationid");
            return this;
        }

        public Criteria andCallingstationidIsNull() {
            addCriterion("callingstationid is null");
            return this;
        }

        public Criteria andCallingstationidIsNotNull() {
            addCriterion("callingstationid is not null");
            return this;
        }

        public Criteria andCallingstationidEqualTo(String value) {
            addCriterion("callingstationid =", value, "callingstationid");
            return this;
        }

        public Criteria andCallingstationidNotEqualTo(String value) {
            addCriterion("callingstationid <>", value, "callingstationid");
            return this;
        }

        public Criteria andCallingstationidGreaterThan(String value) {
            addCriterion("callingstationid >", value, "callingstationid");
            return this;
        }

        public Criteria andCallingstationidGreaterThanOrEqualTo(String value) {
            addCriterion("callingstationid >=", value, "callingstationid");
            return this;
        }

        public Criteria andCallingstationidLessThan(String value) {
            addCriterion("callingstationid <", value, "callingstationid");
            return this;
        }

        public Criteria andCallingstationidLessThanOrEqualTo(String value) {
            addCriterion("callingstationid <=", value, "callingstationid");
            return this;
        }

        public Criteria andCallingstationidLike(String value) {
            addCriterion("callingstationid like", value, "callingstationid");
            return this;
        }

        public Criteria andCallingstationidNotLike(String value) {
            addCriterion("callingstationid not like", value, "callingstationid");
            return this;
        }

        public Criteria andCallingstationidIn(List<String> values) {
            addCriterion("callingstationid in", values, "callingstationid");
            return this;
        }

        public Criteria andCallingstationidNotIn(List<String> values) {
            addCriterion("callingstationid not in", values, "callingstationid");
            return this;
        }

        public Criteria andCallingstationidBetween(String value1, String value2) {
            addCriterion("callingstationid between", value1, value2, "callingstationid");
            return this;
        }

        public Criteria andCallingstationidNotBetween(String value1, String value2) {
            addCriterion("callingstationid not between", value1, value2, "callingstationid");
            return this;
        }

        public Criteria andExpiryTimeIsNull() {
            addCriterion("expiry_time is null");
            return this;
        }

        public Criteria andExpiryTimeIsNotNull() {
            addCriterion("expiry_time is not null");
            return this;
        }

        public Criteria andExpiryTimeEqualTo(Date value) {
            addCriterion("expiry_time =", value, "expiryTime");
            return this;
        }

        public Criteria andExpiryTimeNotEqualTo(Date value) {
            addCriterion("expiry_time <>", value, "expiryTime");
            return this;
        }

        public Criteria andExpiryTimeGreaterThan(Date value) {
            addCriterion("expiry_time >", value, "expiryTime");
            return this;
        }

        public Criteria andExpiryTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("expiry_time >=", value, "expiryTime");
            return this;
        }

        public Criteria andExpiryTimeLessThan(Date value) {
            addCriterion("expiry_time <", value, "expiryTime");
            return this;
        }

        public Criteria andExpiryTimeLessThanOrEqualTo(Date value) {
            addCriterion("expiry_time <=", value, "expiryTime");
            return this;
        }

        public Criteria andExpiryTimeIn(List<Date> values) {
            addCriterion("expiry_time in", values, "expiryTime");
            return this;
        }

        public Criteria andExpiryTimeNotIn(List<Date> values) {
            addCriterion("expiry_time not in", values, "expiryTime");
            return this;
        }

        public Criteria andExpiryTimeBetween(Date value1, Date value2) {
            addCriterion("expiry_time between", value1, value2, "expiryTime");
            return this;
        }

        public Criteria andExpiryTimeNotBetween(Date value1, Date value2) {
            addCriterion("expiry_time not between", value1, value2, "expiryTime");
            return this;
        }

        public Criteria andUsernameIsNull() {
            addCriterion("username is null");
            return this;
        }

        public Criteria andUsernameIsNotNull() {
            addCriterion("username is not null");
            return this;
        }

        public Criteria andUsernameEqualTo(String value) {
            addCriterion("username =", value, "username");
            return this;
        }

        public Criteria andUsernameNotEqualTo(String value) {
            addCriterion("username <>", value, "username");
            return this;
        }

        public Criteria andUsernameGreaterThan(String value) {
            addCriterion("username >", value, "username");
            return this;
        }

        public Criteria andUsernameGreaterThanOrEqualTo(String value) {
            addCriterion("username >=", value, "username");
            return this;
        }

        public Criteria andUsernameLessThan(String value) {
            addCriterion("username <", value, "username");
            return this;
        }

        public Criteria andUsernameLessThanOrEqualTo(String value) {
            addCriterion("username <=", value, "username");
            return this;
        }

        public Criteria andUsernameLike(String value) {
            addCriterion("username like", value, "username");
            return this;
        }

        public Criteria andUsernameNotLike(String value) {
            addCriterion("username not like", value, "username");
            return this;
        }

        public Criteria andUsernameIn(List<String> values) {
            addCriterion("username in", values, "username");
            return this;
        }

        public Criteria andUsernameNotIn(List<String> values) {
            addCriterion("username not in", values, "username");
            return this;
        }

        public Criteria andUsernameBetween(String value1, String value2) {
            addCriterion("username between", value1, value2, "username");
            return this;
        }

        public Criteria andUsernameNotBetween(String value1, String value2) {
            addCriterion("username not between", value1, value2, "username");
            return this;
        }

        public Criteria andPoolKeyIsNull() {
            addCriterion("pool_key is null");
            return this;
        }

        public Criteria andPoolKeyIsNotNull() {
            addCriterion("pool_key is not null");
            return this;
        }

        public Criteria andPoolKeyEqualTo(String value) {
            addCriterion("pool_key =", value, "poolKey");
            return this;
        }

        public Criteria andPoolKeyNotEqualTo(String value) {
            addCriterion("pool_key <>", value, "poolKey");
            return this;
        }

        public Criteria andPoolKeyGreaterThan(String value) {
            addCriterion("pool_key >", value, "poolKey");
            return this;
        }

        public Criteria andPoolKeyGreaterThanOrEqualTo(String value) {
            addCriterion("pool_key >=", value, "poolKey");
            return this;
        }

        public Criteria andPoolKeyLessThan(String value) {
            addCriterion("pool_key <", value, "poolKey");
            return this;
        }

        public Criteria andPoolKeyLessThanOrEqualTo(String value) {
            addCriterion("pool_key <=", value, "poolKey");
            return this;
        }

        public Criteria andPoolKeyLike(String value) {
            addCriterion("pool_key like", value, "poolKey");
            return this;
        }

        public Criteria andPoolKeyNotLike(String value) {
            addCriterion("pool_key not like", value, "poolKey");
            return this;
        }

        public Criteria andPoolKeyIn(List<String> values) {
            addCriterion("pool_key in", values, "poolKey");
            return this;
        }

        public Criteria andPoolKeyNotIn(List<String> values) {
            addCriterion("pool_key not in", values, "poolKey");
            return this;
        }

        public Criteria andPoolKeyBetween(String value1, String value2) {
            addCriterion("pool_key between", value1, value2, "poolKey");
            return this;
        }

        public Criteria andPoolKeyNotBetween(String value1, String value2) {
            addCriterion("pool_key not between", value1, value2, "poolKey");
            return this;
        }
    }
}