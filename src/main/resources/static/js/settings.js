/**
 *  Javascript file for the Settings Controller
 */

/* Directive for making uploading files easier */
website.directive('fileModel', ['$parse',function ($parse) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;

			element.bind('change', function(){
				scope.$apply(function(){
					modelSetter(scope, element[0].files[0]);
				});
			});
		}
	};
}]);

/* Function to dynamically change audio of Audio control and Audio Download link */
function changeAudioSource(url){
	audioControl = $('#welcome-message-audio');
	audioDownload = $('#download-message-audio');
	audioControl.attr("src", url);
	audioControl.load();
	audioDownload.attr("href", url);
}

/* Actual Settings Controller */
website.controller("SettingsCtrl", function($scope, $http, $routeParams, $window, UpdateOrganization, UpdateBroadcastDefaultSettings) {

	// get the current organization Attributes
	var orgid = $('#organizationId').val();
	var abbr = $('#organizationAbbr').val();
	var userid = document.getElementById("settings-page-ids").getAttribute("data-userid");

	$scope.languageUrl  = [];

	$scope.selectOptions = [{
		name: 'Disable',
		value: '0'
	}, {
		name: 'Enable',
		value: '1'
	}];

	$scope.languageOptions = [{
		language: 'English',
		locale: 'en',
		value: '0'
	}, {
		language: 'Marathi',
		locale: 'mr',
		value: '1'
	}, {
		language: 'Hindi',
		locale: 'hi',
		value: '2'
	}];

	$scope.incomingCheckBoxOptions = {
			"order" : false,
			"feedback" : false,
			"response" : false
	};

	$scope.outgoingCheckBoxOptions = {
			"order" : false,
			"feedback" : false,
			"response" : false
	};    


	var organization = UpdateOrganization.get({
		id: orgid
	}, function() {

		/*
		 *  set initial values for 'select' element model from database
		 */

		// 'select' elements from dashboard options
		$scope.feedbackSelect = $scope.selectOptions[Number(organization.enableFeedbacks)].value;
		$scope.autoApproveSelect = $scope.selectOptions[Number(organization.autoApprove)].value;
		$scope.responseSelect = $scope.selectOptions[Number(organization.enableResponses)].value;
		$scope.billSelect = $scope.selectOptions[Number(organization.enableBilling)].value;
		$scope.textSelect = $scope.selectOptions[Number(organization.enableSms)].value;

		// 'select' elements from voice call options
		$scope.orderCancelSelect = $scope.selectOptions[Number(organization.enableOrderCancellation)].value;
		$scope.broadcastEnableSelect = $scope.selectOptions[Number(organization.enableBroadcasts)].value;

		// 'checkbox' elements from incoming call settings
		$scope.incomingCheckBoxOptions.order = organization.inboundCallAskOrder;
		$scope.incomingCheckBoxOptions.feedback = organization.inboundCallAskFeedback;
		$scope.incomingCheckBoxOptions.response = organization.inboundCallAskResponse;

		// 'select' element from welcome message settings
		$scope.WelcomeMessageLanguageSelect = '0';  // set default option in select as "English"

	});

	var outboundcall = UpdateBroadcastDefaultSettings.get({
		id: orgid
	}, function() {

		//intialize 'checkbox' elements from outgoing call settings
		$scope.outgoingCheckBoxOptions.order = outboundcall.askOrder;
		$scope.outgoingCheckBoxOptions.feedback = outboundcall.askFeedback;
		$scope.outgoingCheckBoxOptions.response = outboundcall.askResponse;

	});

	// Get the initial url for the audio files from the Backend
	var postData = new FormData();
	postData.append("orgid", orgid);

	$http({
		method: 'POST',
		url: API_ADDR + 'web/' + abbr + '/getwelcomeMessageUrl', // The URL to Post.
		headers: {'Content-Type': undefined}, // Set the Content-Type to undefined always.
		data: postData,
		transformRequest: function(data, headersGetterFunction) {
			return data;
		}
	}).success(function(data, status) {

		// set the url as recieved from the backend
		$scope.englishMessageurl = data[0];
		$scope.marathiMessageurl = data[1];
		$scope.hindiMessageurl = data[2];

		$scope.languageUrl.push($scope.englishMessageurl);
		$scope.languageUrl.push($scope.marathiMessageurl);
		$scope.languageUrl.push($scope.hindiMessageurl);

		// set initial values for the audio control and dowload link 
		changeAudioSource($scope.languageUrl[0]);
	}).error(function(data, status) {
		createAlert("Error Storing Audio","There was some error in response from the server.");
	});


	// click function for 'save details' button in voice dashboard settings
	$scope.updateDashboardOpt = function() {

		$scope.organization = UpdateOrganization.get({
			id: orgid
		}, function() {

			//change the required attributes
			$scope.organization.enableFeedbacks = Boolean(Number($scope.feedbackSelect));
			$scope.organization.autoApprove = Boolean(Number($scope.autoApproveSelect));
			$scope.organization.enableResponses = Boolean(Number($scope.responseSelect));
			$scope.organization.enableBilling = Boolean(Number($scope.billSelect));
			$scope.organization.enableOrderCancellation = Boolean(Number($scope.rejectSelect));
			$scope.organization.enableSms = Boolean(Number($scope.textSelect));

			//Finally , update the entity with required values
			$scope.organization.$update({
				id: orgid
			}, function() 
			{createAlert("Settings Saved","Your Settings have been saved.") });
		});
	};

	$scope.updateVoiceCallOpt = function() {

		$scope.organization = UpdateOrganization.get({
			id: orgid
		}, function() {

			//make changes in the $resource object
			$scope.organization.enableOrderCancellation  = Boolean(Number($scope.orderCancelSelect));
			$scope.organization.enableBroadcasts = Boolean(Number($scope.broadcastEnableSelect));

			//finally update the database
			$scope.organization.$update({
				id: orgid
			}, function() {createAlert("Settings Saved","Your Settings have been saved.")});
		});
	};

	// click function for 'save details' button in incoming call settings
	$scope.updateIncomingCallOpt = function() {

		$scope.organization = UpdateOrganization.get({
			id: orgid
		}, function() {

			//make changes in the $resource object
			$scope.organization.inboundCallAskOrder = $scope.incomingCheckBoxOptions.order;
			$scope.organization.inboundCallAskFeedback = $scope.incomingCheckBoxOptions.feedback;
			$scope.organization.inboundCallAskResponse = $scope.incomingCheckBoxOptions.response;
			
			// check if at least one option is selected
			if(!$scope.organization.inboundCallAskOrder && !$scope.organization.inboundCallAskFeedback && !$scope.organization.inboundCallAskResponse)
			{
				createAlert("Invalid Inpput","You must select at least one option !");
				return;
			}

			//finally update the database
			$scope.organization.$update({
				id: orgid
			}, function() {createAlert("Your Settings have been saved.")});
		});
	};
	// click function for 'save details' button in outgoing call settings
	$scope.updateOutgoingCallOpt = function() {

		$scope.outboundcall = UpdateBroadcastDefaultSettings.get({
			id: orgid
		}, function() {

			//make changes in the $resource object
			$scope.outboundcall.askOrder = $scope.outgoingCheckBoxOptions.order;
			$scope.outboundcall.askFeedback = $scope.outgoingCheckBoxOptions.feedback;
			$scope.outboundcall.askResponse = $scope.outgoingCheckBoxOptions.response;

			//finally update the database
			$scope.outboundcall.$update({
				id: orgid
			}, function() {createAlert("Settings Saved","Your Settings have been saved.")});
		});
	};

	$scope.uploadFile = function(){

		var localeIndex = $scope.WelcomeMessageLanguageSelect;

		// We use formData to pass various attributes to the spring controller method
		var formData=new FormData();

		formData.append("file", $scope.myFile); // can be accessed in Spring Controller using request.getPart()
		formData.append("locale", $scope.languageOptions[localeIndex].locale);
		formData.append("orgid", orgid);

		$http({
			method: 'POST',
			url: API_ADDR + 'web/' + abbr + '/upload/welcomeMessage', // The URL to Post.
			headers: {'Content-Type': undefined}, // Set the Content-Type to undefined always.
			data: formData,
			transformRequest: function(data, headersGetterFunction) {
				return data;
			}
		}).success(function(data, status) {

			if (data == "-1")

			{
				createAlert("Error Uploading File","Please select a file to upload !");
			}
			else if(data == "-2")
			{
				createAlert("Error Uploading File","Please Upload a File less than 5MB");
			}
			else if(data == "-3")
			{
				createAlert("Error Uploading File","The File you have uploaded is not wav audio file");
			}
			else if(data == "-4")
			{
				createAlert("File not found","The File was not found on server. (Maybe you are on localhost)!");
			}
			else
			{
				$scope.languageUrl[localeIndex] = data;
				changeAudioSource($scope.languageUrl[localeIndex]);
				createAlert("Audio Uploaded","The Audio File was Successfully Uploaded.");
			}
		})
		.error(function(data, status) {
			createAlert("Error Uploading File","There was some error in response from the server.");
		});
	};
	

	$scope.updateBillLayoutSetting=function(){
	
		var name =$.trim($('#orgName').val());
		var address =$.trim($('#address').val());
		var contact =$.trim($('#contact').val());
		var header =$.trim($('#header').val());
		var footer =$.trim($('#footer').val());
		if(!name)
		{
			createAlert("Invalid Input","Please Enter a Name for the Organization !");
		}
		else if(!contact)
		{
			createAlert("Invalid Input","Please Enter a Phone Number !");
		}
		else if(!address)
		{
			createAlert("Invalid Input","Please Enter a Address !");
		}
		else if(!header){
			createAlert("Invalid Input","Please Enter a Header!");
		}
		else if(!footer){
			createAlert("Invalid Input","Please Enter a Footer!");
		}
		
		else {
		
		console.log(name);
		console.log(footer);
		var profileSettingDetails = {};
		profileSettingDetails.name = $.trim($('#orgName').val());
		profileSettingDetails.address =  $.trim($('#address').val());
		profileSettingDetails.contact = $.trim($('#contact').val());
		profileSettingDetails.header =  $.trim($('#header').val());
		profileSettingDetails.footer =  $.trim($('#footer').val());
		if(normalizePhoneNumber(profileSettingDetails.contact) == false)
		{
			createAlert("Invalid Input","Please enter a valid phone number !");
		}
		else
		{
			// Normalize the phone number to database format
			profileSettingDetails.contact = normalizePhoneNumber(profileSettingDetails.contact);
			
		
		$http.post( API_ADDR + 'web/' + abbr + '/billLayout', profileSettingDetails).
		success(function(data, status, headers, config) {
			createAlert("Settings Saved","Your Settings have been saved.")
			
		}).
		error(function(data, status, headers, config) {
			createAlert("Error Saving Settings","There was some error in response from the remote server.");
		});
		}
		}
	}
	
	$scope.updateSetting= function(){

		var name = $.trim($('#name').val());
		var phoneNumber = $.trim($('#phone').val());
		var conformPassword = $.trim($('#re-new-password').val());
		var password = $.trim($('#new-password').val());
		var city = $.trim($('#city').val());
		var email = $.trim($('#email').val());
		
		
		
		
		if (!password || !conformPassword){
			createAlert("Error Saving Profile","Please enter password");
		}
		else if(password == conformPassword){

			var profileSettingDetails = {};
			profileSettingDetails.name = $.trim($('#name').val());
			profileSettingDetails.email =  $.trim($('#email').val());
			profileSettingDetails.phone = $.trim($('#phone').val());
			profileSettingDetails.city =  $.trim($('#city').val());
			profileSettingDetails.password =  $.trim($('#re-new-password').val());
			
			if(normalizePhoneNumber(profileSettingDetails.phone) == false)
			{
				createAlert("Enter Phone Number","Please enter a valid phone number !");
			}
			
			else {
				$http.post( API_ADDR + 'web/' + abbr + '/updateUser', profileSettingDetails).
				success(function(data, status, headers, config) {
					createAlert("Settings Saved","Your Settings have been saved.")
					profileSettingDetails.phone = normalizePhoneNumber(profileSettingDetails.phone);
				}).
				error(function(data, status, headers, config) {
					createAlert("Error Saving Settings","There was some error in response from the remote server.");
				});

			}
		}
		
		else{
			createAlert("Error Saving Profile","Password is not same");
		}
		
	}

	$scope.resetWelcomeMessageSettingsButton = function(){

		// Initialize the table
		$http.get(API_ADDR + 'web/' + abbr + '/resetwelcomeMessageUrl').
		success(function(data, status, headers, config) {

			var localeIndex = $scope.WelcomeMessageLanguageSelect;
			// Clear the contents of the array
			$scope.languageUrl.length = 0;

			// Push the new urls into it
			$scope.languageUrl.push(data[0]);
			$scope.languageUrl.push(data[1]);
			$scope.languageUrl.push(data[2]);

			changeAudioSource($scope.languageUrl[localeIndex]);

			// Hide the modal
			$('#reset-confirmation-modal').modal('hide');

		}).
		error(function(data, status, headers, config) {
			createAlert("Error Reseting Welcome Message","There was some error in response from the remote server.");
		});
	}
	
	//Below part in angular controller would be implemented in beta version
	$scope.organizationData = [];
	$scope.saveConfiguration = function(){
		var updateOrganizationSettings = new FormData();
		$("#system-configuration-settings > #organization-row").each(function(){
			
			
			dataObject = {};
			var orgid = $("#organization-name",this).attr("organizationId");
			dataObject.organizationId = orgid;
			dataObject.hasOnlyInbox = false;
			dataObject.hasFeedback = false;
			dataObject.hasResponse = false;
			dataObject.hasTextMessageResponse = false;
			dataObject.hasBill = false;
			if ($("#only-inbox > input",this).is(":checked"))
			{
				dataObject.hasOnlyInbox = true;
			}
			if ($("#feedback > input",this).is(":checked"))
			{
				dataObject.hasFeedback = true;
			}
			if ($("#response > input",this).is(":checked"))
			{
				dataObject.hasResponse = true;
			}
			if ($("#text-message-response > input",this).is(":checked"))
			{
				console.log(orgid + "text message response is set to be true");
				dataObject.hasTextMessageResponse = true;
			} 
			if ($("#bill > input",this).is(":checked"))
			{
				console.log(orgid + "bill is set to be true");
				dataObject.hasBill = true;
			}
			
			$scope.organizationData.push(dataObject);
			console.log("Organization Id is :" + orgid);
		});
		var myjsonstring = JSON.stringify($scope.organizationData);
		console.log(myjsonstring);
		updateOrganizationSettings.append("organizationData",$scope.organizationData);
		$http.post(API_ADDR + 'web/'+abbr+'/updateOrganizationConfiguration',myjsonstring)
		.success(function(data,status,header,config){
			console.log(data);
		})
		.error(function(data,status,header,config){
			console.log(data);
		});
	}

});

//Jquery Specific Code
$("#page-content").on("change","#select-welcome-message-language",function(e){

	// Get the scope of the angular controller so that we can access required variables from it
	myScope = angular.element('#settings-page-ids').scope();
	
	// Depending on value of select element, update the audio player and download link
	if(this.value === '1')
	{
		changeAudioSource(myScope.languageUrl[1]);
	}
	else if (this.value === '2')
	{
		changeAudioSource(myScope.languageUrl[2]);
	}
	else if(this.value === '0')
	{
		changeAudioSource(myScope.languageUrl[0]);
	}
});

$("#page-content").on("click","welcome-message-file",function(e){
	//In order to set the value of file as null
	this.value = null;
});

$("#page-content").on("change","#welcome-message-file",function(e){
	//get the filename set by the fileModel angular directive
	var filename = angular.element('#settings-page-ids').scope().myFile.name;
	//set the filename to be seen in UI
	$("#welcome-message-url").text(filename);
});
