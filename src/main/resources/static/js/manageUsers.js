'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('manageUsersApp',[]);

app.controller("manageUsersCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	$http.defaults.headers.common['Accept'] = 'application/json';

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

	$scope.rolelist = [ { label: 'USER', value: 'USER' }, { label: 'ADMIN', value: 'ADMIN' }	];

        $scope.loadTeams = function() {
            $http({
                method: "GET",
                url: "getAllTeams",
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

        $scope.loadTeamsSU = function() {
                    $http({
                        method: "GET",
                        url: "getAllTeamsSU",
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

    $scope.getMyProfile = function(){
        $http({
                method: "GET",
                url: "getMyProfileInfo",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.myProfInfo = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
    }

        $scope.chPwd = function() {

            var serviceInput = {};

            serviceInput['pwd'] = $scope.chPwd.pwd;
            serviceInput['repeatpwd'] = $scope.chPwd.repeatpwd;

            if(!$scope.chPwd.pwd || ($scope.chPwd.pwd!=$scope.chPwd.repeatpwd))
            {
                $scope.alertnote = "Passwords are not equal.";
                $scope.showAlertToast();
                return;
            }

            if (!window.confirm("Are you sure, you would like to Change password?")) {
                return;
            }

            $http({
                method: "POST",
                url: "chPwd",
                headers : { 'Content-Type' : 'application/json' },
                params: {'changePwd' : serviceInput },
                data: {'changePwd' : serviceInput}
            }).success(function(output) {
                $scope.alert = "Password changed : "+output.result;
                $scope.showSuccessToast();
            }).error(
                function(error)
                {
                    $scope.alert = error;

                    $scope.alertnote = error;
                    $scope.showAlertToast();
                }
            );

        };

        $scope.deleteTeam = function(){

        if (!window.confirm("Are you sure, you would like to delete the team : "
                                +  $scope.deleteTeam.idval
                                )) {
                                return;
                            }

            $http({
                    method: "POST",
                    url: "deleteTeamRequest",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'teamId' : $scope.deleteTeam.idval },
                    data: {'teamId' : $scope.deleteTeam.idval}
                }).success(function(output) {

                    $scope.alert = "Delete Team Request : "+output.result;
                    $scope.showSuccessToast();
                    $scope.loadTeamsSU();
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                        $scope.alertnote = error;
                        $scope.showAlertToast();
                    }
                );
            }

    $scope.deleteUser = function(){

        if (!window.confirm("Are you sure, you would like to delete the user : "
                                +  $scope.deleteUser.idval
                                )) {
                                return;
                            }

        $http({
                method: "POST",
                url: "deleteUserRequest",
                headers : { 'Content-Type' : 'application/json' },
                params: {'userId' : $scope.deleteUser.idval },
                data: {'userId' : $scope.deleteUser.idval}
            }).success(function(output) {

                $scope.alert = "Delete User Request : "+output.result;
                $scope.showSuccessToast();
                $scope.showUsers();
            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.alertnote = error;
                    $scope.showAlertToast();
                }
            );
    }

	$scope.addNewUser = function() {

            var serviceInput = {};

            if(!$scope.addNewUser.pwd || ($scope.addNewUser.pwd!=$scope.addNewUser.reppwd))
            {
                $scope.alertnote = "Passwords are not equal.";
                $scope.showAlertToast();
                return;
            }
            serviceInput['username'] = $scope.addNewUser.username;
            serviceInput['fullname'] = $scope.addNewUser.fullname;
            serviceInput['pwd'] = $scope.addNewUser.pwd;
            serviceInput['team'] = $scope.addNewUser.team.teamname;
            serviceInput['role'] = $scope.addNewUser.role.value;

            if (!window.confirm("Are you sure, you would like to add user : "
                +  $scope.addNewUser.username + ": " +
                "\nTeam : " + $scope.addNewUser.team.teamname +
                "\nRole : " + $scope.addNewUser.role.value
                )) {
                return;
            }

            $http({
                method: "POST",
                url: "addNewUser",
                headers : { 'Content-Type' : 'application/json' },
                params: {'addNewUser' : serviceInput },
                data: serviceInput
            }).success(function(output) {
                $scope.alert = "New User Request : "+output.result;
                $scope.showSuccessToast();
            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.alertnote = error;
                    $scope.showAlertToast();
                }
            );

        };

        $scope.cancelRequest = function() {
                    $window.location.href = $window.location.origin + "/kafkawize/showTeams";
                }

        $scope.cancelUserRequest = function() {
                            $window.location.href = $window.location.origin + "/kafkawize/showUsers";
                        }

        $scope.addNewTeam = function() {

                    var serviceInput = {};

                    serviceInput['teamname'] = $scope.addNewTeam.teamname;
                    serviceInput['teammail'] = $scope.addNewTeam.teammail;
                    serviceInput['teamphone'] = $scope.addNewTeam.teamphone;
                    serviceInput['contactperson'] = $scope.addNewTeam.contactperson;
                    serviceInput['app'] = "";


                    if (!window.confirm("Are you sure, you would like to add team : "
                        +  $scope.addNewTeam.teamname + ": " +
                        "\nTeammail : " + $scope.addNewTeam.teammail +
                        "\nPhone : " + $scope.addNewTeam.teamphone
                        )) {
                        return;
                    }

                    $http({
                        method: "POST",
                        url: "addNewTeam",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'addNewTeam' : serviceInput },
                        data: serviceInput
                    }).success(function(output) {
                        $scope.alert = "New User Team : "+output.result;
                        $scope.showSuccessToast();
                    }).error(
                        function(error)
                        {
                            $scope.alert = error;
                            $scope.alertnote = error;
                            $scope.showAlertToast();
                        }
                    );

                };

        $scope.showUsers = function() {
            $http({
                method: "GET",
                url: "showUserList",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.userList = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

    $scope.getExecAuth = function() {
    	//alert("onload");
        $http({
            method: "GET",
            url: "getExecAuth",
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


}
);