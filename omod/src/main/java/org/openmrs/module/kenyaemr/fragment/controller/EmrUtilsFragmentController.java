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

package org.openmrs.module.kenyaemr.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.calculation.result.CalculationResult;
import org.openmrs.module.kenyaemr.EmrConstants;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
import org.openmrs.module.kenyaemr.regimen.RegimenManager;
import org.openmrs.module.kenyaemr.util.EmrUiUtils;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.InitialArtStartDateCalculation;
import org.openmrs.module.kenyaemr.regimen.RegimenChange;
import org.openmrs.module.kenyaemr.regimen.RegimenChangeHistory;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppAction;
import org.openmrs.module.kenyaui.annotation.PublicAction;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Fragment actions generally useful for KenyaEMR
 */
public class EmrUtilsFragmentController {

	protected static final Log log = LogFactory.getLog(EmrUtilsFragmentController.class);

	/**
	 * Checks if current user session is authenticated
	 * @return simple object {authenticated: true/false}
	 */
	@PublicAction
	public SimpleObject isAuthenticated() {
		return SimpleObject.create("authenticated", Context.isAuthenticated());
	}

	/**
	 * Attempt to authenticate current user session with the given credentials
	 * @param username the username
	 * @param password the password
	 * @return simple object {authenticated: true/false}
	 */
	@PublicAction
	public SimpleObject authenticate(@RequestParam(value = "username", required = false) String username,
									 @RequestParam(value = "password", required = false) String password) {
		try {
			Context.authenticate(username, password);
		} catch (ContextAuthenticationException ex) {
			// do nothing
		}
		return isAuthenticated();
	}

	/**
	 * Gets the next HIV patient number from the generator
	 * @param comment the optional comment
	 * @return simple object { value: identifier value }
	 */
	public SimpleObject nextHivUniquePatientNumber(@RequestParam(required = false, value = "comment") String comment) {
		if (comment == null) {
			comment = "KenyaEMR UI";
		}

		String id = Context.getService(KenyaEmrService.class).getNextHivUniquePatientNumber(comment);
		return SimpleObject.create("value", id);
	}

	/**
	 * Voids the given relationship
	 * @param relationship the relationship
	 * @param reason the reason for voiding
	 * @return the simplified visit
	 */
	public SuccessResult voidRelationship(@RequestParam("relationshipId") Relationship relationship, @RequestParam("reason") String reason) {
		Context.getPersonService().voidRelationship(relationship, reason);
		return new SuccessResult("Relationship voided");
	}

	/**
	 * Voids the given visit
	 * @param visit the visit
	 * @param reason the reason for voiding
	 * @return the simplified visit
	 */
	@AppAction(EmrConstants.APP_CHART)
	public SuccessResult voidVisit(@RequestParam("visitId") Visit visit, @RequestParam("reason") String reason) {
		Context.getVisitService().voidVisit(visit, reason);
		return new SuccessResult("Visit voided");
	}

	/**
	 * Gets the duration since patient started ART
	 * @param patient the patient
	 * @param now the current time reference
	 * @return the regimen and duration
	 */
	public SimpleObject currentArvRegimen(@RequestParam("patientId") Patient patient, @RequestParam("now") Date now, @SpringBean RegimenManager regimenManager, @SpringBean EmrUiUtils kenyaEmrUi, @SpringBean KenyaUiUtils kenyaUi, UiUtils ui) {
		Concept arvs = regimenManager.getMasterSetConcept("ARV");
		RegimenChangeHistory history = RegimenChangeHistory.forPatient(patient, arvs);
		RegimenChange current = history.getLastChangeBeforeDate(now);

		return SimpleObject.create(
				"regimen", current != null ? kenyaEmrUi.formatRegimenShort(current.getStarted(), ui) : null,
				"duration", current != null ? kenyaUi.formatInterval(current.getDate(), now) : null
		);
	}

	/**
	 * Gets the duration since patient started ART
	 * @param patient the patient
	 * @param now the current time reference
	 * @return the duration interval
	 */
	public SimpleObject durationSinceStartArt(@RequestParam("patientId") Patient patient, @RequestParam("now") Date now, @SpringBean KenyaUiUtils kenyaUi) {
		CalculationResult result = EmrCalculationUtils.evaluateForPatient(InitialArtStartDateCalculation.class, null, patient);
		Date artStartDate = result != null ? (Date) result.getValue() : null;

		return SimpleObject.create("duration", artStartDate != null ? kenyaUi.formatInterval(artStartDate, now) : null);
	}

