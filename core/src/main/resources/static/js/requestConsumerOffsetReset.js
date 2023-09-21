'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('requestConsumerOffsetResetApp',[]);

app.controller("requestConsumerOffsetResetCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	$http.defaults.headers.common['Accept'] = 'application/json';
    const apacheKafkaTopicConfigsUrl = "https://kafka.apache.org/documentation/#topicconfigs_";

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


        $scope.requestTitle = "Reset Consumer Offsets Request";
        $scope.requestType = "Reset Consumer Offsets Request";
        $scope.requestButton = "Submit";
        $scope.resetTypes = ["EARLIEST", "LATEST", "TO_DATE_TIME"];

        $scope.loadResetEnvironmentInfo = function(){
            var envSelected, topicSelected, consumerGroup;

            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');
            for (var i = 0; i < sURLVariables.length; i++)
            {
                var sParameterName = sURLVariables[i].split('=');
                if (sParameterName[0] === "topic")
                {
                    topicSelected = sParameterName[1];
                }
                else if (sParameterName[0] === "envId")
                {
                    envSelected = sParameterName[1];
                }
                else if (sParameterName[0] === "consumerGroup")
                {
                    consumerGroup = sParameterName[1];
                }
            }

            if(!topicSelected && !envSelected && !consumerGroup){
                $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/index";
            }

            $scope.addOperationalReqResetOffset.topicName = topicSelected;
            $scope.addOperationalReqResetOffset.consumerGroup = consumerGroup;
            $scope.addOperationalReqResetOffset.envId = envSelected;
            $scope.validateOffsetRequestDetails(envSelected, topicSelected, consumerGroup);
        }

        $scope.validateOffsetRequestDetails = function(envSelected, topicSelected, consumerGroup) {
            $http({
                method: "GET",
                url: "operationalRequest/consumerOffsetsReset/validate",
                headers : { 'Content-Type' : 'application/json' },
                params: {'envId' : envSelected,
                    'topicName' : topicSelected,
                    'consumerGroup' : consumerGroup
                }
            }).success(function(output) {
                if(!output){
                    $scope.alertnote = "Unable to retrieve offset details. Redirecting to home page.";
                    $scope.showAlertToast();
                    $window.location.href = $window.location.origin;
                }else{
                    $scope.addOperationalReqResetOffset.envName = output.name;
                }
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

        $scope.addOperationalReqResetOffset = function() {

            var serviceInput = {};

            $scope.alert = null;
            $scope.alertnote = null;

            if(!$scope.addOperationalReqResetOffset.envName || $scope.addOperationalReqResetOffset.envName === "")
            {
                $scope.alertnote = "Please select an environment.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addOperationalReqResetOffset.topicName == null || $scope.addOperationalReqResetOffset.topicName.length === 0)
            {
                $scope.alertnote = "Please fill in topic name.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addOperationalReqResetOffset.consumerGroup == null || $scope.addOperationalReqResetOffset.consumerGroup.length === 0)
            {
                $scope.alertnote = "Please fill in consumerGroup.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addOperationalReq.resetType === 'TO_DATE_TIME')
            {
                if(!$scope.addOperationalReq.resetTimestamp){
                    $scope.alertnote = "Please fill in timestamp in UTC timezone.";
                    $scope.showAlertToast();
                    return;
                }
            }

            serviceInput['environment'] = $scope.addOperationalReqResetOffset.envId;
            serviceInput['topicname'] = $scope.addOperationalReqResetOffset.topicName;
            serviceInput['consumerGroup'] = $scope.addOperationalReqResetOffset.consumerGroup;
            serviceInput['offsetResetType'] = $scope.addOperationalReq.resetType;
            serviceInput['resetTimeStampStr'] = $scope.addOperationalReq.resetTimestamp;
            serviceInput['operationalRequestType'] = 'RESET_CONSUMER_OFFSETS';
            serviceInput['remarks'] = $scope.addOperationalReqResetOffset.remarks;
            $scope.httpCreateTopicReq(serviceInput);
        };

        $scope.httpCreateTopicReq = function(serviceInput){
            $http({
                    method: "POST",
                    url: "/operationalRequest/consumerOffsetsReset/create",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'consumerOffsetResetRequestModel' : serviceInput },
                    data: serviceInput
                }).success(function(output) {
                    if(output.success){
                        swal({
                                 title: "Awesome !",
                                 text: "Consumer offset reset Request : "+output.message,
                                 showConfirmButton: true
                             }).then(function(isConfirm){
                                    $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/myOperationalRequests?reqsType=CREATED&opReqCreated=true";
                             });
                    }
                    else{
                            $scope.alert = "Reset Request : "+output.message;
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
            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/browseTopics";
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

                   $scope.checkSwitchTeams($scope.dashboardDetails.canSwitchTeams, $scope.dashboardDetails.teamId, $scope.userlogged);
                   $scope.checkPendingApprovals();
               }).error(
                   function(error)
                   {
                       $scope.alert = error;
                   }
               );
               	}

        $scope.onSwitchTeam = function() {
            var serviceInput = {};
            serviceInput['username'] = $scope.userlogged;
            serviceInput['teamId'] = $scope.teamId;

            swal({
                title: "Are you sure?",
                text: "You would like to update your team ?",
                type: "warning",
                showCancelButton: true,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: "Yes !",
                cancelButtonText: "No, cancel please!",
                closeOnConfirm: true,
                closeOnCancel: true
            }).then(function(isConfirm) {
                if (isConfirm.value) {
                    $http({
                        method: "POST",
                        url: "user/updateTeam",
                        headers : { 'Content-Type' : 'application/json' },
                        data: serviceInput
                    }).success(function (output) {
                        $scope.alert = "User team update request : "+output.message;
                        if(output.success){
                            swal({
                                title: "",
                                text: "User team update request : "+output.message,
                                timer: 2000,
                                showConfirmButton: true
                            }).then(function(isConfirm){
                                $scope.refreshPage();
                            });
                        }else $scope.showSubmitFailed('','');
                    }).error(
                        function (error) {
                            $scope.handleValidationErrors(error);
                        }
                    );
                } else {
                    return;
                }
            });
        }

        $scope.checkSwitchTeams = function(canSwitchTeams, teamId, userId){
            if(canSwitchTeams === 'true'){
                $scope.teamId = parseInt(teamId);
                $scope.getSwitchTeamsList(userId);
            }
        }

        $scope.getSwitchTeamsList = function(userId) {
            $http({
                method: "GET",
                url: "user/" + userId + "/switchTeamsList",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.switchTeamsListDashboard = output;
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
						if (isConfirm.value) {
							$window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/"+redirectPage;
						} else {
							return;
						}
					});
			}

            $scope.checkPendingApprovals = function() {
                if($scope.dashboardDetails.pendingApprovalsRedirectionPage === '')
                    return;
                
                if(sessionStorage.getItem("pending_reqs_shown") === null){
                    $scope.redirectToPendingReqs($scope.dashboardDetails.pendingApprovalsRedirectionPage);
                    sessionStorage.setItem("pending_reqs_shown", "true");
                }
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