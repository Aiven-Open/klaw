'use strict'

// confirmation of delete
// edit
// solution for transaction
// message store / key / gui
var app = angular.module('myRequestsApp',[]);

app.controller("myRequestsCtrl", function($scope, $http, $location, $window) {

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

        $scope.validateSearchParams = function(overwriteFlag){
            var sPageURL = window.location.search.substring(1);

            if(!$scope.requestsType)
                $scope.requestsType = 'created';

            var alertMessage = null;

            var sURLVariables = sPageURL.split('&');

            for (var i = 0; i < sURLVariables.length; i++)
                {
                    var sParameterName = sURLVariables[i].split('=');
                    if (sParameterName[0] == "reqsType")
                    {
                        $scope.requestsType = sParameterName[1];
                    }
                    if (sParameterName[0] == "topicCreated")
                    {
                        alertMessage = sParameterName[1];
                        if(alertMessage == "true" && !overwriteFlag)
                            $scope.alert = "Topic request is successfully created !!"
                    }
                    if (sParameterName[0] == "topicPromotionCreated")
                    {
                        alertMessage = sParameterName[1];
                        if(alertMessage == "true" && !overwriteFlag)
                            $scope.alert = "Topic promotion request is successfully created !!"
                    }
                    if (sParameterName[0] == "deleteTopicCreated")
                    {
                        alertMessage = sParameterName[1];
                        if(alertMessage == "true" && !overwriteFlag)
                            $scope.alert = "Topic delete request is successfully created !!"
                    }
                    if (sParameterName[0] == "claimTopicCreated")
                    {
                        alertMessage = sParameterName[1];
                        if(alertMessage == "true" && !overwriteFlag)
                            $scope.alert = "Topic claim request is successfully created !!"
                    }
                    if (sParameterName[0] == "aclCreated")
                    {
                        alertMessage = sParameterName[1];
                        if(alertMessage == "true" && !overwriteFlag)
                            $scope.alert = "Subscription request is successfully created !!"
                    }
                    if (sParameterName[0] == "connectorCreated")
                    {
                        alertMessage = sParameterName[1];
                        if(alertMessage == "true" && !overwriteFlag)
                            $scope.alert = "Connector request is successfully created !!"
                    }
                    if (sParameterName[0] == "deleteConnectorCreated")
                    {
                        alertMessage = sParameterName[1];
                        if(alertMessage == "true" && !overwriteFlag)
                            $scope.alert = "Connector delete request is successfully created !!"
                    }
                    if (sParameterName[0] == "connectorPromotionCreated")
                    {
                        alertMessage = sParameterName[1];
                        if(alertMessage == "true" && !overwriteFlag)
                            $scope.alert = "Connector promotion request is successfully created !!"
                    }
                    if (sParameterName[0] == "schemaCreated")
                    {
                        alertMessage = sParameterName[1];
                        if(alertMessage == "true" && !overwriteFlag)
                            $scope.alert = "Schema request is successfully created !!"
                    }
                    if (sParameterName[0] == "deleteAclCreated")
                    {
                        alertMessage = sParameterName[1];
                        if(alertMessage == "true" && !overwriteFlag)
                            $scope.alert = "Delete Subscription request is successfully created !!"
                    }
                }
        }

        $scope.getMySchemaRequests = function(pageNoSelected, overwriteFlag) {
                    if($scope.overwriteReqsType)
                       $scope.overwriteReqsType = false;
                    else
                        $scope.validateSearchParams(overwriteFlag);

                    $http({
                        method: "GET",
                        url: "getSchemaRequests",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'pageNo' : pageNoSelected,
                                 'currentPage' : $scope.currentPageSelected,
                                 'requestsType': $scope.requestsType }
                    }).success(function(output) {
                        $scope.schemaRequests = output;
                        if(output!=null && output.length>0){
                            $scope.resultPages = output[0].allPageNos;
                            $scope.resultPageSelected = pageNoSelected;
                            $scope.currentPageSelected = output[0].currentPage;
                        }

                    }).error(
                        function(error)
                        {
                            $scope.alert = error;
                            $scope.topicRequests = null;
                        }
                    );
                }

        $scope.getMyTopicRequests = function(pageNoSelected, overwriteFlag) {
            if($scope.overwriteReqsType)
               $scope.overwriteReqsType = false;
            else
                $scope.validateSearchParams(overwriteFlag);

            $http({
                method: "GET",
                url: "getTopicRequests",
                headers : { 'Content-Type' : 'application/json' },
                params: {'pageNo' : pageNoSelected,
                         'currentPage' : $scope.currentPageSelected,
                         'requestsType': $scope.requestsType }
            }).success(function(output) {
                $scope.topicRequests = output;
                if(output!=null && output.length>0){
                    $scope.resultPages = output[0].allPageNos;
                    $scope.resultPageSelected = pageNoSelected;
                    $scope.currentPageSelected = output[0].currentPage;
                }

            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.topicRequests = null;
                }
            );
        }

        $scope.getMyConnectorRequests = function(pageNoSelected, overwriteFlag) {
            if($scope.overwriteReqsType)
               $scope.overwriteReqsType = false;
            else
                $scope.validateSearchParams(overwriteFlag);

            $http({
                method: "GET",
                url: "getConnectorRequests",
                headers : { 'Content-Type' : 'application/json' },
                params: {'pageNo' : pageNoSelected,
                         'currentPage' : $scope.currentPageSelected,
                         'requestsType': $scope.requestsType }
            }).success(function(output) {
                $scope.connectorRequests = output;
                if(output!=null && output.length>0){
                    $scope.resultPages = output[0].allPageNos;
                    $scope.resultPageSelected = pageNoSelected;
                    $scope.currentPageSelected = output[0].currentPage;
                }
            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.connectorRequests = null;
                }
            );
        }

        $scope.onChangeRequestType = function(requestType){
            $scope.overwriteReqsType = true;
            $scope.requestsType = requestType;
            $scope.alert = "";
            $scope.getMyTopicRequests(1, true);
        }

        $scope.onChangeRequestTypeConnectors = function(requestType){
            $scope.overwriteReqsType = true;
            $scope.requestsType = requestType;
            $scope.alert = "";
            $scope.getMyConnectorRequests(1, true);
        }

        $scope.onChangeRequestTypeSchema = function(requestType){
            $scope.overwriteReqsType = true;
            $scope.requestsType = requestType;
            $scope.alert = "";
            $scope.getMySchemaRequests(1, true);
        }

        $scope.onChangeRequestAclsType = function(requestType){
            $scope.overwriteReqsType = true;
            $scope.requestsType = requestType;
            $scope.alert = "";
            $scope.getMyAclRequests(1, true);
        }

        $scope.getMyAclRequests = function(pageNoSelected, overwriteFlag) {
            if($scope.overwriteReqsType)
               $scope.overwriteReqsType = false;
            else
                $scope.validateSearchParams(overwriteFlag);

            $http({
                method: "GET",
                url: "getAclRequests",
                headers : { 'Content-Type' : 'application/json' },
                 params: {'pageNo' : pageNoSelected,
                   'currentPage' : $scope.currentPageSelected,
                   'requestsType': $scope.requestsType }
            }).success(function(output) {
                $scope.aclRequests = output;
                if(output!=null && output.length>0){
                    $scope.resultPagesAcl = output[0].allPageNos;
                    $scope.resultPageSelectedAcl = pageNoSelected;
                    $scope.currentPageSelected = output[0].currentPage;
                }
            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.aclRequests = null;
                }
            );
        }

        $scope.getRequestStatuses = function() {
            $http({
                method: "GET",
                url: "getRequestTypeStatuses",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.requestTypeStatuses = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.schemaRequests = null;
                }
            );
        }


    $scope.deleteConnectorRequest = function(topicId) {
        swal({
                    title: "Are you sure?",
                    text: "You would like to delete the request ?",
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: "Yes, delete it!",
                    cancelButtonText: "No, cancel please!",
                    closeOnConfirm: true,
                    closeOnCancel: true
                }).then(function(isConfirm){
                    if (isConfirm.dismiss != "cancel") {
                        $http({
                              method: "POST",
                              url: "deleteConnectorRequests",
                              headers : { 'Content-Type' : 'application/json' },
                              params: {'connectorId' : topicId },
                              data: {'connectorId' : topicId }
                          }).success(function(output) {
                              $scope.alert = "Request deleted : " + output.result;
                              if(output.result === 'success'){
                                  swal({
                                       title: "",
                                       text: "Request deleted : "+output.result,
                                       timer: 2000,
                                       showConfirmButton: false
                                   });
                               }else $scope.showSubmitFailed('','');
                              $scope.getMyConnectorRequests(1, true);
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


        $scope.deleteAclRequest = function(req_no) {
            swal({
            		title: "Are you sure?",
            		text: "You would like to delete the request?",
            		type: "warning",
            		showCancelButton: true,
            		confirmButtonColor: "#DD6B55",
            		confirmButtonText: "Yes, delete it!",
            		cancelButtonText: "No, cancel please!",
            		closeOnConfirm: true,
            		closeOnCancel: true
            	}).then(function(isConfirm){
            		if (isConfirm.dismiss != "cancel") {
            			$http({
                            method: "POST",
                            url: "deleteAclRequests",
                            headers : { 'Content-Type' : 'application/json' },
                            params: {'req_no' : req_no },
                            data: {'req_no' : req_no}
                        }).success(function(output) {
                            $scope.alert = "Request deleted : "+output.result;
                            $scope.getMyAclRequests(1, true);
                            if(output.result === 'success'){
                                  swal({
                                       title: "",
                                       text: "Request deleted : "+output.result,
                                       timer: 2000,
                                       showConfirmButton: false
                                   });
                               }else $scope.showSubmitFailed('','');
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

        $scope.deleteSchemaRequest = function(req_no) {
            swal({
                    title: "Are you sure?",
                    text: "You would like to delete the request?",
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: "Yes, delete it!",
                    cancelButtonText: "No, cancel please!",
                    closeOnConfirm: true,
                    closeOnCancel: true
                }).then(function(isConfirm){
                    if (isConfirm.dismiss != "cancel") {
                        $http({
                            method: "POST",
                            url: "deleteSchemaRequests",
                            headers : { 'Content-Type' : 'application/json' },
                            params: {'req_no' : req_no },
                            data: {'req_no' : req_no}
                        }).success(function(output) {
                            $scope.alert = "Request deleted : "+output.result;
                            $scope.getMySchemaRequests(1, true);
                            if(output.result === 'success'){
                                  swal({
                                       title: "",
                                       text: "Request deleted : "+output.result,
                                       timer: 2000,
                                       showConfirmButton: false
                                   });
                               }else $scope.showSubmitFailed('','');
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


	$scope.getTopics = function() {
	//var authStatus = getExecAuth();

		var serviceInput = {};

		serviceInput['env'] = $scope.getTopics.envName.name;

		$http({
			method: "GET",
			url: "getTopics",
            headers : { 'Content-Type' : 'application/json' },
            params: {'env' : $scope.getTopics.envName.name }
		}).success(function(output) {
			$scope.resultBrowse = output;
		}).error(
			function(error)
			{
				$scope.alert = error;
			}
		);

	};

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
