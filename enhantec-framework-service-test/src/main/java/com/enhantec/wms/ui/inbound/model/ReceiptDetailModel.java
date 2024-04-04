package com.enhantec.wms.ui.inbound.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.framework.common.model.EHWMSModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@TableName(value ="RECEIPTDETAIL")
@Data
public class ReceiptDetailModel extends EHWMSModel implements Serializable {
    /**
     * 
     */
    @TableField(value = "RECEIPTKEY")
    private String receiptKey;

    /**
     * 
     */
    @TableField(value = "RECEIPTLINENUMBER")
    private String receiptLineNumber;
    /**
     * 
     */
    @TableField(value = "WHSEID")
    private String whseId;

    /**
     * 
     */
    @TableField(value = "SUBLINENUMBER")
    private String subLineNumber;

    /**
     * 
     */
    @TableField(value = "EXTERNRECEIPTKEY")
    private String externReceiptKey;

    /**
     * 
     */
    @TableField(value = "EXTERNLINENO")
    private String externLineNo;

    /**
     * 
     */
    @TableField(value = "STORERKEY")
    private String storerKey;

    /**
     * 
     */
    @TableField(value = "POKEY")
    private String poKey;

    /**
     * 
     */
    @TableField(value = "TARIFFKEY")
    private String tariffKey;

    /**
     * 
     */
    @TableField(value = "SKU")
    private String sku;

    /**
     * 
     */
    @TableField(value = "ALTSKU")
    private String altSku;

    /**
     * 
     */
    @TableField(value = "ID")
    private String id;

    /**
     * 
     */
    @TableField(value = "STATUS")
    private String status;

    /**
     * 
     */
    @TableField(value = "DATERECEIVED")
    private LocalDateTime dateReceived;

    /**
     * 
     */
    @TableField(value = "QTYEXPECTED")
    private BigDecimal qtyExpected;

    /**
     * 
     */
    @TableField(value = "QTYADJUSTED")
    private BigDecimal qtyAdjusted;

    /**
     * 
     */
    @TableField(value = "QTYRECEIVED")
    private BigDecimal qtyReceived;

    /**
     * 
     */
    @TableField(value = "UOM")
    private String uom;

    /**
     * 
     */
    @TableField(value = "PACKKEY")
    private String packKey;

    /**
     * 
     */
    @TableField(value = "VESSELKEY")
    private String vesselKey;

    /**
     * 
     */
    @TableField(value = "VOYAGEKEY")
    private String voyageKey;

    /**
     * 
     */
    @TableField(value = "XDOCKKEY")
    private String xdockKey;

    /**
     * 
     */
    @TableField(value = "CONTAINERKEY")
    private String containerKey;

    /**
     * 
     */
    @TableField(value = "TOLOC")
    private String toLoc;

    /**
     * 
     */
    @TableField(value = "TOLOT")
    private String toLot;

    /**
     * 
     */
    @TableField(value = "TOID")
    private String toId;

    /**
     * 
     */
    @TableField(value = "CONDITIONCODE")
    private String conditionCode;

    /**
     * 
     */
    @TableField(value = "LOTTABLE01")
    private String lottable01;

    /**
     * 
     */
    @TableField(value = "LOTTABLE02")
    private String lottable02;

    /**
     * 
     */
    @TableField(value = "LOTTABLE03")
    private String lottable03;

    /**
     * 
     */
    @TableField(value = "LOTTABLE04")
    private LocalDateTime lottable04;

    /**
     * 
     */
    @TableField(value = "LOTTABLE05")
    private LocalDateTime lottable05;

    /**
     * 
     */
    @TableField(value = "LOTTABLE06")
    private String lottable06;

    /**
     * 
     */
    @TableField(value = "LOTTABLE07")
    private String lottable07;

    /**
     * 
     */
    @TableField(value = "LOTTABLE08")
    private String lottable08;

