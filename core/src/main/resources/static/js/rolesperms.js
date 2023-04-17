'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('rolesPermsApp',[]);

app.controller("rolesPermsCtrl", function($scope, $http, $location, $window) {
	
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

    $scope.cancelRoleRequest = function() {
                $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/roles";
            }

        $scope.cancelTenantRequest = function() {
                        $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/tenants";
                    }

           $scope.addTenantId = function(){

                     if(!$scope.addTenantId.tenantName)
                     {
                       $scope.alertnote = "Please fill in Tenant Name.";
                       $scope.showAlertToast();
                       return;
                     }

                     if(!$scope.addTenantId.tenantDesc)
                      {
                        $scope.alertnote = "Please fill in Tenant description.";
                        $scope.showAlertToast();
                        return;
                      }

                     var tenantId = $scope.addTenantId.tenantName.trim();

                     if(tenantId.length == 0){
                       $scope.alertnote = "Please fill in Tenant Name.";
                       $scope.showAlertToast();
                     }

                     var serviceInput = {};
                     serviceInput['tenantName'] = $scope.addTenantId.tenantName;
                     serviceInput['tenantDesc'] = $scope.addTenantId.tenantDesc;

                     $http({
                             method: "POST",
                             url: "addTenantId",
                             headers : { 'Content-Type' : 'application/json' },
                             params: {'tenantName' : $scope.addTenantId.tenantName, 'tenantDesc' : $scope.addTenantId.tenantDesc },
                             data: serviceInput
                         }).success(function(output) {
                             $scope.alert = "Tenant add status : "+ output.message;
                             $scope.addTenantId.tenantName = "";
                             if(output.success){
                                 swal({
                                          title: "",
                                          text: "Tenant add status : " + output.message,
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

    $scope.getRolesFromDb = function() {
                $http({
                    method: "GET",
                    url: "getRolesFromDb",
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

     $scope.deleteRole = function(role){
        swal({
        		title: "Are you sure?",
        		text: "You would like to delete the role ? Note : This will delete all the associated permissions.",
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
                             url: "deleteRole",
                             headers : { 'Content-Type' : 'application/json' },
                             params: {'roleId' : role }
                         }).success(function(output) {
                             $scope.alert = "Role delete status : "+ output.message;
                             $scope.getRolesFromDb();
                             if(output.success){
                                  swal({
                                           title: "",
                                           text: "Role delete status : "+output.message,
                                           timer: 2000,
                                           showConfirmButton: true
                                       }).then(function(isConfirm){
                                         $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/roles";
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
     }

     $scope.addRoleId = function(){

              if(!$scope.addRoleId.roleId)
              {
                $scope.alertnote = "Please fill in a role.";
                $scope.showAlertToast();
                return;
              }

              if($scope.addRoleId.roleId.indexOf(" ") > 0){
                $scope.alertnote = "Please fill in a role without spaces.";
                $scope.showAlertToast();
                return;
              }

              var roleId = $scope.addRoleId.roleId.trim();

              if(roleId.length == 0){
                $scope.alertnote = "Please fill in a role.";
                $scope.showAlertToast();
              }

              $http({
                      method: "POST",
                      url: "addRoleId",
                      headers : { 'Content-Type' : 'application/json' },
                      params: {'roleId' : $scope.addRoleId.roleId },
                      data: {'roleId' : $scope.addRoleId.roleId }
                  }).success(function(output) {
                      $scope.alert = "Role add status : "+ output.message + ". You can now update permissions.";
                      $scope.addRoleId.roleId = "";
                      if(output.success){
                          swal({
                                   title: "",
                                   text: "Role add status : "+ output.message + ". You can now update permissions.",
                                   timer: 2000,
                                   showConfirmButton: true
                               }).then(function(isConfirm){
                                  $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/roles";
                            });
                      }else $scope.showSubmitFailed('','');
                  }).error(
                      function(error)
                      {
                          $scope.handleValidationErrors(error);
                      }
                  );
          }

          $scope.getAllPermissionDescriptions = function() {
                      $http({
                          method: "GET",
                          url: "getPermissionDescriptions",
                          headers : { 'Content-Type' : 'application/json' }
                      }).success(function(output) {
                          $scope.permissionDescriptions = output;
                      }).error(
                          function(error)
                          {
                              $scope.alert = error;
                          }
                      );
                  }

    $scope.getVal = function(rolePermKey){
            return $scope.permissionDescriptions[rolePermKey];
    }

	$scope.getPermissions = function() {
            $http({
                method: "GET",
                url: "getPermissions",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.permissions = output;
                if(output == null || output == "")
                    $scope.alert = "Please check if you have access to view this page.";
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

        $scope.updatedPermissionsArray = [];
        $scope.updatePermissionValues = function(role, permission, permissionEnabled){
            var serviceInput = {};
            var rolePermKey = role + "-----" + permission;
            serviceInput['rolePermission'] = rolePermKey;
            serviceInput['permissionEnabled'] = permissionEnabled + "";

            $scope.updatedPermissionsArray.push(serviceInput);
        }

        $scope.httpUpdatePermissions = function(){
            $http({
                    method: "POST",
                    url: "updatePermissions",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'updatePermissionsRequest' : $scope.updatedPermissionsArray },
                    data:  $scope.updatedPermissionsArray
                }).success(function(output) {
                    $scope.alert = "Permissions update: "+output.message;

                    $scope.getPermissions();
                     if(output.success){
                      swal({
                               title: "",
                               text: "Permissions update: "+output.message,
                               timer: 2000,
                               showConfirmButton: true
                           }).then(function(isConfirm){
                               $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/permissions";
                         });
                    }else $scope.showSubmitFailed('','');
                }).error(
                    function(error)
                    {
                        $scope.getPermissions();
                        $scope.handleValidationErrors(error);
                    }
                );
        }

        $scope.updatePermissions = function(){
            var serviceInput = {};

            if($scope.updatedPermissionsArray.length==0){
                return;
            }

            $scope.alert = null;
            $scope.alertnote = null;
            swal({
                     title: "Are you sure?",
                     text: "You would like to update permissions. Make sure atleast one permission exists per role.",
                     type: "warning",
                     showCancelButton: true,
                     confirmButtonColor: "#DD6B55",
                     confirmButtonText: "Yes, please proceed !",
                     cancelButtonText: "No, cancel please!",
                     closeOnConfirm: true,
                     closeOnCancel: true
                 }).then(function(isConfirm){
                     if (isConfirm.dismiss != "cancel") {
                         $scope.httpUpdatePermissions();
                     } else {
                         return;
                     }
                 });

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

        $scope.getTenants = function(){
            $http({
                    method: "GET",
                    url: "getTenants",
                    headers : { 'Content-Type' : 'application/json' }
                }).success(function(output) {
                    $scope.tenantlist = output;
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
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