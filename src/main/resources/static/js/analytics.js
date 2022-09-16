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