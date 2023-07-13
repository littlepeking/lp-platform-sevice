package com.enhantec.wms.backend.outbound;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.LegecyUtilHelper;

import java.sql.Connection;
import java.util.LinkedHashMap;

public class QcSamplingLpnDelete extends LegacyBaseService
{



	/**
	john
	--注册方法
	delete from wmsadmin.sproceduremap where COMPOSITE='QcSamplingLpnDelete';
	insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
	values ('QcSamplingLpnDelete', 'com.enhantec.sce.outbound.order', 'enhantec', 'QcSamplingLpnDelete', 'TRUE',  'john',  'john', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,LOTTABLE06,ORDERKEY,ORDERTYPE,ORDERLINENUMBER,LPN,LOC,ESIGNATUREKEY','0.10','0');
	*/
	

	private static final long serialVersionUID = 1L;

	public QcSamplingLpnDelete()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		
		String userid = context.getUserID();

		Connection conn = context.getConnection();

		try
		{
			
			String LOTTABLE06= serviceDataHolder.getInputDataAsMap().getString( "LOTTABLE06");
		    String ORDERKEY= serviceDataHolder.getInputDataAsMap().getString( "ORDERKEY");
		    String ORDERTYPE= serviceDataHolder.getInputDataAsMap().getString( "ORDERTYPE");
		    String ORDERLINENUMBER= serviceDataHolder.getInputDataAsMap().getString( "ORDERLINENUMBER");
		    String LPN= serviceDataHolder.getInputDataAsMap().getString( "LPN");
			String LOC= serviceDataHolder.getInputDataAsMap().getString( "LOC");
			String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString( "ESIGNATUREKEY");
			String TOTAL1="";
			String TOTAL2="";
			String TOTAL3="";
			
			
			String Status= LegacyDBHelper.GetValue(context, conn
					, "select Status from orders where orderkey=? and ohtype=?", new String[]{ORDERKEY,ORDERTYPE}, "");
			if (LegecyUtilHelper.isNull(Status)) throw new Exception("未找到在库取样单("+ORDERKEY+")");
			if (Status.compareTo("09")>0)  throw new Exception("在库取样单("+ORDERKEY+")已关闭或扣量,不能继续操作");
				
			LinkedHashMap<String,String> mORDER= LegacyDBHelper.GetValueMap(context, conn, "SELECT EXTERNLINENO,SKU,SUSR1,SUSR2,SUSR3 FROM ORDERDETAIL WHERE ORDERKEY=? AND ORDERLINENUMBER=?", new String[]{ORDERKEY,ORDERLINENUMBER});
			if (mORDER.isEmpty()) throw new Exception("未找到记录,是确认否重复删除");
			
			LegacyDBHelper.ExecSql(context, conn, "DELETE ORDERDETAIL WHERE ORDERKEY=? AND ORDERLINENUMBER=?", new String[]{ORDERKEY,ORDERLINENUMBER});

			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=ESIGNATUREKEY;
			UDTRN.FROMTYPE="在库取样-删除取样记录";
			UDTRN.FROMTABLENAME="ORDERDETAIL";
		    UDTRN.FROMKEY=LOTTABLE06;
		    UDTRN.FROMKEY1=ORDERKEY;
		    UDTRN.FROMKEY2=ORDERLINENUMBER;
		    UDTRN.FROMKEY3="";
		    UDTRN.TITLE01="取样批次";    UDTRN.CONTENT01=LOTTABLE06;
		    UDTRN.TITLE02="出库单号";    UDTRN.CONTENT02=ORDERKEY;
		    UDTRN.TITLE03="出库单行号";    UDTRN.CONTENT03=ORDERLINENUMBER;
		    UDTRN.TITLE04="SKU";    UDTRN.CONTENT04=mORDER.get("SKU");
		    UDTRN.TITLE05="容器条码";    UDTRN.CONTENT05=LPN;
		    //UDTRN.TITLE06="PROJECTCODE";    UDTRN.CONTENT06=mORDER.get("EXTERNLINENO");
		    UDTRN.TITLE07="取样量";    UDTRN.CONTENT07=mORDER.get("SUSR1");
		    UDTRN.TITLE08="扣样量";    UDTRN.CONTENT08=mORDER.get("SUSR2");
		    UDTRN.TITLE09="取样库位";    UDTRN.CONTENT09=mORDER.get("SUSR3");
		    UDTRN.Insert(context, conn, userid);
			
				
			//----------------
			TOTAL1= LegacyDBHelper.GetValue(context, conn, "select SUM(CONVERT(decimal(11,5), SUSR1)) from ORDERDETAIL where orderkey=?",new String[]{ORDERKEY},"0");
			TOTAL3= LegacyDBHelper.GetValue(context, conn, "SELECT SUM(CONVERT(decimal(11,5), OPENQTY)) from ORDERDETAIL where orderkey=?",new String[]{ORDERKEY},"0");


			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("TOTAL1", TOTAL1);//取样量合计
			theOutDO.setAttribValue("TOTAL3", TOTAL3);//扣样量合计

			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);

			
		}
		catch (Exception e)
		{
			try
			{
				context.releaseConnection(conn);
			}	catch (Exception e1) {		}
			if ( e instanceof FulfillLogicException)
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
		}finally {
			try	{	context.releaseConnection(conn); }	catch (Exception e1) {		}
		}

	}
}