'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('modifyTeamApp',[]);

app.controller("modifyTeamCtrl", function($scope, $http, $location, $window) {
	
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

        $scope.updatedEnvArray = [];
         $scope.onSelectEnvs = function(envId) {
            if($scope.updatedEnvArray.includes(envId))
                $scope.updatedEnvArray.splice($scope.updatedEnvArray.indexOf(envId), 1);
            else
                $scope.updatedEnvArray.push(envId);
         }

//        $scope.getTenants = function() {
//                $http({
//                    method: "GET",
//                          url: "getTenants",
//                          headers : { 'Content-Type' : 'application/json' }
//                      }).success(function(output) {
//                          $scope.allTenants = output;
//                      }).error(
//                          function(error)
//                          {
//                              $scope.alert = error;
//                          }
//                      );
//        }

        $scope.getRequestTopicsEnvs = function() {

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

        $scope.cancelRequest = function() {
                    $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/teams";
                }

        $scope.cancelUserRequest = function() {
                            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/users";
                        }
        $scope.loadTeamDetails = function(){
                $scope.alert = "";
                var sPageURL = window.location.search.substring(1);
                var sURLVariables = sPageURL.split('&');

                var teamId, tenantName;

                for (var i = 0; i < sURLVariables.length; i++)
                    {
                        var sParameterName = sURLVariables[i].split('=');
                        if (sParameterName[0] == "teamId")
                        {
                            teamId = sParameterName[1];
                        }
                        if (sParameterName[0] == "tenant")
                        {
                            tenantName = sParameterName[1];
                        }
                    }

                if(!teamId)
                    return;

                if(!tenantName)
                    return;

                $scope.teamToEdit = teamId;

                $http({
                        method: "GET",
                        url: "getTeamDetails",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'teamId' : teamId, 'tenantName' : tenantName },
                        data: {'teamId' : teamId}
                    }).success(function(output) {
                        $scope.teamDetails = output;
                        if(output == null || output == "")
                            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/teams";
                    }).error(
                        function(error)
                        {
                            $scope.alert = error;
                        }
                    );
            }

        $scope.updateTeam = function() {

                var serviceInput = {};

                if(!$scope.teamDetails.teamname || $scope.teamDetails.teamname.length==0)
                {
                    $scope.alertnote = "Please fill in Team Name.";
                    $scope.showAlertToast();

                    return;
                }

                if(!$scope.teamDetails.teamname || $scope.teamDetails.teamname.length < 3)
                {
                    $scope.alertnote = "Please fill in a valid Team Name.";
                    $scope.showAlertToast();

                    return;
                }

                if(!$scope.teamDetails.teammail || $scope.teamDetails.teammail.length==0)
                {
                    $scope.alertnote = "Please fill in Team Mail.";
                    $scope.showAlertToast();

                    return;
                }

                if(!$scope.teamDetails.teamphone || $scope.teamDetails.teamname.teamphone==0)
                    {
                        $scope.alertnote = "Please fill in Team phone.";
                        $scope.showAlertToast();

                        return;
                    }

                if(!$scope.teamDetails.contactperson || $scope.teamDetails.contactperson.length==0)
                {
                    $scope.alertnote = "Please fill in Team contact person.";
                    $scope.showAlertToast();

                    return;
                }

                serviceInput['teamname'] = $scope.teamDetails.teamname;
                serviceInput['teamId'] = $scope.teamDetails.teamId;
                serviceInput['teammail'] = $scope.teamDetails.teammail;
                serviceInput['teamphone'] = $scope.teamDetails.teamphone;
                serviceInput['contactperson'] = $scope.teamDetails.contactperson;
                serviceInput['envList'] = $scope.updatedEnvArray;
                serviceInput['app'] = "";

                $http({
                    method: "POST",
                    url: "updateTeam",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'updateTeam' : serviceInput },
                    data: serviceInput
                }).success(function(output) {
                    $scope.alert = "Team update request : "+output.message;
                    if(output.success){
                        swal({
                             title: "",
                             text: "Team update request : "+output.message,
                             timer: 2000,
                             showConfirmButton: true
                         }).then(function(isConfirm){
                            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/teams";
                      });
                     }else $scope.showSubmitFailed('','');
                }).error(
                    function(error)
                    {
                        $scope.handleValidationErrors(error);
                    }
                );

            };


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