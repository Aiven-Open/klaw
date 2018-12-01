'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('createTopicApp',[]);

app.controller("createTopicCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	$http.defaults.headers.common['Accept'] = 'application/json';
	

	$scope.partitions = [ { label: '1', value: '1' }, { label: '2', value: '2' }, 
		{ label: '3', value: '3' }, { label: '4', value: '4' }, { label: '5', value: '5' }, { label: '6', value: '6' }, 
		{ label: '7', value: '7' }, { label: '8', value: '8' }	];
	
	$scope.replications = [ { label: '1', value: '1' }, { label: '2', value: '2' }, 
		{ label: '3', value: '3' }, { label: '4', value: '4' }	];
	
	// We add the "time" query parameter to prevent IE
	// from caching ajax results

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
		
	$scope.addTopic = function() {

		var serviceInput = {};
		
		serviceInput['clusterType'] = $scope.addTopic.clusterType.value;
		serviceInput['environment'] = $scope.addTopic.envName.name;
		serviceInput['topicName'] = $scope.addTopic.topicName;
		serviceInput['partitions'] = $scope.addTopic.partitions.value;
		serviceInput['replications'] = $scope.addTopic.replications.value;
		
		if (!window.confirm("Are you sure, you would like to create the topic : "+ $scope.addTopic.topicName + "\nCluster : " +
				$scope.addTopic.clusterType.label + "\nEnv : " + $scope.addTopic.envName.name + "\nParitions : " + $scope.addTopic.partitions.value +
				"\nReplications : "+ $scope.addTopic.replications.value + " ?")) {
			return;
		}
		
		$http({
			method: "POST",
			url: "/eventbusweb/createtopic",
			headers : { 'Content-Type' : 'application/json' },
            params: {'addTopicRequest' : serviceInput },
            data: {'addTopicRequest' : serviceInput}
		}).success(function(output) {
			$scope.resultAdd = output;
			$scope.alert = "Topic addition : "+output.result;
		}).error(
			function(error) 
			{
				$scope.alert = error;
				alert("Error : "+error);
			}
		);
		
	};

}
);