    /**
     * 
     */
    @TableField(value = "LOTTABLE09")
    private String lottable09;

    /**
     * 
     */
    @TableField(value = "LOTTABLE10")
    private String lottable10;


    @TableField(value = "LOTTABLE11")
    private LocalDateTime  lottable11;


    @TableField(value = "LOTTABLE12")
    private LocalDateTime  lottable12;

    /**
     * 
     */
    @TableField(value = "CASECNT")
    private BigDecimal caseCnt;

    /**
     * 
     */
    @TableField(value = "INNERPACK")
    private BigDecimal innerPack;

    /**
     * 
     */
    @TableField(value = "PALLET")
    private BigDecimal pallet;

    /**
     * 
     */
    @TableField(value = "CUBE")
    private Double cube;

    /**
     * 
     */
    @TableField(value = "GROSSWGT")
    private BigDecimal grossWgt;

    /**
     * 
     */
    @TableField(value = "NETWGT")
    private BigDecimal netWgt;

    /**
     * 
     */
    @TableField(value = "OTHERUNIT1")
    private Double otherUnit1;

    /**
     * 
     */
    @TableField(value = "OTHERUNIT2")
    private Double otherUnit2;

    /**
     * 
     */
    @TableField(value = "UNITPRICE")
    private Double unitPrice;

    /**
     * 
     */
    @TableField(value = "EXTENDEDPRICE")
    private Double extendedPrice;

    /**
     * 
     */
    @TableField(value = "EFFECTIVEDATE")
    private LocalDateTime effectiveDate;

    /**
     * 
     */
    @TableField(value = "FORTE_FLAG")
    private String forteFlag;

    /**
     * 
     */
    @TableField(value = "SUSR1")
    private String susr1;

    /**
     * 
     */
    @TableField(value = "SUSR2")
    private String susr2;

    /**
     * 
     */
    @TableField(value = "SUSR3")
    private String susr3;

    /**
     * 
     */
    @TableField(value = "SUSR4")
    private String susr4;

    /**
     * 
     */
    @TableField(value = "SUSR5")
    private String susr5;

    /**
     * 
     */
    @TableField(value = "NOTES")
    private String notes;

    /**
     * 
     */
    @TableField(value = "REASONCODE")
    private String reasonCode;

    /**
     * 
     */
    @TableField(value = "PALLETID")
    private String palletId;

    /**
     * 
     */
    @TableField(value = "QTYREJECTED")
    private BigDecimal qtyRejected;

    /**
     * 
     */
    @TableField(value = "TYPE")
    private String type;
    @TableField(value = "RETURNTYPE")
    private String returnType;

    @TableField(value = "RETURNREASON")
    private String returnReason;

    @TableField(value = "DISPOSITIONTYPE")
    private String dispositionType;

    @TableField(value = "DISPOSITIONCODE")
    private String dispositionCode;

    @TableField(value = "RETURNCONDITION")
    private String returnCondition;

    @TableField(value = "QCREQUIRED")
    private String qcRequired;

    @TableField(value = "QCQTYINSPECTED")
    private BigDecimal qcQtyInspected;

    @TableField(value = "QCQTYREJECTED")
    private BigDecimal qcQtyRejected;

    @TableField(value = "QCREJREASON")
    private String qcRejReason;

    @TableField(value = "QCSTATUS")
    private String qcStatus;

    @TableField(value = "QCUSER")
    private String qcUser;

    @TableField(value = "QCAUTOADJUST")
    private String qcAutoAdjust;

    @TableField(value = "EXTERNALLOT")
    private String externalLot;

    @TableField(value = "RMA")
    private String rma;

    @TableField(value = "PACKINGSLIPQTY")
    private BigDecimal packingSlipQty;

    @TableField(value = "IPSKey")
    private String ipsKey;

    @TableField(value = "SupplierName")
    private String supplierName;

    @TableField(value = "SupplierKey")
    private String supplierKey;

