'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('manageConnectorsApp',[]);

app.controller("manageConnectorsCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	//$http.defaults.headers.common['Accept'] = 'application/json';

   $scope.showAlertToast = function() {
             var x = document.getElementById("alertbar");
             x.className = "show";
             setTimeout(function(){ x.className = x.className.replace("show", ""); }, 2000);
           }

   $scope.showAlertBulkToast = function() {
        var x = document.getElementById("alertbarbulk");
        x.className = "show";
        setTimeout(function(){ x.className = x.className.replace("show", ""); }, 2000);
      }

   	$scope.showSubmitFailed = function(title, text){
   		swal({
   			 title: "",
   			 text: "Request unsuccessful !!",
   			 timer: 2000,
   			 showConfirmButton: false
   			 });
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


	$scope.getConnectorsEnvs = function() {

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

        $scope.getConnectorConfig = function(connectorName,index){
            var modal = document.getElementById("myModal-"+index);
            var span = document.getElementsByClassName("close-"+index)[0];
            $scope.popupConnector = connectorName;


            span.onclick = function() {
              modal.style.display = "none";
            }


            $http({
                    method: "GET",
                    url: "getConnectorDetails",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'env' : $scope.getConnectors.envName,
                    'connectorName' : connectorName},
                }).success(function(output) {
                    $scope.connectorDetails = output.message;
                    modal.style.display = "block";
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );

        }


	// We add the "time" query parameter to prevent IE
	// from caching ajax results

	$scope.getConnectors = function(pageNoSelected) {

        if(!$scope.getConnectors.envName)
            return;

        var serviceInput = {};
		serviceInput['env'] = $scope.getConnectors.envName;
		$scope.resultBrowse = [];
        $scope.ShowSpinnerStatusTopics = true;

		$http({
			method: "GET",
			url: "getConnectorsToManage",
            headers : { 'Content-Type' : 'application/json' },
            params: {'env' : $scope.getConnectors.envName,
             'connectornamesearch' : $scope.getConnectors.connectornamesearch,
             'pageNo' : pageNoSelected,
             'currentPage' : $scope.currentPageSelected}
		}).success(function(output) {
		    $scope.ShowSpinnerStatusTopics = false;
//			$scope.resultBrowse = output["resultSet"];
			$scope.resultBrowse = output;
			if($scope.resultBrowse != null && $scope.resultBrowse.length != 0){
                $scope.resultPages = $scope.resultBrowse[0].allPageNos;
                $scope.resultPageSelected = pageNoSelected;
                $scope.currentPageSelected = $scope.resultBrowse[0].currentPage;
            }
            $scope.alert = "";
		}).error(
			function(error) 
			{
			    $scope.ShowSpinnerStatusTopics = false;
			    $scope.resultBrowse = [];
				$scope.handleErrorMessage(error);
			}
		);
		
	};
}
);