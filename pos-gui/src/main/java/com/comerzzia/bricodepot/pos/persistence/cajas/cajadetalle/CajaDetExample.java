package com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CajaDetExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CajaDetExample() {
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

        public Criteria andUidDiarioCajaIsNull() {
            addCriterion("uid_diario_caja is null");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaIsNotNull() {
            addCriterion("uid_diario_caja is not null");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaEqualTo(String value) {
            addCriterion("uid_diario_caja =", value, "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaNotEqualTo(String value) {
            addCriterion("uid_diario_caja <>", value, "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaGreaterThan(String value) {
            addCriterion("uid_diario_caja >", value, "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaGreaterThanOrEqualTo(String value) {
            addCriterion("uid_diario_caja >=", value, "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaLessThan(String value) {
            addCriterion("uid_diario_caja <", value, "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaLessThanOrEqualTo(String value) {
            addCriterion("uid_diario_caja <=", value, "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaLike(String value) {
            addCriterion("uid_diario_caja like", value, "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaNotLike(String value) {
            addCriterion("uid_diario_caja not like", value, "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaIn(List<String> values) {
            addCriterion("uid_diario_caja in", values, "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaNotIn(List<String> values) {
            addCriterion("uid_diario_caja not in", values, "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaBetween(String value1, String value2) {
            addCriterion("uid_diario_caja between", value1, value2, "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaNotBetween(String value1, String value2) {
            addCriterion("uid_diario_caja not between", value1, value2, "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andLineaIsNull() {
            addCriterion("linea is null");
            return (Criteria) this;
        }

        public Criteria andLineaIsNotNull() {
            addCriterion("linea is not null");
            return (Criteria) this;
        }

        public Criteria andLineaEqualTo(Integer value) {
            addCriterion("linea =", value, "linea");
            return (Criteria) this;
        }

        public Criteria andLineaNotEqualTo(Integer value) {
            addCriterion("linea <>", value, "linea");
            return (Criteria) this;
        }

        public Criteria andLineaGreaterThan(Integer value) {
            addCriterion("linea >", value, "linea");
            return (Criteria) this;
        }

        public Criteria andLineaGreaterThanOrEqualTo(Integer value) {
            addCriterion("linea >=", value, "linea");
            return (Criteria) this;
        }

        public Criteria andLineaLessThan(Integer value) {
            addCriterion("linea <", value, "linea");
            return (Criteria) this;
        }

        public Criteria andLineaLessThanOrEqualTo(Integer value) {
            addCriterion("linea <=", value, "linea");
            return (Criteria) this;
        }

        public Criteria andLineaIn(List<Integer> values) {
            addCriterion("linea in", values, "linea");
            return (Criteria) this;
        }

        public Criteria andLineaNotIn(List<Integer> values) {
            addCriterion("linea not in", values, "linea");
            return (Criteria) this;
        }

        public Criteria andLineaBetween(Integer value1, Integer value2) {
            addCriterion("linea between", value1, value2, "linea");
            return (Criteria) this;
        }

        public Criteria andLineaNotBetween(Integer value1, Integer value2) {
            addCriterion("linea not between", value1, value2, "linea");
            return (Criteria) this;
        }

        public Criteria andFechaIsNull() {
            addCriterion("fecha is null");
            return (Criteria) this;
        }

        public Criteria andFechaIsNotNull() {
            addCriterion("fecha is not null");
            return (Criteria) this;
        }

        public Criteria andFechaEqualTo(Date value) {
            addCriterion("fecha =", value, "fecha");
            return (Criteria) this;
        }

        public Criteria andFechaNotEqualTo(Date value) {
            addCriterion("fecha <>", value, "fecha");
            return (Criteria) this;
        }

        public Criteria andFechaGreaterThan(Date value) {
            addCriterion("fecha >", value, "fecha");
            return (Criteria) this;
        }

        public Criteria andFechaGreaterThanOrEqualTo(Date value) {
            addCriterion("fecha >=", value, "fecha");
            return (Criteria) this;
        }

        public Criteria andFechaLessThan(Date value) {
            addCriterion("fecha <", value, "fecha");
            return (Criteria) this;
        }

        public Criteria andFechaLessThanOrEqualTo(Date value) {
            addCriterion("fecha <=", value, "fecha");
            return (Criteria) this;
        }

        public Criteria andFechaIn(List<Date> values) {
            addCriterion("fecha in", values, "fecha");
            return (Criteria) this;
        }

        public Criteria andFechaNotIn(List<Date> values) {
            addCriterion("fecha not in", values, "fecha");
            return (Criteria) this;
        }

        public Criteria andFechaBetween(Date value1, Date value2) {
            addCriterion("fecha between", value1, value2, "fecha");
            return (Criteria) this;
        }

        public Criteria andFechaNotBetween(Date value1, Date value2) {
            addCriterion("fecha not between", value1, value2, "fecha");
            return (Criteria) this;
        }

        public Criteria andCargoIsNull() {
            addCriterion("cargo is null");
            return (Criteria) this;
        }

        public Criteria andCargoIsNotNull() {
            addCriterion("cargo is not null");
            return (Criteria) this;
        }

        public Criteria andCargoEqualTo(BigDecimal value) {
            addCriterion("cargo =", value, "cargo");
            return (Criteria) this;
        }

        public Criteria andCargoNotEqualTo(BigDecimal value) {
            addCriterion("cargo <>", value, "cargo");
            return (Criteria) this;
        }

        public Criteria andCargoGreaterThan(BigDecimal value) {
            addCriterion("cargo >", value, "cargo");
            return (Criteria) this;
        }

        public Criteria andCargoGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("cargo >=", value, "cargo");
            return (Criteria) this;
        }

        public Criteria andCargoLessThan(BigDecimal value) {
            addCriterion("cargo <", value, "cargo");
            return (Criteria) this;
        }

        public Criteria andCargoLessThanOrEqualTo(BigDecimal value) {
            addCriterion("cargo <=", value, "cargo");
            return (Criteria) this;
        }

        public Criteria andCargoIn(List<BigDecimal> values) {
            addCriterion("cargo in", values, "cargo");
            return (Criteria) this;
        }

        public Criteria andCargoNotIn(List<BigDecimal> values) {
            addCriterion("cargo not in", values, "cargo");
            return (Criteria) this;
        }

        public Criteria andCargoBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("cargo between", value1, value2, "cargo");
            return (Criteria) this;
        }

        public Criteria andCargoNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("cargo not between", value1, value2, "cargo");
            return (Criteria) this;
        }

        public Criteria andAbonoIsNull() {
            addCriterion("abono is null");
            return (Criteria) this;
        }

        public Criteria andAbonoIsNotNull() {
            addCriterion("abono is not null");
            return (Criteria) this;
        }

        public Criteria andAbonoEqualTo(BigDecimal value) {
            addCriterion("abono =", value, "abono");
            return (Criteria) this;
        }

        public Criteria andAbonoNotEqualTo(BigDecimal value) {
            addCriterion("abono <>", value, "abono");
            return (Criteria) this;
        }

        public Criteria andAbonoGreaterThan(BigDecimal value) {
            addCriterion("abono >", value, "abono");
            return (Criteria) this;
        }

        public Criteria andAbonoGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("abono >=", value, "abono");
            return (Criteria) this;
        }

        public Criteria andAbonoLessThan(BigDecimal value) {
            addCriterion("abono <", value, "abono");
            return (Criteria) this;
        }

        public Criteria andAbonoLessThanOrEqualTo(BigDecimal value) {
            addCriterion("abono <=", value, "abono");
            return (Criteria) this;
        }

        public Criteria andAbonoIn(List<BigDecimal> values) {
            addCriterion("abono in", values, "abono");
            return (Criteria) this;
        }

        public Criteria andAbonoNotIn(List<BigDecimal> values) {
            addCriterion("abono not in", values, "abono");
            return (Criteria) this;
        }

        public Criteria andAbonoBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("abono between", value1, value2, "abono");
            return (Criteria) this;
        }

        public Criteria andAbonoNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("abono not between", value1, value2, "abono");
            return (Criteria) this;
        }

        public Criteria andConceptoIsNull() {
            addCriterion("concepto is null");
            return (Criteria) this;
        }

        public Criteria andConceptoIsNotNull() {
            addCriterion("concepto is not null");
            return (Criteria) this;
        }

        public Criteria andConceptoEqualTo(String value) {
            addCriterion("concepto =", value, "concepto");
            return (Criteria) this;
        }

        public Criteria andConceptoNotEqualTo(String value) {
            addCriterion("concepto <>", value, "concepto");
            return (Criteria) this;
        }

        public Criteria andConceptoGreaterThan(String value) {
            addCriterion("concepto >", value, "concepto");
            return (Criteria) this;
        }

        public Criteria andConceptoGreaterThanOrEqualTo(String value) {
            addCriterion("concepto >=", value, "concepto");
            return (Criteria) this;
        }

        public Criteria andConceptoLessThan(String value) {
            addCriterion("concepto <", value, "concepto");
            return (Criteria) this;
        }

        public Criteria andConceptoLessThanOrEqualTo(String value) {
            addCriterion("concepto <=", value, "concepto");
            return (Criteria) this;
        }

        public Criteria andConceptoLike(String value) {
            addCriterion("concepto like", value, "concepto");
            return (Criteria) this;
        }

        public Criteria andConceptoNotLike(String value) {
            addCriterion("concepto not like", value, "concepto");
            return (Criteria) this;
        }

        public Criteria andConceptoIn(List<String> values) {
            addCriterion("concepto in", values, "concepto");
            return (Criteria) this;
        }

        public Criteria andConceptoNotIn(List<String> values) {
            addCriterion("concepto not in", values, "concepto");
            return (Criteria) this;
        }

        public Criteria andConceptoBetween(String value1, String value2) {
            addCriterion("concepto between", value1, value2, "concepto");
            return (Criteria) this;
        }

        public Criteria andConceptoNotBetween(String value1, String value2) {
            addCriterion("concepto not between", value1, value2, "concepto");
            return (Criteria) this;
        }

        public Criteria andDocumentoIsNull() {
            addCriterion("documento is null");
            return (Criteria) this;
        }

        public Criteria andDocumentoIsNotNull() {
            addCriterion("documento is not null");
            return (Criteria) this;
        }

        public Criteria andDocumentoEqualTo(String value) {
            addCriterion("documento =", value, "documento");
            return (Criteria) this;
        }

        public Criteria andDocumentoNotEqualTo(String value) {
            addCriterion("documento <>", value, "documento");
            return (Criteria) this;
        }

        public Criteria andDocumentoGreaterThan(String value) {
            addCriterion("documento >", value, "documento");
            return (Criteria) this;
        }

        public Criteria andDocumentoGreaterThanOrEqualTo(String value) {
            addCriterion("documento >=", value, "documento");
            return (Criteria) this;
        }

        public Criteria andDocumentoLessThan(String value) {
            addCriterion("documento <", value, "documento");
            return (Criteria) this;
        }

        public Criteria andDocumentoLessThanOrEqualTo(String value) {
            addCriterion("documento <=", value, "documento");
            return (Criteria) this;
        }

        public Criteria andDocumentoLike(String value) {
            addCriterion("documento like", value, "documento");
            return (Criteria) this;
        }

        public Criteria andDocumentoNotLike(String value) {
            addCriterion("documento not like", value, "documento");
            return (Criteria) this;
        }

        public Criteria andDocumentoIn(List<String> values) {
            addCriterion("documento in", values, "documento");
            return (Criteria) this;
        }

        public Criteria andDocumentoNotIn(List<String> values) {
            addCriterion("documento not in", values, "documento");
            return (Criteria) this;
        }

        public Criteria andDocumentoBetween(String value1, String value2) {
            addCriterion("documento between", value1, value2, "documento");
            return (Criteria) this;
        }

        public Criteria andDocumentoNotBetween(String value1, String value2) {
            addCriterion("documento not between", value1, value2, "documento");
            return (Criteria) this;
        }

        public Criteria andCodmedpagIsNull() {
            addCriterion("codmedpag is null");
            return (Criteria) this;
        }

        public Criteria andCodmedpagIsNotNull() {
            addCriterion("codmedpag is not null");
            return (Criteria) this;
        }

        public Criteria andCodmedpagEqualTo(String value) {
            addCriterion("codmedpag =", value, "codmedpag");
            return (Criteria) this;
        }

        public Criteria andCodmedpagNotEqualTo(String value) {
            addCriterion("codmedpag <>", value, "codmedpag");
            return (Criteria) this;
        }

        public Criteria andCodmedpagGreaterThan(String value) {
            addCriterion("codmedpag >", value, "codmedpag");
            return (Criteria) this;
        }

        public Criteria andCodmedpagGreaterThanOrEqualTo(String value) {
            addCriterion("codmedpag >=", value, "codmedpag");
            return (Criteria) this;
        }

        public Criteria andCodmedpagLessThan(String value) {
            addCriterion("codmedpag <", value, "codmedpag");
            return (Criteria) this;
        }

        public Criteria andCodmedpagLessThanOrEqualTo(String value) {
            addCriterion("codmedpag <=", value, "codmedpag");
            return (Criteria) this;
        }

        public Criteria andCodmedpagLike(String value) {
            addCriterion("codmedpag like", value, "codmedpag");
            return (Criteria) this;
        }

        public Criteria andCodmedpagNotLike(String value) {
            addCriterion("codmedpag not like", value, "codmedpag");
            return (Criteria) this;
        }

        public Criteria andCodmedpagIn(List<String> values) {
            addCriterion("codmedpag in", values, "codmedpag");
            return (Criteria) this;
        }

        public Criteria andCodmedpagNotIn(List<String> values) {
            addCriterion("codmedpag not in", values, "codmedpag");
            return (Criteria) this;
        }

        public Criteria andCodmedpagBetween(String value1, String value2) {
            addCriterion("codmedpag between", value1, value2, "codmedpag");
            return (Criteria) this;
        }

        public Criteria andCodmedpagNotBetween(String value1, String value2) {
            addCriterion("codmedpag not between", value1, value2, "codmedpag");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoIsNull() {
            addCriterion("id_documento is null");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoIsNotNull() {
            addCriterion("id_documento is not null");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoEqualTo(String value) {
            addCriterion("id_documento =", value, "idDocumento");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoNotEqualTo(String value) {
            addCriterion("id_documento <>", value, "idDocumento");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoGreaterThan(String value) {
            addCriterion("id_documento >", value, "idDocumento");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoGreaterThanOrEqualTo(String value) {
            addCriterion("id_documento >=", value, "idDocumento");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoLessThan(String value) {
            addCriterion("id_documento <", value, "idDocumento");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoLessThanOrEqualTo(String value) {
            addCriterion("id_documento <=", value, "idDocumento");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoLike(String value) {
            addCriterion("id_documento like", value, "idDocumento");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoNotLike(String value) {
            addCriterion("id_documento not like", value, "idDocumento");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoIn(List<String> values) {
            addCriterion("id_documento in", values, "idDocumento");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoNotIn(List<String> values) {
            addCriterion("id_documento not in", values, "idDocumento");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoBetween(String value1, String value2) {
            addCriterion("id_documento between", value1, value2, "idDocumento");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoNotBetween(String value1, String value2) {
            addCriterion("id_documento not between", value1, value2, "idDocumento");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovIsNull() {
            addCriterion("codconcepto_mov is null");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovIsNotNull() {
            addCriterion("codconcepto_mov is not null");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovEqualTo(String value) {
            addCriterion("codconcepto_mov =", value, "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovNotEqualTo(String value) {
            addCriterion("codconcepto_mov <>", value, "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovGreaterThan(String value) {
            addCriterion("codconcepto_mov >", value, "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovGreaterThanOrEqualTo(String value) {
            addCriterion("codconcepto_mov >=", value, "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovLessThan(String value) {
            addCriterion("codconcepto_mov <", value, "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovLessThanOrEqualTo(String value) {
            addCriterion("codconcepto_mov <=", value, "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovLike(String value) {
            addCriterion("codconcepto_mov like", value, "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovNotLike(String value) {
            addCriterion("codconcepto_mov not like", value, "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovIn(List<String> values) {
            addCriterion("codconcepto_mov in", values, "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovNotIn(List<String> values) {
            addCriterion("codconcepto_mov not in", values, "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovBetween(String value1, String value2) {
            addCriterion("codconcepto_mov between", value1, value2, "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovNotBetween(String value1, String value2) {
            addCriterion("codconcepto_mov not between", value1, value2, "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andIdTipoDocumentoIsNull() {
            addCriterion("id_tipo_documento is null");
            return (Criteria) this;
        }

        public Criteria andIdTipoDocumentoIsNotNull() {
            addCriterion("id_tipo_documento is not null");
            return (Criteria) this;
        }

        public Criteria andIdTipoDocumentoEqualTo(Long value) {
            addCriterion("id_tipo_documento =", value, "idTipoDocumento");
            return (Criteria) this;
        }

        public Criteria andIdTipoDocumentoNotEqualTo(Long value) {
            addCriterion("id_tipo_documento <>", value, "idTipoDocumento");
            return (Criteria) this;
        }

        public Criteria andIdTipoDocumentoGreaterThan(Long value) {
            addCriterion("id_tipo_documento >", value, "idTipoDocumento");
            return (Criteria) this;
        }

        public Criteria andIdTipoDocumentoGreaterThanOrEqualTo(Long value) {
            addCriterion("id_tipo_documento >=", value, "idTipoDocumento");
            return (Criteria) this;
        }

        public Criteria andIdTipoDocumentoLessThan(Long value) {
            addCriterion("id_tipo_documento <", value, "idTipoDocumento");
            return (Criteria) this;
        }

        public Criteria andIdTipoDocumentoLessThanOrEqualTo(Long value) {
            addCriterion("id_tipo_documento <=", value, "idTipoDocumento");
            return (Criteria) this;
        }

        public Criteria andIdTipoDocumentoIn(List<Long> values) {
            addCriterion("id_tipo_documento in", values, "idTipoDocumento");
            return (Criteria) this;
        }

        public Criteria andIdTipoDocumentoNotIn(List<Long> values) {
            addCriterion("id_tipo_documento not in", values, "idTipoDocumento");
            return (Criteria) this;
        }

        public Criteria andIdTipoDocumentoBetween(Long value1, Long value2) {
            addCriterion("id_tipo_documento between", value1, value2, "idTipoDocumento");
            return (Criteria) this;
        }

        public Criteria andIdTipoDocumentoNotBetween(Long value1, Long value2) {
            addCriterion("id_tipo_documento not between", value1, value2, "idTipoDocumento");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetIsNull() {
            addCriterion("uid_transaccion_det is null");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetIsNotNull() {
            addCriterion("uid_transaccion_det is not null");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetEqualTo(String value) {
            addCriterion("uid_transaccion_det =", value, "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetNotEqualTo(String value) {
            addCriterion("uid_transaccion_det <>", value, "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetGreaterThan(String value) {
            addCriterion("uid_transaccion_det >", value, "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetGreaterThanOrEqualTo(String value) {
            addCriterion("uid_transaccion_det >=", value, "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetLessThan(String value) {
            addCriterion("uid_transaccion_det <", value, "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetLessThanOrEqualTo(String value) {
            addCriterion("uid_transaccion_det <=", value, "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetLike(String value) {
            addCriterion("uid_transaccion_det like", value, "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetNotLike(String value) {
            addCriterion("uid_transaccion_det not like", value, "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetIn(List<String> values) {
            addCriterion("uid_transaccion_det in", values, "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetNotIn(List<String> values) {
            addCriterion("uid_transaccion_det not in", values, "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetBetween(String value1, String value2) {
            addCriterion("uid_transaccion_det between", value1, value2, "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetNotBetween(String value1, String value2) {
            addCriterion("uid_transaccion_det not between", value1, value2, "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andCoddivisaIsNull() {
            addCriterion("coddivisa is null");
            return (Criteria) this;
        }

        public Criteria andCoddivisaIsNotNull() {
            addCriterion("coddivisa is not null");
            return (Criteria) this;
        }

        public Criteria andCoddivisaEqualTo(String value) {
            addCriterion("coddivisa =", value, "coddivisa");
            return (Criteria) this;
        }

        public Criteria andCoddivisaNotEqualTo(String value) {
            addCriterion("coddivisa <>", value, "coddivisa");
            return (Criteria) this;
        }

        public Criteria andCoddivisaGreaterThan(String value) {
            addCriterion("coddivisa >", value, "coddivisa");
            return (Criteria) this;
        }

        public Criteria andCoddivisaGreaterThanOrEqualTo(String value) {
            addCriterion("coddivisa >=", value, "coddivisa");
            return (Criteria) this;
        }

        public Criteria andCoddivisaLessThan(String value) {
            addCriterion("coddivisa <", value, "coddivisa");
            return (Criteria) this;
        }

        public Criteria andCoddivisaLessThanOrEqualTo(String value) {
            addCriterion("coddivisa <=", value, "coddivisa");
            return (Criteria) this;
        }

        public Criteria andCoddivisaLike(String value) {
            addCriterion("coddivisa like", value, "coddivisa");
            return (Criteria) this;
        }

        public Criteria andCoddivisaNotLike(String value) {
            addCriterion("coddivisa not like", value, "coddivisa");
            return (Criteria) this;
        }

        public Criteria andCoddivisaIn(List<String> values) {
            addCriterion("coddivisa in", values, "coddivisa");
            return (Criteria) this;
        }

        public Criteria andCoddivisaNotIn(List<String> values) {
            addCriterion("coddivisa not in", values, "coddivisa");
            return (Criteria) this;
        }

        public Criteria andCoddivisaBetween(String value1, String value2) {
            addCriterion("coddivisa between", value1, value2, "coddivisa");
            return (Criteria) this;
        }

        public Criteria andCoddivisaNotBetween(String value1, String value2) {
            addCriterion("coddivisa not between", value1, value2, "coddivisa");
            return (Criteria) this;
        }

        public Criteria andTipoDeCambioIsNull() {
            addCriterion("tipo_de_cambio is null");
            return (Criteria) this;
        }

        public Criteria andTipoDeCambioIsNotNull() {
            addCriterion("tipo_de_cambio is not null");
            return (Criteria) this;
        }

        public Criteria andTipoDeCambioEqualTo(BigDecimal value) {
            addCriterion("tipo_de_cambio =", value, "tipoDeCambio");
            return (Criteria) this;
        }

        public Criteria andTipoDeCambioNotEqualTo(BigDecimal value) {
            addCriterion("tipo_de_cambio <>", value, "tipoDeCambio");
            return (Criteria) this;
        }

        public Criteria andTipoDeCambioGreaterThan(BigDecimal value) {
            addCriterion("tipo_de_cambio >", value, "tipoDeCambio");
            return (Criteria) this;
        }

        public Criteria andTipoDeCambioGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("tipo_de_cambio >=", value, "tipoDeCambio");
            return (Criteria) this;
        }

        public Criteria andTipoDeCambioLessThan(BigDecimal value) {
            addCriterion("tipo_de_cambio <", value, "tipoDeCambio");
            return (Criteria) this;
        }

        public Criteria andTipoDeCambioLessThanOrEqualTo(BigDecimal value) {
            addCriterion("tipo_de_cambio <=", value, "tipoDeCambio");
            return (Criteria) this;
        }

        public Criteria andTipoDeCambioIn(List<BigDecimal> values) {
            addCriterion("tipo_de_cambio in", values, "tipoDeCambio");
            return (Criteria) this;
        }

        public Criteria andTipoDeCambioNotIn(List<BigDecimal> values) {
            addCriterion("tipo_de_cambio not in", values, "tipoDeCambio");
            return (Criteria) this;
        }

        public Criteria andTipoDeCambioBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("tipo_de_cambio between", value1, value2, "tipoDeCambio");
            return (Criteria) this;
        }

        public Criteria andTipoDeCambioNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("tipo_de_cambio not between", value1, value2, "tipoDeCambio");
            return (Criteria) this;
        }

        public Criteria andUsuarioIsNull() {
            addCriterion("usuario is null");
            return (Criteria) this;
        }

        public Criteria andUsuarioIsNotNull() {
            addCriterion("usuario is not null");
            return (Criteria) this;
        }

        public Criteria andUsuarioEqualTo(String value) {
            addCriterion("usuario =", value, "usuario");
            return (Criteria) this;
        }

        public Criteria andUsuarioNotEqualTo(String value) {
            addCriterion("usuario <>", value, "usuario");
            return (Criteria) this;
        }

        public Criteria andUsuarioGreaterThan(String value) {
            addCriterion("usuario >", value, "usuario");
            return (Criteria) this;
        }

        public Criteria andUsuarioGreaterThanOrEqualTo(String value) {
            addCriterion("usuario >=", value, "usuario");
            return (Criteria) this;
        }

        public Criteria andUsuarioLessThan(String value) {
            addCriterion("usuario <", value, "usuario");
            return (Criteria) this;
        }

        public Criteria andUsuarioLessThanOrEqualTo(String value) {
            addCriterion("usuario <=", value, "usuario");
            return (Criteria) this;
        }

        public Criteria andUsuarioLike(String value) {
            addCriterion("usuario like", value, "usuario");
            return (Criteria) this;
        }

        public Criteria andUsuarioNotLike(String value) {
            addCriterion("usuario not like", value, "usuario");
            return (Criteria) this;
        }

        public Criteria andUsuarioIn(List<String> values) {
            addCriterion("usuario in", values, "usuario");
            return (Criteria) this;
        }

        public Criteria andUsuarioNotIn(List<String> values) {
            addCriterion("usuario not in", values, "usuario");
            return (Criteria) this;
        }

        public Criteria andUsuarioBetween(String value1, String value2) {
            addCriterion("usuario between", value1, value2, "usuario");
            return (Criteria) this;
        }

        public Criteria andUsuarioNotBetween(String value1, String value2) {
            addCriterion("usuario not between", value1, value2, "usuario");
            return (Criteria) this;
        }

        public Criteria andUidActividadLikeInsensitive(String value) {
            addCriterion("upper(uid_actividad) like", value.toUpperCase(), "uidActividad");
            return (Criteria) this;
        }

        public Criteria andUidDiarioCajaLikeInsensitive(String value) {
            addCriterion("upper(uid_diario_caja) like", value.toUpperCase(), "uidDiarioCaja");
            return (Criteria) this;
        }

        public Criteria andConceptoLikeInsensitive(String value) {
            addCriterion("upper(concepto) like", value.toUpperCase(), "concepto");
            return (Criteria) this;
        }

        public Criteria andDocumentoLikeInsensitive(String value) {
            addCriterion("upper(documento) like", value.toUpperCase(), "documento");
            return (Criteria) this;
        }

        public Criteria andCodmedpagLikeInsensitive(String value) {
            addCriterion("upper(codmedpag) like", value.toUpperCase(), "codmedpag");
            return (Criteria) this;
        }

        public Criteria andIdDocumentoLikeInsensitive(String value) {
            addCriterion("upper(id_documento) like", value.toUpperCase(), "idDocumento");
            return (Criteria) this;
        }

        public Criteria andCodconceptoMovLikeInsensitive(String value) {
            addCriterion("upper(codconcepto_mov) like", value.toUpperCase(), "codconceptoMov");
            return (Criteria) this;
        }

        public Criteria andUidTransaccionDetLikeInsensitive(String value) {
            addCriterion("upper(uid_transaccion_det) like", value.toUpperCase(), "uidTransaccionDet");
            return (Criteria) this;
        }

        public Criteria andCoddivisaLikeInsensitive(String value) {
            addCriterion("upper(coddivisa) like", value.toUpperCase(), "coddivisa");
            return (Criteria) this;
        }

        public Criteria andUsuarioLikeInsensitive(String value) {
            addCriterion("upper(usuario) like", value.toUpperCase(), "usuario");
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