    @TableField(value = "MatchLottable")
    private String matchLottable;

    @TableField(value = "RECEIPTDETAILID")
    private String receiptDetailId;

    @TableField(value = "POLINENUMBER")
    private String poLineNumber;

    @TableField(value = "SOURCELOCATION")
    private String sourceLocation;

    @TableField(value = "SOURCEVERSION")
    private String sourceVersion;

    @TableField(value = "REFERENCETYPE")
    private String referenceType;

    @TableField(value = "REFERENCEDOCUMENT")
    private String referenceDocument;

    @TableField(value = "REFERENCELOCATION")
    private String referenceLocation;

    @TableField(value = "REFERENCEVERSION")
    private String referenceVersion;

    @TableField(value = "REFERENCELINE")
    private String referenceLine;

    @TableField(value = "CUBICMETER")
    private BigDecimal cubicMeter;

    @TableField(value = "HUNDREDWEIGHT")
    private BigDecimal hundredWeight;

    @TableField(value = "TAREWGT")
    private BigDecimal tareWgt;

    @TableField(value = "ReferenceAccountingEntity")
    private String referenceAccountingEntity;

    @TableField(value = "ReferenceScheduleLine")
    private String referenceScheduleLine;

    @TableField(value = "REQUISITIONDOCUMENT")
    private String requisitionDocument;

    @TableField(value = "REQUISITIONACCOUNTINGENTITY")
    private String requisitionAccountingEntity;

    @TableField(value = "REQUISITIONLOCATION")
    private String requisitionLocation;

    @TableField(value = "REQUISITIONVERSION")
    private String requisitionVersion;

    @TableField(value = "REQUISITIONLINE")
    private String requisitionLine;

    @TableField(value = "REQUISITIONSCHEDULELINE")
    private String requisitionScheduleLine;

    @TableField(value = "PURCHASEORDERDOCUMENT")
    private String purchaseOrderDocument;

    @TableField(value = "PURCHASEORDERACCOUNTINGENTITY")
    private String purchaseOrderAccountingEntity;

    @TableField(value = "PURCHASEORDERLOCATION")
    private String purchaseOrderLocation;

    @TableField(value = "PURCHASEORDERVERSION")
    private String purchaseOrderVersion;

    @TableField(value = "PURCHASEORDERLINE")
    private String purchaseOrderLine;

    @TableField(value = "PURCHASEORDERSCHEDULELINE")
    private String purchaseOrderScheduleLine;

    @TableField(value = "SALESORDERDOCUMENT")
    private String salesOrderDocument;

    @TableField(value = "SALESORDERACCOUNTINGENTITY")
    private String salesOrderAccountingEntity;

    @TableField(value = "SALESORDERLOCATION")
    private String salesOrderLocation;

    @TableField(value = "SALESORDERVERSION")
    private String salesOrderVersion;

    @TableField(value = "SALESORDERLINE")
    private String salesOrderLine;

    @TableField(value = "SALESORDERSCHEDULELINE")
    private String salesOrderScheduleLine;

    @TableField(value = "PRODUCTIONORDERDOCUMENT")
    private String productionOrderDocument;

    @TableField(value = "PRODUCTIONORDERACCENTITY")
    private String productionOrderAccEntity;

    @TableField(value = "PRODUCTIONORDERLOCATION")
    private String productionOrderLocation;

    @TableField(value = "PRODUCTIONORDERVERSION")
    private String productionOrderVersion;

    @TableField(value = "PRODUCTIONORDERLINE")
    private String productionOrderLine;

    @TableField(value = "PRODUCTIONORDERSCHEDULELINE")
    private String productionOrderScheduleLine;

    @TableField(value = "TEMPERATURE")
    private BigDecimal temperature;

    @TableField(value = "ORIGINALLINENUMBER")
    private String originalLineNumber;

    @TableField(value = "DISPOSITION")
    private String disposition;

