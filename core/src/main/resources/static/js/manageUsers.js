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

    	$scope.showSubmitFailed = function(title, text){
		swal({
			 title: "",
			 text: "Request unsuccessful !!",
			 timer: 2000,
			 showConfirmButton: false
			 });
	     }

	     $scope.handleValidationErrors = function(error){
	        if(error.errors != null && error.errors.length > 0){
                    $scope.alert = error.errors[0].defaultMessage;
                }else if(error.message != null){
                    $scope.alert = error.message;
                }else if(error.result != null){
                    $scope.alert = error.result;
                }
                else
                    $scope.alert = "Unable to process the request. Please verify the request or contact our Administrator !!";

                $scope.alertnote = $scope.alert;
                $scope.showAlertToast();
	     }

	$scope.showAlertToast = function() {
                  var x = document.getElementById("alertbar");
                  x.className = "show";
                  setTimeout(function(){ x.className = x.className.replace("show", ""); }, 2000);
                }

	$scope.getRoles = function() {
            $http({
                method: "GET",
                url: "getRoles",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.rolelist = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }


        $scope.getTenants = function() {
                $http({
                    method: "GET",
                          url: "getTenants",
                          headers : { 'Content-Type' : 'application/json' }
                      }).success(function(output) {
                          $scope.allTenants = output;
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

            swal({
            		title: "Are you sure?",
            		text: "You would like to Change password?",
            		type: "warning",
            		showCancelButton: true,
            		confirmButtonColor: "#DD6B55",
            		confirmButtonText: "Yes, update it!",
            		cancelButtonText: "No, cancel please!",
            		closeOnConfirm: true,
            		closeOnCancel: true
            	}).then(function(isConfirm){
            		if (isConfirm.dismiss != "cancel") {
            			$http({
                            method: "POST",
                            url: "chPwd",
                            headers : { 'Content-Type' : 'application/json' },
                            params: {'changePwd' : serviceInput },
                            data: {'changePwd' : serviceInput}
                        }).success(function(output) {
                            $scope.alert = "Password changed : "+output.message;
                            if(output.success){
                                swal({
                                     title: "",
                                     text: "Password changed : "+output.message,
                                     timer: 2000,
                                     showConfirmButton: false
                                 });
                             }else $scope.showSubmitFailed('','');
                        }).error(
                            function(error)
                            {
                                $scope.handleValidationErrors(error);
                            }
                        );
            		} else {
            			return;
            		}
            	});

        };

        $scope.deleteTeam = function(idval, teamname){

        swal({
        		title: "Are you sure?",
        		text: "You would like to delete the team " + teamname + "? Note : Associated users are not deleted.",
        		type: "warning",
        		showCancelButton: true,
        		confirmButtonColor: "#DD6B55",
        		confirmButtonText: "Yes, delete it!",
        		cancelButtonText: "No, cancel please!",
        		closeOnConfirm: true,
        		closeOnCancel: true
        	}).then(function(isConfirm){
        		if (isConfirm.dismiss != "cancel") {
        			$http({
                            method: "POST",
                            url: "deleteTeamRequest",
                            headers : { 'Content-Type' : 'application/json' },
                            params: {'teamId' : idval },
                            data: {'teamId' : idval}
                        }).success(function(output) {

                            $scope.alert = "Delete Team Request : "+output.message;
                            if(output.success){
                                swal({
                                     title: "",
                                     text: "Delete Team Request : "+output.message,
                                     timer: 2000,
                                     showConfirmButton: false
                                 });
                             }else $scope.showSubmitFailed('','');
                            $scope.loadTeamsSU();
                        }).error(
                            function(error)
                            {
                                $scope.handleValidationErrors(error);
                            }
                        );
        		} else {
        			return;
        		}
        	});

            }

    $scope.deleteUser = function(idval){

        swal({
        		title: "Are you sure?",
        		text: "You would like to delete the user " + idval + "?",
        		type: "warning",
        		showCancelButton: true,
        		confirmButtonColor: "#DD6B55",
        		confirmButtonText: "Yes, delete it!",
        		cancelButtonText: "No, cancel please!",
        		closeOnConfirm: true,
        		closeOnCancel: true
        	}).then(function(isConfirm){
        		if (isConfirm.dismiss != "cancel") {
        			$http({
                        method: "POST",
                        url: "deleteUserRequest",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'userId' : idval },
                        data: {'userId' : idval}
                    }).success(function(output) {
                        $scope.alert = "Delete User Request : "+output.message;
                        if(output.success){
                            swal({
                                 title: "",
                                 text: "Delete User Request : "+output.message,
                                 timer: 2000,
                                 showConfirmButton: false
                             });
                         }else $scope.showSubmitFailed('','');
                        $scope.showUsers(1);
                    }).error(
                        function(error)
                        {
                            $scope.handleValidationErrors(error);
                        }
                    );
        		} else {
        			return;
        		}
        	});
    }

    $scope.updateProfile = function() {
        var serviceInput = {};

        if(!$scope.myProfInfo.mailid)
        {
            $scope.alertnote = "Email id is mandatory.";
            $scope.showAlertToast();
            return;
        }

        if(!$scope.myProfInfo.fullname)
        {
            $scope.alertnote = "Full name id is mandatory.";
            $scope.showAlertToast();
            return;
        }

        serviceInput['username'] = $scope.myProfInfo.username;
        serviceInput['fullname'] = $scope.myProfInfo.fullname;
        serviceInput['mailid'] = $scope.myProfInfo.mailid;

        $http({
                method: "POST",
                url: "updateProfile",
                headers : { 'Content-Type' : 'application/json' },
                params: {'updateProfile' : serviceInput },
                data: serviceInput
            }).success(function(output) {
                $scope.alert = "Update User Request : "+output.message;
                if(output.success){
                        swal({
                             title: "",
                             text: "Update User Request : "+output.message,
                             timer: 2000,
                             showConfirmButton: false
                         });
                     }else $scope.showSubmitFailed('','');
            }).error(
                function(error)
                {
                    $scope.handleValidationErrors(error);
                }
            );

    }

	$scope.addNewUser = function() {

            var serviceInput = {};

            if(!$scope.addNewUser.username && $scope.addNewUser.username.indexOf(" "))
            {
                $scope.alertnote = "Please enter a valid username with no spaces.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addNewUser.username.length < 6)
            {
                $scope.alertnote = "Username should be atleast 6 characters.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.addNewUser.fullname)
            {
                $scope.alertnote = "Please enter Full Name.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addNewUser.fullname.length < 6)
            {
                $scope.alertnote = "Please enter Full Name atleast 6 characters.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.addNewUser.pwd)
            {
                $scope.alertnote = "Please enter a password.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addNewUser.pwd.length < 8)
            {
                $scope.alertnote = "Password should be atleast 8 characters.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addNewUser.pwd !== $scope.addNewUser.reppwd)
            {
                $scope.alertnote = "Passwords are not equal.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.addNewUser.emailid)
            {
                $scope.alertnote = "Please enter a valid email id.";
                $scope.showAlertToast();
                return;
            }

            else if($scope.addNewUser.emailid.length < 7)
            {
                $scope.alertnote = "Please enter a valid email id.";
                $scope.showAlertToast();
                return;
            }
            else if(!$scope.addNewUser.emailid.includes("@"))
            {
                $scope.alertnote = "Please enter a valid email id.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.addNewUser.team)
            {
                $scope.alertnote = "Please select a team.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.addNewUser.role)
            {
                $scope.alertnote = "Please select a role.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addNewUser.canswitchteams){
                $scope.getUpdatedListOfSwitchTeams();
                if($scope.updatedTeamsSwitchList.length < 2){
                    $scope.alertnote = "Please select atleast 2 teams, to switch between.";
                    $scope.showAlertToast();
                    return;
                }
                if(!$scope.updatedTeamsSwitchList.includes($scope.addNewUser.team.teamId)){
                    $scope.alertnote = "Please select your own team, in the switch teams list.";
                    $scope.showAlertToast();
                    return;
                }
            }

            serviceInput['username'] = $scope.addNewUser.username;
            serviceInput['fullname'] = $scope.addNewUser.fullname;
            serviceInput['userPassword'] = $scope.addNewUser.pwd;
            serviceInput['team'] = $scope.addNewUser.team.teamname;
            serviceInput['role'] = $scope.addNewUser.role;
            serviceInput['mailid'] = $scope.addNewUser.emailid;
            serviceInput['switchTeams'] = $scope.addNewUser.canswitchteams;
            serviceInput['switchAllowedTeamIds'] = $scope.updatedTeamsSwitchList;

            $scope.addUserHttpCall(serviceInput);
        };

        $scope.addNewSaasUser = function() {

            var serviceInput = {};

            if(!$scope.addNewUser.username)
            {
                $scope.alertnote = "Please enter a valid email id.";
                $scope.showAlertToast();
                return;
            }

            else if($scope.addNewUser.username.length < 7)
            {
                $scope.alertnote = "Please enter a valid email id.";
                $scope.showAlertToast();
                return;
            }
            else if(!$scope.addNewUser.username.includes("@"))
            {
                $scope.alertnote = "Please enter a valid email id.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.addNewUser.fullname)
            {
                $scope.alertnote = "Please enter Full Name.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addNewUser.fullname.length < 6)
            {
                $scope.alertnote = "Please enter Full Name atleast 6 characters.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.addNewUser.pwd)
            {
                $scope.alertnote = "Please enter a password.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addNewUser.pwd.length < 8)
            {
                $scope.alertnote = "Password should be atleast 8 characters.";
                $scope.showAlertToast();
                return;
            }

            if($scope.addNewUser.pwd != $scope.addNewUser.reppwd)
            {
                $scope.alertnote = "Passwords are not equal.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.addNewUser.team)
            {
                $scope.alertnote = "Please select a team.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.addNewUser.role)
            {
                $scope.alertnote = "Please select a role.";
                $scope.showAlertToast();
                return;
            }

            serviceInput['username'] = $scope.addNewUser.username;
            serviceInput['fullname'] = $scope.addNewUser.fullname;
            serviceInput['userPassword'] = $scope.addNewUser.pwd;
            serviceInput['team'] = $scope.addNewUser.team.teamname;
            serviceInput['role'] = $scope.addNewUser.role;
            serviceInput['mailid'] = $scope.addNewUser.username;

            $scope.addUserHttpCall(serviceInput);
        };

        $scope.addUserHttpCall = function(serviceInput){
            $http({
                    method: "POST",
                    url: "addNewUser",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'addNewUser' : serviceInput },
                    data: serviceInput
                }).success(function(output) {
                    $scope.alert = "New User Request : "+output.message;
                    $scope.addNewUser.username = "";
                    $scope.addNewUser.fullname = "";
                    $scope.addNewUser.pwd = "";
                    $scope.addNewUser.reppwd = "";
                    $scope.addNewUser.emailid = "";
                    if(output.success){
                            swal({
                                 title: "",
                                 text: "New User Request : "+output.message,
                                 timer: 2000,
                                 showConfirmButton: true
                             }).then(function(isConfirm){
                                   $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/users";
                             });
                         }else $scope.showSubmitFailed('','');
                }).error(
                    function(error)
                    {
                        $scope.handleValidationErrors(error);
                    }
                );
        }

        $scope.addNewUserLdap = function() {

                    var serviceInput = {};

                    if(!$scope.addNewUser.emailid)
                    {
                        $scope.alertnote = "Please enter a valid email id.";
                        $scope.showAlertToast();
                        return;
                    }
                    else if($scope.addNewUser.emailid.length < 7)
                    {
                        $scope.alertnote = "Please enter a valid email id.";
                        $scope.showAlertToast();
                        return;
                    }
                    else if(!$scope.addNewUser.emailid.includes("@"))
                    {
                        $scope.alertnote = "Please enter a valid email id.";
                        $scope.showAlertToast();
                        return;
                    }

                    if($scope.dashboardDetails.adAuthRoleEnabled === 'true')
                        $scope.addNewUser.role = 'NA';

                    if(!$scope.addNewUser.role)
                    {
                        $scope.alertnote = "Please select a role.";
                        $scope.showAlertToast();
                        return;
                    }

                    serviceInput['username'] = $scope.addNewUser.username;
                    serviceInput['fullname'] = $scope.addNewUser.fullname;
                    serviceInput['team'] = $scope.addNewUser.team.teamname;
                    serviceInput['role'] = $scope.addNewUser.role;
                    serviceInput['mailid'] = $scope.addNewUser.emailid;
                    serviceInput['userPassword'] = '';

                    $http({
                        method: "POST",
                        url: "addNewUser",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'addNewUser' : serviceInput },
                        data: serviceInput
                    }).success(function(output) {
                        $scope.alert = "New User Request : "+output.message;
                        if(output.success){
                            $scope.addNewUser.username = "";
                            $scope.addNewUser.fullname = "";
                            $scope.addNewUser.emailid = "";
                            swal({
                                 title: "",
                                 text: "New User Request : "+output.message,
                                 timer: 2000,
                                 showConfirmButton: true
                             }).then(function(isConfirm){
                                  $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/users";
                            });
                         }else $scope.showSubmitFailed('','');
                    }).error(
                        function(error)
                        {
                            $scope.handleValidationErrors(error);
                        }
                    );

                };

        $scope.cancelRequest = function() {
                    $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/teams";
                }

        $scope.cancelUserRequest = function() {
                            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/users";
                        }

        $scope.addNewTeam = function() {

                var serviceInput = {};

                if(!$scope.addNewTeam.teamname || $scope.addNewTeam.teamname.length==0)
                {
                    $scope.alertnote = "Please fill in Team Name.";
                    $scope.showAlertToast();

                    return;
                }

                if(!$scope.addNewTeam.teamname || $scope.addNewTeam.teamname.length < 3)
                {
                    $scope.alertnote = "Please fill in a valid Team Name.";
                    $scope.showAlertToast();

                    return;
                }

                if(!$scope.addNewTeam.teammail || $scope.addNewTeam.teammail.length==0)
                {
                    $scope.alertnote = "Please fill in Team Mail.";
                    $scope.showAlertToast();

                    return;
                }

                if(!$scope.addNewTeam.teamphone || $scope.addNewTeam.teamname.teamphone==0)
                    {
                        $scope.alertnote = "Please fill in Team phone.";
                        $scope.showAlertToast();

                        return;
                    }

                if(!$scope.addNewTeam.contactperson || $scope.addNewTeam.contactperson.length==0)
                {
                    $scope.alertnote = "Please fill in Team contact person.";
                    $scope.showAlertToast();

                    return;
                }


                serviceInput['teamname'] = $scope.addNewTeam.teamname.trim();
                serviceInput['teammail'] = $scope.addNewTeam.teammail;
                serviceInput['teamphone'] = $scope.addNewTeam.teamphone;
//                serviceInput['tenantId'] = $scope.addNewTeam.tenant;
                serviceInput['contactperson'] = $scope.addNewTeam.contactperson.trim();
                serviceInput['app'] = "";
                serviceInput['envList'] = $scope.updatedEnvArray;

                $http({
                    method: "POST",
                    url: "addNewTeam",
                    headers : { 'Content-Type' : 'application/json' },
                    data: serviceInput
                }).success(function(output) {
                    $scope.alert = "New Team added : "+output.message;
                    if(output.success){
                        swal({
                             title: "",
                             text: "New Team added : "+output.message,
                             timer: 2000,
                             showConfirmButton: true
                         }).then(function(isConfirm){
                             $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/teams";
                       });
                     }else $scope.showSubmitFailed('','');
                    $scope.addNewTeam.teamname = "";
                    $scope.addNewTeam.teammail = "";
                    $scope.addNewTeam.contactperson = "";
                    $scope.addNewTeam.teamphone = "";
                }).error(
                    function(error)
                    {
                        $scope.handleValidationErrors(error);
                    }
                );

            };

        $scope.searchUsers = function(){
            if($scope.usersearch)
            {
               $scope.usersearch = $scope.usersearch.trim();
            }

            $scope.showUsers(1);
        }

        $scope.onChangeTeamVar = "false";

        $scope.onChangeTeam = function(){
            $scope.showUsers(1);
            $scope.onChangeTeamVar = "true";
        }

        $scope.showUsers = function(pageNo) {
            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');

            var teamSelectedFromDropDown = $scope.teamsearch;
            var teamSel="";
            if(!teamSelectedFromDropDown && teamSelectedFromDropDown !== "")
            {
                for (var i = 0; i < sURLVariables.length; i++)
                    {
                        var sParameterName = sURLVariables[i].split('=');
                        if (sParameterName[0] === "team")
                        {
                            teamSel = sParameterName[1];
                        }
                    }
            }else {
                teamSel = teamSelectedFromDropDown;
            }

            if($scope.onChangeTeamVar === "true")
            {
                teamSel = teamSelectedFromDropDown;
            }

            if(teamSel === "null" || teamSel == null || !teamSel)
                teamSel = "";

            $http({
                method: "GET",
                url: "showUserList",
                headers : { 'Content-Type' : 'application/json' },
                params: {'teamName' : decodeURI(teamSel) , 'pageNo' : pageNo, 'searchUserParam' : $scope.usersearch},
            }).success(function(output) {
                $scope.userList = output;
                if(output && output.length > 0 && output[0] != null){
                    $scope.resultPages = output[0].allPageNos;
                    $scope.resultPageSelected = pageNo;
                }
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

     $scope.updatedEnvArray = [];
     $scope.onSelectEnvs = function(envId) {
        if($scope.updatedEnvArray.includes(envId))
            $scope.updatedEnvArray.splice($scope.updatedEnvArray.indexOf(envId), 1);
        else
            $scope.updatedEnvArray.push(envId);
     }

    $scope.getRequestTopicsEnvs = function() {

            $http({
                method: "GET",
                url: "getEnvsBaseCluster",
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


    $scope.displayTeamsForSwitch = false;
    $scope.onChangeSwitchTeams = function(){
        if($scope.addNewUser.canswitchteams){
            $scope.displayTeamsForSwitch = true;
        }else{
            $scope.displayTeamsForSwitch = false;
        }
    }

    $scope.updatedTeamsSwitchList = [];
    $scope.getUpdatedListOfSwitchTeams = function() {
        $scope.updatedTeamsSwitchList = [];
        if($scope.addNewUser.switchallowedteams !== null){
            for (let i = 0; i < $scope.addNewUser.switchallowedteams.length; i++)
            {
                $scope.updatedTeamsSwitchList.push($scope.addNewUser.switchallowedteams[i].teamId);
            }
        }
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
                $scope.dashboardDetails = output;
                $scope.userlogged = output.username;
                $scope.teamname = output.teamname;
                $scope.userrole = output.userrole;
                 $scope.notifications = output.notifications;
                $scope.notificationsAcls = output.notificationsAcls;
                $scope.notificationsSchemas = output.notificationsSchemas;
                $scope.notificationsUsers = output.notificationsUsers;

                if(output.companyinfo == null){
                    $scope.companyinfo = "Company not defined!!";
                }
                else
                    $scope.companyinfo = output.companyinfo;

                if($scope.userlogged != null)
                    $scope.loggedinuser = "true";

                $scope.checkSwitchTeams($scope.dashboardDetails.canSwitchTeams, $scope.dashboardDetails.teamId, $scope.userlogged);
                   $scope.checkPendingApprovals();
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

        $scope.onSwitchTeam = function() {
            var serviceInput = {};
            serviceInput['username'] = $scope.userlogged;
            serviceInput['teamId'] = $scope.teamId;

            swal({
                title: "Are you sure?",
                text: "You would like to update your team ?",
                type: "warning",
                showCancelButton: true,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: "Yes !",
                cancelButtonText: "No, cancel please!",
                closeOnConfirm: true,
                closeOnCancel: true
            }).then(function(isConfirm) {
                if (isConfirm.dismiss !== "cancel") {
                    $http({
                        method: "POST",
                        url: "user/updateTeam",
                        headers : { 'Content-Type' : 'application/json' },
                        data: serviceInput
                    }).success(function (output) {
                        $scope.alert = "User team update request : "+output.message;
                        if(output.success){
                            swal({
                                title: "",
                                text: "User team update request : "+output.message,
                                timer: 2000,
                                showConfirmButton: true
                            }).then(function(isConfirm){
                                $scope.refreshPage();
                            });
                        }else $scope.showSubmitFailed('','');
                    }).error(
                        function (error) {
                            $scope.handleValidationErrors(error);
                        }
                    );
                } else {
                    return;
                }
            });
        }

        $scope.checkSwitchTeams = function(canSwitchTeams, teamId, userId){
            if(canSwitchTeams === 'true'){
                $scope.teamId = parseInt(teamId);
                $scope.getSwitchTeamsList(userId);
            }
        }

        $scope.getSwitchTeamsList = function(userId) {
            $http({
                method: "GET",
                url: "user/" + userId + "/switchTeamsList",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.switchTeamsListDashboard = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

		$scope.redirectToPendingReqs = function(redirectPage){
				swal({
						title: "Pending Requests",
						text: "Would you like to look at them ?",
						type: "info",
						showCancelButton: true,
						confirmButtonColor: "#DD6B55",
						confirmButtonText: "Yes, show me!",
						cancelButtonText: "No, later!",
						closeOnConfirm: true,
						closeOnCancel: true
					}).then(function(isConfirm){
						if (isConfirm.dismiss != "cancel") {
							$window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/"+redirectPage;
						} else {
							return;
						}
					});
			}

        $scope.checkPendingApprovals = function() {
            if($scope.dashboardDetails.pendingApprovalsRedirectionPage === '')
                return;
            
            if(sessionStorage.getItem("pending_reqs_shown") === null){
                $scope.redirectToPendingReqs($scope.dashboardDetails.pendingApprovalsRedirectionPage);
                sessionStorage.setItem("pending_reqs_shown", "true");
            }
        }

        $scope.logout = function() {
                    $http({
                        method: "POST",
                        url: "logout",
                        headers : { 'Content-Type' : 'application/json' }
                    }).success(function(output) {
                        $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/" + "login";
                    }).error(
                        function(error)
                        {
                            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/" + "login";
                        }
                    );
                }

        $scope.sendMessageToAdmin = function(){

                if(!$scope.contactFormSubject)
                    return;
                if(!$scope.contactFormMessage)
                    return;
                if($scope.contactFormSubject.trim().length==0)
                    return;
                if($scope.contactFormMessage.trim().length==0)
                    return;

                $http({
                        method: "POST",
                        url: "sendMessageToAdmin",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'contactFormSubject' : $scope.contactFormSubject,'contactFormMessage' : $scope.contactFormMessage },
                        data:  {'contactFormSubject' : $scope.contactFormSubject,'contactFormMessage' : $scope.contactFormMessage }
                    }).success(function(output) {
                        $scope.alert = "Message Sent.";
                        swal({
                             title: "",
                             text: "Message sent.",
                             timer: 2000,
                             showConfirmButton: false
                         });
                    }).error(
                        function(error)
                        {
                            $scope.alert = error;
                        }
                    );
            }
}
);