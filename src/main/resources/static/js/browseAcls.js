'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('browseAclsApp',[]);

app.controller("browseAclsCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	//$http.defaults.headers.common['Accept'] = 'application/json';
	$scope.envSelectedParam;

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

        $scope.getAllTopics = function() {

                            $scope.alltopics = null;
                                    $http({
                                        method: "GET",
                                        url: "getTopicsOnly?env="+$scope.getAcls.envName.name,
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

	// We add the "time" query parameter to prevent IE
	// from caching ajax results

	$scope.getAcls = function(pageNoSelected) {

        var serviceInput = {};

        var str = window.location.search;
        var envSelected, topicSelected;
        if(str){
            var topicNameIndex = str.indexOf("topicname");

            if(topicNameIndex > 0)
            {
                topicSelected = str.substring(topicNameIndex+10);
            }
        }

        if(!topicSelected)
            return;

		$scope.topicSelectedParam = topicSelected;

		$http({
			method: "GET",
			url: "getAcls",
            headers : { 'Content-Type' : 'application/json' },
            params: {'topicnamesearch' : topicSelected }
		}).success(function(output) {
			$scope.resultBrowse = output.aclInfoList;
			$scope.topicOverview = output.topicInfoList;
			if(output.aclInfoList != null){
                $scope.resultPages = output.aclInfoList[0].allPageNos;
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