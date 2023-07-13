package com.enhantec.wms.backend.utils.print;

import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class PrintByPreTask extends LegacyBaseService
{

	/**
	--注册方法
	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHPrintByPreTask';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('EHPrintByPreTask', 'com.enhantec.sce.utils.print', 'enhantec', 'PrintByPreTask', 'TRUE',  'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,USER,PRINTER,ESIGNATUREKEY','0.10','0');
	 */
	

	private static final long serialVersionUID = 1L;

	public PrintByPreTask()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		//public RrDateTime currentDate;
		//this.currentDate = UtilHelper.getCurrentDate();

		
		String userid = context.getUserID();

		Connection conn = context.getConnection();

	    String USER= serviceDataHolder.getInputDataAsMap().getString( "USER");
	    String PRINTER= serviceDataHolder.getInputDataAsMap().getString( "PRINTER");
		String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString( "ESIGNATUREKEY");
		try
		{
			if (PRINTER.equals("0")) throw new Exception("不能选择缓存打印机");

			int i0= Integer.valueOf(DBHelper.getValue(context, conn, "select count(1) from LABELPRINTER where PRINTERNAME=?",
					new Object[]{PRINTER},"打印机配置").toString());

			if (i0==0)  throw new Exception("系统未注册您选择的打印机");

			String paperSpec = CodeLookup.getCodeLookupValue(context,conn,"PRINTER",PRINTER,"UDF5","打印配置");

			String labelSuffix = "";
			if(!UtilHelper.isEmpty(paperSpec)) {
				labelSuffix += "_" + paperSpec;
			}else{
				ExceptionHelper.throwRfFulfillLogicException("标签打印:纸张规格不允许为空");
				//labelSuffix += "_DEFAULT";
			}

			List<HashMap<String,String>> TASKS=DBHelper.executeQuery(context, conn
					, "select TASKID from PRINT_TASK where PRINTWHO=? and PRINTSTATUS=? ORDER BY TASKID"
					, new Object[]{USER, "-1"});
			if (TASKS.size()==0) throw new Exception("未找到打印任务");

			DBHelper.executeUpdate(context, conn, "update PRINT_TASK set PRINTER=?, REPORTNAME = CONCAT(REPORTNAME, ?),  PRINTSTATUS=?,EDITWHO=?,EDITDATE=? WHERE PRINTSTATUS=? and PRINTWHO=?"
					, new Object[]{PRINTER,labelSuffix, "0",userid,UtilHelper.getCurrentSqlDate(),"-1",USER});

		}
		catch (Exception e)
		{
			if ( e instanceof FulfillLogicException)
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
		}finally {
			try{context.releaseConnection(conn);}  catch(Exception e2){}
		}

		ServiceDataMap theOutDO = new ServiceDataMap();
		theOutDO.setAttribValue("TOTAL", "0");
		serviceDataHolder.setReturnCode(1);
		serviceDataHolder.setOutputData(theOutDO);


		
	}
}