/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 *
 *******************************************************************************/

package com.enhantec.wms.backend.core;

public class WMSServiceNames {
    public static final String CORE_PREFIX = "core-";

    public static final String INBOUND_CLOSE_ASN = CORE_PREFIX + "inbound-closeASNService";
    public static final String INBOUND_RECEIVING_BY_ID = CORE_PREFIX + "inbound-receivingById";
    public static final String INV_SINGLE_LOT_ID_MOVE = CORE_PREFIX + "inventory-singleLotLpnMove";
    public static final String INV_HOLD = CORE_PREFIX + "inventory-hold";
    public static final String INV_INTERNAL_TRANSFER = CORE_PREFIX + "inventory-internalTransfer";
    public static final String OUTBOUND_SINGLE_LOT_ID_PICK = CORE_PREFIX + "outbound-singleLotLpnPick";
    public static final String OUTBOUND_ADD_SINGLE_LOT_ID_PD = CORE_PREFIX + "outbound-addSingleLotLpnPickDetail";
    public static final String OUTBOUND_REMOVE_SINGLE_LOT_ID_PD = CORE_PREFIX + "outbound-removeSingleLotLpnPickDetail";
    public static final String OUTBOUND_SHIP_BY_ID = CORE_PREFIX + "outbound-shipById";
    public static final String OUTBOUND_SHIP_BY_ORDER = CORE_PREFIX + "outbound-shipByOrder";
    public static final String OUTBOUND_ALLOCATE_ORDER = CORE_PREFIX + "outbound-allocateOrder";
}
