<%
	ui.decorateWith("kenyaui", "panel", [ heading: (config.heading ?: "Edit Patient"), frameOnly: true ])
	
	ui.includeJavascript("kenyaemr", "controllers/addresshierarchy.js")

	def nameFields = [
			[
					[ object: command, property: "personName.givenName", label: "Patient's Name *" ],	[ object: command, property: "fatherName", label: "Father's Name *" ]
			]
	]

	def ingoNameFields = [
		[
				[ object: command, property: "ingoTypeConcept", label:"", config: [ style: "list", answerTo: ingoConcept ] ],
			]	
	]


	def otherDemogFieldRows = [
			[
					[ object: command, property: "maritalStatus", label: "Marital status", config: [ style: "list", options: maritalStatusOptions ] ],
					[ object: command, property: "occupation", label: "Occupation", config: [ style: "list", answerTo: occupationConcept ] ],
					[ object: command, property: "education", label: "Education", config: [ style: "list", options: educationOptions ] ]
			],
			[
					[ object: command, property: "dead", label: "Deceased" ],
					[ object: command, property: "deathDate", label: "Date of death" ]
			]
	]

	def nextOfKinFieldRows = [
			[
					[ object: command, property: "nameOfNextOfKin", label: "Name" ],
					[ object: command, property: "nextOfKinAddress", label: "Physical Address" ],
					[ object: command, property: "nextOfKinContact", label: "Contact Number" ]
			]
	]
	
	def enrollmentStatus = [
			[
					[ object: command, property: "enrollmentName", label: "Status at enrollment", config: [ style: "list", answerTo: enrollmentList ] ],
					[ object: command, property: "entryPoint", label: "Entry Point *", config: [ style: "list", answerTo: entryPointList ] ],
					[ object: command, property: "otherEntryPoint", label: "If other, Please specify" ],
					[ object: command, property: "previousClinicName", label: "Previous Clinic Name*"],
					[ object: command, property: "transferredInDate", label: "Transferred In Date " ]
			]		
	] 

	def addressFieldRows = [
			[
					[ object: command, property: "personAddress.address1", label: "Physical Address", config: [ size: 60 ] ],
					[ object: command, property: "telephoneContact", label: "Contact Number" ]
			]
	]
%>

