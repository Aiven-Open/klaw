'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('requestAclsApp',[]);

app.controller("requestAclsCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	$http.defaults.headers.common['Accept'] = 'application/json';
	
    $scope.TopReqTypeList = [ { label: 'Producer', value: 'Producer' }, { label: 'Consumer', value: 'Consumer' }	];

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

        $scope.getTopicTeam = function(topicName) {

            if(topicName == null){
                this.addAcl.topicname.focus();
                alert("Please mention a topic name.");
                return false;
            }

            $http({
                method: "GET",
                url: "/getTopicTeam",
                headers : { 'Content-Type' : 'application/json' },
                params: {'env' : $scope.addAcl.envName.name,
                    'topicName' : topicName }
            }).success(function(output) {
                $scope.topicDetails = output;
                //alert($scope.topicDetails.teamname + "---");
                if(!$scope.topicDetails.teamname){
                        alert("There is NO team found for this topic : " +  topicName);
                        $scope.addAcl.team="";
                        addAcl.topicname.focus();
                            return;
                }
                $scope.addAcl.team = $scope.topicDetails.teamname;
                //alert("---"+$scope.topicDetails.teamname);
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );

        };

        $scope.addAcl = function() {

            var serviceInput = {};

            serviceInput['environment'] = $scope.addAcl.envName.name;
            serviceInput['topicname'] = $scope.addAcl.topicname;
            serviceInput['topictype'] = $scope.addAcl.topicreqtype.value;
            serviceInput['teamname'] = $scope.addAcl.team;
            serviceInput['appname'] = $scope.addAcl.app;
            serviceInput['remarks'] = $scope.addAcl.remarks;
            serviceInput['acl_ip'] = $scope.addAcl.acl_ip;
            serviceInput['acl_ssl'] = $scope.addAcl.acl_ssl;
            serviceInput['consumergroup'] = $scope.addAcl.consumergroup;

            if(!$scope.addAcl.team || !$scope.addAcl.topicname )
            {
                alert("Please enter a valid topic name to identify the team.");
                return false;
            }

            if (!window.confirm("Are you sure, you would like to create the acl : "
                +  $scope.addAcl.topicname +
                "\nEnv : " + $scope.addAcl.envName.name +
                "\nTeam :" + $scope.addAcl.team +
                "\nApp :" + $scope.addAcl.app +
                "\nAcl :" + $scope.addAcl.acl_ip + "  " + $scope.addAcl.acl_ssl
            )) {
                return;
            }

            $http({
                method: "POST",
                url: "/createAcl",
                headers : { 'Content-Type' : 'application/json' },
                params: {'addAclRequest' : serviceInput },
                data: {'addAclRequest' : serviceInput}
            }).success(function(output) {
                $scope.alert = "Acl Request : "+output.result;
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



}
);