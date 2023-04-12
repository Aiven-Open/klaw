'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('connectorOverviewApp',['textAngular']);

app.controller("connectorOverviewCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	//$http.defaults.headers.common['Accept'] = 'application/json';
	$scope.envSelectedParam;

	$scope.showSubmitFailed = function(title, text){
		swal({
			 title: "",
			 text: "Request unsuccessful !!",
			 timer: 2000,
			 showConfirmButton: false
			 });
	}

	$scope.showAlertToast = function() {
              var x = document.getElementById("alertbar");
              x.className = "show";
              setTimeout(function(){ x.className = x.className.replace("show", ""); }, 2000);
            }

    $scope.handleErrorMessage = function(error){
                if(error != null && error.message != null){
                    $scope.alert = error.message;
                    $scope.alertnote = $scope.alert;
                    $scope.showAlertToast();
                }else{
                        $scope.alert = error;
                        $scope.alertnote = error;
                        $scope.showAlertToast();
                }
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

	$scope.getEnvs = function() {

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

            if(output.viewTopics!='Authorized')
            {
                $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/index";
            }

            if(output.companyinfo == null){
                $scope.companyinfo = "Company not defined!!";
            }
            else
                $scope.companyinfo = output.companyinfo;

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
                if (isConfirm.dismiss !== "cancel") {
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
						if (isConfirm.dismiss != "cancel") {
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

	$scope.onDeleteConnector = function(env, topicDeletable){
	    $scope.alertTopicDelete = null;
	    $scope.alert = null;

        swal({
                    title: "Are you sure?",
                    text: "You would like to delete this connector ?",
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: "Yes, request for deletion!",
                    cancelButtonText: "No, cancel please!",
                    closeOnConfirm: true,
                    closeOnCancel: true
                }).then(function(isConfirm){
                    if (isConfirm.dismiss != "cancel") {
                        $http({
                                method: "POST",
                                url: "createConnectorDeleteRequest",
                                headers : { 'Content-Type' : 'application/json' },
                                params: {'connectorName' : $scope.topicSelectedParam,
                                                           'env' : env},
                                data: {'connectorName' : $scope.topicSelectedParam,
                                                           'env' : env},
                            }).success(function(output) {

                                if(output.success){
                                    swal({
                                         title: "",
                                         text: "Connector Delete Request : "+output.message,
                                         showConfirmButton: true
                                     }).then(function(isConfirm){
                                            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/myConnectorRequests?reqsType=CREATED&deleteConnectorCreated=true";
                                        });
                                }
                                else{
                                        $scope.alertTopicDelete = "Connector Delete Request : "+output.message;
                                        $scope.alertnote = $scope.alertTopicDelete;
                                        $scope.showSubmitFailed('','');
                                    }
                            }).error(
                                function(error)
                                {
                                    $scope.handleValidationErrors(error);
                                }
                            );
                    } else {
                        return;
                    }
                });
	}

	$scope.onClaimConnector = function(topicName, env){
    	    $scope.alert = null;

            swal({
                        title: "Are you sure?",
                        text: "You would like to claim this connector ?",
                        type: "warning",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55",
                        confirmButtonText: "Yes, request for ownership!",
                        cancelButtonText: "No, cancel please!",
                        closeOnConfirm: true,
                        closeOnCancel: true
                    }).then(function(isConfirm){
                        if (isConfirm.dismiss != "cancel") {
                            $http({
                                    method: "POST",
                                    url: "createClaimConnectorRequest",
                                    headers : { 'Content-Type' : 'application/json' },
                                    params: {'connectorName' : $scope.topicSelectedParam,
                                                               'env' : env},
                                    data: {'connectorName' : $scope.topicSelectedParam,
                                                               'env' : env},
                                }).success(function(output) {

                                    if(output.success){
                                        swal({
                                             title: "",
                                             text: "Connector Claim Request : "+output.message,
                                             showConfirmButton: true
                                         }).then(function(isConfirm){
                                                $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/myConnectorRequests?reqsType=CREATED&claimConnectorCreated=true";
                                             });
                                    }
                                    else{
                                            $scope.alertTopicDelete = "Connector Claim Request : "+output.message;
                                            $scope.alertnote = "Connector Claim Request : "+output.message;
                                            $scope.showSubmitFailed('','');
                                        }
                                }).error(
                                    function(error)
                                    {
                                        $scope.handleValidationErrors(error);
                                    }
                                );
                        } else {
                            return;
                        }
                    });
    	}

    	$scope.onFinalPromote = function(envSelected) {

                var serviceInput = {};
                $scope.alertTopicDelete = null;
                $scope.alert = null;

                if(!$scope.promotionDetails.sourceConnectorConfig){
                    $scope.alertnote = "Please fill in a valid config.";
                    $scope.alert = $scope.alertnote;
                    $scope.showAlertToast();
                    return;
                }

                serviceInput['environment'] = envSelected;
                serviceInput['connectorName'] = $scope.topicSelectedParam;
                serviceInput['connectorConfig'] = $scope.promotionDetails.sourceConnectorConfig;
                serviceInput['teamName'] = $scope.teamname;
                serviceInput['remarks'] = "Connector promotion."
                serviceInput['connectortype'] = 'Create';
                serviceInput['description'] = $scope.topicSelectedParam + " connector."

                swal({
                        title: "Are you sure?",
                        text: "You would like to promote this connector ?",
                        type: "warning",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55",
                        confirmButtonText: "Yes, please proceed!",
                        cancelButtonText: "No, cancel please!",
                        closeOnConfirm: true,
                        closeOnCancel: true
                    }).then(function(isConfirm){
                        if (isConfirm.dismiss !== "cancel") {
                            $http({
                                    method: "POST",
                                    url: "createConnector",
                                    headers : { 'Content-Type' : 'application/json' },
                                    params: {'addTopicRequest' : serviceInput },
                                    data: serviceInput
                                }).success(function(output) {
                                    if(output.success){
                                        swal({
                                             title: "",
                                             text: "Connector Promotion Request : "+output.message,
                                             showConfirmButton: true
                                         }).then(function(isConfirm){
                                                $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/myConnectorRequests?reqsType=CREATED&connectorPromotionCreated=true";
                                          });
                                    }
                                    else{
                                            $scope.alert = "Connector Promotion Request : "+output.message;
                                            $scope.showSubmitFailed('','');
                                        }
                                }).error(
                                    function(error)
                                    {
                                        $scope.handleValidationErrors(error);
                                    }
                                )
                        } else {
                            return;
                        }
                    });
            }

    	$scope.onFirstPromote = function(envSelected){
    	    $scope.firstPromote = "true";
    	    $scope.alertTopicDelete = null;
    	    $scope.alert = null;

    	}

    $scope.addDocsVar = false;

    $scope.addDocs = function(){
        $scope.addDocsVar = 'true';
    }

    $scope.updateDocs = function(){
        $scope.addDocsVar = 'true';
    }

    $scope.cancelSaveDocs = function() {
        $scope.topicDocumentation = $scope.tmpTopicDocumentation;
        $scope.addDocsVar = false;
    }

    $scope.saveDocs = function(){

            if($scope.topicDocumentation == null || $scope.topicDocumentation.length==0)
                {
                    $scope.alertnote = "Please add some documentation related to connector.";
                    $scope.showAlertToast();
                    return;
                }else
                {
                    $scope.topicDocumentation = $scope.topicDocumentation.trim();
                    if($scope.topicDocumentation.length==0)
                    {
                        $scope.alertnote = "Please add some documentation related to topic.";
                        $scope.showAlertToast();
                        return;
                    }
                }

            var serviceInput = {};
            serviceInput['connectorId'] = $scope.topicIdForDocumentation;
            serviceInput['connectorName'] = $scope.topicSelectedParam;
            serviceInput['documentation'] = $scope.topicDocumentation;

            swal({
                        title: "Are you sure?",
                        text: "You would like to save the documentation?",
                        type: "warning",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55",
                        confirmButtonText: "Yes, save it!",
                        cancelButtonText: "No, cancel please!",
                        closeOnConfirm: true,
                        closeOnCancel: true
                    }).then(function(isConfirm){
                        if (isConfirm.dismiss !== "cancel") {
                            $http({
                                    method: "POST",
                                    url: "saveConnectorDocumentation",
                                    headers : { 'Content-Type' : 'application/json' },
                                    data: serviceInput,
                                }).success(function(output) {
                                    if(output.success){
                                        swal({
                                             title: "",
                                             text: "Documentation Update Request : "+output.message,
                                             timer: 2000,
                                             showConfirmButton: false
                                         });
                                         $scope.addDocsVar = false;
                                         $scope.tmpTopicDocumentation = $scope.topicDocumentation;
                                         document.getElementById("topicDocId").innerHTML = $scope.topicDocumentation;
                                    }
                                    else{
                                            $scope.alertTopicDelete = "Documentation Update Request : "+output.message;
                                            $scope.alertnote = $scope.alertTopicDelete;
                                            $scope.showSubmitFailed('','');
                                        }
                                }).error(
                                    function(error)
                                    {
                                        $scope.handleValidationErrors(error);
                                    }
                                );
                        } else {
                            return;
                        }
                    });
    }

    $scope.getVersionSchema = function(schemaVersion){
        $scope.newSchemaVersion = schemaVersion;
        $scope.getAcls();
    }

	$scope.getConnectorDetails = function() {
        $scope.firstPromote = "false";
        $scope.alertTopicDelete = null;
        $scope.alert = null;

        var serviceInput = {};

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
        }

        if(!topicSelected)
            return;

		$scope.topicSelectedParam = topicSelected;

		$http({
			method: "GET",
			url: "getConnectorOverview",
            headers : { 'Content-Type' : 'application/json' },
            params: {'connectornamesearch' : topicSelected
             }
		}).success(function(output) {
		    if(output.connectorExists === true){
		        $scope.resultBrowse = output.aclInfoList;
            	$scope.connectorOverview = output.connectorInfoList;
            	$scope.promotionDetails = output.promotionDetails;

                $scope.topicHistoryList = output.topicHistoryList;

            	$scope.topicDocumentation = output.topicDocumentation;
            	$scope.tmpTopicDocumentation = output.topicDocumentation;
            	$scope.topicIdForDocumentation = output.topicIdForDocumentation;
            	document.getElementById("topicDocId").innerHTML = output.topicDocumentation;
		    }
		    else
		        $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/kafkaConnectors";
		}).error(
			function(error) 
			{
				$scope.alert = error;
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

        $scope.getOffsetValues = function(environment, topicName, consumergroup){
            $http({
                method: "GET",
                url: "getConsumerOffsets",
                headers : { 'Content-Type' : 'application/json' },
                params: {'env' : environment, 'topicName' : topicName, 'consumerGroupId' : consumergroup},
            }).success(function(output) {
                $scope.consumeroffsetInfo = output;
                var result = "";
                var i;
                for (i = 0; i < output.length; i++) {
                  result += "Partition "+output[i].topicPartitionId;
                  result += " CurrentOffset "+output[i].currentOffset;
                  result += " EndOffset "+output[i].endOffset;
                  result += " Lag "+output[i].lag + "\n";
                }

                if(output.length == 0)
                {
                    swal({
                        title: "Group Id: " + consumergroup,
                        text: "No offsets information found."
                    });
                }else{
                swal({
                        title: "Group Id: " + consumergroup,
                        text: result
                    });
                }

            }).error(
                function(error)
                {
                    $scope.handleErrorMessage(error);
                }
            );
        }

        $scope.showTopicEvents = function() {
                $scope.topiccontents = null;

                if(!$scope.topicEventsSelectedEnv || $scope.topicEventsSelectedEnv == ""){
                    swal({
                         title: "",
                         text: "Please select an Environment !!",
                         timer: 2000,
                         showConfirmButton: false
                         });
                    return;
                }


                if(!$scope.topicOffsetsVal || $scope.topicOffsetsVal == ""){
                    swal({
                         title: "",
                         text: "Please select an Offset Position !!",
                         timer: 2000,
                         showConfirmButton: false
                         });
                    return;
                }

                $scope.ShowSpinnerStatus = true;

                $http({
                    method: "GET",
                    url: "getTopicEvents",
                    headers : { 'Content-Type' : 'application/json' },
                    params : {'topicName' : $scope.topicSelectedParam, 'offsetId' : $scope.topicOffsetsVal,
                     'envId' : $scope.topicEventsSelectedEnv, 'consumerGroupId': "notdefined"}
                }).success(function(output) {
                    $scope.ShowSpinnerStatus = false;
                    if(output.status != null && output.status === "false"){
                        swal({
                             title: "",
                             text: "No events found or Could not be retrieved !!",
                             timer: 2000,
                             showConfirmButton: true
                             });
                    }else
                        $scope.topiccontents = output;
                }).error(
                    function(error)
                    {
                        $scope.ShowSpinnerStatus = false;
                        $scope.alert = error;
                        $scope.alertnote = error;
                        $scope.showAlertToast();
                    }
                );
            }

}
);