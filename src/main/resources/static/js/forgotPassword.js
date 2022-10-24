'use strict'

// confirmation of delete
// edit
// solution for transaction
// message store / key / gui
var app = angular.module('forgotPwdApp',[]);

app.controller("forgotPwdCtrl", function($scope, $http, $location, $window) {

	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed.
	$http.defaults.headers.common['Accept'] = 'application/json';

    	$scope.showSubmitFailed = function(title, text){
		swal({
			 title: "",
			 text: "Request unsuccessful !!",
			 timer: 2000,
			 showConfirmButton: false
			 });
	}

	$scope.showAlertToast = function() {
          var x = document.getElementById("alertbar");
          x.className = "show";
          setTimeout(function(){ x.className = x.className.replace("show", ""); }, 2000);
        }

        $scope.getDbInfo = function() {
            $http({
                    method: "GET",
                    url: "getDbAuth",
                    headers : { 'Content-Type' : 'application/json' }
                }).success(function(output) {
                    $scope.dbauth = output.dbauth;

                    if($scope.dbauth == 'false')
                        $scope.alert = 'Please contact Admin or update your ActiveDirectory/Ldap password.';
                }).error(
                    function(error)
                    {
                        $scope.alert = 'Please contact your Administrator.';
                        $scope.dbauth = 'true';
                    }
                );
        }

        $scope.resetPassword = function() {
            if(!$scope.forgotPwdUsername)
                {
                    $scope.alertnote = "Please enter your username.";
                    $scope.showAlertToast();
                    return;
                }

            $http({
                    method: "POST",
                    url: "resetPassword",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'username' : $scope.forgotPwdUsername },
                    data: {'username' : $scope.forgotPwdUsername}
                }).success(function(output) {
                    $scope.userFound = output.userFound;
                    if($scope.userFound == 'false')
                        $scope.alert = 'User not found !';
                    else if($scope.userFound == 'true' && output.passwordSent == 'false'){
                        $scope.alert = 'An issue occurred while resetting password. Please contact Admin !';
                    }
                    else
                        $scope.alert = 'Dear User, an email is sent to your configured email id with a new Password !';
                }).error(
                    function(error)
                    {
                        $scope.alert = 'User not found !'
                    }
                );
        }

}
);
