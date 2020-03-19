'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('requestTopicsApp',[]);

app.controller("requestTopicsCtrl", function($scope, $http, $location, $window) {
	
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

        $scope.addTopic = function() {

            var serviceInput = {};

            $scope.alert = null;
            $scope.alertnote = null;

            if($scope.addTopic.topicpartitions ==null && $scope.addTopic.topicpartitions.length<0){
                //alert("Please fill in topic partitions");
                $scope.alertnote = "Please fill in topic partitions.";
                $scope.showAlertToast();
                return;
            }

            if(isNaN($scope.addTopic.topicpartitions)){
                //alert("Please fill in a valid number for partitions for topic");
                $scope.alertnote = "Please fill in a valid number for partitions for topic.";
                $scope.showAlertToast();
                return;
            }

             if(!$scope.addTopic.team)
              {
                 //alert("Please select your team.");
                 $scope.alertnote = "Please select your team.";
                 $scope.showAlertToast();
                 return;
              }

            serviceInput['environment'] = $scope.addTopic.envName.name;
            serviceInput['topicname'] = $scope.addTopic.topicname;
            serviceInput['topicpartitions'] = $scope.addTopic.topicpartitions;
            serviceInput['teamname'] = $scope.addTopic.team.teamname;
            serviceInput['appname'] = "App";//$scope.addTopic.app;
            serviceInput['remarks'] = $scope.addTopic.remarks;

            $http({
                method: "POST",
                url: "createTopics",
                headers : { 'Content-Type' : 'application/json' },
                params: {'addTopicRequest' : serviceInput },
                data: serviceInput
            }).success(function(output) {
                $scope.alert = "Topic Request : "+output.result;
                $scope.showSuccessToast();
               // $window.location.href = $window.location.origin + "/kafkawize/browseTopics";
            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.alertnote = error;
                   // alert("Error : "+error.value);
                    $scope.showAlertToast();
                }
            );

        };

        $scope.cancelRequest = function() {
            $window.location.href = $window.location.origin + "/kafkawize/browseTopics";
        }

        $scope.getEnvs = function() {

                $http({
                    method: "GET",
                    url: "getEnvs",
                    headers : { 'Content-Type' : 'application/json' }
                }).success(function(output) {
                    $scope.allenvs = output;
                  //  alert(allenvs);
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );
            }

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
                    //alert("Error : "+error.value);
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
                   $scope.statusauthexectopics_su = output.statusauthexectopics_su;
                   $scope.alerttop = output.alertmessage;
                   if(output.companyinfo == null){
                       $scope.companyinfo = "Company not defined!!";
                   }
                   else{
                       $scope.companyinfo = output.companyinfo;
                    }
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





}
);