    @TableField(value = "QCSAMPLEDTYPE")
    private String qcSampledType;

    @TableField(value = "QTYTOINSPECT")
    private BigDecimal qtyToInspect;

    @TableField(value = "CHECKFORBACKFLUSHING")
    private String checkForBackflushing;

    @TableField(value = "QUARANTINELPN")
    private String quarantineLPN;

    @TableField(value = "HOSTPROCESSREQUIRED")
    private String hostProcessRequired;

    @TableField(value = "ALLOWREOPEN")
    private String allowReopen;

    @TableField(value = "ADDDATE")
    private LocalDateTime addDate;

    @TableField(value = "ADDWHO")
    private String addWho;

    @TableField(value = "EDITDATE")
    private LocalDateTime editDate;

    @TableField(value = "EDITWHO")
    private String editWho;

    @TableField(value = "EXT_UDF_STR1")
    private String extUdfStr1;

    @TableField(value = "EXT_UDF_STR2")
    private String extUdfStr2;

    @TableField(value = "EXT_UDF_STR3")
    private String extUdfStr3;

    @TableField(value = "EXT_UDF_STR4")
    private String extUdfStr4;

    @TableField(value = "EXT_UDF_STR5")
    private String extUdfStr5;

    @TableField(value = "EXT_UDF_STR6")
    private String extUdfStr6;

    @TableField(value = "EXT_UDF_STR7")
    private String extUdfStr7;

    @TableField(value = "EXT_UDF_STR8")
    private String extUdfStr8;

    @TableField(value = "EXT_UDF_STR9")
    private String extUdfStr9;

    @TableField(value = "EXT_UDF_STR10")
    private String extUdfStr10;

    @TableField(value = "EXT_UDF_STR11")
    private String extUdfStr11;

    @TableField(value = "EXT_UDF_STR12")
    private String extUdfStr12;

    @TableField(value = "EXT_UDF_STR13")
    private String extUdfStr13;

    @TableField(value = "EXT_UDF_STR14")
    private String extUdfStr14;

    @TableField(value = "EXT_UDF_STR15")
    private String extUdfStr15;

    @TableField(value = "EXT_UDF_DATE1")
    private LocalDateTime  extUdfDate1;

    @TableField(value = "EXT_UDF_DATE2")
    private LocalDateTime  extUdfDate2;

    @TableField(value = "EXT_UDF_DATE3")
    private LocalDateTime  extUdfDate3;

    @TableField(value = "EXT_UDF_DATE4")
    private LocalDateTime  extUdfDate4;

    @TableField(value = "EXT_UDF_DATE5")
    private LocalDateTime  extUdfDate5;

    @TableField(value = "EXT_UDF_FLOAT1")
    private Double extUdfFloat1;

    @TableField(value = "EXT_UDF_FLOAT2")
    private Double extUdfFloat2;

    @TableField(value = "EXT_UDF_FLOAT3")
    private Double extUdfFloat3;

    @TableField(value = "EXT_UDF_FLOAT4")
    private Double extUdfFloat4;

    @TableField(value = "EXT_UDF_FLOAT5")
    private Double extUdfFloat5;

    @TableField(value = "EXT_UDF_LKUP1")
    private String extUdfLkup1;

    @TableField(value = "EXT_UDF_LKUP2")
    private String extUdfLkup2;

    @TableField(value = "EXT_UDF_LKUP3")
    private String extUdfLkup3;

    @TableField(value = "EXT_UDF_LKUP4")
    private String extUdfLkup4;

    @TableField(value = "EXT_UDF_LKUP5")
    private String extUdfLkup5;

    @TableField(value = "EXT_UDF_LKUP6")
    private String extUdfLkup6;

    @TableField(value = "EXT_UDF_LKUP7")
    private String extUdfLkup7;

    @TableField(value = "EXT_UDF_LKUP8")
    private String extUdfLkup8;

