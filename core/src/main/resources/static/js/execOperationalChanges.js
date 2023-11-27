'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('execOperationalChangesApp',[]);

app.controller("execOperationalChangesCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	$http.defaults.headers.common['Accept'] = 'application/json';

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

    $scope.refreshPage = function(){
        $window.location.reload();
    }

        $scope.onChangeRequestType = function(requestType){
                    $scope.overwriteReqsType = true;
                    $scope.requestsType = requestType;
                    $scope.alert = "";
                    $scope.getMyOperationalRequests(1, true);
                }

        $scope.getMyOperationalRequests = function(pageNoSelected, overwriteFlag) {
            if($scope.overwriteReqsType)
               $scope.overwriteReqsType = false;

            if(!$scope.requestsType)
                $scope.requestsType = "CREATED";

            $http({
                method: "GET",
                url: "/operationalRequests/requestsFor/approver",
                headers : { 'Content-Type' : 'application/json' },
                params: {'pageNo' : pageNoSelected,
                 'currentPage' : $scope.currentPageSelected,
                 'requestStatus': $scope.requestsType }
            }).success(function(output) {
                $scope.operationalRequests = output;
                if(output!=null && output.length>0){
                    $scope.resultPages = output[0].allPageNos;
                    $scope.resultPageSelected = pageNoSelected;
                    $scope.currentPageSelected = output[0].currentPage;
                }
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

        $scope.getRequestStatuses = function() {
            $http({
                method: "GET",
                url: "getRequestTypeStatuses",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                const indOfDeleted = output.indexOf("deleted");
                if(indOfDeleted > 0)
                {
                    output.splice(indOfDeleted, 1);
                }
                $scope.requestTypeStatuses = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

        $scope.execTopicRequestUp = function(topicId){
            $scope.showDeclinePanel = "false";
                swal({
                        title: "Are you sure?",
                        text: "You would like to approve the Operational request ?",
                        type: "warning",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55",
                        confirmButtonText: "Yes, approve it!",
                        cancelButtonText: "No, cancel please!",
                        closeOnConfirm: true,
                        closeOnCancel: true
                    }).then(function(isConfirm){
                        if (isConfirm.value) {
                            $http({
                                        method: "POST",
                                        url: "operationalRequest/reqId/" + topicId + "/approve",
                                        headers : { 'Content-Type' : 'application/json' },
                                    }).success(function(output) {

                                        $scope.alert = "Operational change Approve Request : "+output.message;
                                        $scope.getMyOperationalRequests(1, false);
                                        if(output.success){
                                            swal({
                                                 title: "",
                                                 text: "Operational change Approve Request : "+output.message,
                                                 timer: 2000,
                                                 showConfirmButton: false
                                             });
                                         }else $scope.showSubmitFailed('','');
                                    }).error(
                                        function(error)
                                        {
                                            $scope.handleErrorMessage(error);
                                        }
                                    );
                        } else {
                            return;
                        }
                    });
        }

        $scope.declineWithReason = function(){
            var reason = $scope.reasonForRejection;

            if(reason == null || reason.trim().length === 0)
            {
                return;
            }
            $scope.execTopicRequestReject('IGNORE_REQ');
        }

        $scope.showDeclinePanel = "false";

        $scope.execTopicRequestReject = function(reqNo) {

            var reason = $scope.reasonForRejection;
            if(reqNo === 'IGNORE_REQ'){
            }else{
                $scope.selectedReqToDecline = reqNo;
            }
            if(reason == null || reason.trim().length === 0)
            {
                swal({
                         title: "",
                         text: "Please mention a reason for declining this request.",
                         timer: 2000,
                         showConfirmButton: false
                     });
                $scope.showDeclinePanel = "true";
                $scope.reqForDecline = reqNo;

                return;
            }
            if($scope.reqForDecline != null && $scope.reqForDecline !== $scope.selectedReqToDecline)
            {
                return;
            }

            reqNo = $scope.reqForDecline;

                    swal({
                            title: "Are you sure?",
                            text: "You would like to decline the operational change request ?",
                            type: "warning",
                            showCancelButton: true,
                            confirmButtonColor: "#DD6B55",
                            confirmButtonText: "Yes, decline it!",
                            cancelButtonText: "No, cancel please!",
                            closeOnConfirm: true,
                            closeOnCancel: true
                        }).then(function(isConfirm){
                            if (isConfirm.value) {
                                $http({
                                        method: "POST",
                                        url: "operationalRequest/reqId/" + reqNo + "/decline",
                                        headers : { 'Content-Type' : 'application/json' },
                                        params: {'reasonForDecline' : reason },
                                        data: { 'reasonForDecline' : reason}
                                    }).success(function(output) {

                                        $scope.alert = "Operational change Decline Request : "+output.message;
                                        $scope.reasonForRejection = "";
                                        $scope.showDeclinePanel = "false";
                                        $scope.topicForDecline = "";
                                        $scope.getMyOperationalRequests(1, false);
                                        if(output.success){
                                            swal({
                                                 title: "",
                                                 text: "Operational change Decline Request : "+output.message,
                                                 timer: 2000,
                                                 showConfirmButton: false
                                             });
                                         }else $scope.showSubmitFailed('','');

                                    }).error(
                                        function(error)
                                        {
                                            $scope.handleErrorMessage(error);
                                        }
                                    );
                            } else {
                                return;
                            }
                        });
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
                    $scope.checkSwitchTeams($scope.dashboardDetails.canSwitchTeams, $scope.dashboardDetails.teamId, $scope.userlogged);
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