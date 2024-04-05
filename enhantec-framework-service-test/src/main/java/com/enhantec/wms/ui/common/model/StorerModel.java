package com.enhantec.wms.ui.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.enhantec.framework.common.model.EHWMSModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@TableName(value ="STORER")
@SuperBuilder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorerModel extends EHWMSModel implements Serializable {
    
    @TableField(value = "STORERKEY")
    private String storerKey;


    @TableField(value = "TYPE")
    private String type;


    @TableField(value = "WHSEID")
    private String whseId;


    @TableField(value = "COMPANY")
    private String company;



    @TableField(value = "ADDRESS1")
    private String address1;


    @TableField(value = "ADDRESS2")
    private String address2;


    @TableField(value = "ADDRESS3")
    private String address3;


    @TableField(value = "ADDRESS4")
    private String address4;


    @TableField(value = "CITY")
    private String city;


    @TableField(value = "STATE")
    private String state;


    @TableField(value = "ZIP")
    private String zip;


    @TableField(value = "COUNTRY")
    private String country;


    @TableField(value = "ISOCNTRYCODE")
    private String isoCntryCode;


    @TableField(value = "CONTACT1")
    private String contact1;


    @TableField(value = "CONTACT2")
    private String contact2;


    @TableField(value = "PHONE1")
    private String phone1;


    @TableField(value = "PHONE2")
    private String phone2;


    @TableField(value = "FAX1")
    private String fax1;


    @TableField(value = "FAX2")
    private String fax2;


    @TableField(value = "EMAIL1")
    private String email1;


    @TableField(value = "EMAIL2")
    private String email2;


    @TableField(value = "B_CONTACT1")
    private String bContact1;


    @TableField(value = "B_CONTACT2")
    private String bContact2;


    @TableField(value = "B_COMPANY")
    private String bCompany;


    @TableField(value = "B_ADDRESS1")
    private String bAddress1;


    @TableField(value = "B_ADDRESS2")
    private String bAddress2;


    @TableField(value = "B_ADDRESS3")
    private String bAddress3;


    @TableField(value = "B_ADDRESS4")
    private String bAddress4;


    @TableField(value = "B_CITY")
    private String bCity;


    @TableField(value = "B_STATE")
    private String bState;


    @TableField(value = "B_ZIP")
    private String bZip;


    @TableField(value = "B_COUNTRY")
    private String bCountry;


    @TableField(value = "B_ISOCNTRYCODE")
    private String bIsoCntryCode;


    @TableField(value = "B_PHONE1")
    private String bPhone1;


    @TableField(value = "B_PHONE2")
    private String bPhone2;


    @TableField(value = "B_FAX1")
    private String bFax1;


    @TableField(value = "B_FAX2")
    private String bFax2;


    @TableField(value = "B_EMAIL1")
    private String bEmail1;


    @TableField(value = "B_EMAIL2")
    private String bEmail2;


    @TableField(value = "CARTONGROUP")
    private String cartonGroup;


    @TableField(value = "STATUS")
    private String status;


    @TableField(value = "ACTIVEDATE")
    private LocalDateTime activeDate;


    @TableField(value = "INACTIVEDATE")
    private LocalDateTime inactiveDate;


    @TableField(value = "DEFAULTSTRATEGY")
    private String defaultStrategy;


    @TableField(value = "DEFAULTSKUROTATION")
    private String defaultSkuRotation;


    @TableField(value = "DEFAULTROTATION")
    private String defaultRotation;


    @TableField(value = "TITLE1")
    private String title1;


    @TableField(value = "TITLE2")
    private String title2;


    @TableField(value = "DESCRIPTION")
    private String description;


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


    @TableField(value = "NOTES1")
    private String notes1;


    @TableField(value = "NOTES2")
    private String notes2;


    @TableField(value = "ENABLEOPPXDOCK")
    private String enableOppXDock;


    @TableField(value = "RECEIPTVALIDATIONTEMPLATE")
    private String receiptValidationTemplate;




    @TableField(value = "ALLOWCOMMINGLEDLPN")
    private String allowCommingledLpn;



    @TableField(value = "DEFAULTPUTAWAYSTRATEGY")
    private String defaultPutawayStrategy;




    @TableField(value = "DEFAULTRETURNSLOC")
    private String defaultReturnsLoc;


    @TableField(value = "DEFAULTQCLOC")
    private String defaultQcLoc;


    @TableField(value = "SKUSETUPREQUIRED")
    private String skuSetupRequired;


    @TableField(value = "ADDRESS5")
    private String address5;


    @TableField(value = "ADDRESS6")
    private String address6;


    @TableField(value = "LOCALE")
    private String locale;


    @TableField(value = "EXTERNSTORERKEY")
    private String externStorerKey;


    @TableField(value = "DEFAULTNEWALLOCATIONSTRATEGY")
    private String defaultNewAllocationStrategy;


    @TableField(value = "DEFAULTLOTTABLEVALIDATIONKEY")
    private String defaultLottableValidationKey;


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


    @TableField(value = "EXT_UDF_STR31")
    private String extUdfStr31;


    @TableField(value = "EXT_UDF_STR32")
    private String extUdfStr32;


    @TableField(value = "EXT_UDF_STR33")
    private String extUdfStr33;


    @TableField(value = "EXT_UDF_STR34")
    private String extUdfStr34;


    @TableField(value = "EXT_UDF_DATE1")
    private LocalDateTime extUdfDate1;


    @TableField(value = "EXT_UDF_DATE2")
    private LocalDateTime extUdfDate2;


    @TableField(value = "EXT_UDF_DATE3")
    private LocalDateTime extUdfDate3;


    @TableField(value = "EXT_UDF_DATE4")
    private LocalDateTime extUdfDate4;


    @TableField(value = "EXT_UDF_DATE5")
    private LocalDateTime extUdfDate5;


    @TableField(value = "EXT_UDF_DATE6")
    private LocalDateTime extUdfDate6;


    @TableField(value = "EXT_UDF_DATE7")
    private LocalDateTime extUdfDate7;


    @TableField(value = "EXT_UDF_DATE8")
    private LocalDateTime extUdfDate8;


    @TableField(value = "EXT_UDF_DATE9")
    private LocalDateTime extUdfDate9;


    @TableField(value = "EXT_UDF_DATE10")
    private LocalDateTime extUdfDate10;


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