    @TableField(value = "EXT_UDF_LKUP9")
    private String extUdfLkup9;

    @TableField(value = "EXT_UDF_LKUP10")
    private String extUdfLkup10;

    @TableField(value = "SUSR6")
    private String susr6;

    @TableField(value = "SUSR7")
    private String susr7;

    @TableField(value = "SUSR8")
    private String susr8;

    @TableField(value = "SUSR9")
    private String susr9;

    @TableField(value = "SUSR10")
    private String susr10;

    @TableField(value = "SUSR11")
    private String susr11;

    @TableField(value = "SUSR12")
    private String susr12;

    @TableField(value = "SUSR13")
    private String susr13;

    @TableField(value = "SUSR14")
    private String susr14;

    @TableField(value = "SUSR15")
    private String susr15;

    @TableField(value = "SUSR16")
    private String susr16;

    @TableField(value = "SUSR17")
    private String susr17;

    @TableField(value = "SUSR18")
    private String susr18;

    @TableField(value = "SUSR19")
    private String susr19;

    @TableField(value = "SUSR20")
    private String susr20;

    @TableField(value = "GROSSWGTRECEIVED")
    private BigDecimal grossWgtReceived;

    @TableField(value = "TAREWGTRECEIVED")
    private BigDecimal tareWgtReceived;

    @TableField(value = "BARRELNUMBER")
    private String barrelNumber;

    @TableField(value = "GROSSWGTEXPECTED")
    private BigDecimal grossWgtExpected;

    @TableField(value = "TAREWGTEXPECTED")
    private BigDecimal tareWgtExpected;

    @TableField(value = "TOTALBARRELNUMBER")
    private String totalBarrelNumber;

    @TableField(value = "REGROSSWGT")
    private BigDecimal regrossWgt;


    @TableField(value = "EEXTERNALLOT")
    private String eExternalLot;

    @TableField(value = "ELOTTABLE01")
    private String elottable01;

    @TableField(value = "ELOTTABLE02")
    private String elottable02;

    @TableField(value = "ELOTTABLE03")
    private String elottable03;

    @TableField(value = "ELOTTABLE04")
    private LocalDateTime elottable04;

    @TableField(value = "ELOTTABLE05")
    private LocalDateTime elottable05;

    @TableField(value = "ELOTTABLE06")
    private String elottable06;

    @TableField(value = "ELOTTABLE07")
    private String elottable07;

    @TableField(value = "ELOTTABLE08")
    private String elottable08;

    @TableField(value = "ELOTTABLE09")
    private String elottable09;

    @TableField(value = "ELOTTABLE10")
    private String elottable10;


    @TableField(value = "ELOTTABLE11")
    private LocalDateTime elottable11;

    @TableField(value = "ELOTTABLE12")
    private LocalDateTime elottable12;

    @TableField(value = "ELOTTABLE13")
    private String elottable13;

    @TableField(value = "ELOTTABLE14")
    private String elottable14;

    @TableField(value = "ELOTTABLE15")
    private String elottable15;

    @TableField(value = "ELOTTABLE16")
    private String elottable16;

    @TableField(value = "ELOTTABLE17")
    private String elottable17;

    @TableField(value = "ELOTTABLE18")
    private String elottable18;

    @TableField(value = "ELOTTABLE19")
    private String elottable19;

    @TableField(value = "ELOTTABLE20")
    private String elottable20;

    @TableField(value = "PARENTID")
    private String parentId;

    @TableField(value = "SERIALNUMBER")
    private String serialNumber;

    @TableField(value = "MEMO")
    private String memo;

    @TableField(value = "PRODLOTEXPECTED")
    private String prodLotExpected;

    @TableField(value = "LASTSHIPPEDLOC")
    private String lastShippedLoc;

    @TableField(value = "ELOTTABLE21")
    private String elottable21;

    @TableField(value = "LASTID")
    private String lastId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}