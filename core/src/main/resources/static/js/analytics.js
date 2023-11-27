'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('showAnalyticsApp',['chart.js']);

app.controller("showAnalyticsCtrl", function($scope, $http, $location, $window) {

	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed.
	$http.defaults.headers.common['Accept'] = 'application/json';
	$scope.showServerStatus = "false";

        $scope.getTeamsOverviewChart = function() {

                    $http({
                        method: "GET",
                        url: "getTeamsOverview",
                        headers : { 'Content-Type' : 'application/json' }
                    }).success(function(output) {
                        $scope.analyticsReports = output;
                        $scope.topicsperenvchart = output.topicsPerEnvOverview;
                        $scope.partitionsperenvchart = output.partitionsPerEnvOverview;
                        $scope.aclsperenvchart = output.aclsPerEnvOverview;
                        $scope.topicsperteamchart = output.topicsPerTeamsOverview;
                        $scope.produceraclsperteamchart = output.producerAclsPerTeamsOverview;
                        $scope.consumeraclsperteamchart = output.consumerAclsPerTeamsOverview;
                        $scope.activitylogchart = output.activityLogOverview;
                    }).error(
                        function(error)
                        {
                            $scope.alert = error;
                        }
                    );
                };

          $scope.downloadReport = function(){
                    $http({
                        method: "GET",
                        url: "getKwReport",
                        headers : { 'Content-Type' : 'application/json' }
                    }).success(function(output, status, headers) {
                        var binary_string = window.atob(output.data);
                        var len = binary_string.length;
                        var bytes = new Uint8Array(len);
                        for (var i = 0; i < len; i++) {
                            bytes[i] = binary_string.charCodeAt(i);
                        }

                        var a = window.document.createElement('a');
                        a.href = window.URL.createObjectURL(new Blob([bytes.buffer], { type:'application/octet-stream' }));
                        a.download = output.filename;
                        document.body.appendChild(a)
                        a.click();

                        swal({
                        	 title: "",
                        	 text: "Report is being downloaded.",
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
                   $scope.myteamtopics = output.myteamtopics;
                   $scope.klawversion = output.klawversion;
                   $scope.notifications = output.notifications;
                   $scope.notificationsAcls = output.notificationsAcls;
                   $scope.notificationsSchemas = output.notificationsSchemas;
                   $scope.notificationsUsers = output.notificationsUsers;

                   $scope.notificationsSchemas = output.notificationsSchemas;

                   if(output.companyinfo == null){
                       $scope.companyinfo = "Company not defined!!";
                   }
                   else
                       $scope.companyinfo = output.companyinfo;

                   if($scope.userlogged != null)
                       $scope.loggedinuser = "true";

                   $scope.schema_clusters_count = output.schema_clusters_count;
                   $scope.kafka_clusters_count = output.kafka_clusters_count;
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
                if($scope.contactFormSubject.trim().length===0)
                    return;
                if($scope.contactFormMessage.trim().length===0)
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