'use strict'

// confirmation of delete
// edit
// solution for transaction
// message store / key / gui
var app = angular.module('requestConnectorApp',[]);

app.controller("requestConnectorCtrl", function($scope, $http, $location, $window) {

	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed.
	$http.defaults.headers.common['Accept'] = 'application/json';

    	$scope.showSubmitFailed = function(title, text){
		swal({
			 title: "",
			 text: "Request unsuccessful !!",
			 timer: 2000,
			 showConfirmButton: false
			 });
	}

        $scope.handleValidationErrors = function(error){
            if(error.errors != null && error.errors.length > 0){
                $scope.alert = error.errors[0].defaultMessage;
            }else if(error.message != null){
                $scope.alert = error.message;
            }else if(error.result != null){
                $scope.alert = error.result;
            }
            else
                $scope.alert = "Unable to process the request. Please verify the request or contact our Administrator !!";

            $scope.alertnote = $scope.alert;
            $scope.showAlertToast();
        }

	$scope.showAlertToast = function() {
                  var x = document.getElementById("alertbar");
                  x.className = "show";
                  setTimeout(function(){ x.className = x.className.replace("show", ""); }, 2000);
                }

        $scope.requestTitle = "Connector Create Request";
        $scope.requestType = "CreateConnector";
        $scope.requestButton = "Submit";

        $scope.loadEditConnectorInfo = function(){
            var envSelected, topicSelected;

            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');
            for (var i = 0; i < sURLVariables.length; i++)
            {
                var sParameterName = sURLVariables[i].split('=');
                if (sParameterName[0] == "connectorName")
                {
                    topicSelected = sParameterName[1];
                }
                else if (sParameterName[0] == "env")
                {
                    envSelected = sParameterName[1];
                }
            }

            if(!topicSelected && !envSelected)
                return;

            $scope.addConnector.connectorName = topicSelected;
            $scope.addConnector.envName = envSelected;

            $http({
                    method: "GET",
                    url: "getConnectorDetailsPerEnv",
                     headers : { 'Content-Type' : 'application/json' },
                     params: {'envSelected' : envSelected,  'connectorName' : topicSelected }
                }).success(function(output) {
                    if(output.connectorExists){
                        $scope.requestTitle = "Connector Edit Request";
                        $scope.requestType = "EditConnector";
                        $scope.requestButton = "Submit Update";
                        $scope.connectorIdForEdit = output.connectorId;
                        $scope.addConnector.connectorConfig = output.connectorContents.connectorConfig;
                        $scope.addConnector.description = output.connectorContents.description;
                    }
                    else{
                        swal({
                                 title: "",
                                 text: "Connector Edit Request : " + output.error,
                                 showConfirmButton: true
                             }).then(function(isConfirm){
                                    $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/connectorOverview?connectorName=" + topicSelected;
                              });
                    }
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );
        }

        $scope.addConnector = function() {

            var serviceInput = {};

            $scope.alert = null;
            $scope.alertnote = null;

            if(!$scope.addConnector.envName || $scope.addConnector.envName == "")
            {
                $scope.alertnote = "Please select an environment.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addConnector.connectorName == null || $scope.addConnector.connectorName.length==0)
            {
                $scope.alertnote = "Please fill in connector name.";
                $scope.showAlertToast();
                return;
            }else
            {
                $scope.addConnector.connectorName = $scope.addConnector.connectorName.trim();
                if($scope.addConnector.connectorName.length==0)
                {
                    $scope.alertnote = "Please fill in connector name.";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addConnector.connectorName.indexOf(" ") > 0){
                    $scope.alertnote = "Connector name should not contain any spaces.";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addConnector.connectorName.length < 5){
                    $scope.alertnote = "Connector name should be atleast 5 characters.";
                    $scope.showAlertToast();
                    return;
                }
            }

            // selecting default partitions

            if(!$scope.addConnector.description)
            {
                $scope.alertnote = "Please fill in description.";
                $scope.showAlertToast();
                return;
            }
            else {
                $scope.addConnector.description = $scope.addConnector.description.trim();
                if($scope.addConnector.description.length==0)
                {
                    $scope.alertnote = "Please fill in description.";
                    $scope.showAlertToast();
                    return;
                }
            }

            serviceInput['environment'] = $scope.addConnector.envName;
            serviceInput['connectorName'] = $scope.addConnector.connectorName;
            serviceInput['connectorConfig'] = $scope.addConnector.connectorConfig;
            serviceInput['teamName'] = $scope.teamname;
            serviceInput['remarks'] = $scope.addConnector.remarks;
            serviceInput['description'] = $scope.addConnector.description;

            if($scope.requestType == 'CreateConnector'){
                serviceInput['connectortype'] = 'Create';
            }
            else{
                serviceInput['connectortype'] = 'Update';
                serviceInput['otherParams'] = $scope.connectorIdForEdit;
            }

            $scope.httpCreateConnectorReq(serviceInput);
        };

        $scope.httpCreateConnectorReq = function(serviceInput){
            $http({
                    method: "POST",
                    url: "createConnector",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'addConnectorRequest' : serviceInput },
                    data: serviceInput
                }).success(function(output) {
                    if(output.result === 'success'){
                        swal({
                                 title: "Awesome !",
                                 text: "Connector Request : "+output.result,
                                 showConfirmButton: true
                             }).then(function(isConfirm){
                                    $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/myConnectorRequests?reqsType=created&connectorCreated=true";
                             });
                    }
                    else{
                            $scope.alert = "Connector Request : "+output.result;
                            $scope.showSubmitFailed('','');
                        }
                }).error(
                    function(error)
                    {
                        $scope.handleValidationErrors(error);
                    }
                );
        }

        $scope.cancelRequest = function() {
            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/kafkaConnectors";
        }

        $scope.getConnectorsEnvs = function() {

            $http({
                method: "GET",
                url: "getSyncConnectorsEnv",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.allenvs = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }


        $scope.refreshPage = function(){
                $window.location.reload();
            }

       $scope.getAuth = function() {
            $http({
                   method: "GET",
                   url: "getAuth",
                   headers : { 'Content-Type' : 'application/json' }
               }).success(function(output) {
                   $scope.dashboardDetails = output;
                   $scope.userlogged = output.username;
                   $scope.teamname = output.teamname;
                   $scope.userrole = output.userrole;
                   $scope.notifications = output.notifications;
                    $scope.notificationsAcls = output.notificationsAcls;
                    $scope.notificationsSchemas = output.notificationsSchemas;
                    $scope.notificationsUsers = output.notificationsUsers;

                    if(output.requestItems!='Authorized')
                    {
                        swal({
                             title: "Not Authorized !",
                             text: "",
                             showConfirmButton: true
                         }).then(function(isConfirm){
                                $scope.alertnote = "You are not authorized to request.";
                                $scope.showAlertToast();
                                $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/index";
                         });
                    }

                   if(output.companyinfo == null){
                       $scope.companyinfo = "Company not defined!!";
                   }
                   else{
                       $scope.companyinfo = output.companyinfo;
                    }
                   if($scope.userlogged != null)
                       $scope.loggedinuser = "true";

                   $scope.checkPendingApprovals();
               }).error(
                   function(error)
                   {
                       $scope.alert = error;
                   }
               );
               	}

		$scope.redirectToPendingReqs = function(redirectPage){
				swal({
						title: "Pending Requests",
						text: "Would you like to look at them ?",
						type: "info",
						showCancelButton: true,
						confirmButtonColor: "#DD6B55",
						confirmButtonText: "Yes, show me!",
						cancelButtonText: "No, later!",
						closeOnConfirm: true,
						closeOnCancel: true
					}).then(function(isConfirm){
						if (isConfirm.dismiss != "cancel") {
							$window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/"+redirectPage;
						} else {
							return;
						}
					});
			}

			$scope.checkPendingApprovals = function() {

				if($scope.dashboardDetails.pendingApprovalsRedirectionPage == '')
					return;

				var sPageURL = window.location.search.substring(1);
				var sURLVariables = sPageURL.split('&');
				var foundLoggedInVar  = "false";
				for (var i = 0; i < sURLVariables.length; i++)
				{
					var sParameterName = sURLVariables[i].split('=');
					if (sParameterName[0] == "loggedin")
					{
						foundLoggedInVar  = "true";
						if(sParameterName[1] != "true")
							return;
					}
				}
				if(foundLoggedInVar == "true")
					$scope.redirectToPendingReqs($scope.dashboardDetails.pendingApprovalsRedirectionPage);
			}

        $scope.logout = function() {
                    $http({
                        method: "POST",
                        url: "logout",
                        headers : { 'Content-Type' : 'application/json' }
                    }).success(function(output) {
                        $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/" + "login";
                    }).error(
                        function(error)
                        {
                            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/" + "login";
                        }
                    );
                }


        $scope.sendMessageToAdmin = function(){

                if(!$scope.contactFormSubject)
                    return;
                if(!$scope.contactFormMessage)
                    return;
                if($scope.contactFormSubject.trim().length==0)
                    return;
                if($scope.contactFormMessage.trim().length==0)
                    return;

                $http({
                        method: "POST",
                        url: "sendMessageToAdmin",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'contactFormSubject' : $scope.contactFormSubject,'contactFormMessage' : $scope.contactFormMessage },
                        data:  {'contactFormSubject' : $scope.contactFormSubject,'contactFormMessage' : $scope.contactFormMessage }
                    }).success(function(output) {
                        $scope.alert = "Message Sent.";
                        swal({
                             title: "",
                             text: "Message sent.",
                             timer: 2000,
                             showConfirmButton: false
                         });
                    }).error(
                        function(error)
                        {
                            $scope.alert = error;
                        }
                    );
            }
}
);
