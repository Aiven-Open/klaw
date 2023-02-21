'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('syncBackTopicsApp',[]);

app.controller("syncBackTopicsCtrl", function($scope, $http, $location, $window) {
	
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

	// We add the "time" query parameter to prevent IE
	// from caching ajax results

	$scope.getTopics = function(pageNoSelected, fromSelect, topicsDisplayType) {

        var serviceInput = {};
        var envSelected;
        $scope.resultBrowse = null;
        $scope.resultPages = null;
        $scope.alert = null;
        $scope.resultPageSelected = null;
        var teamSel = $scope.getTopics.team;
        var topicType = null;
        $scope.allTopicsCount = 0;

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
                    $scope.getTopics.envName = envSelected;
                }else return;
            }
        }else if(fromSelect == "true"){
                 if(!$scope.getTopics.envName)
                        envSelected = "ALL";
                 else
                    envSelected = $scope.getTopics.envName;

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
                            window.history.pushState({}, document.title, "syncBackTopics");
                            $scope.getTopics.team = teamSel;
                        }
                        else if (sParameterName[0] == "producer")
                        {
                            topicType = 'Producer';
                        }
                        else if (sParameterName[0] == "consumer")
                        {
                            topicType = 'Consumer';
                        }
                    }
        }

		var topicFilter = $scope.getTopics.topicnamesearch;
		if(topicFilter && topicFilter.length>0 && topicFilter.length<3){
		    alert("Please enter atleast 3 characters of the topic name.");
		        return;
		    }

		    var getTopicsUrl = "";
		    if(topicsDisplayType == 'grid')
                getTopicsUrl = "getTopics";
            else getTopicsUrl = "getTopicsRowView";

		$http({
			method: "GET",
			url: getTopicsUrl,
            headers : { 'Content-Type' : 'application/json' },
            params: {'env' : envSelected,
                'pageNo' : pageNoSelected,
                'currentPage' : $scope.currentPageSelected,
                 'topicnamesearch' : $scope.getTopics.topicnamesearch,
                 'teamName' : teamSel,
                 'topicType' : topicType
                 }
		}).success(function(output) {
			$scope.resultBrowse = output;
			if(output!=null && output.length !=0){
			    $scope.onChangeSourceEnv();

			    if(topicsDisplayType == "grid"){
			        $scope.resultPages = output[0][0].allPageNos;
			        $scope.currentPageSelected = output[0][0].currentPage;
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
				$scope.handleErrorMessage(error);
				$scope.resultPages = null;
				$scope.resultPageSelected = null;
			}
		);
	}

    $scope.enableCreateTopicsButton = false;

	$scope.syncBackTopics = function(){
	    $scope.alert = "";
	    $scope.alertnote = "";
	    $scope.syncbacklog = "";

	    var typeOfSync = $scope.typeOfSync;
	    if(!typeOfSync)
	        return;

        if(typeOfSync == "SELECTED_TOPICS")
        {
            if($scope.updatedTopicIdsArray.length == 0)
            {
                $scope.alertnote = "Please select topics from above.";
                $scope.showAlertToast();
                return;
            }
        }
        else if(typeOfSync == "ALL_TOPICS")
        {
            if(!$scope.allTopicsCount || $scope.allTopicsCount == 0)
            {
                $scope.alertnote = "No topics found in source environment!";
                $scope.showAlertToast();
                return;
            }
        }

        if(!$scope.getTopics.envName || $scope.getTopics.envName == null)
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
        serviceInput['topicIds'] = $scope.updatedTopicIdsArray;
        serviceInput['sourceEnv'] = $scope.getTopics.envName;
        serviceInput['targetEnv'] = $scope.targetEnvId;
        serviceInput['typeOfSync'] = typeOfSync;

        swal({
        		title: "Are you sure?",
        		text: "You would like to create topics based on this selection ?",
        		type: "warning",
        		showCancelButton: true,
        		confirmButtonColor: "#DD6B55",
        		confirmButtonText: "Yes, create them!",
        		cancelButtonText: "No, cancel please!",
        		closeOnConfirm: true,
        		closeOnCancel: true
        	}).then(function(isConfirm){
        		if (isConfirm.dismiss !== "cancel") {

        		    $scope.ShowSpinnerStatus = true;

        			$http({
                        method: "POST",
                        url: "updateSyncBackTopics",
                        headers : { 'Content-Type' : 'application/json' },
                        data:  serviceInput
                    }).success(function(output) {
                        $scope.ShowSpinnerStatus = false;
                        $scope.alert = "Sync back topic request : "+ output.result;
                        if(output.result === "success"){
                            $scope.resetCheckBoxes();
                            $scope.syncbacklog = output.data;
                            $scope.alert = $scope.alert + ". Errors are ignored if topics already exist on the target environment. Please verify logs.";
                        }
                         if(output.result === 'success'){
                          swal({
                        		   title: "",
                        		   text: "Sync back topic request : "+ output.result,
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

	$scope.updateTopicIds = function(topicId, isTopicSelected){
	    if($scope.updatedTopicIdsArray.includes(topicId) && !isTopicSelected)
            $scope.updatedTopicIdsArray.splice($scope.updatedTopicIdsArray.indexOf(topicId), 1);
        else if(isTopicSelected)
            $scope.updatedTopicIdsArray.push(topicId);
	}

	$scope.onChangeSourceEnv = function(){
	    var sourceEnv = $scope.getTopics.envName;
	    $scope.updatedTopicIdsArray = [];
	    $scope.resetCheckBoxes();
	    $scope.alert = "";
	    $scope.alertnote = "";
	    $scope.syncbacklog = "";

	    if(!sourceEnv || sourceEnv == null)
	    {
	        return;
	    }
	    $scope.allTopicsCount = 0;

        $http({
                method: "GET",
                url: "getTopicsCountPerEnv",
                headers : { 'Content-Type' : 'application/json' },
                params: {'sourceEnvSelected' : sourceEnv}
            }).success(function(output) {
                if(output.status == "success"){
                    $scope.allTopicsCount = output.topicsCount;
                    }
                else{
                    $scope.alertnote = "Cannot retrieve any topics from source environment!";
                }
            }).error(
                function(error)
                {
                    $scope.handleErrorMessage(error);
                }
            );
	}

    $scope.syncBackTopicCbId = [];
	$scope.resetCheckBoxes = function(){
	    $scope.updatedTopicIdsArray = [];
	    $scope.targetEnvId = null;

	    if($scope.syncBackTopicCbId || $scope.syncBackTopicCbId != null){
            for(var i=0;i<$scope.syncBackTopicCbId.length;i++){
                $scope.syncBackTopicCbId[i].topicIdSelected = false;
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