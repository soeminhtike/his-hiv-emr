/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.kenyaemr.calculation.library.hiv.art;

import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.BooleanResult;
import org.openmrs.module.kenyacore.calculation.Filters;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
import org.openmrs.module.kenyaemr.model.DrugOrderProcessed;
import org.openmrs.module.kenyaemr.regimen.RegimenOrder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculates whether patients are on alternative first-line ART regimens, i.e. have they changed regimen
 */
public class OnOriginalFirstLineArtCalculation extends AbstractPatientCalculation {

	/**
	 * @see org.openmrs.calculation.patient.PatientCalculation#evaluate(java.util.Collection, java.util.Map, org.openmrs.calculation.patient.PatientCalculationContext)
	 * @should return null for patients who have never started ARVs
	 * @should return null for patients who aren't currently on ARVs
	 * @should return whether patients have changed regimens
	 */
	@Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> arg1, PatientCalculationContext context) {

		
		Set<Integer> alive = Filters.alive(cohort, context);
		CalculationResultMap ret = new CalculationResultMap();
		for (Integer ptId : alive) {
			boolean onOrigFirstLine = false;
			 KenyaEmrService kenyaEmrService = (KenyaEmrService) Context.getService(KenyaEmrService.class);
		 	   List<DrugOrderProcessed> drugorderprocess = kenyaEmrService.getAllfirstLine();
		 	  DrugOrderProcessed drugorder = new DrugOrderProcessed();
		 	   {
		 	  for(DrugOrderProcessed order:drugorderprocess)
		 	  {
		 		 if(order.getPatient().getAge()<=14)
		 		 {
		 	  
		 	  if((ptId.equals(order.getPatient().getPatientId())&&(order.getRegimenChangeType().equals("Start")) &&(order.getTypeOfRegimen().equals("Fixed dose combinations (FDCs)"))))
		 	  { if(order.getDrugRegimen().equals(drugorder.getDrugRegimen()))
		 		  { 
		 			 onOrigFirstLine = false;
		 			
		 		  }
		 		  else
		 		  {  onOrigFirstLine = true;
		 			drugorder=order;
		 			ret.put(ptId, new BooleanResult(onOrigFirstLine, this, context));
		 		  }
		 		 if(order.getDiscontinuedDate()!=null)
			 	  { 
			 		 onOrigFirstLine=false; 
			 		ret.put(ptId, new BooleanResult(onOrigFirstLine, this, context));
			 	  }
		 		
		 	  }
		 	  }
		 	 
		 	  else
		 	  {
		 		 if((ptId.equals(order.getPatient().getPatientId()) &&(order.getRegimenChangeType().equals("Start")) &&(order.getTypeOfRegimen().equals("First line Anti-retoviral drugs"))))
		 		 { 
		 			onOrigFirstLine=true; 
		 			ret.put(ptId, new BooleanResult(onOrigFirstLine, this, context));
		 			 if(order.getDiscontinuedDate()!=null)
				 	  { 
				 		 onOrigFirstLine=false; 
				 		ret.put(ptId, new BooleanResult(onOrigFirstLine, this, context));
				 	  }
		 		 }
		 	 
		 		 else 
		 		 {
		 			 if((ptId.equals(order.getPatient().getPatientId())&&(order.getRegimenChangeType().equals("Start")) &&(order.getTypeOfRegimen().equals("Fixed dose combinations (FDCs)"))))
		 		 
		 	  {
		 	  if(order.getDrugRegimen().equals(drugorder.getDrugRegimen()))
		 		  { 
		 			 onOrigFirstLine = false;
		 			
		 		  }
		 		  else
		 		  { 
		 			  onOrigFirstLine = true;
		 			drugorder=order;
		 			ret.put(ptId, new BooleanResult(onOrigFirstLine, this, context));
		 		  }
		 		 if(order.getDiscontinuedDate()!=null)
			 	  { 
			 		 onOrigFirstLine=false; 
			 		ret.put(ptId, new BooleanResult(onOrigFirstLine, this, context));
			 	  }
		 		
		 	  }
		 		 }
		 	  }  
		 	  }
		}
			
			
		}
		return ret;
    }
}
