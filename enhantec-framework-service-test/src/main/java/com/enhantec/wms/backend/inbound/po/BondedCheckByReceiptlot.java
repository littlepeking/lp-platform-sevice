package com.enhantec.wms.backend.inbound.po;

import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import java.util.Map;

public class BondedCheckByReceiptlot extends WMSBaseService
{



	/**
	 *   追加保税检查 当多po生成一个ASN时生成多条收货检查记录 做一个保税检查时收货检查同步更新
	--注册方法

	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE COMPOSITE='AddReceiptLotByPO';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('BondedCheckByReceiptlot', 'com.enhantec.sce.inbound.po', 'enhantec', 'BondedCheckByReceiptlot', 'TRUE', 'JOHN', 'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,SERIALKEY,ESIGNATUREKEY','0.10','0');

	 */


	private static final long serialVersionUID = 1L;

	public BondedCheckByReceiptlot()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{

		try
		{
		    String serialkey= serviceDataHolder.getInputDataAsMap().getString("SERIALKEY");
			//String receiptLot=DBHelper.getValue("select  RECEIPTLOT from prreceiptcheck where SERIALKEY=?",new Object[]{serialkey)},"收货检查记录");;
			String count = DBHelper.getValue("select COUNT(*) from prereceiptcheck where RECEIPTLOT in (select  RECEIPTLOT from prereceiptcheck where SERIALKEY=?)",new Object[]{serialkey},"收货检查记录");
			if (Integer.parseInt(count)>1) {
			Map<String, String> bondedCheck = DBHelper.getRecord( "SELECT BONDEDCHECK,BONDEDNOTES4,BONDEDRECQTY,BONDEDSTORES,BONDEDUOM,LOTTABLE10,MHLINENO,MHTASKKEY,RECEIPTLOT " +
							" FROM PRERECEIPTCHECK WHERE RECEIPTLOT in (select  RECEIPTLOT from prereceiptcheck where SERIALKEY=?) and not(BONDEDSTORES is NULL)", new String[]{serialkey},"");
			DBHelper.executeUpdate( "update prereceiptcheck set BONDEDCHECK=?,BONDEDRECQTY=?,BONDEDSTORES=?,BONDEDUOM=?," +
							"LOTTABLE10=?,MHLINENO=?,MHTASKKEY=? " +
							"where RECEIPTLOT=? "
					, new String[]{bondedCheck.get("BONDEDCHECK"),
							bondedCheck.get("BONDEDRECQTY"),bondedCheck.get("BONDEDSTORES"),
							bondedCheck.get("BONDEDUOM"),bondedCheck.get("LOTTABLE10"),
							bondedCheck.get("MHLINENO"),bondedCheck.get("MHTASKKEY"),
							bondedCheck.get("RECEIPTLOT")
							});
			}


			ServiceDataMap theOutDO = new ServiceDataMap();
			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);

		}
		catch (Exception e) {
			if ( e instanceof FulfillLogicException)
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
		}
	}

}