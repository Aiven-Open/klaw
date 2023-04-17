'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('serverConfigApp',[]);

app.controller("serverConfigCtrl", function($scope, $http, $location, $window) {
	
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

        $scope.resetCache = function() {
                $http({
                        method: "GET",
                        url: "resetCache",
                        headers : { 'Content-Type' : 'application/json' }
                    }).success(function(output) {
                        $scope.alert = "Cache is reset.";
                        swal({
                                 title: "",
                                 text: "Cache is reset.",
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

        $scope.shutdownKw = function() {
                swal({
                		title: "Are you sure?",
                		text: "You would like to stop Klaw?",
                		type: "warning",
                		showCancelButton: true,
                		confirmButtonColor: "#DD6B55",
                		confirmButtonText: "Yes, stop it!",
                		cancelButtonText: "No, cancel please!",
                		closeOnConfirm: true,
                		closeOnCancel: true
                	}).then(function(isConfirm){
                		if (isConfirm.dismiss != "cancel") {
                			$http({
                                        method: "GET",
                                        url: "shutdownContext",
                                        headers : { 'Content-Type' : 'application/json' }
                                    }).success(function(output) {
                                        $scope.alert = "Stopping Klaw ...";
                                         if(output.result === 'success'){
                                          swal({
                                        		   title: "",
                                        		   text: "Stopping Klaw ...",
                                        		   timer: 2000,
                                        		   showConfirmButton: false
                                        	   });
                                        }else $scope.showSubmitFailed('','');
                                    }).error(
                                        function(error)
                                        {
                                            $scope.handleValidationErrors(error);
                                        }
                                    );
                		} else {
                			return;
                		}
                	});
                }

        $scope.getAllServerProperties = function() {
            $http({
                method: "GET",
                url: "getAllServerConfig",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.allProps = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

        $scope.getAllEditableProperties = function() {
                $http({
                    method: "GET",
                    url: "getAllServerEditableConfig",
                    headers : { 'Content-Type' : 'application/json' }
                }).success(function(output) {
                    $scope.allEditableProps = output;
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );
            }


         $scope.testClusterApiConnection = function(kwkey, kwvalue){
            $http({
                    method: "GET",
                    url: "testClusterApiConnection",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'clusterApiUrl' : kwvalue}
                }).success(function(output) {
                    $scope.alert = "Cluster Api Connection Url " + kwvalue  + ", status: " + output.result;
                    swal({
                            title: "",
                            text: "Cluster Api Connection " + kwvalue + ". Status: " + output.result,
                            timer: 2000,
                            showConfirmButton: false
                        });

                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                        $scope.showAlertToast();
                    }
                );
         }

         $scope.updateEditableConfig = function(kwkey, kwvalue) {

                 var serviceInput = {};

                 if(kwkey!='klaw.broadcast.text'){
                    if(!kwvalue || kwvalue.trim().length == 0)
                         {
                             $scope.alert = "Please fill in a valid value.";
                             return;
                         }
                 }


                 serviceInput['kwKey'] = kwkey;
                 serviceInput['kwValue'] = kwvalue;

                 $http({
                     method: "POST",
                     url: "updateKwCustomProperty",
                     headers : { 'Content-Type' : 'application/json' },
                     params: {'kwPropertiesModel' : serviceInput },
                     data: serviceInput
                 }).success(function(output) {
                     $scope.alert = "Property ("+ output.data +") update status : " + output.message;

                     if(output.success){
                     swal({
                             title: "",
                             text: "Property Update status. ("+ output.data +") " + output.message,
                             timer: 2000,
                             showConfirmButton: false
                         });
                        $scope.getAllEditableProperties();
                     }
                     else
                     {
                        swal({
                             title: "",
                             text: "Property Update status Failed",
                             timer: 2000,
                             showConfirmButton: false
                         });
                        $scope.getAllEditableProperties();
                     }
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