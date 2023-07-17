package com.enhantec.wms.backend.utils.audit;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.*;
import com.enhantec.framework.common.utils.EHContextHelper;

public class UdtrnSaveCommit extends LegacyBaseService
{

	/**
	--注册方法

	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE THEPROCNAME = 'UdtrnSaveCommit';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('UdtrnSaveCommit', 'com.enhantec.sce.utils.audit', 'enhantec', 'UdtrnSaveCommit', 'TRUE',  'JOHN', 'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,SERIALKEY','0.10','0');

	 */
	private static final long serialVersionUID = 1L;

	public UdtrnSaveCommit()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		//public RrDateTime currentDate;
		//this.currentDate = UtilHelper.getCurrentDate();

		String userid = EHContextHelper.getUser().getUsername();



		try
		{
		    String SERIALKEY= serviceDataHolder.getInputDataAsMap().getString("SERIALKEY");

		    if(UtilHelper.isEmpty(SERIALKEY)) ExceptionHelper.throwRfFulfillLogicException("数据未发生变化，无需保存");
		    
		    String[] aSERIALKEY=SERIALKEY.split(",");
		    
		    String FIELDS=" ESIGNATUREKEY, WHSEID, FROMTYPE, FROMTABLENAME, FROMKEY, FROMKEY1LABEL, FROMKEY2LABEL, FROMKEY3LABEL, FROMKEY1, FROMKEY2, FROMKEY3, TITLE01, CONTENT01, TITLE02, CONTENT02, TITLE03, CONTENT03, TITLE04, CONTENT04, TITLE05, CONTENT05, TITLE06, CONTENT06, TITLE07, CONTENT07, TITLE08, CONTENT08, TITLE09, CONTENT09, TITLE10, CONTENT10, TITLE11, CONTENT11, TITLE12, CONTENT12, TITLE13, CONTENT13, TITLE14, CONTENT14, TITLE15, CONTENT15, TITLE16, CONTENT16, TITLE17, CONTENT17, TITLE18, CONTENT18, TITLE19, CONTENT19, TITLE20, CONTENT20, TITLE21, CONTENT21, TITLE22, CONTENT22, TITLE23, CONTENT23, TITLE24, CONTENT24, TITLE25, CONTENT25, TITLE26, CONTENT26, TITLE27, CONTENT27, TITLE28, CONTENT28, TITLE29, CONTENT29, TITLE30, CONTENT30, ADDDATE, ADDWHO, EDITDATE, EDITWHO";
		    
		    for(int i1=0;i1<aSERIALKEY.length;i1++)
		    {
		    	DBHelper.executeUpdate( "INSERT INTO UDTRN("+FIELDS+") SELECT "+FIELDS+" FROM UDTRN_TEMP WHERE SERIALKEY=?", new String[] {aSERIALKEY[i1]});
		    	DBHelper.executeUpdate( "DELETE UDTRN_TEMP WHERE SERIALKEY=?", new String[] {aSERIALKEY[i1]});
		    }

			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("SERIALKEY", SERIALKEY);
			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);
			
		}
		catch (Exception e)
		{
			if ( e instanceof FulfillLogicException )
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException( e.getMessage());
		}finally {
			
		}


		
	}
	
	private String GetFieldValue(String[] aFieldName,String[] aFieldNew,String Key)
	{
		for(int i1=0;i1<aFieldName.length;i1++)
		{
			if (aFieldName[i1].equals(Key)) return aFieldNew[i1];
		}
		return "";
	}
	
	
	
}