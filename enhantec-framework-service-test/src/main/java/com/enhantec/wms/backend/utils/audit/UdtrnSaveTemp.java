package com.enhantec.wms.backend.utils.audit;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;
import java.util.HashMap;

public class UdtrnSaveTemp  extends LegacyBaseService
{



	/**

	 
	--注册方法


	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE THEPROCNAME = 'UdtrnSaveTemp';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('UdtrnSaveTemp', 'com.enhantec.sce.utils.audit', 'enhantec', 'UdtrnSaveTemp', 'TRUE',  'JOHN', 'JOHN'	, 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,type,bioname,fields,notes,ESIGNATUREKEY','0.10','0');

	 */


	private static final long serialVersionUID = 1L;

	public UdtrnSaveTemp()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		//public RrDateTime currentDate;
		//this.currentDate = UtilHelper.getCurrentDate();

		
		String userid = EHContextHelper.getUser().getUsername();



		try
		{
			String SERIALKEY="";			
		    String type= serviceDataHolder.getInputDataAsMap().getString( "type");
		    String bioname= serviceDataHolder.getInputDataAsMap().getString( "bioname");
			String fields= serviceDataHolder.getInputDataAsMap().getString( "fields");
			String notes= serviceDataHolder.getInputDataAsMap().getString( "notes");
			String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString( "ESIGNATUREKEY");
			
			String[] aFields=fields.split("~~~");
			String[] aFieldName=new String[aFields.length];
			String[] aFieldOld=new String[aFields.length];
			String[] aFieldNew=new String[aFields.length];
			for(int i1=0;i1<aFields.length;i1++)
			{
				String[] aValues=aFields[i1].split(";;;");
				aFieldName[i1]=aValues[0];
				if (("[BLANK]").equalsIgnoreCase(aValues[1])){
					aFieldOld[i1]="";
				}else {
					aFieldOld[i1]=aValues[1];
				}
				if (("[BLANK]").equalsIgnoreCase(aValues[1])){
					aFieldNew[i1]="";
				}else {
					aFieldNew[i1]=aValues[2];
				}
			}
			
			String typename=type;
			Map<String,String> mBIO= DBHelper.getRecord( "SELECT BIONAME, TABLENAME, SAVETYPE, DELTYPE, LOTFIELD, KEYFIELD, KEYFIELDLABEL FROM UDTRN_TABLE WHERE BIONAME=?", new String[] {bioname});
			if (mBIO.isEmpty()) throw new Exception("BIO("+bioname+")未注册日志信息");
			if (type.equals("SAVE")) typename=mBIO.get("SAVETYPE");
			if (type.equals("DELETE")) typename=mBIO.get("DELTYPE");
			
			int count=0;
			Map<String,String> tempUdtrn=null;
			for(int i1=0;i1<aFieldName.length;i1++)
			{
				if ((!aFieldOld[i1].equals(aFieldNew[i1]))||(type.equals("DELETE")))
				{
					//一行最多插入30个字段，超过再新建一条记录
					if ((count==0)||(count>=30))
					{
						if (tempUdtrn!=null) LegacyDBHelper.ExecInsert( "UDTRN_TEMP", tempUdtrn);
						count=0;
						tempUdtrn=new HashMap<String,String>();
						String SEQ_UDTRN= String.valueOf(IdGenerationHelper.getNCounter( "UDTRN_TEMP"));
						if (!SERIALKEY.equals("")) SERIALKEY+=",";
						SERIALKEY+=SEQ_UDTRN;
						tempUdtrn.put("SERIALKEY", SEQ_UDTRN);
						tempUdtrn.put("ADDWHO", userid);
						tempUdtrn.put("EDITWHO", userid);
						tempUdtrn.put("ESIGNATUREKEY", ESIGNATUREKEY);
						tempUdtrn.put("FROMTYPE",typename);//从配置表中获取修改或删除操作的中文描述
						tempUdtrn.put("FROMTABLENAME",mBIO.get("TABLENAME"));
						//FROMKEY存储LOT属性，没有则忽略
						tempUdtrn.put("FROMKEY", GetFieldValue(aFieldName,type.equals("DELETE")?aFieldOld:aFieldNew,mBIO.get("LOTFIELD")));
						//FROMKEY1LABEL存储这个BIO重要的KEY属性列表的中文名称
						tempUdtrn.put("FROMKEY1LABEL", mBIO.get("KEYFIELDLABEL"));
						//FROMKEY1存储这个BIO重要的KEY属性列表的值
						String[] keyFieldNames= mBIO.get("KEYFIELD").split(",");
						String keyFieldValuesStr="";
						//为每个新记录赋予所有KEY值
						for(int i15=0;i15<keyFieldNames.length;i15++)
						{
							if (i15>0) keyFieldValuesStr+=",";
							keyFieldValuesStr+=GetFieldValue(aFieldName,type.equals("DELETE")?aFieldOld:aFieldNew,keyFieldNames[i15]);
						}
						tempUdtrn.put("FROMKEY1", keyFieldValuesStr);
						
//						tempUdtrn.put("FROMKEY3LABEL", "备注");
//						tempUdtrn.put("FROMKEY3", notes);
						
					}
					count++;
					if (count<=30)
					{
						//将变化的值记录下来，最多记录30个变化的值
						tempUdtrn.put("TITLE"+ LegecyUtilHelper.To_Char(count, 2), aFieldName[i1]);
						if (type.equals("DELETE"))
							tempUdtrn.put("CONTENT"+ LegecyUtilHelper.To_Char(count, 2), aFieldOld[i1]);
						else
							tempUdtrn.put("CONTENT"+ LegecyUtilHelper.To_Char(count, 2), "原值="+aFieldOld[i1]+";新值="+aFieldNew[i1]);
					}
				}

			}
			//最后将剩余的凑不足30个的变化字段保存下来
			if (tempUdtrn!=null) LegacyDBHelper.ExecInsert( "UDTRN_TEMP", tempUdtrn);
			
			/*
			Map<String,String> mTRAN=new HashMap<String,String>();
			String SEQ_UDTRN=Integer.toString(XtSql.GetSeq( "SEQ_UDTRN"));
			mTRAN.put("ADDWHO", userid);
			mTRAN.put("EDITWHO", userid);
			mTRAN.put("FROMTYPE", GetFieldValue(aFieldName,aFieldNew,mBIO.get("LOTFIELD")));
			mTRAN.put("FROMKEY1LABEL", mBIO.get("KEYFIELDLABEL"));
			String[] KEYFIELD= mBIO.get("KEYFIELD").split(",");
			String KEYFIELD1="";
			for(int i1=0;i1<KEYFIELD.length;i1++)
			{
				if (i1>0) KEYFIELD1+=",";
				KEYFIELD1+=GetFieldValue(aFieldName,aFieldNew,KEYFIELD[i1]);
			}
			mTRAN.put("FROMKEY1", KEYFIELD1);
			
			mTRAN.put("FROMKEY3LABEL", "备注");
			mTRAN.put("FROMKEY3", notes);
			
			int iField=0;
			for(int i1=0;i1<aFieldName.length;i1++)
			{
				if ((!aFieldOld[i1].equals(aFieldNew[i1]))||(type.equals("DELETE")))
				{
					iField++;
					if (iField<=30)
					{
						mTRAN.put("TITLE"+XtUtils.To_Char(iField, 2), aFieldName[i1]);
						if (type.equals("DELETE"))
							mTRAN.put("CONTENT"+XtUtils.To_Char(iField, 2), aFieldOld[i1]);
						else
							mTRAN.put("CONTENT"+XtUtils.To_Char(iField, 2), "原值="+aFieldOld[i1]+";新值="+aFieldNew[i1]);
					}
				}
			}
			XtSql.ExecInsert( "UDTRN_TEMP", mTRAN);
			*/


			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("SERIALKEY", SERIALKEY);
			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);
			
		}
		catch (Exception e)
		{
			if ( e instanceof FulfillLogicException)
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
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