'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('indexApp',[]);

app.controller("indexCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	//$http.defaults.headers.common['Accept'] = 'application/json';
	
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
}
);