'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('modifyUserApp',[]);

app.controller("modifyUserCtrl", function($scope, $http, $location, $window) {
	
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

	    $scope.getRoles = function() {
                $http({
                    method: "GET",
                    url: "getRoles",
                    headers : { 'Content-Type' : 'application/json' }
                }).success(function(output) {
                    $scope.rolelist = output;
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );
            }


        $scope.loadTeamsSU = function() {
                $http({
                    method: "GET",
                    url: "getAllTeamsSU",
                    headers : { 'Content-Type' : 'application/json' }
                }).success(function(output) {
                    $scope.allTeams = output;
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );
            }

    $scope.getMyProfile = function(){
        $http({
                method: "GET",
                url: "getMyProfileInfo",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.myProfInfo = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
    }

    $scope.loadUserDetails = function(){
            $scope.alert = "";
            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');

            var userId;

            for (var i = 0; i < sURLVariables.length; i++)
                {
                    var sParameterName = sURLVariables[i].split('=');
                    if (sParameterName[0] == "userId")
                    {
                        userId = sParameterName[1];
                    }
                }

            if(!userId)
                return;

            $scope.userToEdit = userId;

            $http({
                    method: "GET",
                    url: "getUserDetails",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'userId' : decodeURI(userId) },
                    data: {'userId' : decodeURI(userId)}
                }).success(function(output) {
                    $scope.userDetails = output;
                    $scope.userDetails.teamId = output.teamId;
                    $scope.displayTeamsForSwitch = $scope.userDetails.switchTeams;
                    if(output === null || output === "")
                        $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/users";
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );
        }

	$scope.updateUser = function() {

            var serviceInput = {};

            if($scope.userDetails.userPassword.length < 6)
            {
                $scope.alertnote = "Password should be atleast 6 characters.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.userDetails.mailid)
            {
                $scope.alertnote = "Email id is mandatory.";
                $scope.showAlertToast();
                return;
            }
            else if($scope.userDetails.mailid.length < 7)
            {
                $scope.alertnote = "Please enter a valid email id.";
                $scope.showAlertToast();
                return;
            }
            else if(!$scope.userDetails.mailid.includes("@"))
            {
                $scope.alertnote = "Please enter a valid email id.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.userDetails.role)
            {
                $scope.alertnote = "Please select a role.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.userDetails.teamId)
            {
                $scope.alertnote = "Please select a team.";
                $scope.showAlertToast();
                return;
            }

            if($scope.userDetails.switchTeams){
                $scope.getUpdatedListOfSwitchTeams();
                if($scope.updatedTeamsSwitchList.length < 2){
                    $scope.alertnote = "Please select atleast 2 teams, to switch between.";
                    $scope.showAlertToast();
                    return;
                }
                if(!$scope.updatedTeamsSwitchList.includes($scope.userDetails.teamId)){
                    $scope.alertnote = "Please select your own team, in the switch teams list.";
                    $scope.showAlertToast();
                    return;
                }
            }

            serviceInput['username'] = $scope.userDetails.username;
            serviceInput['fullname'] = $scope.userDetails.fullname;
            serviceInput['userPassword'] = $scope.userDetails.userPassword;
            serviceInput['teamId'] = $scope.userDetails.teamId;
            serviceInput['role'] = $scope.userDetails.role;
            serviceInput['mailid'] = $scope.userDetails.mailid;
            serviceInput['switchTeams'] = $scope.userDetails.switchTeams;
            serviceInput['switchAllowedTeamIds'] = $scope.updatedTeamsSwitchList;

            $http({
                method: "POST",
                url: "updateUser",
                headers : { 'Content-Type' : 'application/json' },
                params: {'updateUserObj' : serviceInput },
                data: serviceInput
            }).success(function(output) {
                $scope.alert = "User update request : "+output.message;
                if(output.success){
                    swal({
                         title: "",
                         text: "User update request : "+output.message,
                         timer: 2000,
                         showConfirmButton: true
                     }).then(function(isConfirm){
                          $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/users";
                    });
                 }else $scope.showSubmitFailed('','');
            }).error(
                function(error)
                {
                    $scope.handleValidationErrors(error);
                }
            );
        };

        $scope.onChangeSwitchTeams = function(){
            if($scope.userDetails.switchTeams){
                $scope.displayTeamsForSwitch = true;
            }else{
                $scope.displayTeamsForSwitch = false;
            }
        }

        $scope.updatedTeamsSwitchList = [];
        $scope.getUpdatedListOfSwitchTeams = function() {
            $scope.updatedTeamsSwitchList = [];
            if($scope.userDetails.switchAllowedTeamIds !== null){
                for (let i = 0; i < $scope.userDetails.switchAllowedTeamIds.length; i++)
                {
                    $scope.updatedTeamsSwitchList.push($scope.userDetails.switchAllowedTeamIds[i]);
                }
            }
        }


    $scope.cancelRequest = function() {
                $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/teams";
            }

    $scope.cancelUserRequest = function() {
                        $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/users";
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