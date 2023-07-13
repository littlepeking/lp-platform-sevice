package com.enhantec.wms.backend.utils.common;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;


public class LocaleHelper {
    private final static String language = "zh-CN"; //todo Task.getLocale();

    private final static String ehProperties= "EH";
    private final static String inforProperties= "WM40";
    public static Map loadLocale=new Hashtable();
    public static String getEHLocaleMsg(int levelnum,int msgnum,String defaultmsg){
        Properties properties = (Properties) loadLocale.get(ehProperties+language+"properties");
        if (properties==null){
            properties=new Properties();
            try {
                properties.load(LocaleHelper.class.getResourceAsStream("/" + ehProperties + "_"+language+".properties"));
                loadLocale.put(ehProperties+language+"properties",properties);
                String msg=(String) properties.get(levelnum+ "-"+msgnum);
                if(msg!=null&&msg .length()>0){
                    return msg;
                }else {
                    return getEHLocale(levelnum,msgnum,defaultmsg);
                }
            }catch (Exception e){
                return getEHLocale(levelnum,msgnum,defaultmsg);
            }
        }else {
            String msg=(String) properties.get(levelnum+"-"+msgnum);
            if(msg!=null&&msg .length()>0){
                return msg;
            }else {
                return getEHLocale(levelnum,msgnum,defaultmsg);
            }
        }


    }
    private static String getEHLocale(int levelnum,int msgnum,String defaultmsg){

        throw new RuntimeException("nto implement");

    }

}









