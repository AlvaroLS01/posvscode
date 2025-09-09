package com.comerzzia.bricodepot.pos.persistence.cajas.apuntes.movimientos;

import java.util.ArrayList;
import java.util.List;

public class CajasMovimientosExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CajasMovimientosExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andUidActividadIsNull() {
            addCriterion("uid_actividad is null");
            return (Criteria) this;
        }

        public Criteria andUidActividadIsNotNull() {
            addCriterion("uid_actividad is not null");
            return (Criteria) this;
        }

        public Criteria andUidActividadEqualTo(String value) {
            addCriterion("uid_actividad =", value, "uidActividad");
            return (Criteria) this;
        }

        public Criteria andUidActividadNotEqualTo(String value) {
            addCriterion("uid_actividad <>", value, "uidActividad");
            return (Criteria) this;
        }

        public Criteria andUidActividadGreaterThan(String value) {
            addCriterion("uid_actividad >", value, "uidActividad");
            return (Criteria) this;
        }

        public Criteria andUidActividadGreaterThanOrEqualTo(String value) {
            addCriterion("uid_actividad >=", value, "uidActividad");
            return (Criteria) this;
        }

        public Criteria andUidActividadLessThan(String value) {
            addCriterion("uid_actividad <", value, "uidActividad");
            return (Criteria) this;
        }

        public Criteria andUidActividadLessThanOrEqualTo(String value) {
            addCriterion("uid_actividad <=", value, "uidActividad");
            return (Criteria) this;
        }

        public Criteria andUidActividadLike(String value) {
            addCriterion("uid_actividad like", value, "uidActividad");
            return (Criteria) this;
        }

        public Criteria andUidActividadNotLike(String value) {
            addCriterion("uid_actividad not like", value, "uidActividad");
            return (Criteria) this;
        }

        public Criteria andUidActividadIn(List<String> values) {
            addCriterion("uid_actividad in", values, "uidActividad");
            return (Criteria) this;
        }

        public Criteria andUidActividadNotIn(List<String> values) {
            addCriterion("uid_actividad not in", values, "uidActividad");
            return (Criteria) this;
        }

        public Criteria andUidActividadBetween(String value1, String value2) {
            addCriterion("uid_actividad between", value1, value2, "uidActividad");
            return (Criteria) this;
        }

        public Criteria andUidActividadNotBetween(String value1, String value2) {
            addCriterion("uid_actividad not between", value1, value2, "uidActividad");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenIsNull() {
            addCriterion("codconcepto_mov_origen is null");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenIsNotNull() {
            addCriterion("codconcepto_mov_origen is not null");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenEqualTo(String value) {
            addCriterion("codconcepto_mov_origen =", value, "codconceptoMovOrigen");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenNotEqualTo(String value) {
            addCriterion("codconcepto_mov_origen <>", value, "codconceptoMovOrigen");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenGreaterThan(String value) {
            addCriterion("codconcepto_mov_origen >", value, "codconceptoMovOrigen");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenGreaterThanOrEqualTo(String value) {
            addCriterion("codconcepto_mov_origen >=", value, "codconceptoMovOrigen");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenLessThan(String value) {
            addCriterion("codconcepto_mov_origen <", value, "codconceptoMovOrigen");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenLessThanOrEqualTo(String value) {
            addCriterion("codconcepto_mov_origen <=", value, "codconceptoMovOrigen");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenLike(String value) {
            addCriterion("codconcepto_mov_origen like", value, "codconceptoMovOrigen");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenNotLike(String value) {
            addCriterion("codconcepto_mov_origen not like", value, "codconceptoMovOrigen");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenIn(List<String> values) {
            addCriterion("codconcepto_mov_origen in", values, "codconceptoMovOrigen");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenNotIn(List<String> values) {
            addCriterion("codconcepto_mov_origen not in", values, "codconceptoMovOrigen");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenBetween(String value1, String value2) {
            addCriterion("codconcepto_mov_origen between", value1, value2, "codconceptoMovOrigen");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovOrigenNotBetween(String value1, String value2) {
            addCriterion("codconcepto_mov_origen not between", value1, value2, "codconceptoMovOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenIsNull() {
            addCriterion("codcaja_origen is null");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenIsNotNull() {
            addCriterion("codcaja_origen is not null");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenEqualTo(String value) {
            addCriterion("codcaja_origen =", value, "codcajaOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenNotEqualTo(String value) {
            addCriterion("codcaja_origen <>", value, "codcajaOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenGreaterThan(String value) {
            addCriterion("codcaja_origen >", value, "codcajaOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenGreaterThanOrEqualTo(String value) {
            addCriterion("codcaja_origen >=", value, "codcajaOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenLessThan(String value) {
            addCriterion("codcaja_origen <", value, "codcajaOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenLessThanOrEqualTo(String value) {
            addCriterion("codcaja_origen <=", value, "codcajaOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenLike(String value) {
            addCriterion("codcaja_origen like", value, "codcajaOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenNotLike(String value) {
            addCriterion("codcaja_origen not like", value, "codcajaOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenIn(List<String> values) {
            addCriterion("codcaja_origen in", values, "codcajaOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenNotIn(List<String> values) {
            addCriterion("codcaja_origen not in", values, "codcajaOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenBetween(String value1, String value2) {
            addCriterion("codcaja_origen between", value1, value2, "codcajaOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaOrigenNotBetween(String value1, String value2) {
            addCriterion("codcaja_origen not between", value1, value2, "codcajaOrigen");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoIsNull() {
            addCriterion("codcaja_destino is null");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoIsNotNull() {
            addCriterion("codcaja_destino is not null");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoEqualTo(String value) {
            addCriterion("codcaja_destino =", value, "codcajaDestino");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoNotEqualTo(String value) {
            addCriterion("codcaja_destino <>", value, "codcajaDestino");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoGreaterThan(String value) {
            addCriterion("codcaja_destino >", value, "codcajaDestino");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoGreaterThanOrEqualTo(String value) {
            addCriterion("codcaja_destino >=", value, "codcajaDestino");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoLessThan(String value) {
            addCriterion("codcaja_destino <", value, "codcajaDestino");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoLessThanOrEqualTo(String value) {
            addCriterion("codcaja_destino <=", value, "codcajaDestino");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoLike(String value) {
            addCriterion("codcaja_destino like", value, "codcajaDestino");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoNotLike(String value) {
            addCriterion("codcaja_destino not like", value, "codcajaDestino");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoIn(List<String> values) {
            addCriterion("codcaja_destino in", values, "codcajaDestino");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoNotIn(List<String> values) {
            addCriterion("codcaja_destino not in", values, "codcajaDestino");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoBetween(String value1, String value2) {
            addCriterion("codcaja_destino between", value1, value2, "codcajaDestino");
            return (Criteria) this;
        }

        public Criteria andCodcajaDestinoNotBetween(String value1, String value2) {
            addCriterion("codcaja_destino not between", value1, value2, "codcajaDestino");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoIsNull() {
            addCriterion("codconcepto_mov_destino is null");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoIsNotNull() {
            addCriterion("codconcepto_mov_destino is not null");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoEqualTo(String value) {
            addCriterion("codconcepto_mov_destino =", value, "codconceptoMovDestino");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoNotEqualTo(String value) {
            addCriterion("codconcepto_mov_destino <>", value, "codconceptoMovDestino");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoGreaterThan(String value) {
            addCriterion("codconcepto_mov_destino >", value, "codconceptoMovDestino");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoGreaterThanOrEqualTo(String value) {
            addCriterion("codconcepto_mov_destino >=", value, "codconceptoMovDestino");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoLessThan(String value) {
            addCriterion("codconcepto_mov_destino <", value, "codconceptoMovDestino");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoLessThanOrEqualTo(String value) {
            addCriterion("codconcepto_mov_destino <=", value, "codconceptoMovDestino");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoLike(String value) {
            addCriterion("codconcepto_mov_destino like", value, "codconceptoMovDestino");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoNotLike(String value) {
            addCriterion("codconcepto_mov_destino not like", value, "codconceptoMovDestino");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoIn(List<String> values) {
            addCriterion("codconcepto_mov_destino in", values, "codconceptoMovDestino");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoNotIn(List<String> values) {
            addCriterion("codconcepto_mov_destino not in", values, "codconceptoMovDestino");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoBetween(String value1, String value2) {
            addCriterion("codconcepto_mov_destino between", value1, value2, "codconceptoMovDestino");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovDestinoNotBetween(String value1, String value2) {
            addCriterion("codconcepto_mov_destino not between", value1, value2, "codconceptoMovDestino");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoIsNull() {
            addCriterion("signo_destino is null");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoIsNotNull() {
            addCriterion("signo_destino is not null");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoEqualTo(String value) {
            addCriterion("signo_destino =", value, "signoDestino");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoNotEqualTo(String value) {
            addCriterion("signo_destino <>", value, "signoDestino");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoGreaterThan(String value) {
            addCriterion("signo_destino >", value, "signoDestino");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoGreaterThanOrEqualTo(String value) {
            addCriterion("signo_destino >=", value, "signoDestino");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoLessThan(String value) {
            addCriterion("signo_destino <", value, "signoDestino");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoLessThanOrEqualTo(String value) {
            addCriterion("signo_destino <=", value, "signoDestino");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoLike(String value) {
            addCriterion("signo_destino like", value, "signoDestino");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoNotLike(String value) {
            addCriterion("signo_destino not like", value, "signoDestino");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoIn(List<String> values) {
            addCriterion("signo_destino in", values, "signoDestino");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoNotIn(List<String> values) {
            addCriterion("signo_destino not in", values, "signoDestino");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoBetween(String value1, String value2) {
            addCriterion("signo_destino between", value1, value2, "signoDestino");
            return (Criteria) this;
        }

        public Criteria andSignoDestinoNotBetween(String value1, String value2) {
            addCriterion("signo_destino not between", value1, value2, "signoDestino");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}