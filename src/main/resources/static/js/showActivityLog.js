'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('showActivityLogApp',[]);

app.controller("showActivityLogCtrl", function($scope, $http, $location, $window) {
	
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

	// We add the "time" query parameter to prevent IE
	// from caching ajax results

	$scope.getActivityLog = function(pageNoSelected) {

        var serviceInput = {};

        if(!$scope.getActivityLog.envName)
             return;
		
		//serviceInput['clusterType'] = $scope.getTopics.clusterType.value;
		serviceInput['env'] = $scope.getActivityLog.envName.name;
		//alert("---"+$scope.getTopics.envName.value);
//		if (!window.confirm("Are you sure, you would like to view the activity Log in Environment : " +
//				$scope.getActivityLog.envName.name + " ?")) {
//			return;
//		}
		
		$http({
			method: "GET",
			url: "activityLog",
            headers : { 'Content-Type' : 'application/json' },
            params: {'env' : $scope.getActivityLog.envName.name,
                'pageNo' : pageNoSelected }
		}).success(function(output) {
			$scope.resultBrowse = output;
			if(output!=null && output.length>0){
                $scope.resultPages = output[0].allPageNos;
                $scope.resultPageSelected = pageNoSelected;
            }
		}).error(
			function(error) 
			{
				$scope.alert = error;
				$scope.resultPages = null;
                $scope.resultPageSelected = null;
			}
		);
		
	};


}
);