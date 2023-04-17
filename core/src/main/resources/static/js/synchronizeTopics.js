'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('synchronizeTopicsApp',[]);

app.controller("synchronizeTopicsCtrl", function($scope, $http, $location, $window) {
	
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


	$scope.getEnvs = function() {

	        $http({
                method: "GET",
                url: "getSyncEnv",
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

        $scope.updatedSyncArray = [];
        $scope.updateTopicDetails = function(sequence, teamselected,topic, partitions, replicationFactor) {

            var seqFound = -1;
            var i;
            for (i = 0; i < $scope.updatedSyncArray.length; i++) {
              if($scope.updatedSyncArray[i]['sequence'] == sequence)
                seqFound = i;
            }
            if(seqFound != -1){
                $scope.updatedSyncArray.splice(seqFound,1);
                return;
            }

            var serviceInput = {};
            serviceInput['sequence'] = sequence;
            serviceInput['topicName'] = topic;
            serviceInput['partitions'] = partitions;
            serviceInput['replicationFactor'] = replicationFactor;
            serviceInput['teamSelected'] = teamselected;
            serviceInput['envSelected'] = $scope.getTopics.envName;

            $scope.updatedSyncArray.push(serviceInput);
        }

        $scope.synchTopics = function() {

            var serviceInput = {};

            if(!$scope.getTopics.envName)
                   return;

            if($scope.updatedSyncArray.length == 0)
            {
                swal({
                       title: "",
                       text: "Please select a record.",
                       timer: 2000,
                       showConfirmButton: false
                   });
                return;
            }

            swal({
                    title: "Are you sure?",
                    text: "You would like to Synchronize topics with this selection ? ",
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: "Yes, synchronize it!",
                    cancelButtonText: "No, cancel please!",
                    closeOnConfirm: true,
                    closeOnCancel: true
                }).then(function(isConfirm){

                    if (isConfirm.dismiss != "cancel") {
                        $scope.ShowSpinnerStatus = true;
                        $http({
                            method: "POST",
                            url: "updateSyncTopics",
                            headers : { 'Content-Type' : 'application/json' },
                            params: {'updatedSyncTopics' : $scope.updatedSyncArray},
                            data:  $scope.updatedSyncArray
                        }).success(function(output) {
                            $scope.ShowSpinnerStatus = false;
                            $scope.alert = "Topic Sync Request : "+output.message;
                            $scope.updatedSyncArray = [];

                             if(output.success){
                              swal({
                            		   title: "",
                            		   text: "Topic Sync Request : "+output.message,
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

        };

	// We add the "time" query parameter to prevent IE
	// from caching ajax results

	$scope.getTopics = function(pageNoSelected) {

        if(!$scope.getTopics.envName)
            return;

        var serviceInput = {};
		serviceInput['env'] = $scope.getTopics.envName;
		$scope.resultBrowse = [];
        $scope.ShowSpinnerStatusTopics = true;

		$http({
			method: "GET",
			url: "getSyncTopics",
            headers : { 'Content-Type' : 'application/json' },
            params: {'env' : $scope.getTopics.envName,
             'topicnamesearch' : $scope.getTopics.topicnamesearch,
             'showAllTopics' : "" + $scope.showAllTopics,
                'pageNo' : pageNoSelected,
                 'currentPage' : $scope.currentPageSelected}
		}).success(function(output) {
		    $scope.ShowSpinnerStatusTopics = false;
			$scope.resultBrowse = output["resultSet"];
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

	$scope.getTopicsBulk = function(pageNoSelected) {

            if(!$scope.getTopicsBulk.envName)
                return;

            var serviceInput = {};
    		serviceInput['env'] = $scope.getTopicsBulk.envName;
    		$scope.resultBrowseBulk = [];
    		$scope.updatedTopicIdsArray = [];
    		$scope.allTopicsCount = 0;
    		$scope.alertnotebulk = "";
    		$scope.alertbulk = "";

    		$scope.ShowSpinnerStatusTopicsBulk = true;

    		$http({
    			method: "GET",
    			url: "getSyncTopics",
                headers : { 'Content-Type' : 'application/json' },
                params: {'env' : $scope.getTopicsBulk.envName,
                 'topicnamesearch' : $scope.getTopicsBulk.topicnamesearch,
                 'showAllTopics' : "" + $scope.showAllTopics,
                 'isBulkOption' : "true",
                    'pageNo' : pageNoSelected,
                    'currentPage' : $scope.currentPageSelectedBulk }
    		}).success(function(output) {
    		    $scope.ShowSpinnerStatusTopicsBulk = false;
    			$scope.resultBrowseBulk = output["resultSet"];
    			if($scope.resultBrowseBulk != null && $scope.resultBrowseBulk.length != 0){
    			    $scope.allTopicsCount = output["allTopicsCount"];
                    $scope.resultPagesBulk = $scope.resultBrowseBulk[0].allPageNos;
                    $scope.resultPageSelectedBulk = pageNoSelected;
                    $scope.currentPageSelectedBulk = $scope.resultBrowseBulk[0].currentPage;
                }
    		}).error(
    			function(error)
    			{
    			    $scope.ShowSpinnerStatusTopicsBulk = false;
    			    $scope.resultBrowseBulk = [];
    				if(error != null && error.message != null){
                           $scope.alertbulk = error.message;
                           $scope.alertnotebulk = $scope.alertbulk;
                           $scope.showAlertBulkToast();
                       }else{
                           $scope.alertbulk = error;
                           $scope.alertnotebulk = error;
                           $scope.showAlertBulkToast();
                       }
    			}
    		);

    	};

    	$scope.syncTopicCbId = [];
        $scope.resetCheckBoxes = function(){
            $scope.updatedTopicIdsArray = [];
            $scope.getTopicsBulk.team = null;

            if($scope.syncTopicCbId || $scope.syncTopicCbId != null){
                for(var i=0;i<$scope.syncTopicCbId.length;i++){
                    $scope.syncTopicCbId[i].topicname = false;
                 }
             }
        }

	    $scope.updatedTopicIdsArray = [];

	    $scope.updatedTopicDetailsArray = [];

    	$scope.updateTopicIds = function(topicId, topicPartitions, topicReplicationFactor, isTopicSelected){
    	    if($scope.updatedTopicIdsArray.includes(topicId) && !isTopicSelected)
                $scope.updatedTopicIdsArray.splice($scope.updatedTopicIdsArray.indexOf(topicId), 1);
            else if(isTopicSelected)
                $scope.updatedTopicIdsArray.push(topicId);

            var serviceInput = {};
            serviceInput['topicName'] = topicId;
            serviceInput['topicPartitions'] = topicPartitions;
            serviceInput['topicReplicationFactor'] = topicReplicationFactor;

            $scope.updatedTopicDetailsArray.push(serviceInput);
    	}

    	$scope.enableCreateTopicsButton = false;

    	$scope.onSelectSyncTopics = function(typeOfSync){
            $scope.enableCreateTopicsButton = true;
        }

        $scope.syncTopicsBulk = function() {
            $scope.alertbulk = "";
            $scope.alertnotebulk = "";
            $scope.syncbulklog = "";

            var typeOfSync = $scope.typeOfSync;
            if(!typeOfSync)
                return;

            var tmpCount = 0;

            if(typeOfSync == "SELECTED_TOPICS")
            {
                if($scope.updatedTopicIdsArray.length == 0)
                {
                    $scope.alertnotebulk = "Please select topics from above.";
                    $scope.showAlertBulkToast();
                    return;
                }
                tmpCount = $scope.updatedTopicIdsArray.length;
            }
            else if(typeOfSync == "ALL_TOPICS")
            {
                if(!$scope.allTopicsCount || $scope.allTopicsCount == 0)
                {
                    $scope.alertnotebulk = "No topics found in source environment!";
                    $scope.showAlertBulkToast();
                    return;
                }
                tmpCount = $scope.allTopicsCount;
            }

            if(!$scope.getTopicsBulk.envName || $scope.getTopicsBulk.envName == null)
            {
                $scope.alertnotebulk = "Please select a source environment!";
                $scope.showAlertBulkToast();
                return;
            }

            if(!$scope.getTopicsBulk.team || $scope.getTopicsBulk.team == null)
            {
                $scope.alertnotebulk = "Please select a team !";
                $scope.showAlertBulkToast();
                return;
            }

            var serviceInput = {};
            serviceInput['topicNames'] = $scope.updatedTopicIdsArray;
            serviceInput['sourceEnv'] = $scope.getTopicsBulk.envName;
            serviceInput['selectedTeam'] = $scope.getTopicsBulk.team;
            serviceInput['typeOfSync'] = typeOfSync;
            serviceInput['topicDetails'] = $scope.updatedTopicDetailsArray;
            serviceInput['topicSearchFilter'] = $scope.getTopicsBulk.topicnamesearch;

            swal({
                    title: "Are you sure?",
                    text: "You would like to Synchronize "+ tmpCount +" topics to the selected team " + $scope.getTopicsBulk.team +
                    " and environment ?",
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: "Yes, synchronize it!",
                    cancelButtonText: "No, cancel please!",
                    closeOnConfirm: true,
                    closeOnCancel: true
                }).then(function(isConfirm){
                    if (isConfirm.dismiss != "cancel") {
                        $scope.ShowSpinnerStatus = true;

                        $http({
                            method: "POST",
                            url: "updateSyncTopicsBulk",
                            headers : { 'Content-Type' : 'application/json' },
                            data:  serviceInput
                        }).success(function(output) {
                            $scope.ShowSpinnerStatus = false;
                            $scope.alertbulk = "Topic Sync Bulk Request : "+output.message;
                            $scope.updatedSyncArray = [];

                             if(output.success){
                                $scope.resetCheckBoxes();
                                $scope.syncbulklog = output.data;
                                $scope.alertbulk = $scope.alertbulk + ". Please verify logs below.";

                                swal({
                                       title: "",
                                       text: "Topic Sync Bulk Request : "+output.message,
                                       timer: 2000,
                                       showConfirmButton: false
                                   });
//                               $scope.getTopicsBulk(1);
                            }else $scope.showSubmitFailed('','');
                        }).error(
                            function(error)
                            {
                                $scope.ShowSpinnerStatus = false;
                                if(error != null && error.message != null){
                                       $scope.alertbulk = error.message;
                                       $scope.showSubmitFailed('','');
//                                       $scope.alertnotebulk = $scope.alertbulk;
//                                       $scope.showAlertBulkToast();
                                   }else{
                                       $scope.alertbulk = error;
                                       $scope.showSubmitFailed('','');
//                                       $scope.alertnotebulk = error;
//                                       $scope.showAlertBulkToast();
                               }
                            }
                        );
                    } else {
                        return;
                    }
                });


        }
}
);