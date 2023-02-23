'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('docsApp',[]);

app.controller("docsCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	$http.defaults.headers.common['Accept'] = 'application/json';

    $scope.topics = "true";
	$scope.docTopics = function(){
	    $scope.resetAll();
	    $scope.topics = "true";
	}

	$scope.docAcls = function(){
    	    $scope.resetAll();
            $scope.acls = "true";
    	}

    $scope.docSchemas = function(){
        $scope.resetAll();
        $scope.schemas = "true";
    }

    $scope.docAnalytics = function(){
            $scope.resetAll();
            $scope.analytics = "true";
        }

    $scope.docDashboard = function(){
            $scope.resetAll();
            $scope.dashboard = "true";
        }

    $scope.docSyncFromCluster = function(){
            $scope.resetAll();
            $scope.syncfromcluster = "true";
        }

    $scope.docSyncToCluster = function(){
        $scope.resetAll();
        $scope.synctocluster = "true";
    }

    $scope.docServerConfig = function(){
        $scope.resetAll();
        $scope.serverconfig = "true";
    }

    $scope.docTenants = function(){
        $scope.resetAll();
        $scope.tenants = "true";
    }

    $scope.docClusters = function(){
        $scope.resetAll();
        $scope.clusters = "true";
    }

    $scope.docEnvironments = function(){
        $scope.resetAll();
        $scope.envs = "true";
    }

    $scope.docTeams = function(){
        $scope.resetAll();
        $scope.teams = "true";
    }

    $scope.docUsers = function(){
            $scope.resetAll();
            $scope.users = "true";
        }

    $scope.docRoles = function(){
        $scope.resetAll();
        $scope.roles = "true";
    }

    $scope.docPermissions = function(){
        $scope.resetAll();
        $scope.perms = "true";
    }

    $scope.docOthers = function(){
        $scope.resetAll();
        $scope.others = "true";
    }

   $scope.resetAll = function(){
        $scope.topics = "false";
        $scope.acls = "false";
        $scope.schemas = "false";
        $scope.analytics = "false";
        $scope.dashboard = "false";
        $scope.syncfromcluster = "false";
        $scope.synctocluster = "false";

        $scope.serverconfig = "false";
        $scope.tenants = "false";
        $scope.clusters = "false";
        $scope.envs = "false";
        $scope.teams = "false";
        $scope.users = "false";
        $scope.roles = "false";
        $scope.perms = "false";
        $scope.others = "false";
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
                        $scope.alert = "User team update request : "+output.result;
                        if(output.result === 'success'){
                            swal({
                                title: "",
                                text: "User team update request : "+output.result,
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