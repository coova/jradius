package net.jradius.dal.model;

import com.coova.dal.Example;
import com.coova.dal.ExampleCriteria;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NASExample extends Example {
    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database table nas
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    protected String orderByClause;

    protected Integer startRow;

    protected Integer rowCount;

    /**
     * This field was generated by Abator for iBATIS.
     * This field corresponds to the database table nas
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    protected List<Criteria> oredCriteria;

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table nas
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public NASExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table nas
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    protected NASExample(NASExample example) {
        this.orderByClause = example.orderByClause;
        this.oredCriteria = example.oredCriteria;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table nas
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
     * This method corresponds to the database table nas
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
     * This method corresponds to the database table nas
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table nas
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table nas
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
     * This method corresponds to the database table nas
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
     * This method corresponds to the database table nas
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by Abator for iBATIS.
     * This method corresponds to the database table nas
     *
     * @abatorgenerated Wed Sep 10 13:54:28 CEST 2008
     */
    public void clear() {
        oredCriteria.clear();
    }

    /**
     * This class was generated by Abator for iBATIS.
     * This class corresponds to the database table nas
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

        public Criteria andNasnameIsNull() {
            addCriterion("nasname is null");
            return this;
        }

        public Criteria andNasnameIsNotNull() {
            addCriterion("nasname is not null");
            return this;
        }

        public Criteria andNasnameEqualTo(String value) {
            addCriterion("nasname =", value, "nasname");
            return this;
        }

        public Criteria andNasnameNotEqualTo(String value) {
            addCriterion("nasname <>", value, "nasname");
            return this;
        }

        public Criteria andNasnameGreaterThan(String value) {
            addCriterion("nasname >", value, "nasname");
            return this;
        }

        public Criteria andNasnameGreaterThanOrEqualTo(String value) {
            addCriterion("nasname >=", value, "nasname");
            return this;
        }

        public Criteria andNasnameLessThan(String value) {
            addCriterion("nasname <", value, "nasname");
            return this;
        }

        public Criteria andNasnameLessThanOrEqualTo(String value) {
            addCriterion("nasname <=", value, "nasname");
            return this;
        }

        public Criteria andNasnameLike(String value) {
            addCriterion("nasname like", value, "nasname");
            return this;
        }

        public Criteria andNasnameNotLike(String value) {
            addCriterion("nasname not like", value, "nasname");
            return this;
        }

        public Criteria andNasnameIn(List<String> values) {
            addCriterion("nasname in", values, "nasname");
            return this;
        }

        public Criteria andNasnameNotIn(List<String> values) {
            addCriterion("nasname not in", values, "nasname");
            return this;
        }

        public Criteria andNasnameBetween(String value1, String value2) {
            addCriterion("nasname between", value1, value2, "nasname");
            return this;
        }

        public Criteria andNasnameNotBetween(String value1, String value2) {
            addCriterion("nasname not between", value1, value2, "nasname");
            return this;
        }

        public Criteria andNastypeIsNull() {
            addCriterion("nastype is null");
            return this;
        }

        public Criteria andNastypeIsNotNull() {
            addCriterion("nastype is not null");
            return this;
        }

        public Criteria andNastypeEqualTo(String value) {
            addCriterion("nastype =", value, "nastype");
            return this;
        }

        public Criteria andNastypeNotEqualTo(String value) {
            addCriterion("nastype <>", value, "nastype");
            return this;
        }

        public Criteria andNastypeGreaterThan(String value) {
            addCriterion("nastype >", value, "nastype");
            return this;
        }

        public Criteria andNastypeGreaterThanOrEqualTo(String value) {
            addCriterion("nastype >=", value, "nastype");
            return this;
        }

        public Criteria andNastypeLessThan(String value) {
            addCriterion("nastype <", value, "nastype");
            return this;
        }

        public Criteria andNastypeLessThanOrEqualTo(String value) {
            addCriterion("nastype <=", value, "nastype");
            return this;
        }

        public Criteria andNastypeLike(String value) {
            addCriterion("nastype like", value, "nastype");
            return this;
        }

        public Criteria andNastypeNotLike(String value) {
            addCriterion("nastype not like", value, "nastype");
            return this;
        }

        public Criteria andNastypeIn(List<String> values) {
            addCriterion("nastype in", values, "nastype");
            return this;
        }

        public Criteria andNastypeNotIn(List<String> values) {
            addCriterion("nastype not in", values, "nastype");
            return this;
        }

        public Criteria andNastypeBetween(String value1, String value2) {
            addCriterion("nastype between", value1, value2, "nastype");
            return this;
        }

        public Criteria andNastypeNotBetween(String value1, String value2) {
            addCriterion("nastype not between", value1, value2, "nastype");
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

        public Criteria andShortnameIsNull() {
            addCriterion("shortname is null");
            return this;
        }

        public Criteria andShortnameIsNotNull() {
            addCriterion("shortname is not null");
            return this;
        }

        public Criteria andShortnameEqualTo(String value) {
            addCriterion("shortname =", value, "shortname");
            return this;
        }

        public Criteria andShortnameNotEqualTo(String value) {
            addCriterion("shortname <>", value, "shortname");
            return this;
        }

        public Criteria andShortnameGreaterThan(String value) {
            addCriterion("shortname >", value, "shortname");
            return this;
        }

        public Criteria andShortnameGreaterThanOrEqualTo(String value) {
            addCriterion("shortname >=", value, "shortname");
            return this;
        }

        public Criteria andShortnameLessThan(String value) {
            addCriterion("shortname <", value, "shortname");
            return this;
        }

        public Criteria andShortnameLessThanOrEqualTo(String value) {
            addCriterion("shortname <=", value, "shortname");
            return this;
        }

        public Criteria andShortnameLike(String value) {
            addCriterion("shortname like", value, "shortname");
            return this;
        }

        public Criteria andShortnameNotLike(String value) {
            addCriterion("shortname not like", value, "shortname");
            return this;
        }

        public Criteria andShortnameIn(List<String> values) {
            addCriterion("shortname in", values, "shortname");
            return this;
        }

        public Criteria andShortnameNotIn(List<String> values) {
            addCriterion("shortname not in", values, "shortname");
            return this;
        }

        public Criteria andShortnameBetween(String value1, String value2) {
            addCriterion("shortname between", value1, value2, "shortname");
            return this;
        }

        public Criteria andShortnameNotBetween(String value1, String value2) {
            addCriterion("shortname not between", value1, value2, "shortname");
            return this;
        }

        public Criteria andPortsIsNull() {
            addCriterion("ports is null");
            return this;
        }

        public Criteria andPortsIsNotNull() {
            addCriterion("ports is not null");
            return this;
        }

        public Criteria andPortsEqualTo(Integer value) {
            addCriterion("ports =", value, "ports");
            return this;
        }

        public Criteria andPortsNotEqualTo(Integer value) {
            addCriterion("ports <>", value, "ports");
            return this;
        }

        public Criteria andPortsGreaterThan(Integer value) {
            addCriterion("ports >", value, "ports");
            return this;
        }

        public Criteria andPortsGreaterThanOrEqualTo(Integer value) {
            addCriterion("ports >=", value, "ports");
            return this;
        }

        public Criteria andPortsLessThan(Integer value) {
            addCriterion("ports <", value, "ports");
            return this;
        }

        public Criteria andPortsLessThanOrEqualTo(Integer value) {
            addCriterion("ports <=", value, "ports");
            return this;
        }

        public Criteria andPortsIn(List<Integer> values) {
            addCriterion("ports in", values, "ports");
            return this;
        }

        public Criteria andPortsNotIn(List<Integer> values) {
            addCriterion("ports not in", values, "ports");
            return this;
        }

        public Criteria andPortsBetween(Integer value1, Integer value2) {
            addCriterion("ports between", value1, value2, "ports");
            return this;
        }

        public Criteria andPortsNotBetween(Integer value1, Integer value2) {
            addCriterion("ports not between", value1, value2, "ports");
            return this;
        }

        public Criteria andSecretIsNull() {
            addCriterion("secret is null");
            return this;
        }

        public Criteria andSecretIsNotNull() {
            addCriterion("secret is not null");
            return this;
        }

        public Criteria andSecretEqualTo(String value) {
            addCriterion("secret =", value, "secret");
            return this;
        }

        public Criteria andSecretNotEqualTo(String value) {
            addCriterion("secret <>", value, "secret");
            return this;
        }

        public Criteria andSecretGreaterThan(String value) {
            addCriterion("secret >", value, "secret");
            return this;
        }

        public Criteria andSecretGreaterThanOrEqualTo(String value) {
            addCriterion("secret >=", value, "secret");
            return this;
        }

        public Criteria andSecretLessThan(String value) {
            addCriterion("secret <", value, "secret");
            return this;
        }

        public Criteria andSecretLessThanOrEqualTo(String value) {
            addCriterion("secret <=", value, "secret");
            return this;
        }

        public Criteria andSecretLike(String value) {
            addCriterion("secret like", value, "secret");
            return this;
        }

        public Criteria andSecretNotLike(String value) {
            addCriterion("secret not like", value, "secret");
            return this;
        }

        public Criteria andSecretIn(List<String> values) {
            addCriterion("secret in", values, "secret");
            return this;
        }

        public Criteria andSecretNotIn(List<String> values) {
            addCriterion("secret not in", values, "secret");
            return this;
        }

        public Criteria andSecretBetween(String value1, String value2) {
            addCriterion("secret between", value1, value2, "secret");
            return this;
        }

        public Criteria andSecretNotBetween(String value1, String value2) {
            addCriterion("secret not between", value1, value2, "secret");
            return this;
        }

        public Criteria andCommunityIsNull() {
            addCriterion("community is null");
            return this;
        }

        public Criteria andCommunityIsNotNull() {
            addCriterion("community is not null");
            return this;
        }

        public Criteria andCommunityEqualTo(String value) {
            addCriterion("community =", value, "community");
            return this;
        }

        public Criteria andCommunityNotEqualTo(String value) {
            addCriterion("community <>", value, "community");
            return this;
        }

        public Criteria andCommunityGreaterThan(String value) {
            addCriterion("community >", value, "community");
            return this;
        }

        public Criteria andCommunityGreaterThanOrEqualTo(String value) {
            addCriterion("community >=", value, "community");
            return this;
        }

        public Criteria andCommunityLessThan(String value) {
            addCriterion("community <", value, "community");
            return this;
        }

        public Criteria andCommunityLessThanOrEqualTo(String value) {
            addCriterion("community <=", value, "community");
            return this;
        }

        public Criteria andCommunityLike(String value) {
            addCriterion("community like", value, "community");
            return this;
        }

        public Criteria andCommunityNotLike(String value) {
            addCriterion("community not like", value, "community");
            return this;
        }

        public Criteria andCommunityIn(List<String> values) {
            addCriterion("community in", values, "community");
            return this;
        }

        public Criteria andCommunityNotIn(List<String> values) {
            addCriterion("community not in", values, "community");
            return this;
        }

        public Criteria andCommunityBetween(String value1, String value2) {
            addCriterion("community between", value1, value2, "community");
            return this;
        }

        public Criteria andCommunityNotBetween(String value1, String value2) {
            addCriterion("community not between", value1, value2, "community");
            return this;
        }

        public Criteria andDescriptionIsNull() {
            addCriterion("description is null");
            return this;
        }

        public Criteria andDescriptionIsNotNull() {
            addCriterion("description is not null");
            return this;
        }

        public Criteria andDescriptionEqualTo(String value) {
            addCriterion("description =", value, "description");
            return this;
        }

        public Criteria andDescriptionNotEqualTo(String value) {
            addCriterion("description <>", value, "description");
            return this;
        }

        public Criteria andDescriptionGreaterThan(String value) {
            addCriterion("description >", value, "description");
            return this;
        }

        public Criteria andDescriptionGreaterThanOrEqualTo(String value) {
            addCriterion("description >=", value, "description");
            return this;
        }

        public Criteria andDescriptionLessThan(String value) {
            addCriterion("description <", value, "description");
            return this;
        }

        public Criteria andDescriptionLessThanOrEqualTo(String value) {
            addCriterion("description <=", value, "description");
            return this;
        }

        public Criteria andDescriptionLike(String value) {
            addCriterion("description like", value, "description");
            return this;
        }

        public Criteria andDescriptionNotLike(String value) {
            addCriterion("description not like", value, "description");
            return this;
        }

        public Criteria andDescriptionIn(List<String> values) {
            addCriterion("description in", values, "description");
            return this;
        }

        public Criteria andDescriptionNotIn(List<String> values) {
            addCriterion("description not in", values, "description");
            return this;
        }

        public Criteria andDescriptionBetween(String value1, String value2) {
            addCriterion("description between", value1, value2, "description");
            return this;
        }

        public Criteria andDescriptionNotBetween(String value1, String value2) {
            addCriterion("description not between", value1, value2, "description");
            return this;
        }

        public Criteria andLatitudeIsNull() {
            addCriterion("latitude is null");
            return this;
        }

        public Criteria andLatitudeIsNotNull() {
            addCriterion("latitude is not null");
            return this;
        }

        public Criteria andLatitudeEqualTo(Double value) {
            addCriterion("latitude =", value, "latitude");
            return this;
        }

        public Criteria andLatitudeNotEqualTo(Double value) {
            addCriterion("latitude <>", value, "latitude");
            return this;
        }

        public Criteria andLatitudeGreaterThan(Double value) {
            addCriterion("latitude >", value, "latitude");
            return this;
        }

        public Criteria andLatitudeGreaterThanOrEqualTo(Double value) {
            addCriterion("latitude >=", value, "latitude");
            return this;
        }

        public Criteria andLatitudeLessThan(Double value) {
            addCriterion("latitude <", value, "latitude");
            return this;
        }

        public Criteria andLatitudeLessThanOrEqualTo(Double value) {
            addCriterion("latitude <=", value, "latitude");
            return this;
        }

        public Criteria andLatitudeIn(List<Double> values) {
            addCriterion("latitude in", values, "latitude");
            return this;
        }

        public Criteria andLatitudeNotIn(List<Double> values) {
            addCriterion("latitude not in", values, "latitude");
            return this;
        }

        public Criteria andLatitudeBetween(Double value1, Double value2) {
            addCriterion("latitude between", value1, value2, "latitude");
            return this;
        }

        public Criteria andLatitudeNotBetween(Double value1, Double value2) {
            addCriterion("latitude not between", value1, value2, "latitude");
            return this;
        }

        public Criteria andLongitudeIsNull() {
            addCriterion("longitude is null");
            return this;
        }

        public Criteria andLongitudeIsNotNull() {
            addCriterion("longitude is not null");
            return this;
        }

        public Criteria andLongitudeEqualTo(Double value) {
            addCriterion("longitude =", value, "longitude");
            return this;
        }

        public Criteria andLongitudeNotEqualTo(Double value) {
            addCriterion("longitude <>", value, "longitude");
            return this;
        }

        public Criteria andLongitudeGreaterThan(Double value) {
            addCriterion("longitude >", value, "longitude");
            return this;
        }

        public Criteria andLongitudeGreaterThanOrEqualTo(Double value) {
            addCriterion("longitude >=", value, "longitude");
            return this;
        }

        public Criteria andLongitudeLessThan(Double value) {
            addCriterion("longitude <", value, "longitude");
            return this;
        }

        public Criteria andLongitudeLessThanOrEqualTo(Double value) {
            addCriterion("longitude <=", value, "longitude");
            return this;
        }

        public Criteria andLongitudeIn(List<Double> values) {
            addCriterion("longitude in", values, "longitude");
            return this;
        }

        public Criteria andLongitudeNotIn(List<Double> values) {
            addCriterion("longitude not in", values, "longitude");
            return this;
        }

        public Criteria andLongitudeBetween(Double value1, Double value2) {
            addCriterion("longitude between", value1, value2, "longitude");
            return this;
        }

        public Criteria andLongitudeNotBetween(Double value1, Double value2) {
            addCriterion("longitude not between", value1, value2, "longitude");
            return this;
        }
    }
}