'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('indexApp',[]);

app.controller("indexCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	$http.defaults.headers.common['Accept'] = 'application/json';

        $scope.getClusterApi = function() {

            $http({
                method: "GET",
                url: "getClusterApiStatus",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.clusterapi = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        };

        $scope.getEnvs = function() {

            $http({
                method: "GET",
                url: "getEnvsStatus",
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

        $scope.deleteEnv = function() {

        if (!window.confirm("Are you sure, you would like to delete the cluster : "
                        +  $scope.deleteEnv.idval
                        )) {
                        return;
                    }

            $http({
                            method: "POST",
                            url: "deleteClusterRequest",
                            headers : { 'Content-Type' : 'application/json' },
                            params: {'clusterId' : $scope.deleteEnv.idval },
                            data: {'clusterId' : $scope.deleteEnv.idval}
                        }).success(function(output) {

                            $scope.alert = "Delete Cluster Request : "+output.result;
                            $scope.getEnvs();

                        }).error(
                            function(error)
                            {
                                $scope.alert = error;
                            }
                        );
        }

        $scope.addNewEnv = function() {


                if($scope.addNewEnv.defparts.length<=0 || $scope.addNewEnv.defparts<=0)
                {
                    alert("Default partitions should not be empty and should be greater than 0");
                    return;
                }

                if($scope.addNewEnv.defmaxparts.length<=0 || $scope.addNewEnv.defmaxparts<=0)
                {
                    alert("Maximum partitions should not be empty and should be greater than 0");
                    return;
                }
                if($scope.addNewEnv.defrepfctr.length<=0 || $scope.addNewEnv.defrepfctr<=0)
                {
                    alert("Default replication factor should not be empty and should be greater than 0");
                    return;
                }
                var serviceInput = {};

                serviceInput['name'] = $scope.addNewEnv.envname;
                serviceInput['host'] = $scope.addNewEnv.host;
                serviceInput['port'] = $scope.addNewEnv.port;
                serviceInput['protocol'] = $scope.addNewEnv.protocol;
                serviceInput['type'] = $scope.addNewEnv.type;

                serviceInput['otherParams'] = "default.paritions=" + $scope.addNewEnv.defparts
                 + ",max.partitions=" + $scope.addNewEnv.defmaxparts + ",replication.factor=" + $scope.addNewEnv.defrepfctr;

                if (!window.confirm("Are you sure, you would like to add Env : "
                    +  $scope.addNewEnv.envname + ": " +
                    "\nHost : " + $scope.addNewEnv.host +
                    "\nPort : " + $scope.addNewEnv.port
                    )) {
                    return;
                }

                $http({
                    method: "POST",
                    url: "addNewEnv",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'addNewEnv' : serviceInput },
                    data: serviceInput
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
                url: "getSchemaRegEnvsStatus",
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
                   url: "getAuth",
                   headers : { 'Content-Type' : 'application/json' }
               }).success(function(output) {
                   $scope.statusauth = output.status;
                   $scope.userlogged = output.username;
                   $scope.teamname = output.teamname;
                    $scope.notifications = output.notifications;
                    $scope.notificationsAcls = output.notificationsAcls;
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