<form id="edit-patient-form" method="post" action="${ ui.actionLink("kenyaemr", "patient/editPatient", "savePatient") }">
	<% if (command.original) { %>
		<input type="hidden" name="personId" value="${ command.original.id }"/>
	<% } %>

	<div class="ke-panel-content">

		<div class="ke-form-globalerrors" style="display: none"></div>

		<div class="ke-form-instructions">
			<strong>*</strong> indicates a required field
		</div>

		<fieldset>
			<legend>ID Numbers</legend>

			<table>
				<% if (command.inHivProgram) { %>
					<tr>
						<td class="ke-field-label">Unique Patient Number</td>
						<td>${ ui.includeFragment("kenyaui", "widget/field", [ object: command, property: "uniquePatientNumber" ]) }</td>
						<td class="ke-field-instructions">(HIV program<% if (!command.uniquePatientNumber) { %>, if assigned<% } %>)</td>
					</tr>
				<% } %>
				<tr>
					<td class="ke-field-label">Pre ART Registration Number</td>
					<td>${ ui.includeFragment("kenyaui", "widget/field", [ object: command, property: "preArtRegistrationNumber" ]) }</td>
					<td class="ke-field-instructions"><% if (!command.preArtRegistrationNumber) { %>(if available)<% } %></td>
				</tr>
				<tr>
					<td class="ke-field-label">NAP ART Registration Number</td>
					<td>${ ui.includeFragment("kenyaui", "widget/field", [ object: command, property: "napArtRegistrationNumber" ]) }</td>
					<td class="ke-field-instructions"><% if (!command.napArtRegistrationNumber) { %>(if available)<% } %></td>
				</tr>
				<tr>
					<td class="ke-field-label">INGO ART Registration Number</td>
					<td>${ ui.includeFragment("kenyaui", "widget/field", [ object: command, property: "artRegistrationNumber" ]) }</td>
					<td class="ke-field-instructions"><% if (!command.artRegistrationNumber) { %>(if available)<% } %></td>
					
					<td class="ke-field-label"> INGO NAME </td>
					<td>	<% ingoNameFields.each { %>
							${ ui.includeFragment("kenyaui", "widget/rowOfFields", [ fields: it ]) }<% } 
						%> </td> 
				</tr>
				<tr>
					<td class="ke-field-label"><b>Patient ID*</b></td>
					<td><input name="systemPatientId" style="width: 260px" value=${ patientIdentifier} readonly autocomplete="off" ></td>
				</tr>
			</table>

		</fieldset>

		<fieldset>
			<legend>Demographics</legend>

			<% nameFields.each { %>
			${ ui.includeFragment("kenyaui", "widget/rowOfFields", [ fields: it ]) }
			<% } %>

			<table>
				<tr>
					<td valign="top">
						<label class="ke-field-label">Gender *</label>
						<span class="ke-field-content">
							<input type="radio" name="gender" value="F" id="gender-F" ${ command.gender == 'F' ? 'checked="checked"' : '' }/> Female
							<input type="radio" name="gender" value="M" id="gender-M" ${ command.gender == 'M' ? 'checked="checked"' : '' }/> Male
							<span id="gender-F-error" class="error" style="display: none"></span>
							<span id="gender-M-error" class="error" style="display: none"></span>
						</span>
					</td>
					<td valign="top"></td>
					<td valign="top">
						<label class="ke-field-label">Birthdate *</label>
						<span class="ke-field-content">
							${ ui.includeFragment("kenyaui", "widget/field", [ id: "patient-birthdate", object: command, property: "birthdate" ]) }

							<span id="patient-birthdate-estimated">
								<input type="radio" name="birthdateEstimated" value="true" ${ command.birthdateEstimated ? 'checked="checked"' : '' }/> Estimated
								<input type="radio" name="birthdateEstimated" value="false" ${ !command.birthdateEstimated ? 'checked="checked"' : '' }/> Exact
							</span>
							&nbsp;&nbsp;&nbsp;

							<span id="from-age-button-placeholder"></span>
						</span>
					</td>
				</tr>
			</table>

			<% otherDemogFieldRows.each { %>
				${ ui.includeFragment("kenyaui", "widget/rowOfFields", [ fields: it ]) }
			<% } %>

		</fieldset>

		<fieldset>
			<legend>Address</legend>

			<% addressFieldRows.each { %>
				${ ui.includeFragment("kenyaui", "widget/rowOfFields", [ fields: it ]) }
			<% } %>

		<div ng-controller="AddresshierarchyCtrl" data-ng-init="init()">
        <table>
        <tr>
        
        <td valign="top">
        <label class="ke-field-label">State / Region</label>
        <span class="ke-field-content">
        <select style="width: 180px;" name="personAddress.stateProvince" ng-model="myState" ng-options="state for state in states track by state" ng-change="stateSelection(myState)"></select>
        </span>
        </td>
        
        <td valign="top">
        <label class="ke-field-label">Township</label>
        <span class="ke-field-content">
        <select style="width: 180px;" name="personAddress.countyDistrict" ng-model="myTownship" ng-options="township for township in townships track by township" ng-change="townshipSelection(myState,myTownship)"></select>
        </span>
        </td>
        
        <td valign="top">
        <label class="ke-field-label">Town / Village</label>
        <span class="ke-field-content">
        <select style="width: 180px;"name="personAddress.cityVillage" ng-model="myVillage" ng-options="village for village in villages track by village"></select>
        </span>
        </td>
     
        </tr>
        </table>
        </div>
		</fieldset>

		<fieldset>
			<legend>Treatment Supporter's</legend>

			 <% nextOfKinFieldRows.each { %>
			   ${ ui.includeFragment("kenyaui", "widget/rowOfFields", [ fields: it ]) }
			 <% } %>

		</fieldset>

		<fieldset>
			<legend>Enrollment Status & Patient Source</legend>

			 <% enrollmentStatus.each { %>
			   ${ ui.includeFragment("kenyaui", "widget/rowOfFields", [ fields: it ]) }
			 <% } %>

		</fieldset>
		
			<fieldset>
			<legend>HIV Testing</legend>

			<table>
				<tr>
					<td valign="top">
						<label class="ke-field-label">Have you performed HIV confirmatory test previously? *</label>
						<span class="ke-field-content" >
							<input type="radio" name="hivTestPerformed" value="Yes" id="hivTestPerformed-Yes" onClick="getDateAndPlace();" ${ command.hivTestPerformed == 'Yes' ? 'checked="checked"' : '' }/> Yes
							<input type="radio" name="hivTestPerformed" value="No" id="hivTestPerformed-No" onClick="getDateAndPlace();" ${ command.hivTestPerformed == 'No' ? 'checked="checked"' : '' }/> No
							<span id="hivTestPerformed-Yes-error" class="error" style="display: none"></span>
							<span id="hivTestPerformed-No-error" class="error" style="display: none"></span>
						</span>
					</td>
					<td valign="top"></td>
					<td valign="top" id="hivTestPerformedDate">
						<label class="ke-field-label">If yes, Date of Test</label>
						<span class="ke-field-content">
							${ ui.includeFragment("kenyaui", "widget/field", [id: "patient-hivTestPerformedDate", object: command, property: "hivTestPerformedDate" ]) }
						</span>
					</td>
					<td valign="top"></td>
					<td valign="top"  id="hivTestPerformedPlace">
						<label class="ke-field-label">Place</label>
						<span class="ke-field-content">
							${ ui.includeFragment("kenyaui", "widget/field", [ object: command, property: "hivTestPerformedPlace" ]) }
						</span>	
					</td>
					<td valign="top" id="checkInField">
						<span class="ke-field-content">
							<input name="checkInType" id="checkInType" ${ command.checkInType == '1' ? '1' : '0' }/> 
						</span>		
					</td>
				</tr>
			</table>

			

		</fieldset>

	</div>
	
	<div class="ke-panel-footer">
		<% if (command.original) { %>
			<button onClick="checkIn(0)" type="submit">
				<img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> ${"Save Changes" }
			</button>		
		<% } else {%>
			<button onClick="checkIn(1)" type="submit">
				<img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> ${ "Create Patient and Check In" }
			</button>
			<button onClick="checkIn(0)" type="submit" style="float: right;">
				<img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> ${ "Create Patient" }
			</button>
		<% } %>
		<% if (config.returnUrl) { %>
			<button type="button" class="cancel-button"><img src="${ ui.resourceLink("kenyaui", "images/glyphs/cancel.png") }" /> Cancel</button>
		<% } %>
	</div>
	
