'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('sidebarApp',[]);

app.controller("sidebarCtrl", function($scope, $http, $location, $window) {
	


    $scope.getAuth = function() {
        alert();
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

	// We add the "time" query parameter to prevent IE
	// from caching ajax results

	$scope.getTopics = function(pageNoSelected) {

        var serviceInput = {};
		
		//serviceInput['clusterType'] = $scope.getTopics.clusterType.value;
		serviceInput['env'] = $scope.getTopics.envName.name;
		//alert("---"+$scope.getTopics.envName.value);
		if (!window.confirm("Are you sure, you would like to view the topics in Environment : " +
				$scope.getTopics.envName.name + " ?")) {
			return;
		}
		
		$http({
			method: "GET",
			url: "/getTopics",
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


}
);