'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('browseTopicsApp',[]);

app.controller("browseTopicsCtrl", function($scope, $http, $location, $window) {
	
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
                url: "getEnvsOnly",
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

        $scope.loadTeams = function() {
                $http({
                    method: "GET",
                    url: "getAllTeamsSUOnly",
                    headers : { 'Content-Type' : 'application/json' }
                }).success(function(output) {
                    $scope.allTeams = output;
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

	$scope.getTopics = function(pageNoSelected, fromSelect) {

        var serviceInput = {};
        var envSelected;
        $scope.resultBrowse = null;
        $scope.resultPages = null;
        $scope.alert = null;
        $scope.resultPageSelected = null;
        var teamSel = $scope.getTopics.team;
        var str = window.location.search;
        if(fromSelect == "false")
        {
            var envSelected;
            if(str && str.length>10){
                var envSelectedIndex = str.indexOf("envSelected");
                if(envSelectedIndex > 0)
                {
                    envSelected = str.substring(13);
                    if(envSelected && envSelected.length>0) {
                        serviceInput['env'] = envSelected;
                        $scope.envSelected = envSelected;
                        $scope.getTopics.envName = envSelected;
                    }else return;
                }
            }else return;
        }else if(fromSelect == "true"){
                 if(!$scope.getTopics.envName)
                        envSelected = "ALL";
                 else
                    envSelected = $scope.getTopics.envName;

                serviceInput['env'] = envSelected;
                $scope.envSelected = envSelected;
        }else{
                envSelected = "ALL";
                var teamFromSearchParams = str.indexOf("team=");
                if(teamFromSearchParams > 0){
                    teamSel = str.substring(6);
                    window.history.pushState({}, document.title, "browseTopics");
                    $scope.getTopics.team = teamSel;
                }
        }

		var topicFilter = $scope.getTopics.topicnamesearch;
		if(topicFilter && topicFilter.length>0 && topicFilter.length<3){
		    alert("Please enter atleast 3 characters of the topic name.");
		    return;
		    }

		$http({
			method: "GET",
			url: "getTopics",
            headers : { 'Content-Type' : 'application/json' },
            params: {'env' : envSelected,
                'pageNo' : pageNoSelected,
                 'topicnamesearch' : $scope.getTopics.topicnamesearch,
                 'teamName' : teamSel
                 }
		}).success(function(output) {
			$scope.resultBrowse = output;
			if(output!=null && output.length !=0){
                $scope.resultPages = output[0][0].allPageNos;
                $scope.resultPageSelected = pageNoSelected;
            }else{
                $scope.resultPages = null;
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