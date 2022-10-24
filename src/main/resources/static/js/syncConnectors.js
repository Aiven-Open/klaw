'use strict'

// confirmation of delete
// edit
// solution for transaction
// message store / key / gui
var app = angular.module('syncConnectorsApp',[]);

app.controller("syncConnectorsCtrl", function($scope, $http, $location, $window) {

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
                    $scope.connectorDetails = output.result;
                    modal.style.display = "block";
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );

        }

        $scope.updatedSyncArray = [];
        $scope.updateConnectorDetails = function(sequence, teamselected, connectorName) {

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
            serviceInput['connectorName'] = connectorName;
            serviceInput['teamSelected'] = teamselected;
            serviceInput['envSelected'] = $scope.getConnectors.envName;

            $scope.updatedSyncArray.push(serviceInput);
        }

        $scope.syncConnectors = function() {

            var serviceInput = {};

            if(!$scope.getConnectors.envName)
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
                    text: "You would like to Synchronize connectors with this selection ? ",
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
                            url: "updateSyncConnectors",
                            headers : { 'Content-Type' : 'application/json' },
                            params: {'updatedSyncConnectors' : $scope.updatedSyncArray},
                            data:  $scope.updatedSyncArray
                        }).success(function(output) {
                            $scope.ShowSpinnerStatus = false;
                            $scope.alert = "Connectors Sync Request : "+output.result;
                            $scope.updatedSyncArray = [];

                             if(output.result == 'success'){
                              swal({
                            		   title: "",
                            		   text: "Connectors Sync Request : "+output.result,
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

	$scope.getConnectors = function(pageNoSelected) {

        if(!$scope.getConnectors.envName)
            return;

        var serviceInput = {};
		serviceInput['env'] = $scope.getConnectors.envName;
		$scope.resultBrowse = [];
        $scope.ShowSpinnerStatusTopics = true;

		$http({
			method: "GET",
			url: "getSyncConnectors",
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
                            $scope.alertbulk = "Topic Sync Bulk Request : "+output.result;
                            $scope.updatedSyncArray = [];

                             if(output.result == 'success'){
                                $scope.resetCheckBoxes();
                                $scope.syncbulklog = output.data;
                                $scope.alertbulk = $scope.alertbulk + ". Please verify logs below.";

                                swal({
                                       title: "",
                                       text: "Topic Sync Bulk Request : "+output.result,
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
