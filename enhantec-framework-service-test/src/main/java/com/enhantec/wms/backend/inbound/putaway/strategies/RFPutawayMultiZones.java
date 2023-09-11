/*******************************************************************************
 *                                     NOTICE                            
 *          						      	       
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS             
 *             CONFIDENTIAL INFORMATION OF INFOR AND/OR ITS AFFILIATES   
 *             OR SUBSIDIARIES AND SHALL NOT BE DISCLOSED WITHOUT PRIOR  
 *             WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND       
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH  
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.            
 *             ALL OTHER RIGHTS RESERVED.			       	       
 *                                                                       
 *             (c) COPYRIGHT 2012 INFOR.  ALL RIGHTS RESERVED.           
 *             THE WORD AND DESIGN MARKS SET FORTH HEREIN ARE            
 *             TRADEMARKS AND/OR REGISTERED TRADEMARKS OF INFOR          
 *             AND/OR ITS AFFILIATES AND SUBSIDIARIES. ALL RIGHTS        
 *             RESERVED.  ALL OTHER TRADEMARKS LISTED HEREIN ARE         
 *             THE PROPERTY OF THEIR RESPECTIVE OWNERS.     
 *******************************************************************************/
package com.enhantec.wms.backend.inbound.putaway.strategies;

import com.enhantec.wms.backend.framework.WMSBaseService;import com.enhantec.framework.common.utils.EHContextHelper;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.List;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.18.2.1 $
 */
@Service("rfPutawayMultiZones")
public class RFPutawayMultiZones extends WMSBaseService {
//
//	private static Logger logger = Logger.getLogger(RFPutawayMultiZones.class);

	public String currentUser;

	public RFPutawayMultiZones() {
		super();

	}

	public void execute(ServiceDataHolder serviceDataHolder) {


		currentUser = EHContextHelper.getUser().getUsername();

		String zones = serviceDataHolder.getInputDataAsMap().getString("zones");

		String suggestedLoc = "UNKNOWN";
		String suggestedzone = "";

		List<String> searchZoneArray =null;


		if(zones ==null ||zones.isEmpty()){
			ExceptionHelper.throwRfFulfillLogicException("未获取到物料上架区列表");
		}else{
			searchZoneArray =  Arrays.asList(zones.split(","));
		}


		for(String zone : searchZoneArray) {

			List<Map<String,String>>  res = DBHelper.executeQuery(
					" SELECT * FROM putawayzone WHERE putawayzone = ?",
					Arrays.asList(zone)
			);
				if(res.size()==0)  ExceptionHelper.throwRfFulfillLogicException("未在系统中找到配置的上架区"+zone);
		}

		for(String searchZone: searchZoneArray) {

			serviceDataHolder.getInputDataAsMap().setAttribValue("searchZone", searchZone);

			 ServiceHelper.executeService( "RFPutawayP1S1Wrapper", serviceDataHolder);

			String toLoc = serviceDataHolder.getOutputDataAsMap().getString("ToLoc");

			if(!"UNKNOWN".equals(toLoc) && !toLoc.isEmpty()){
				suggestedLoc = toLoc;
				suggestedzone = searchZone;
				break;
			}
		}

		serviceDataHolder.getOutputDataAsMap().setAttribValue("loc", suggestedLoc);
		serviceDataHolder.getOutputDataAsMap().setAttribValue("zone", suggestedzone);

	}


	@Deprecated
	public String getPutawayLoc( ServiceDataMap pDO) {
		String result;
		try {
//			logger.debug("starting getPutawayLoc( EXEDataObject pDO)");
//			context.theEXEDataObjectStack.push(pDO);
//			Process RFPUTAWAY = context.searchObjectLibrary("NSPRFPUTAWAY"));
//			context = ()RFPUTAWAY.execute();
//			EXEDataObject theReturnDO = (EXEDataObject)context.theEXEDataObjectStack.stackList.get(1);
//			String toLoc = null;
//			if (theReturnDO.isState(EXEConstantsConstants.STATE_OK)) {
//				EXEDataObject.GetStringOutputParam qqGetStringOutputParam = theReturnDO.getString("ToLoc"), toLoc);
//				toLoc = qqGetStringOutputParam.pResult;
//			}

//			result = toLoc;
		} finally {
//			logger.debug("leaving getPutawayLoc( EXEDataObject pDO)");
		}

		throw new RuntimeException("not implement");
	}



}
