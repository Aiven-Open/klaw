'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('kafkaConnectApp',[]);

app.controller("kafkaConnectCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	//$http.defaults.headers.common['Accept'] = 'application/json';
	

	$scope.getEnvs = function() {

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

        $scope.loadTeams = function() {
                $http({
                    method: "GET",
                    url: "getAllTeamsSUOnly",
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

	// We add the "time" query parameter to prevent IE
	// from caching ajax results

	$scope.getConnectors = function(pageNoSelected, fromSelect, topicsDisplayType) {

        var serviceInput = {};
        var envSelected;
        $scope.resultBrowse = null;
        $scope.resultPages = null;
        $scope.alert = null;
        $scope.resultPageSelected = null;
        var teamSel = $scope.getConnectors.team;

        if(fromSelect == "false")
        {
            var envSelected;

            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');
            for (var i = 0; i < sURLVariables.length; i++)
            {
                var sParameterName = sURLVariables[i].split('=');
                if (sParameterName[0] == "envSelected")
                {
                    envSelected = sParameterName[1];
                    serviceInput['env'] = envSelected;
                    $scope.envSelected = envSelected;
                    $scope.getConnectors.envName = envSelected;
                }else return;
            }
        }else if(fromSelect == "true"){
                 if(!$scope.getConnectors.envName)
                        envSelected = "ALL";
                 else
                    envSelected = $scope.getConnectors.envName;

                serviceInput['env'] = envSelected;
                $scope.envSelected = envSelected;
        }else{
                envSelected = "ALL";
                var sPageURL = window.location.search.substring(1);
                var sURLVariables = sPageURL.split('&');

                for (var i = 0; i < sURLVariables.length; i++)
                    {
                        var sParameterName = sURLVariables[i].split('=');
                        if (sParameterName[0] == "team")
                        {
                            teamSel = sParameterName[1];
                            window.history.pushState({}, document.title, "kafkaConnectors");
                            $scope.getConnectors.team = teamSel;
                        }
                    }
        }

		var topicFilter = $scope.getConnectors.connectornamesearch;
		if(topicFilter && topicFilter.length>0 && topicFilter.length<3){

		    swal({
             title: "",
             text: "Please enter atleast 3 characters to search.",
             timer: 2000,
             showConfirmButton: false
             });
		    return;
		    }

		    var getTopicsUrl = "";
		    if(topicsDisplayType == 'grid')
                getTopicsUrl = "getConnectors";
            else getTopicsUrl = "getConnectorsRowView";

		$http({
			method: "GET",
			url: getTopicsUrl,
            headers : { 'Content-Type' : 'application/json' },
            params: {'env' : envSelected,
                'pageNo' : pageNoSelected,
                'currentPage' : $scope.currentPageSelected,
                 'connectornamesearch' : $scope.getConnectors.connectornamesearch,
                 'teamName' : teamSel
                 }
		}).success(function(output) {
			$scope.resultBrowse = output;
			if(output!=null && output.length !=0){
			    if(topicsDisplayType == "grid"){
			        $scope.currentPageSelected = output[0][0].currentPage;
			        $scope.resultPages = output[0][0].allPageNos;
			    }
                else{
                    $scope.resultPages = output[0].allPageNos;
                    $scope.currentPageSelected = output[0].currentPage;
                }

                $scope.resultPageSelected = pageNoSelected;

            }else{
                $scope.resultPages = null;
            }
		}).error(
			function(error) 
			{
				$scope.alert = error;
				$scope.resultPages = null;
				$scope.resultPageSelected = null;
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