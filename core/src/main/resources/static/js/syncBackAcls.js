'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('syncBackAclsApp',[]);

app.controller("syncBackAclsCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	//$http.defaults.headers.common['Accept'] = 'application/json';

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

	// We add the "time" query parameter to prevent IE
	// from caching ajax results

	$scope.getAcls = function(pageNoSelected) {

        var serviceInput = {};
        var envSelected = $scope.getAcls.envName;
        if(!$scope.getAcls.envName)
            return;
        $scope.resultBrowse = null;
        $scope.resultPages = null;
        $scope.alert = null;
        $scope.resultPageSelected = null;
        var teamSel = $scope.getAcls.team;
        $scope.allTopicsCount = 0;

		var topicFilter = $scope.getAcls.topicnamesearch;
		if(topicFilter && topicFilter.length>0 && topicFilter.length<3){
		    alert("Please enter at least 3 characters of the topic name.");
		    return;
		    }

		$http({
			method: "GET",
			url: "getSyncBackAcls",
            headers : { 'Content-Type' : 'application/json' },
            params: {'env' : envSelected,
                'pageNo' : pageNoSelected,
                'currentPage' : $scope.currentPageSelected,
                 'topicnamesearch' : $scope.getAcls.topicnamesearch,
                 'teamName' : teamSel
                 }
		}).success(function(output) {
			$scope.resultBrowse = output;
			if(output!=null && output.length !=0){
			    $scope.onChangeSourceEnv();
                $scope.resultPages = output[0].allPageNos;
                $scope.resultPageSelected = pageNoSelected;
                $scope.currentPageSelected = output[0].currentPage;
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

    $scope.enableCreateTopicsButton = false;

	$scope.syncBackAcls = function(){
	    $scope.alert = "";
	    $scope.alertnote = "";
	    $scope.syncbacklog = [];

	    var typeOfSync = $scope.typeOfSync;
	    if(!typeOfSync)
	        return;

        if(typeOfSync == "SELECTED_ACLS")
        {
            if($scope.updatedTopicIdsArray.length == 0)
            {
                $scope.alertnote = "Please select acls from above.";
                $scope.showAlertToast();
                return;
            }
        }
        else if(typeOfSync == "ALL_ACLS")
        {
            if(!$scope.allAclsCount || $scope.allAclsCount == 0)
            {
                $scope.alertnote = "No acls found in source environment!";
                $scope.showAlertToast();
                return;
            }
        }

        if(!$scope.getAcls.envName || $scope.getAcls.envName == null)
        {
            $scope.alertnote = "Please select a source environment!";
            $scope.showAlertToast();
            return;
        }

        if(!$scope.targetEnvId || $scope.targetEnvId == null)
        {
            $scope.alertnote = "Please select a target environment!";
            $scope.showAlertToast();
            return;
        }

        var serviceInput = {};
        serviceInput['aclIds'] = $scope.updatedTopicIdsArray;
        serviceInput['sourceEnv'] = $scope.getAcls.envName;
        serviceInput['targetEnv'] = $scope.targetEnvId;
        serviceInput['typeOfSync'] = typeOfSync;

        swal({
        		title: "Are you sure?",
        		text: "You would like to create acls based on this selection ?",
        		type: "warning",
        		showCancelButton: true,
        		confirmButtonColor: "#DD6B55",
        		confirmButtonText: "Yes, create them!",
        		cancelButtonText: "No, cancel please!",
        		closeOnConfirm: true,
        		closeOnCancel: true
        	}).then(function(isConfirm){
        		if (isConfirm.dismiss != "cancel") {
        		    $scope.ShowSpinnerStatus = true;
        			$http({
                        method: "POST",
                        url: "updateSyncBackAcls",
                        headers : { 'Content-Type' : 'application/json' },
                        data:  serviceInput
                    }).success(function(output) {
                        $scope.ShowSpinnerStatus = false;
                        $scope.alert = "Sync back acls request : "+ output.message;
                        if(output.success){
                            $scope.resetCheckBoxes();
                            $scope.syncbacklog = output.data;
                            $scope.alert = $scope.alert + ". Please verify logs.";

                            swal({
                            		   title: "",
                            		   text: "Sync back subscriptions request : "+ output.message,
                            		   timer: 2000,
                            		   showConfirmButton: false
                            	   });

                        }else $scope.showSubmitFailed('','');
                    }).error(
                        function(error)
                        {
                            $scope.ShowSpinnerStatus = false;
                            $scope.handleErrorMessage(error);
                        }
                    );
        		} else {
        			return;
        		}
        	});

	}

	$scope.onSelectSyncTopics = function(typeOfSync){
	    $scope.enableCreateTopicsButton = true;
	}

    $scope.updatedTopicIdsArray = [];

	$scope.updateAclIds = function(aclId, isAclSelected){
	    if($scope.updatedTopicIdsArray.includes(aclId) && !isAclSelected)
            $scope.updatedTopicIdsArray.splice($scope.updatedTopicIdsArray.indexOf(aclId), 1);
        else if(isAclSelected)
            $scope.updatedTopicIdsArray.push(aclId);
	}

	$scope.onChangeSourceEnv = function(){
	    var sourceEnv = $scope.getAcls.envName;
	    $scope.updatedTopicIdsArray = [];
	    $scope.resetCheckBoxes();
	    $scope.alert = "";
	    $scope.alertnote = "";
	    $scope.syncbacklog = [];

	    if(!sourceEnv || sourceEnv == null)
	    {
	        return;
	    }
	    $scope.allAclsCount = 0;

        $http({
                method: "GET",
                url: "getAclsCountPerEnv",
                headers : { 'Content-Type' : 'application/json' },
                params: {'sourceEnvSelected' : sourceEnv}
            }).success(function(output) {
                if(output.status == "success"){
                    $scope.allAclsCount = output.aclsCount;
                    }
                else{
                    $scope.alertnote = "Cannot retrieve any acls from source environment!";
                }
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
	}

    $scope.syncBackTopicCbId = [];
	$scope.resetCheckBoxes = function(){
	    $scope.updatedTopicIdsArray = [];
	    $scope.targetEnvId = null;

	    if($scope.syncBackTopicCbId || $scope.syncBackTopicCbId != null){
            for(var i=0;i<$scope.syncBackTopicCbId.length;i++){
                $scope.syncBackTopicCbId[i].aclIdSelected = false;
             }
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
}
);