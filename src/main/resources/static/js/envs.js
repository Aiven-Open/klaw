'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('envsApp',[]);

app.controller("envsCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	$http.defaults.headers.common['Accept'] = 'application/json';

        $scope.getEnvs = function() {

            $http({
                method: "GET",
                url: "/getEnvs",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.allenvs = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        };

        $scope.addNewEnv = function() {

                var serviceInput = {};

                serviceInput['name'] = $scope.addNewEnv.envname;
                serviceInput['host'] = $scope.addNewEnv.host;
                serviceInput['port'] = $scope.addNewEnv.port;
                serviceInput['protocol'] = $scope.addNewEnv.protocol;
                serviceInput['type'] = $scope.addNewEnv.type;

                if (!window.confirm("Are you sure, you would like to add Env : "
                    +  $scope.addNewEnv.envname + ": " +
                    "\nHost : " + $scope.addNewEnv.host +
                    "\nPort :" + $scope.addNewEnv.port
                    )) {
                    return;
                }

                $http({
                    method: "POST",
                    url: "/addNewEnv",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'addNewEnv' : serviceInput },
                    data: {'addNewEnv' : serviceInput}
                }).success(function(output) {
                    $scope.alert = "New Environment : "+output.result;
                }).error(
                    function(error)
                    {
                     $scope.alert = error;
                    }
                );

            };

        $scope.getSchemaRegEnvs = function() {

            $http({
                method: "GET",
                url: "/getSchemaRegEnvs",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.allschenvs = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

           $scope.getAuth = function() {
           	$http({
                   method: "GET",
                   url: "/getAuth",
                   headers : { 'Content-Type' : 'application/json' }
               }).success(function(output) {
                   $scope.statusauth = output.status;
                   $scope.userlogged = output.username;
                    $scope.notifications = output.notifications;
                   $scope.statusauthexectopics = output.statusauthexectopics;
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
                url: "/logout"
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