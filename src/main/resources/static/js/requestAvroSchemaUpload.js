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

	 $scope.getEnvs = function() {

        $http({
            method: "GET",
            url: "getEnvs",
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

    $scope.getTopicTeam = function(topicName) {

                if(topicName == null){
                    this.addAcl.topicname.focus();
                    alert("Please mention a topic name.");
                    return false;
                }

                $http({
                    method: "GET",
                    url: "getTopicTeam",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'env' : $scope.addSchema.envName.name,
                        'topicName' : topicName }
                }).success(function(output) {
                    $scope.topicteamname = output.team;
                    //alert($scope.topicDetails.teamname + "---");
                    if(!$scope.topicteamname){
                            alert("There is NO team found for this topic : " +  topicName);
                            $scope.addSchema.team="";
                            addSchema.topicname.focus();
                                return;
                    }
                    $scope.addSchema.team = $scope.topicteamname;
                    //alert("---"+$scope.topicDetails.teamname);
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );

            };

        $scope.getAllTopics = function() {

            $scope.alltopics = null;
                    $http({
                        method: "GET",
                        url: "getTopicsOnly?env="+$scope.addSchema.envName.name,
                        headers : { 'Content-Type' : 'application/json' }
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
                            $window.location.href = $window.location.origin + "/kafkawize/browseTopics";
                        }

        $scope.addSchema = function() {

            var serviceInput = {};
            $scope.alert = null;
             $scope.alertnote = null;

            serviceInput['environment'] = $scope.addSchema.envName.name;
            serviceInput['topicname'] = $scope.addSchema.topicname;
            serviceInput['teamname'] = $scope.addSchema.team;
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
                // $scope.showSuccessToast();
            }).error(
                function(error)
                {
                    $scope.alert = error;
                    //alert("Error : "+error.value);
                    $scope.alertnote = error;
                    // $scope.showAlertToast();
                }
            );

        };

        $scope.loadTeams = function() {
            $http({
                method: "GET",
                url: "getAllTeams",
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
                    $scope.userrole = output.userrole;
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