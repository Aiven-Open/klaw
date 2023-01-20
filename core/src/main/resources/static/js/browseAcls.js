'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('browseAclsApp',['textAngular']);

app.controller("browseAclsCtrl", function($scope, $http, $location, $window) {
	
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

    $scope.showSuccessToast = function() {
        var x = document.getElementById("successbar");
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
                url: "getEnvs",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.allenvs = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.alertnote = error;
                    $scope.showAlertToast();
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

	$scope.onDeleteTopic = function(env, topicDeletable){
	    $scope.alertTopicDelete = null;
	    $scope.alert = null;

        if(!topicDeletable){
            $scope.alertTopicDelete = "Please delete all the subscriptions in "+ env +" cluster, before topic can be deleted.";
            return;
        }

        swal({
                    title: "Are you sure?",
                    text: "You would like to delete this topic ?",
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
                                url: "createTopicDeleteRequest",
                                headers : { 'Content-Type' : 'application/json' },
                                params: {'topicName' : $scope.topicSelectedParam,
                                                           'env' : env},
                                data: {'topicName' : $scope.topicSelectedParam,
                                                           'env' : env},
                            }).success(function(output) {

                                if(output.result == 'success'){
                                    swal({
                                         title: "",
                                         text: "Topic Delete Request : "+output.result,
                                         showConfirmButton: true
                                     }).then(function(isConfirm){
                                            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/myTopicRequests?reqsType=created&deleteTopicCreated=true";
                                        });
                                }
                                else{
                                        $scope.alertTopicDelete = "Topic Delete Request : "+output.result;
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

	$scope.onClaimTopic = function(topicName, env){
    	    $scope.alert = null;

            swal({
                        title: "Are you sure?",
                        text: "You would like to claim this topic ?",
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
                                    url: "createClaimTopicRequest",
                                    headers : { 'Content-Type' : 'application/json' },
                                    params: {'topicName' : $scope.topicSelectedParam,
                                                               'env' : env},
                                    data: {'topicName' : $scope.topicSelectedParam,
                                                               'env' : env},
                                }).success(function(output) {

                                    if(output.result == 'success'){
                                        swal({
                                             title: "",
                                             text: "Topic Claim Request : "+output.result,
                                             showConfirmButton: true
                                         }).then(function(isConfirm){
                                                $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath +  "/myTopicRequests?reqsType=created&claimTopicCreated=true";
                                             });
                                    }
                                    else{
                                            $scope.alertTopicDelete = "Topic Claim Request : "+output.result;
                                            $scope.alertnote = "Topic Claim Request : "+output.result;
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

	$scope.deleteAcls = function(reqNo, teamAcl){
            $scope.alertTopicDelete = null;
            $scope.alert = null;

            if($scope.teamname != teamAcl){
                $scope.alert = "Not authorized. You are part of a different team.";
                $scope.alertnote = $scope.alert;
                $scope.showAlertToast();
                return;
            }

            swal({
                        title: "Are you sure?",
                        text: "You would like to delete this subscription ?",
                        type: "warning",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55",
                        confirmButtonText: "Yes, request for deletion !",
                        cancelButtonText: "No, cancel please!",
                        closeOnConfirm: true,
                        closeOnCancel: true
                    }).then(function(isConfirm){
                        if (isConfirm.dismiss != "cancel") {
                            $http({
                                    method: "POST",
                                    url: "createDeleteAclSubscriptionRequest",
                                    headers : { 'Content-Type' : 'application/json' },
                                    params: {'req_no' : reqNo },
                                    data: {'req_no' : reqNo }
                                }).success(function(output) {
                                    if(output.result != 'success'){
                                        $scope.alert = "Delete Request : "+output.result+ ". Please check if there is any pending request.";
                                        $scope.showSubmitFailed('','');
                                     }
                                    else{
                                            $scope.alert = "Delete Subscription Request : "+output.result;
                                            swal({
                                                 title: "",
                                                 text: "Delete Subscription Request : "+output.result,
                                                 showConfirmButton: true
                                             }).then(function(isConfirm){
                                                    $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/myAclRequests?reqsType=created&deleteAclCreated=true";
                                                });
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

        $scope.onFirstSchemaPromote= function(schemaContent,allSchemaVersions){
                  $scope.firstSchemaPromote = 'true';
                  $scope.firstSchemaEnvPromote = schemaContent.env;
                  $scope.schema = {};
                  $scope.schema.forceRegister = 'false';
                  const schemaVersions = [];

                // Add Latest to let the user know which schema version is the current latest schema version.
                  allSchemaVersions[schemaContent.env].forEach(function(part, index) {
                    schemaVersions[index] = this[index];
                    if(this[index] == schemaContent.version) {
                    schemaVersions[index] = schemaVersions[index] + " (latest)";
                        }
                  }, allSchemaVersions[schemaContent.env]);

                  $scope.schemaVersions = schemaVersions;

                  //future will add check here if force promote is allowed based on setting in server config.
                  $scope.isForceRegisterAllowed = 'true';
                }

        $scope.onFinalSchemaPromote = function(sourceEnvironment,targetEnvironment) {

             if(isNaN($scope.schema.versionSelected) && $scope.schema.versionSelected.includes(" (latest)")){
             $scope.schema.versionSelected = $scope.schema.versionSelected.replace(' (latest)','')
             }
                // If the version is not a number it has not been correctly selected.
             if(isNaN($scope.schema.versionSelected)){
                $scope.alertnote = "Please select the schema version.";
                $scope.alert = $scope.alertnote;
                $scope.showAlertToast();
                return;
                }
             var remarks = "Schema promotion.";
            //Ensure if force Register is not allowed any value is set to false.
             if(!$scope.isForceRegisterAllowed) {
             $scope.schema.forceRegister='false';
             }

             if($scope.schema.forceRegister === true) {
             remarks += " Force Register Schema option overriding schema compatibility has been selected."
             }

            var promoteSchemaReq = {};
            promoteSchemaReq['targetEnvironment'] = targetEnvironment;
            promoteSchemaReq['sourceEnvironment'] = sourceEnvironment;
            promoteSchemaReq['topicName'] = $scope.topicSelectedParam;
            promoteSchemaReq['schemaVersion'] = $scope.schema.versionSelected;
            promoteSchemaReq['forceRegister'] = $scope.schema.forceRegister;
            promoteSchemaReq['appName'] = "App";
            promoteSchemaReq['remarks'] = remarks;

            $http({
                                method: "POST",
                                url: "/promote/schema",
                                headers : { 'Content-Type' : 'application/json' },
                                data: promoteSchemaReq
                            }).success(function(output) {
                                if(output.result == 'success'){
                                    swal({
                                    	 title: "",
                                    	 text: "Schema Promotion Request : " + output.result,
                                    	 showConfirmButton: true
                                     }).then(function(isConfirm){
                                           $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/mySchemaRequests?reqsType=created";
                                      });
                                }
                                else{
                                        $scope.alert = "Schema Promotion Request : " + output.result;
                                        $scope.showSubmitFailed('','');
                                    }
                            }).error(
                                function(error)
                                {
                                    $scope.handleValidationErrors(error);
                                }
                            );

        }

    	$scope.onFinalPromote = function(envSelected) {

                var serviceInput = {};
                $scope.alertTopicDelete = null;
                $scope.alert = null;

                if(!$scope.partitionsSelected || $scope.partitionsSelected == 'selected'){
                    $scope.alertnote = "Please select topic partitions.";
                    $scope.alert = $scope.alertnote;
                    $scope.showAlertToast();
                    return;
                }

                var tmpPartitionsSelected = $scope.partitionsSelected;
                var tmpRepsSelected = $scope.replicationFactorSelected;

                // selecting default partitions
                if(tmpPartitionsSelected.indexOf("default") > 0)
                {
                    tmpPartitionsSelected = tmpPartitionsSelected.replace(" (default)","");
                }

                // selecting default rf
                if(tmpRepsSelected.indexOf("default") > 0)
                {
                    tmpRepsSelected = tmpRepsSelected.replace(" (default)","");
                }

                serviceInput['environment'] = envSelected;
                serviceInput['topicname'] = $scope.topicSelectedParam;
                serviceInput['topicpartitions'] = tmpPartitionsSelected;
                serviceInput['replicationfactor'] = tmpRepsSelected;
                serviceInput['teamname'] = $scope.teamname;
                serviceInput['appname'] = "App";//$scope.addTopic.app;
                serviceInput['remarks'] = "Topic promotion."
                serviceInput['topictype'] = 'Create';
                serviceInput['description'] = $scope.topicSelectedParam + " topic."

                $http({
                    method: "POST",
                    url: "createTopics",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'addTopicRequest' : serviceInput },
                    data: serviceInput
                }).success(function(output) {
                    if(output.result == 'success'){
                        swal({
                        	 title: "",
                        	 text: "Topic Promotion Request : "+output.result,
                        	 showConfirmButton: true
                         }).then(function(isConfirm){
                                $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/myTopicRequests?reqsType=created&topicPromotionCreated=true";
                          });
                    }
                    else{
                            $scope.alert = "Topic Promotion Request : "+output.result;
                            $scope.showSubmitFailed('','');
                        }
                }).error(
                    function(error)
                    {
                        $scope.handleValidationErrors(error);
                    }
                );

            };

    	$scope.onFirstPromote = function(envSelected){
    	    $scope.firstPromote = "true";
    	    $scope.alertTopicDelete = null;
    	    $scope.alert = null;

    	    $http({
            			method: "GET",
            			url: "getEnvParams",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'envSelected' : envSelected }
            		}).success(function(output) {
            			$scope.promotionParams = output;
            		}).error(
            			function(error)
            			{
            				$scope.alert = error;
            			}
            		);
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
                    $scope.alertnote = "Please add some documentation related to topic.";
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
            serviceInput['topicid'] = $scope.topicIdForDocumentation;
            serviceInput['topicName'] = $scope.topicSelectedParam;
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
                        if (isConfirm.dismiss != "cancel") {
                            $http({
                                    method: "POST",
                                    url: "saveTopicDocumentation",
                                    headers : { 'Content-Type' : 'application/json' },
                                    data: serviceInput,
                                }).success(function(output) {
                                    if(output.result == 'success'){
                                        swal({
                                             title: "",
                                             text: "Documentation Update Request : "+output.result,
                                             timer: 2000,
                                             showConfirmButton: false
                                         });
                                         $scope.addDocsVar = false;
                                         $scope.tmpTopicDocumentation = $scope.topicDocumentation;
                                         document.getElementById("topicDocId").innerHTML = $scope.topicDocumentation;
                                    }
                                    else{
                                            $scope.alertTopicDelete = "Documentation Update Request : "+output.result;
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
        $scope.getSchemaOfTopic();
    }

	$scope.getAcls = function() {
        $scope.firstPromote = "false";
        $scope.alertTopicDelete = null;
        $scope.alert = null;

        var topicSelected;

        var sPageURL = window.location.search.substring(1);
        var sURLVariables = sPageURL.split('&');
        for (var i = 0; i < sURLVariables.length; i++)
        {
            var sParameterName = sURLVariables[i].split('=');
            if (sParameterName[0] == "topicname")
            {
                topicSelected = sParameterName[1];
            }
        }

        if(!topicSelected)
            return;

		$scope.topicSelectedParam = topicSelected;
		$scope.ShowSpinnerStatusTopics = true;
        $scope.schemaDetails = null;
        $scope.firstSchemaPromote = 'false';
		$http({
			method: "GET",
			url: "getAcls",
            headers : { 'Content-Type' : 'application/json' },
            params: {'topicnamesearch' : topicSelected,
             }
		}).success(function(output) {
		    $scope.ShowSpinnerStatusTopics = false;
		    if(output.topicExists === true){
		        $scope.resultBrowse = output.aclInfoList;
		        $scope.resultBrowsePrefix = output.prefixedAclInfoList;
		        $scope.resultBrowseTxnId = output.transactionalAclInfoList;
            	$scope.topicOverview = output.topicInfoList;
            	$scope.topicPromotionDetails = output.topicPromotionDetails;
            	$scope.schemaDetails = output.schemaDetails;

            	$scope.schemaExists = output.schemaExists;
            	$scope.prefixAclsExists = output.prefixAclsExists;
            	$scope.txnAclsExists = output.txnAclsExists;
                $scope.topicHistoryList = output.topicHistoryList;

            	$scope.topicDocumentation = output.topicDocumentation;
            	$scope.tmpTopicDocumentation = output.topicDocumentation;
            	$scope.topicIdForDocumentation = output.topicIdForDocumentation;
            	document.getElementById("topicDocId").innerHTML = output.topicDocumentation;
		    }
		    else
		        $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/browseTopics";
		}).error(
			function(error) 
			{
			    $scope.ShowSpinnerStatusTopics = false;
				$scope.alert = error;
			}
		);
		
	}

    $scope.getSchemaOfTopic = function(){

        if($scope.schemaDetails != null && $scope.newSchemaVersion === $scope.displayedSchemaVersion)
            return;

        $scope.displayedSchemaVersion = null;
        $scope.ShowSpinnerStatusSchemas = true;
            $http({
                method: "GET",
                url: "getSchemaOfTopic",
                headers : { 'Content-Type' : 'application/json' },
                params: {'topicnamesearch' : $scope.topicSelectedParam,
                    'schemaVersionSearch' : $scope.newSchemaVersion,
                }
            }).success(function(output) {
                $scope.ShowSpinnerStatusSchemas = false;
                if(output.schemaDetails != null){
                    $scope.schemaDetails = output.schemaDetails;
                    $scope.schemaExists = output.schemaExists;
                    $scope.schemaPromotionDetails = output.schemaPromotionDetails;
                    $scope.allSchemaVersions = output.allSchemaVersions;
                    $scope.displayedSchemaVersion = $scope.schemaDetails[0].version;
                }
            }).error(
                function(error)
                {
                    $scope.ShowSpinnerStatusTopics = false;
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
                  result += "Partition "+output[i].topicPartitionId + " | ";
                  result += "CurrentOffset "+output[i].currentOffset + " | ";
                  result += "EndOffset "+output[i].endOffset + " | ";
                  result += "Lag "+output[i].lag + "\n\n";
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

        $scope.getAivenServiceAccount = function(environment, topicName, userName, aclReqNo){
            $http({
                method: "GET",
                url: "getAivenServiceAccount",
                headers : { 'Content-Type' : 'application/json' },
                params: {'env' : environment,
                    'topicName' : topicName,
                    'userName' : userName,
                    'aclReqNo' : aclReqNo
                },
            }).success(function(output) {
                $scope.serviceAccountInfo = output;
                if(output.result === 'failure')
                {
                    swal({
                        title: "Failure:",
                        text: "Could not retrieve service account details, or doesn't exist !!"
                    });
                }else{
                    swal({
                        title: "Success: ",
                        text: "Password is displayed above !!"
                    });
                    $scope.alert = "Password : " + output.data.password;
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
                    if(output.status != null && output.status == "false"){
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