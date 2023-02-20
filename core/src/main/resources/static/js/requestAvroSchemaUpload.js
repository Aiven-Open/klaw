'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('requestSchemaApp',[]);

app.directive('onReadFile', function ($parse) {
	return {
		restrict: 'A',
		scope: false,
		link: function(scope, element, attrs) {
            var fn = $parse(attrs.onReadFile);

			element.on('change', function(onChangeEvent) {
				var reader = new FileReader();

				reader.onload = function(onLoadEvent) {
					scope.$apply(function() {
						fn(scope, {$fileContent:onLoadEvent.target.result});
					});
				};

				reader.readAsText((onChangeEvent.srcElement || onChangeEvent.target).files[0]);
			});
		}
	};
});

app.controller("requestSchemaCtrl", function($scope, $http, $location, $window) {
	
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

	 $scope.getSchemaEnvs = function() {

        $http({
            method: "GET",
            url: "getEnvsForSchemaRequests",
            headers : { 'Content-Type' : 'application/json' }
        }).success(function(output) {
            $scope.allschemaenvs = output;
        }).error(
            function(error)
            {
                $scope.alert = error;
            }
        );
    }

    $scope.loadParams = function() {
            var topicSelected;

            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');
            for (var i = 0; i < sURLVariables.length; i++)
            {
                var sParameterName = sURLVariables[i].split('=');
                if (sParameterName[0] == "topicname")
                {
                    $scope.topicSelectedFromUrl = sParameterName[1];
                    $scope.addSchema.topicname = $scope.topicSelectedFromUrl;

                    $scope.getAllTopics();
                }
            }
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

        $scope.getAllTopics = function() {

            $scope.alltopics = null;
                    $http({
                        method: "GET",
                        url: "getTopicsOnly",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'isMyTeamTopics' : 'true' },
                    }).success(function(output) {
                        $scope.alltopics = output;
                    }).error(
                        function(error)
                        {
                            $scope.alert = error;
                        }
                    );
                }

        $scope.cancelRequest = function() {
                $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/browseTopics";
            }

        $scope.showContent = function($fileContent){
                $scope.addSchema.schemafull = $fileContent;
            };

        $scope.addSchema = function() {

            if(!$scope.addSchema.envName)
            {
                $scope.alertnote = "Please select an environment";
                $scope.showAlertToast();
                return;
            }

            if($scope.addSchema.topicname == null || $scope.addSchema.topicname.length==0)
            {
                $scope.alertnote = "Please fill in topic name.";
                $scope.showAlertToast();
                return;
            }else
            {
                $scope.addSchema.topicname = $scope.addSchema.topicname.trim();
                if($scope.addSchema.topicname.length==0){
                    $scope.alertnote = "Please fill in topic name.";
                    $scope.showAlertToast();
                    return;
                }
            }

            if(!$scope.addSchema.schemafull)
            {
                $scope.alertnote = "Please select a valid Avro schema file";
                $scope.showAlertToast();
                return;
            }

            var serviceInput = {};
            $scope.alert = null;
             $scope.alertnote = null;

            serviceInput['environment'] = $scope.addSchema.envName;
            serviceInput['topicname'] = $scope.addSchema.topicname;
            serviceInput['appname'] = "App";
            serviceInput['remarks'] = $scope.addSchema.remarks;
            serviceInput['schemafull'] = $scope.addSchema.schemafull;
            serviceInput['schemaversion'] = "1.0";

            $http({
                method: "POST",
                url: "uploadSchema",
                headers : { 'Content-Type' : 'application/json' },
                params: {'addSchemaRequest' : serviceInput },
                data: serviceInput
            }).success(function(output) {
                $scope.alert = "Schema Upload Request : "+output.result;
                $scope.addSchema.topicname = "";
                if(output.result == 'success'){
                    swal({
                             title: "Awesome !",
                             text: "Schema Request : " + output.result,
                             showConfirmButton: true
                         }).then(function(isConfirm){
                                $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath +"/mySchemaRequests?reqsType=CREATED&schemaCreated=true";
                         });
                 }else
                 {
                    $scope.alert = "Schema Request : " + output.result;
                    $scope.showSubmitFailed('','');
                 }

            }).error(
                function(error)
                {
                    if(!error){
                        error = "Schema could not be uploaded. Please check schema.";
                         $scope.alert = error;
                         $scope.alertnote = error;
                         $scope.showAlertToast();
                    }
                    else{
                            $scope.handleValidationErrors(error);
                     }
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

                    if(output.requestItems!='Authorized')
                    {
                        swal({
                             title: "Not Authorized !",
                             text: "",
                             showConfirmButton: true
                         }).then(function(isConfirm){
                                $scope.alertnote = "You are not authorized to request.";
                                $scope.showAlertToast();
                                $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/index";
                         });
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