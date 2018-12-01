'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('synchronizeTopicsApp',[]);

app.controller("synchronizeTopicsCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	//$http.defaults.headers.common['Accept'] = 'application/json';
	

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

        $scope.updatedSyncStr="";
        $scope.getDetails = function(teamselected,topic) {
            $scope.updatedSyncStr  = $scope.updatedSyncStr + topic + "-----" + teamselected+"\n";
        }

        $scope.synchTopics = function() {

            var serviceInput = {};

            if (!window.confirm("Are you sure, you would like to Synchronize this info ? "+$scope.getTopics.envName.name)) {
                $scope.updatedSyncStr="";
                return;
            }

            $http({
                method: "POST",
                url: "/updateSyncTopics",
                headers : { 'Content-Type' : 'application/json' },
                params: {'updatedSyncTopics' : $scope.updatedSyncStr , 'envSelected': $scope.getTopics.envName.name},
                data: {'updatedSyncTopics' : $scope.updatedSyncStr}
            }).success(function(output) {
                $scope.alert = "Topic Sync Request : "+output.result;
                $scope.updatedSyncStr="";
            }).error(
                function(error)
                {
                    $scope.alert = error;
                   // alert("Error : "+error.value);
                }
            );

        };

	// We add the "time" query parameter to prevent IE
	// from caching ajax results

	$scope.getTopics = function(pageNoSelected) {

        var serviceInput = {};
		
		//serviceInput['clusterType'] = $scope.getTopics.clusterType.value;
		serviceInput['env'] = $scope.getTopics.envName.name;
		//alert("---"+$scope.getTopics.envName.value);

		$http({
			method: "GET",
			url: "/getSyncTopics",
            headers : { 'Content-Type' : 'application/json' },
            params: {'env' : $scope.getTopics.envName.name,
                'pageNo' : pageNoSelected }
		}).success(function(output) {
			$scope.resultBrowse = output;
			if(output!=null){
                $scope.resultPages = output[0].allPageNos;
                $scope.resultPageSelected = pageNoSelected;
            }
		}).error(
			function(error) 
			{
				$scope.alert = error;
			}
		);
		
	};

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