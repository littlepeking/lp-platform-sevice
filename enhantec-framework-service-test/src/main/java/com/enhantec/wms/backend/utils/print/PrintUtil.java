package com.enhantec.wms.backend.utils.print;

import com.alibaba.fastjson.JSON;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintUtil {

//    private static final ILogger SCE_LOGGER = SCELoggerFactory.getInstance(PrintUtil.class);

    public static List<HashMap<String,String>> getData(Context context, String labelName, HashMap<String,String> sqlParams)  {

//        SCE_LOGGER.info(" LabelName: {}", labelName);

        List<HashMap<String,String>> labelData =new ArrayList<>();

        String tmpText = "";

        if (labelName != null) {

          if(sqlParams==null || sqlParams.size()==0) ExceptionHelper.throwRfFulfillLogicException("标签SQL绑定参数不允许为空");

          String dataSourceStr = String.valueOf(DBHelper.getValue(context," SELECT DataSource FROM LabelConfig WHERE LabelName = ? ", new Object[]{labelName},"标签配置"+labelName));

          if(UtilHelper.isEmpty(dataSourceStr)) ExceptionHelper.throwRfFulfillLogicException("请到标签配置中提供"+labelName+"标签数据源信息后再打印");

          if(dataSourceStr.contains("*")) ExceptionHelper.throwRfFulfillLogicException("标签数据源不允许包含*,请指定明确的列名");
          if(!dataSourceStr.toUpperCase().contains("SELECT")) ExceptionHelper.throwRfFulfillLogicException("SQL语句非法, 缺少SELECT关键字");
          if(!dataSourceStr.toUpperCase().contains("FROM")) ExceptionHelper.throwRfFulfillLogicException("SQL语句非法, 缺少FROM关键字");
          if(!dataSourceStr.toUpperCase().contains("WHERE")) ExceptionHelper.throwRfFulfillLogicException("SQL语句非法, 缺少WHERE关键字");


//          SCE_LOGGER.info(" DataSource: {} FROM LabelConfig for LabelName = {}", dataSourceStr, labelName);

//todo: fix following logic

//            dataSourceStr.moveToString("WHERE", true, true);
//
//          tmpText.setValue(dataSource.copyRange(0, dataSource.getOffset()));

          List<String> params = new ArrayList<>();

          sqlParams.entrySet().stream().forEach(e->{
              if(!UtilHelper.isEmpty(e.getValue())){
                  tmpText.concat(" "+ e.getKey() + " = ? AND ");
                  params.add(e.getValue());
              }
          });

//          tmpText.concat(dataSource.copyRange(dataSource.getOffset() + 1));

          try {
              labelData = DBHelper.executeQuery(context, tmpText, params.toArray(new Object[]{}));
          }catch (Exception e){
              ExceptionHelper.throwRfFulfillLogicException("标签数据源获取失败，请检查标签"+labelName+"的SQL语句是否书写正确:"+e.getMessage());
          }
        }

        return labelData;

    }

    //PRINTSTATUS -1 缓存后续打印 0 正在打印
    public static void printLabel(Context context, String printer, String labelTempName, String labelName, String copies, String notes, List<HashMap<String,String>> data, String key) throws Exception {
        if(UtilHelper.isEmpty(printer)) throw new Exception("打印机不允许为空");
        //PRINTER=-1 不打印 PRINTER = 0 缓存打印 其他 = 直接打印
        if("-1".equals(printer)) return;

        int copyNum = Integer.valueOf(UtilHelper.isEmpty(copies)? "1": copies);

        if (data == null || data.size()==0)
            ExceptionHelper.throwRfFulfillLogicException("未找到打印数据");

        //int TaskID=XtSql.GetSeq(context, "SEQ_PRINT_TASK");
        String PRINTSTATUS = "0";
        if (printer.equals("0")) PRINTSTATUS = "-1";
        else {
            String paperSpec = CodeLookup.getCodeLookupValue(context, "PRINTER", printer, "UDF5", "打印配置");

            if (!UtilHelper.isEmpty(paperSpec)) {
                labelTempName += "_" + paperSpec;
            } else {
                ExceptionHelper.throwRfFulfillLogicException("标签打印:纸张规格不允许为空");
                //labelName += "_DEFAULT";
            }
        }


        String user = context.getUserID();

        String PRINTFIELD = "";
        for (Map.Entry<String, String> entry : data.get(0).entrySet()) {
            String mapKey = entry.getKey();
            if (!PRINTFIELD.equals("")) PRINTFIELD += ",";
            PRINTFIELD += mapKey;
        }

        try {
            for (int i1 = 0; i1 < data.size(); i1++) {

                StringBuffer bf = new StringBuffer();
                bf.append("[");
                //if (i1 > 0) bf.append(",");
                String mapJson = JSON.toJSONString(data.get(i1));
                bf.append(mapJson);
                bf.append("]");
                for(int i=0;i<copyNum;i++) {
                    DBHelper.executeUpdate(context, "Insert Into PRINT_TASK(AddWho,EditWho"
                                    + ",REPORTNAME,LABELNAME,PRINTER,NOTE"
                                    + ",PRINTFIELD"
                                    + ",PRINTWHO,PRINTSTATUS,PRINTDATA,[KEY])"
                                    + " values(?,?"
                                    + ",?,?,?"
                                    + ",?"
                                    + ",?,?,?,?,?)"
                            , new Object[]{user, user
                                    , labelTempName,labelName, printer, notes
                                    , PRINTFIELD
                                    , user, PRINTSTATUS, bf.toString(),key});
                }
            }



        }catch (Exception e){
            ExceptionHelper.throwRfFulfillLogicException("打印任务生成失败: "+e.getMessage());

        }finally {
            
        }




    }


}