	/**
	 * Calculates an estimated birthdate from an age value
	 * @param now the current time reference
	 * @param age the age
	 * @return the ISO8601 formatted birthdate
	 */
	public SimpleObject birthdateFromAge(@RequestParam(value = "age") String text,
										 @RequestParam(value = "now", required = false) Date now,
										 @SpringBean KenyaUiUtils kenyaui) {
		/*
		Calendar cal = Calendar.getInstance();
		cal.setTime(now != null ? now : new Date());
		cal.add(Calendar.YEAR, -age);
		*/
		
		text = text.toLowerCase();
		String ageStr = text.substring(0, text.length() - 1);
		String type = text.substring(text.length() - 1);
		int age = Integer.parseInt(ageStr);
		
		Calendar cal = Calendar.getInstance();
		if (type.equalsIgnoreCase("y")) {
			cal.add(Calendar.YEAR, -age);
		} else if (type.equalsIgnoreCase("m")) {
			cal.add(Calendar.MONTH, -age);
		} else if (type.equalsIgnoreCase("w")) {
			cal.add(Calendar.WEEK_OF_YEAR, -age);
		} else if (type.equalsIgnoreCase("d")) {
			cal.add(Calendar.DATE, -age);
		}
		return SimpleObject.create("birthdate", kenyaui.formatDateParam(cal.getTime()));
	}
	
	public SimpleObject addressHierarchy(@RequestParam(value= "county", required = false) String county,
			@RequestParam(value= "subcounty", required = false) String subcounty,@SpringBean KenyaUiUtils kenyaUi) {
		
		Map<String,List> subCountyMap= new LinkedHashMap<String,List>();
		
		Map<String,List> locationMap= new LinkedHashMap<String,List>();
		
		String[] countyArr = null ;
		
		File addressFile = new File(OpenmrsUtil.getApplicationDataDirectory()
				+ "myanmaraddresshierarchy.xml");
		if (addressFile.exists()) {
			SAXReader reader = new SAXReader();
			Document document = null;
			try {
				document = reader.read(addressFile.toURI().toURL());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			XPath distSelector = null;
			try {
				distSelector = new Dom4jXPath("//country/county");
			} catch (JaxenException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			@SuppressWarnings("rawtypes")
			List countyList = null;
			try {
				countyList = distSelector.selectNodes(document);
			} catch (JaxenException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 countyArr = new String[countyList.size()];
			String[] subcountyArr = new String[countyList.size()];

			if (countyList.size() > 0) {
				for (int i = 0; i < countyList.size(); i++) {
					
					List<String> subCountyList=new LinkedList<String>();

					countyArr[i] = ((Element) countyList.get(i))
							.attributeValue("name");
					@SuppressWarnings("rawtypes")
					List subcountyList = ((Element) countyList.get(i))
							.elements("subcounty");

					String countyName = ((Element) countyList.get(i))
							.attributeValue("name");

					String subcountyName = ((Element) subcountyList.get(0))
							.attributeValue("name");
					
					subcountyArr[i] = ((Element) subcountyList.get(0))
							.attributeValue("name") + ",";
					
					//
					subCountyList.add(subcountyName);
					
					@SuppressWarnings("rawtypes")
					List locationList = ((Element) subcountyList.get(0))
							.elements("location");
					//
					List<String> locList1=new LinkedList<String>();
					for (int k = 0; k < (locationList.size()); k++) {
						
						String locationName = ((Element) locationList.get(k))
								.attributeValue("name");
						//
						locList1.add(locationName);
						
					}
					locationMap.put(countyName+subcountyName, locList1);

					for (int j = 1; j < (subcountyList.size() - 1); j++) {
						
						subcountyName = ((Element) subcountyList.get(j))
								.attributeValue("name");
						//
						subCountyList.add(subcountyName);
						
						subcountyArr[i] += ((Element) subcountyList.get(j))
								.attributeValue("name") + ",";
						locationList = ((Element) subcountyList.get(j))
								.elements("location");
						//
						List<String> locList2=new LinkedList<String>();
						for (int k = 0; k < (locationList.size()); k++) {
							
							String locationName = ((Element) locationList
									.get(k)).attributeValue("name");
							//
							locList2.add(locationName);
						}
						//
						locationMap.put(countyName+subcountyName, locList2);
					}

					subcountyName = ((Element) subcountyList.get((subcountyList
							.size() - 1))).attributeValue("name");
					//
					subCountyList.add(subcountyName);
					subCountyMap.put(countyName, subCountyList);
					
					subcountyArr[i] += ((Element) subcountyList
							.get((subcountyList.size() - 1)))
							.attributeValue("name")
							+ ",";
					locationList = ((Element) subcountyList.get((subcountyList
							.size() - 1))).elements("location");
					//
					List<String> locList3=new LinkedList<String>();
					for (int k = 0; k < (locationList.size()); k++) {
						
						String locationName = ((Element) locationList.get(k))
								.attributeValue("name");
						
						//
						locList3.add(locationName);
					}
					locationMap.put(countyName+subcountyName, locList3);

				}
			}
		}
		
		
		if(county.equals("")){
			return SimpleObject.create("county",countyArr);	
		}
		else{
			if(subcounty.equals("")){
				return SimpleObject.create("subcounty",subCountyMap.get(county));
			}
			else{
		    return SimpleObject.create("location",locationMap.get(county+subcounty));
			}
		}
		
	}
}