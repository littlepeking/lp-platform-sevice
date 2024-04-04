package com.enhantec.wms.ui.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.enhantec.framework.common.model.EHWMSModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@TableName(value ="SKU")
@SuperBuilder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuModel extends EHWMSModel implements Serializable {

    @TableField(value = "SKU")
    private String sku;


    @TableField(value = "STORERKEY")
    private String storerKey;

    @TableField(value = "WHSEID")
    private String whseId;


    @TableField(value = "DESCR")
    private String descr;


    @TableField(value = "MANUFACTURERSKU")
    private String manufacturerSku;


    @TableField(value = "RETAILSKU")
    private String retailSku;


    @TableField(value = "ALTSKU")
    private String altSku;


    @TableField(value = "PACKKEY")
    private String packKey;


    @TableField(value = "SKUGROUP")
    private String skuGroup;

    @TableField(value = "SKUGROUP2")
    private String skuGroup2;


    @TableField(value = "PUTAWAYSTRATEGYKEY")
    private String putawayStrategyKey;


    @TableField(value = "STRATEGYKEY")
    private String strategyKey;


    @TableField(value = "NEWALLOCATIONSTRATEGY")
    private String newAllocationStrategy;


    @TableField(value = "CARTONGROUP")
    private String cartonGroup;

    @TableField(value = "PUTAWAYLOC")
    private String putawayLoc;


    @TableField(value = "PUTAWAYZONE")
    private String putawayZone;


    @TableField(value = "INNERPACK")
    private BigDecimal innerPack;


    @TableField(value = "CUBE")
    private Double cube;


    @TableField(value = "GROSSWGT")
    private Double grossWgt;


    @TableField(value = "NETWGT")
    private Double netWgt;


    @TableField(value = "TAREWEIGHT")
    private BigDecimal tareWeight;


    @TableField(value = "ABC")
    private String abc;


    @TableField(value = "CYCLECOUNTFREQUENCY")
    private Integer cycleCountFrequency;


    @TableField(value = "LASTCYCLECOUNT")
    private Date lastCycleCount;


    @TableField(value = "REORDERPOINT")
    private Integer reorderPoint;


    @TableField(value = "REORDERQTY")
    private BigDecimal reorderQty;


    @TableField(value = "ROTATEBY")
    private String rotateBy;


    @TableField(value = "DEFAULTROTATION")
    private String defaultRotation;


    @TableField(value = "DATECODEDAYS")
    private Integer dateCodeDays;


    @TableField(value = "RFDEFAULTPACK")
    private String rfDefaultPack;


    @TableField(value = "RFDEFAULTUOM")
    private String rfDefaultUom;


    @TableField(value = "TYPE")
    private String type;


    @TableField(value = "SKUTYPE")
    private String skuType;


    @TableField(value = "LOTTABLEVALIDATIONKEY")
    private String lottableValidationKey;


    @TableField(value = "NOTES1")
    private String notes1;


    @TableField(value = "NOTES2")
    private String notes2;


    @TableField(value = "RETURNSLOC")
    private String returnsLoc;


    @TableField(value = "StackLimit")
    private Integer stackLimit;


    @TableField(value = "MaxPalletsPerZone")
    private Integer maxPalletsPerZone;


    @TableField(value = "TOEXPIREDAYS")
    private Integer toExpireDays;


    @TableField(value = "MANUALSETUPREQUIRED")
    private boolean manualSetupRequired;


    @TableField(value = "ACTIVE")
    private boolean active;


    @TableField(value = "ISSERIALCONTROL")
    private String isSerialControl;


    @TableField(value = "ALLOWMULTILOTLPN")
    private String allowMultiLotLpn;


    @TableField(value = "ENABLEBARCODE")
    private String enableBarcode;


    @TableField(value = "CATCHGROSSWGT")
    private Integer catchGrossWgt;


    @TableField(value = "CATCHNETWGT")
    private Integer catchNetWgt;


    @TableField(value = "CATCHTAREWGT")
    private Integer catchTareWgt;


    @TableField(value = "STORAGETYPE")
    private String storageType;


    @TableField(value = "ITEMCHARACTERISTIC1")
    private String itemCharacteristic1;


    @TableField(value = "ITEMCHARACTERISTIC2")
    private String itemCharacteristic2;


    @TableField(value = "COMMODITYCLASS")
    private String commodityClass;


    @TableField(value = "STOCKCATEGORY")
    private String stockCategory;


    @TableField(value = "COUNTRYOFORIGIN")
    private String countryOfOrigin;


    @TableField(value = "LOTTABLE01LABEL")
    private String lottable01Label;


    @TableField(value = "LOTTABLE02LABEL")
    private String lottable02Label;


    @TableField(value = "LOTTABLE03LABEL")
    private String lottable03Label;


    @TableField(value = "LOTTABLE04LABEL")
    private String lottable04Label;


    @TableField(value = "LOTTABLE05LABEL")
    private String lottable05Label;


    @TableField(value = "LOTTABLE06LABEL")
    private String lottable06Label;


    @TableField(value = "LOTTABLE07LABEL")
    private String lottable07Label;


    @TableField(value = "LOTTABLE08LABEL")
    private String lottable08Label;


    @TableField(value = "LOTTABLE09LABEL")
    private String lottable09Label;


    @TableField(value = "LOTTABLE10LABEL")
    private String lottable10Label;


    @TableField(value = "LOTTABLE11LABEL")
    private String lottable11Label;


    @TableField(value = "LOTTABLE12LABEL")
    private String lottable12Label;


    @TableField(value = "SUSR1")
    private String susr1;


    @TableField(value = "SUSR2")
    private String susr2;


    @TableField(value = "SUSR3")
    private String susr3;


    @TableField(value = "SUSR4")
    private String susr4;


    @TableField(value = "SUSR5")
    private String susr5;


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



    @TableField(value = "BUSR1")
    private String busr1;


    @TableField(value = "BUSR2")
    private String busr2;


    @TableField(value = "BUSR3")
    private String busr3;


    @TableField(value = "BUSR4")
    private String busr4;


    @TableField(value = "BUSR5")
    private String busr5;


    @TableField(value = "BUSR6")
    private String busr6;


    @TableField(value = "BUSR7")
    private String busr7;


    @TableField(value = "BUSR8")
    private String busr8;


    @TableField(value = "BUSR9")
    private String busr9;


    @TableField(value = "BUSR10")
    private String busr10;


    @TableField(value = "BUSR11")
    private String busr11;


    @TableField(value = "BUSR12")
    private String busr12;


    @TableField(value = "BUSR13")
    private String busr13;


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


    @TableField(value = "EXT_UDF_STR16")
    private String extUdfStr16;


    @TableField(value = "EXT_UDF_STR17")
    private String extUdfStr17;


    @TableField(value = "EXT_UDF_STR18")
    private String extUdfStr18;


    @TableField(value = "EXT_UDF_STR19")
    private String extUdfStr19;


    @TableField(value = "EXT_UDF_STR20")
    private String extUdfStr20;


    @TableField(value = "EXT_UDF_STR21")
    private String extUdfStr21;


    @TableField(value = "EXT_UDF_STR22")
    private String extUdfStr22;


    @TableField(value = "EXT_UDF_STR23")
    private String extUdfStr23;


    @TableField(value = "EXT_UDF_STR24")
    private String extUdfStr24;


    @TableField(value = "EXT_UDF_STR25")
    private String extUdfStr25;


    @TableField(value = "EXT_UDF_STR26")
    private String extUdfStr26;


    @TableField(value = "EXT_UDF_STR27")
    private String extUdfStr27;


    @TableField(value = "EXT_UDF_STR28")
    private String extUdfStr28;


    @TableField(value = "EXT_UDF_STR29")
    private String extUdfStr29;


    @TableField(value = "EXT_UDF_STR30")
    private String extUdfStr30;


    @TableField(value = "EXT_UDF_DATE1")
    private Date extUdfDate1;


    @TableField(value = "EXT_UDF_DATE2")
    private Date extUdfDate2;


    @TableField(value = "EXT_UDF_DATE3")
    private Date extUdfDate3;


    @TableField(value = "EXT_UDF_DATE4")
    private Date extUdfDate4;


    @TableField(value = "EXT_UDF_DATE5")
    private Date extUdfDate5;


    @TableField(value = "EXT_UDF_DATE6")
    private Date extUdfDate6;


    @TableField(value = "EXT_UDF_DATE7")
    private Date extUdfDate7;


    @TableField(value = "EXT_UDF_DATE8")
    private Date extUdfDate8;


    @TableField(value = "EXT_UDF_DATE9")
    private Date extUdfDate9;


    @TableField(value = "EXT_UDF_DATE10")
    private Date extUdfDate10;


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


    @TableField(value = "EXT_UDF_FLOAT6")
    private Double extUdfFloat6;


    @TableField(value = "EXT_UDF_FLOAT7")
    private Double extUdfFloat7;


    @TableField(value = "EXT_UDF_FLOAT8")
    private Double extUdfFloat8;


    @TableField(value = "EXT_UDF_FLOAT9")
    private Double extUdfFloat9;


    @TableField(value = "EXT_UDF_FLOAT10")
    private Double extUdfFloat10;


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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}