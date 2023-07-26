'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('editTopicRequestApp',[]);

app.controller("editTopicRequestCtrl", function($scope, $http, $location, $window) {
	
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

         $scope.getEnvTopicPartitions = function(envSelected){
                $scope.topicPrefix = null;
                $scope.topicSuffix = null;
                $scope.topicRegex = null;
                $scope.applyRegex = null;
             	    $http({
                            method: "GET",
                            url: "getEnvParams",
                             headers : { 'Content-Type' : 'application/json' },
                             params: {'envSelected' : envSelected }
                        }).success(function(output) {
                            $scope.envTopicMap = output;
                            $scope.addTopic.topicpartitions = '' + $scope.addTopic.topicpartitions;
                            $scope.addTopic.replicationfactor = '' + $scope.addTopic.replicationfactor;

                            if($scope.envTopicMap.defaultRepFactor === $scope.addTopic.replicationfactor){
                                $scope.addTopic.replicationfactor = $scope.envTopicMap.defaultRepFactor + " (default)"
                            }
                            if($scope.envTopicMap.defaultPartitions === $scope.addTopic.topicpartitions){
                                $scope.addTopic.topicpartitions = $scope.envTopicMap.defaultPartitions + " (default)"
                            }
                            $scope.checkPartitionAndRepFactorWarnings();
                        }).error(
                            function(error)
                            {
                                $scope.alert = error;
                            }
                        );
             	}

        $scope.requestTitle = "Topic Create Request";
        $scope.requestType = "CreateTopic";
        $scope.requestButton = "Submit";

        $scope.loadEditTopicInfo = function(){
            var topicRequestId;

            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');
            for (var i = 0; i < sURLVariables.length; i++)
            {
                var sParameterName = sURLVariables[i].split('=');
                if (sParameterName[0] === "topicRequestId")
                {
                    topicRequestId = sParameterName[1];
                }
            }

            if(topicRequestId){
                $scope.getMyTopicRequestDetail(topicRequestId);
            }

        }

        $scope.checkPartitionAndRepFactorWarnings = function() {

                if(parseInt($scope.envTopicMap.defaultRepFactor,10) > parseInt($scope.addTopic.replicationfactor,10)) {
                $scope.repFactorWarn = 'Replication factor is below default value';
                } else {
                $scope.repFactorWarn = '';
                }

                if(parseInt($scope.envTopicMap.defaultPartitions,10) > parseInt($scope.addTopic.topicpartitions,10)) {
                $scope.partitionWarn = 'Partitions is below default value';
                } else {
                $scope.partitionWarn = '';
                }
        }

         $scope.getTopicEnvDetails = function(envSelected, topicSelected) {
                    $http({
                        method: "GET",
                        url: "getTopicDetailsPerEnv",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'envSelected' : envSelected,  'topicname' : topicSelected }
                    }).success(function(output) {
                        if(output.topicExists){
                            $scope.oldtopicpartitions = output.topicContents.noOfPartitions;
                            $scope.addTopic.topicpartitions = '' + output.topicContents.noOfPartitions;
                            $scope.addTopic.replicationfactor = '' + output.topicContents.noOfReplicas;
                            $scope.addTopic.advancedTopicConfiguration = output.topicContents.advancedTopicConfiguration
                            $scope.addTopic.description = output.topicContents.description;
                            //set env defaults
                            $scope.addTopic.topicpartitions = $scope.envTopicMap.defaultPartitions  + " (default)"
                            $scope.addTopic.replicationfactor = $scope.envTopicMap.defaultRepFactor + " (default)"


                            $scope.checkPartitionAndRepFactorWarnings();

                            for (let m in $scope.addTopic.advancedTopicConfiguration){
                                $scope.topicConfigsSelectedDropdown.push(m);
                                $scope.topicConfigsSelected.push(output.topicContents.advancedTopicConfiguration[m]);
                                $scope.propertyInfoLink.push(apacheKafkaTopicConfigsUrl + m);
                            }
                        }
                        else{
                            $scope.alertnote = "Unable to load Promotion Config from lower environment.";
                            $scope.showAlertToast();
                        }
                    }).error(
                        function(error)
                        {
                            $scope.alert = error;
                        }
                    );
                }

        $scope.submitEditTopicRequest = function() {

            var serviceInput = {};

            $scope.alert = null;
            $scope.alertnote = null;

            if(!$scope.addTopic.environment || $scope.addTopic.environment === "")
            {
                $scope.alertnote = "Please select an environment.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addTopic.topicname == null || $scope.addTopic.topicname.length === 0)
            {
                $scope.alertnote = "Please fill in topic name.";
                $scope.showAlertToast();
                return;
            }else
            {
                $scope.addTopic.topicname = $scope.addTopic.topicname.trim();
                if($scope.addTopic.topicname.length === 0)
                {
                    $scope.alertnote = "Please fill in topic name.";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addTopic.topicname.indexOf(" ") > 0){
                    $scope.alertnote = "Topic name should not contain any spaces.";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addTopic.topicname.length < 5){
                    $scope.alertnote = "Topic name should be atleast 5 characters.";
                    $scope.showAlertToast();
                    return;
                }
            }

            if(!$scope.addTopic.topicpartitions || $scope.addTopic.topicpartitions === 'selected'){

                $scope.alertnote = "Please select topic partitions.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.addTopic.replicationfactor || $scope.addTopic.replicationfactor === 'selected'){

                $scope.alertnote = "Please select topic replication factor.";
                $scope.showAlertToast();
                return;
            }

            // selecting default partitions

            if(!$scope.addTopic.description)
            {
                $scope.alertnote = "Please fill in description.";
                $scope.showAlertToast();
                return;
            }
            else {
                $scope.addTopic.description = $scope.addTopic.description.trim();
                if($scope.addTopic.description.length === 0)
                {
                    $scope.alertnote = "Please fill in description.";
                    $scope.showAlertToast();
                    return;
                }
            }

            let tmpTopicPartitions = $scope.addTopic.topicpartitions;
            let tmpTopicRepFactor = $scope.addTopic.replicationfactor;

            if(tmpTopicPartitions.indexOf("default") > 0)
            {
                tmpTopicPartitions = tmpTopicPartitions.replace(" (default)","");
            }

            // selecting default rf
            if(tmpTopicRepFactor.indexOf("default") > 0)
            {
                tmpTopicRepFactor = tmpTopicRepFactor.replace(" (default)","");
            }

            let advancedTopicConfigEntries = [];
            let serviceInputTopicConfigs;
            for (let i = 0; i < $scope.topicConfigsSelectedDropdown.length; i++) {
                if($scope.topicConfigsSelectedDropdown[i] !== "" && $scope.topicConfigsSelected[i] !== ""){
                    serviceInputTopicConfigs = {};
                    serviceInputTopicConfigs['configKey'] = $scope.topicConfigsSelectedDropdown[i];
                    serviceInputTopicConfigs['configValue'] = $scope.topicConfigsSelected[i];
                    advancedTopicConfigEntries.push(serviceInputTopicConfigs);
                }
                else if($scope.topicConfigsSelectedDropdown[i] !== "" && $scope.topicConfigsSelected[i] === ""){
                    $scope.alertnote = "Please fill in a value for the selected topic configuration.";
                    $scope.showAlertToast();
                    return;
                }
            }

            serviceInput['requestId'] = $scope.addTopic.topicid;
            serviceInput['environment'] = $scope.addTopic.environment;
            serviceInput['topicname'] = $scope.addTopic.topicname;
            serviceInput['topicpartitions'] = tmpTopicPartitions;
            serviceInput['replicationfactor'] = tmpTopicRepFactor;
            serviceInput['appname'] = "App";//$scope.addTopic.app;
            serviceInput['remarks'] = $scope.addTopic.remarks;
            serviceInput['description'] = $scope.addTopic.description;
            serviceInput['advancedTopicConfigEntries'] = advancedTopicConfigEntries;
            serviceInput['requestOperationType'] = $scope.addTopic.requestOperationType;
            if($scope.addTopic.requestOperationType === 'CREATE' || $scope.addTopic.requestOperationType === 'PROMOTE'){
                serviceInput['requestOperationType'] = 'CREATE';
                $scope.httpCreateTopicReq(serviceInput);
            }
            else{
                if($scope.addTopic.topicpartitions < $scope.oldtopicpartitions) {
                     swal({
                             title: "Are you sure?",
                             text: "To decrease partitions of a topic, topic has to be deleted.",
                             type: "warning",
                             showCancelButton: true,
                             confirmButtonColor: "#DD6B55",
                             confirmButtonText: "Yes, I understand the risks !",
                             cancelButtonText: "No, cancel please!",
                             closeOnConfirm: true,
                             closeOnCancel: true
                         }).then(function(isConfirm){
                         if(serviceInput['remarks'] === undefined ){
                               serviceInput['remarks'] = "Warning To decrease partitions of a topic the topic has to be deleted";
                         } else {
                               serviceInput['remarks'] += " Warning To decrease partitions of a topic the topic has to be deleted";
                         }
                             if (isConfirm.dismiss !== "cancel") {
                                 $scope.httpCreateUpdateTopicReq(serviceInput);
                             }
                         });
                    } else {
                        $scope.httpCreateUpdateTopicReq(serviceInput);
                    }
            }
        }

        $scope.httpCreateTopicReq = function(serviceInput){
            $http({
                    method: "POST",
                    url: "createTopics",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'addTopicRequest' : serviceInput },
                    data: serviceInput
                }).success(function(output) {
                    if(output.success){
                        swal({
                                 title: "Awesome !",
                                 text: "Topic Request : "+output.message,
                                 showConfirmButton: true
                             }).then(function(isConfirm){
                                    $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/myTopicRequests?reqsType=CREATED&topicCreated=true";
                             });
                    }
                    else{
                            $scope.alert = "Topic Request : "+output.message;
                            $scope.showSubmitFailed('','');
                        }
                }).error(
                    function(error)
                    {
                        $scope.handleValidationErrors(error);
                    }
                );
        }

        $scope.httpCreateUpdateTopicReq = function(serviceInput){
                    $http({
                            method: "POST",
                            url: "updateTopics",
                            headers : { 'Content-Type' : 'application/json' },
                            data: serviceInput
                        }).success(function(output) {
                            if(output.success){
                                swal({
                                         title: "Awesome !",
                                         text: "Topic Request : "+output.message,
                                         showConfirmButton: true
                                     }).then(function(isConfirm){
                                            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/myTopicRequests?reqsType=CREATED&topicCreated=true";
                                     });
                            }
                            else{
                                    $scope.alert = "Topic Request : "+output.message;
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
            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/myTopicRequests";
        }

        $scope.getEnvs = function() {

                $http({
                    method: "GET",
                    url: "getEnvsBaseCluster",
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

        $scope.topicConfigsSelected = [];
        $scope.propertyInfoLink = [];
        $scope.topicConfigsSelectedDropdown = [];
        $scope.topicConfigsSelectedLength = $scope.topicConfigsSelected.length;

        $scope.canShowInfo = function(indexOfRec){
            return $scope.propertyInfoLink[indexOfRec] !== "";
        }

        $scope.updateLink = function(indexOfRec){
            $scope.propertyInfoLink[indexOfRec] = apacheKafkaTopicConfigsUrl + $scope.topicConfigsSelectedDropdown[indexOfRec];
        }

        $scope.addConfigRecord = function(indexToAdd){
            $scope.propertyInfoLink.push("");
            $scope.topicConfigsSelected.push("");
            $scope.topicConfigsSelectedLength = $scope.topicConfigsSelected.length;
        }

        $scope.removeConfigRecord = function(indexToRemove){
            if($scope.topicConfigsSelected.length === 1){}
            else{
                $scope.topicConfigsSelected.splice(indexToRemove, 1);
                $scope.topicConfigsSelectedDropdown.splice(indexToRemove, 1);
            }
            $scope.topicConfigsSelectedLength = $scope.topicConfigsSelected.length;
        }

        $scope.getTopicConfigs = function(){
            $http({
                method: "GET",
                url: "getAdvancedTopicConfigs",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.topicConfigs = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

        $scope.getMyTopicRequestDetail = function(topicReqId) {
            $http({
                method: "GET",
                url: "topic/request/" + topicReqId,
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.topicRequestDetail = output;
                if(output != null && output !== ''){
                    $scope.addTopic = $scope.topicRequestDetail;
                    if($scope.addTopic.requestOperationType === 'CREATE'){
                        $scope.requestTitle = "Topic Create Request";
                    }
                    else if($scope.addTopic.requestOperationType === 'UPDATE'){
                        $scope.requestTitle = "Topic Update Request";
                    }else if($scope.addTopic.requestOperationType === 'PROMOTE'){
                        $scope.requestTitle = "Topic Promote Request";
                    }
                    $scope.getEnvTopicPartitions($scope.addTopic.environment);

                    if($scope.addTopic.advancedTopicConfigEntries.length > 0){
                        for (let m in $scope.addTopic.advancedTopicConfigEntries){
                            $scope.topicConfigsSelectedDropdown.push($scope.addTopic.advancedTopicConfigEntries[m].configKey);
                            $scope.topicConfigsSelected.push($scope.addTopic.advancedTopicConfigEntries[m].configValue);
                            $scope.propertyInfoLink.push(apacheKafkaTopicConfigsUrl + m);
                        }
                    }
                    else{
                        $scope.addConfigRecord(0);
                    }
                    $scope.topicConfigsSelectedLength = $scope.topicConfigsSelected.length;
                }else{
                    $scope.alertnote = "Request not found.";
                    $scope.showAlertToast();
                    $window.location.href = $window.location.origin;
                }

            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.topicRequests = null;
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

                    if(output.requestItems !== 'Authorized')
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