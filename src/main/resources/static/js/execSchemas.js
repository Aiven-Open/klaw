'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('execSchemasApp',[]);

app.controller("execSchemasCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	$http.defaults.headers.common['Accept'] = 'application/json';

	$scope.showSuccessToast = function() {
                      var x = document.getElementById("successbar");
                      x.className = "show";
                      setTimeout(function(){ x.className = x.className.replace("show", ""); }, 4000);
                    }

            $scope.showAlertToast = function() {
                      var x = document.getElementById("alertbar");
                      x.className = "show";
                      setTimeout(function(){ x.className = x.className.replace("show", ""); }, 4000);
                    }

        $scope.getMySchemaRequests = function() {
            $http({
                method: "GET",
                url: "getCreatedSchemaRequests",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.schemaRequests = output;

            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

        $scope.execSchemaRequest = function(topicName, env) {

            $http({
                method: "POST",
                url: "execSchemaRequests",
                headers : { 'Content-Type' : 'application/json' },
                params: {'topicName' : topicName,
                    'env' : env},
                    data: {'topicName' : topicName, 'env' : env}
            }).success(function(output) {

                $scope.alert = "Schema Approve Request : "+output.result;
                $scope.getMySchemaRequests();
                $scope.showSuccessToast();

            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.alertnote = error;
                    $scope.showAlertToast();
                }
            );
        }

        $scope.execSchemaRequestDecline = function(topicName, env) {

                    $http({
                        method: "POST",
                        url: "execSchemaRequestsDecline",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'topicName' : topicName,
                            'env' : env},
                            data: {'topicName' : topicName, 'env' : env}
                    }).success(function(output) {

                        $scope.alert = "Schema Decline Request : "+output.result;
                        $scope.getMySchemaRequests();
                        $scope.showSuccessToast();

                    }).error(
                        function(error)
                        {
                            $scope.alert = error;
                            $scope.alertnote = error;
                            $scope.showAlertToast();
                        }
                    );
                }

    $scope.getExecAuth = function() {
    	//alert("onload");
        $http({
            method: "GET",
            url: "getExecAuth",
            headers : { 'Content-Type' : 'application/json' }
        }).success(function(output) {
            $scope.statusauth = output.status;
            if(output.status=="NotAuthorized")
                $scope.alerttop = output.status;
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
                    $scope.statusauth = output.status;
                    $scope.userlogged = output.username;
                    $scope.teamname = output.teamname;
                     $scope.notifications = output.notifications;
                    $scope.notificationsAcls = output.notificationsAcls;
                    $scope.notificationsSchemas = output.notificationsSchemas;
                    $scope.notificationsUsers = output.notificationsUsers;
                    $scope.statusauthexectopics = output.statusauthexectopics;
                    $scope.statusauthexectopics_su = output.statusauthexectopics_su;
                    $scope.alerttop = output.alertmessage;
                    if(output.companyinfo == null){
                        $scope.companyinfo = "Company not defined!!";
                    }
                    else
                        $scope.companyinfo = output.companyinfo;

                    if($scope.userlogged != null)
                        $scope.loggedinuser = "true";
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );
        	}

        $scope.logout = function() {
            //alert("onload");
            $http({
                method: "GET",
                url: "logout"
            }).success(function(output) {

                $location.path('/');
                $window.location.reload();
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }


}
);