</form>

<!-- You can't nest forms in HTML, so keep the dialog box form down here -->
${ ui.includeFragment("kenyaui", "widget/dialogForm", [
		buttonConfig: [ id: "from-age-button", label: "from age", iconProvider: "kenyaui", icon: "glyphs/calculate.png" ],
		dialogConfig: [ heading: "Calculate Birthdate", width: 40, height: 40 ],
		fields: [
				[ label: "Age in y/m/w/d", formFieldName: "age", class: java.lang.String ],
				[ label: "On date", formFieldName: "now", class: java.util.Date, initialValue: new Date() ]
		],
		fragmentProvider: "kenyaemr",
		fragment: "emrUtils",
		action: "birthdateFromAge",
		onSuccessCallback: "updateBirthdate(data);",
		onOpenCallback: """jQuery('input[name="age"]').focus()""",
		submitLabel: ui.message("general.submit"),
		cancelLabel: ui.message("general.cancel")
]) }

<script type="text/javascript">
var patientId=${patientId};
jQuery(document).ready(function(){
	 document.getElementById('checkInField').style.display='none';
	var hivTestPerformedYes = document.getElementById('hivTestPerformed-Yes').checked; 
			if(hivTestPerformedYes==true)
			{
			  document.getElementById('hivTestPerformedDate').style.display='';
			  document.getElementById('hivTestPerformedPlace').style.display=''; 
			}
			else
			{
			  document.getElementById('hivTestPerformedDate').style.display='none';
  			  document.getElementById('hivTestPerformedPlace').style.display='none'; 
			}
	});
	

	jQuery(function() {
		jQuery('#from-age-button').appendTo(jQuery('#from-age-button-placeholder'));
		
		jQuery('#edit-patient-form .cancel-button').click(function() {
			ui.navigate('${ config.returnUrl }');
		});

		kenyaui.setupAjaxPost('edit-patient-form', {
			onSuccess: function(data) {
				if (data.id) {
					<% if (config.returnUrl) { %>
					ui.navigate('${ config.returnUrl }');
					<% } else { %>
					
					 if (document.getElementById('checkInType').value==1) { 
						ui.navigate('kenyaemr', 'registration/registrationHome');
						} else { 
						ui.navigate('kenyaemr', 'registration/registrationViewPatient', { patientId: data.id });
					 } 
					<% } %>
				} else {
					kenyaui.notifyError('Saving patient was successful, but unexpected response');
				}
			}
		});
	});
	
	function checkIn(check){
		document.getElementById('checkInType').value = check;
	}

	function getDateAndPlace(){
			var hivTestPerformedYes = document.getElementById('hivTestPerformed-Yes').checked; 
			if(hivTestPerformedYes==true)
			{
			  document.getElementById('hivTestPerformedDate').style.display='';
			  document.getElementById('hivTestPerformedPlace').style.display=''; 
			}
			else
			{
			  document.getElementById('hivTestPerformedDate').style.display='none';
  			  document.getElementById('hivTestPerformedPlace').style.display='none'; 
			}
	};

	function updateBirthdate(data) {
		var birthdate = new Date(data.birthdate);

		kenyaui.setDateField('patient-birthdate', birthdate);
		kenyaui.setRadioField('patient-birthdate-estimated', 'true');
	}
</script>
<style>

</style>