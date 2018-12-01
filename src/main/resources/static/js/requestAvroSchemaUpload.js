'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('requestSchemaApp',[]);

app.controller("requestSchemaCtrl", function($scope, $http, $location, $window) {
	
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
           // alert(allenvs);
        }).error(
            function(error)
            {
                $scope.alert = error;
            }
        );
    }

        $scope.addSchema = function() {

            var serviceInput = {};

            serviceInput['environment'] = $scope.addSchema.envName.name;
            serviceInput['topicname'] = $scope.addSchema.topicname;
            serviceInput['teamname'] = $scope.addSchema.team.teamname;
            serviceInput['appname'] = $scope.addSchema.app;
            serviceInput['remarks'] = $scope.addSchema.remarks;
            serviceInput['schemafull'] = $scope.addSchema.schemafull;
            serviceInput['schemaversion'] = $scope.addSchema.schemaversion;

            if (!window.confirm("Are you sure, you would like to upload the schema : "
                +  $scope.addSchema.topicname + ": " +
                "\nEnv : " + $scope.addSchema.envName.name +
                "\nTeam :" + $scope.addSchema.team.teamname +
                "\nApp :" + $scope.addSchema.app +
                "\nSchema :" + $scope.addSchema.schemafull
                )) {
                return;
            }

            $http({
                method: "POST",
                url: "/uploadSchema",
                headers : { 'Content-Type' : 'application/json' },
                params: {'addSchemaRequest' : serviceInput },
                data: {'addSchemaRequest' : serviceInput}
            }).success(function(output) {
                $scope.alert = "Schema Upload Request : "+output.result;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                    alert("Error : "+error.value);
                }
            );

        };

        $scope.loadTeams = function() {
            $http({
                method: "GET",
                url: "/getAllTeams",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.allTeams = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                    alert("Error : "+error.value);
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

    $scope.getExecAuth = function() {
    	//alert("onload");
        $http({
            method: "GET",
            url: "/getExecAuth",
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


}
);