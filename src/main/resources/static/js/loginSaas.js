'use strict'

// confirmation of delete
// edit
// solution for transaction
// message store / key / gui
var app = angular.module('loginSaasApp',[]);

app.controller("loginSaasCtrl", function($scope, $http, $location, $window) {

	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed.
	$http.defaults.headers.common['Accept'] = 'application/json';

        $scope.getBasicInfo = function() {
            $http({
                       method: "GET",
                       url: "getBasicInfo",
                       headers : { 'Content-Type' : 'application/json' }
                   }).success(function(output) {
                       $scope.dashboardDetails = output;
                   }).error(
                       function(error)
                       {
                           $scope.alert = error;
                       }
                   );
        }

	    $scope.getTenantsInfo = function() {
                $http({
                    method: "GET",
                    url: "getTenantsInfo",
                    headers : { 'Content-Type' : 'application/json' }
                }).success(function(output) {
                    $scope.tenantsInfo = output;
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );
            }

            $scope.onSubmitCaptcha = function() {
                 document.getElementById("demo-form").submit();
               }


            $scope.refreshPage = function(){
                        $window.location.reload();
                    }
    }
);
