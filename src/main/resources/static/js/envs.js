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

        $scope.cancelRequest = function() {
            $window.location.href = $window.location.origin + "/kafkawize/envs";
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
        };

        $scope.deleteEnv = function(idval) {

        if (!window.confirm("Are you sure, you would like to delete the cluster : "
                        +  idval
                        )) {
                        return;
                    }

            $http({
                            method: "POST",
                            url: "deleteClusterRequest",
                            headers : { 'Content-Type' : 'application/json' },
                            params: {'clusterId' : idval },
                            data: {'clusterId' : idval}
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
                    $scope.alertnote = "Default partitions should not be empty and should be greater than 0";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addNewEnv.defmaxparts.length<=0 || $scope.addNewEnv.defmaxparts<=0)
                {
                    $scope.alertnote = "Maximum partitions should not be empty and should be greater than 0";
                    $scope.showAlertToast();
                    return;
                }
                if($scope.addNewEnv.defrepfctr.length<=0 || $scope.addNewEnv.defrepfctr<=0)
                {
                    $scope.alertnote = "Default replication factor should not be empty and should be greater than 0";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addNewEnv.type == undefined)
                    {
                        $scope.alertnote = "Please select the cluster type";
                        $scope.showAlertToast();
                        return;
                    }

                if($scope.addNewEnv.host == undefined)
                    {
                        $scope.alertnote = "Please fill in host";
                        $scope.showAlertToast();
                        return;
                    }

                if($scope.addNewEnv.envname == undefined)
                {
                    $scope.alertnote = "Please fill in a name for cluster";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addNewEnv.envname.length > 3)
                {
                    $scope.alertnote = "Cluster name cannot be more than 3 characters.";
                    $scope.showAlertToast();
                    return;
                }

                var serviceInput = {};

                serviceInput['name'] = $scope.addNewEnv.envname;
                serviceInput['host'] = $scope.addNewEnv.host;
                serviceInput['port'] = $scope.addNewEnv.port;
                serviceInput['protocol'] = $scope.addNewEnv.protocol;
                serviceInput['type'] = $scope.addNewEnv.type;

                serviceInput['otherParams'] = "default.partitions=" + $scope.addNewEnv.defparts
                 + ",max.partitions=" + $scope.addNewEnv.defmaxparts + ",replication.factor=" + $scope.addNewEnv.defrepfctr;

                $http({
                    method: "POST",
                    url: "addNewEnv",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'addNewEnv' : serviceInput },
                    data: serviceInput
                }).success(function(output) {
                    $scope.alert = "New cluster added: "+output.result;
                    $scope.showSuccessToast();
                }).error(
                    function(error)
                    {
                     $scope.alert = error;
                     $scope.alertnote = error;
                     $scope.showAlertToast();
                    }
                );

            };

        $scope.getSchemaRegEnvs = function() {

            $http({
                method: "GET",
                url: "getSchemaRegEnvs",
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