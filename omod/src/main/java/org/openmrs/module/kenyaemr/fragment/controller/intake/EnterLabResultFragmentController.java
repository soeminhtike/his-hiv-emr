package org.openmrs.module.kenyaemr.fragment.controller.intake;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.form.FormManager;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentActionRequest;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.resource.ResourceFactory;
import org.springframework.web.bind.annotation.RequestParam;

public class EnterLabResultFragmentController {
	
	protected static final Log log = LogFactory.getLog(EnterLabResultFragmentController.class);
	
	public void controller(
			@RequestParam(required = false, value = "visitId") Visit visit,
            @RequestParam(required = false, value = "formUuid") String formUuid,
            @RequestParam(required = false, value = "returnUrl") String returnUrl,
            UiUtils ui,
            PageModel model) {
		
		Set<Obs> listObs = null;
		Encounter enc = null;
		if (visit != null) {
			EncounterType encType = Context.getEncounterService().getEncounterType("Lab Results");
			List<Encounter> encs = Context.getEncounterService().getEncounters(visit.getPatient(), null, null, null, null, Collections.singleton(encType), null, null, Collections.singleton(visit), false);
			enc = null;
			if (encs != null && !encs.isEmpty()) {
				enc = encs.get(encs.size() - 1);
			}
			
			if (enc != null) {
				 listObs = enc.getAllObs();
			}
		}
		model.addAttribute("resultEncounter", enc);
		model.addAttribute("listResultObs", listObs);
		 model.addAttribute("visit", visit);
		 model.addAttribute("returnUrl", returnUrl);
		 
		 Set<Obs> listLabOrderObs  = null;
		 if (visit != null) {
			 Encounter encounter = Context.getService(KenyaEmrService.class).getLabbOrderEncounter(visit);
			
			if (encounter != null) {
				listLabOrderObs = encounter.getAllObs();
			}
		 }
			model.addAttribute("listObs", listLabOrderObs);
	}
	
	public Object submit(
			 @RequestParam(value = "visitId", required = false) Visit visit,
			 @RequestParam(value="conceptIds", required = true) String[] conceptIds,
			 @RequestParam(value = "encounterId", required = false) Encounter resultEncounter,
			 @SpringBean ResourceFactory resourceFactory,
			 @SpringBean KenyaUiUtils kenyaUi,
			 @SpringBean FormManager formManager,
			 FragmentActionRequest actionRequest) throws Exception {
		
		EncounterService encService = Context.getService(EncounterService.class);
		
		Date curDate = new Date();

		if (resultEncounter != null) {
			// edit
			Set<Obs> listObs = resultEncounter.getAllObs();
			boolean changed = false;
			boolean isNewObs = true;
			for (String submittedConceptId : conceptIds) {
				isNewObs = true;
				String value = actionRequest.getParameter(submittedConceptId+"_value");
				Concept concept = Context.getConceptService().getConcept(NumberUtils.toInt(submittedConceptId));
				for (Obs obs : listObs) {
					if (obs.getConcept().getConceptId().equals(concept.getConceptId())) {
						// edit existed Obs
						isNewObs = false;

						if (obs.getValueText() != null && !obs.getValueText().equalsIgnoreCase(value)) {
							// value has been changed
							Obs obsToUpdate = Context.getObsService().getObs(obs.getObsId());
							obsToUpdate.setValueText(value);
							obsToUpdate.setDateChanged(curDate);
							
							changed = true;
						} 
						
					} 
				}
				
				if (isNewObs) {
					// save new Obs
					Obs newObs = new Obs();
					newObs.setConcept(concept);
					newObs.setValueText(value);
					newObs.setEncounter(resultEncounter);
					newObs.setDateCreated(curDate);
					resultEncounter.addObs(newObs);
					
					changed = true;
				}
			}
			
			if (changed) {
				resultEncounter.setDateChanged(curDate);
				resultEncounter.setEncounterDatetime(curDate);
				Context.getEncounterService().saveEncounter(resultEncounter);
			}
		} else {
			// save new
			Encounter encounter = new Encounter();
			encounter.setDateCreated(curDate);
			encounter.setEncounterType(encService.getEncounterType("Lab Results"));
			encounter.setVisit(visit);
			encounter.setPatient(visit.getPatient());
			encounter.setEncounterDatetime(curDate);
			
			for (String conceptId : conceptIds) {
				String value = actionRequest.getParameter(conceptId+"_value");	
				Concept concept = Context.getConceptService().getConcept(conceptId);
				
				Obs obs = new Obs();
				obs.setConcept(concept);
				obs.setValueText(value);
				obs.setEncounter(encounter);
				obs.setDateCreated(curDate);
				encounter.addObs(obs);
			}
			encService.saveEncounter(encounter);
		}
			
			return new SuccessResult("Saved Lab Results");
		
	}
}
