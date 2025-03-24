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

        $scope.generateToken = function() {
                    if(!$scope.forgotPwdUsername)
                        {
                            $scope.alertnote = "Please enter your username.";
                            $scope.showAlertToast();
                            return;
                        }

                    $http({
                            method: "POST",
                            url: "reset/token",
                            headers : { 'Content-Type' : 'application/json' },
                            params: {'username' : $scope.forgotPwdUsername },
                            data: {'username' : $scope.forgotPwdUsername}
                        }).success(function(output) {
                            $scope.userFound = output.userFound;
                            if($scope.userFound == 'false')
                                $scope.alert = 'User not found !';
                            else if($scope.userFound == 'true' && output.tokenSent == 'false'){
                                $scope.alert = 'An issue occurred while resetting password. Please contact Admin !';
                            }
                            else {
                                $scope.alert = 'A password reset token has been sent to your account to reset your password.';
                                $scope.tokenSent = 'true';
                                }
                        }).error(
                            function(error)
                            {
                                $scope.alert = 'User not found !'
                            }
                        );
                }

                $scope.resetPassword = function() {
                            if(!$scope.forgotPwdUsername || !$scope.password || !$scope.confirmationPassword || !$scope.resetToken)
                                {
                                    $scope.alertnote = "Please ensure you have filled out your username, password, confirmation password and reset token.";
                                    $scope.showAlertToast();
                                    return;
                                } else if($scope.password !== $scope.confirmationPassword )
{
                                    $scope.alertnote = "Password and confirmation password must match!";
                                    $scope.showAlertToast();
                                    return;
                                }
                            $http({
                                    method: "POST",
                                    url: "reset/password",
                                    headers : { 'Content-Type' : 'application/json' },
                                    params: {'username' : $scope.forgotPwdUsername,
                                              'password' : $scope.password,
                                              'token' : $scope.resetToken},
                                    data: {'username' : $scope.forgotPwdUsername}
                                }).success(function(output) {
                                console.log(" What is the output? : " + output);
                                    $scope.userFound = output.userFound;
                                    if($scope.userFound == 'false') {
                                        $scope.alert = 'User not found !';
                                    } else if($scope.userFound == 'true' && output.tokenSent == 'false'){
                                        $scope.alert = 'An issue occurred while resetting password. Please contact Admin !';
                                    } else {

                                        $scope.alert = 'Your password has been reset.';
                                        $scope.tokenSent = 'true';
                                       $scope.delay(1000).then(() => $window.location.href = $window.location.origin + "/login");

                                        }
                                }).error(
                                    function(error)
                                    {
                                        if(error != null && error.detail != null && error.detail.includes("Validation failure")){
                                            $scope.alert = error.detail + ": Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.";
                                            $scope.alertnote = $scope.alert;
                                            $scope.showAlertToast();
                                        } else {
                                            $scope.alert = 'Unable to update your password. Please check your token has not expired and your user name is spelt correctly.'
                                            $scope.alertnote = error;
                                            $scope.showAlertToast();
                                        }
                                    }
                                );
                        }

                $scope.delay = function(time) {
                  return new Promise(resolve => setTimeout(resolve, time));
                }
}
);