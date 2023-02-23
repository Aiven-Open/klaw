'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('registerUsersApp',[]);

app.controller("registerUsersCtrl", function($scope, $http, $location, $window) {
	
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

        $scope.getActivationInfo = function() {
            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');
            var activationId = "";
            for (var i = 0; i < sURLVariables.length; i++)
            {
                var sParameterName = sURLVariables[i].split('=');
                if (sParameterName[0] == "activationId")
                {
                    activationId = sParameterName[1];
                }
            }

            $http({
                method: "GET",
                url: "getActivationInfo",
                headers : { 'Content-Type' : 'application/json' },
                params: {'userActivationId' : activationId },
            }).success(function(output) {
                $scope.activationResult = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
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
                    $scope.alert = "User team update request : "+output.result;
                    if(output.result === 'success'){
                        swal({
                            title: "",
                            text: "User team update request : "+output.result,
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


        $scope.getAllTeamsSUFromRegisterUsers = function() {
                    $http({
                        method: "GET",
                        url: "getAllTeamsSUFromRegisterUsers",
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


    $scope.getUserRequests = function() {
                $http({
                    method: "GET",
                    url: "getNewUserRequests",
                    headers : { 'Content-Type' : 'application/json' }
                }).success(function(output) {
                    $scope.userRequests = output;
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


            $scope.processRegistrationId = function(){
                var userRegistrationId = "";
                var sPageURL = window.location.search.substring(1);
                var sURLVariables = sPageURL.split('&');
                for (var i = 0; i < sURLVariables.length; i++)
                {
                    var sParameterName = sURLVariables[i].split('=');
                    if (sParameterName[0] == "userRegistrationId")
                    {
                        userRegistrationId = sParameterName[1];
                    }
                }

                if(userRegistrationId != ""){
                    $http({
                            method: "GET",
                            url: "getUserInfoFromRegistrationId",
                            headers : { 'Content-Type' : 'application/json' },
                            params: {'userRegistrationId' : userRegistrationId },
                            data: {'userRegistrationId' : userRegistrationId}
                        }).success(function(output) {
                            $scope.registerUser = output;
                            if(output != null && output != "")
                                $scope.userNotFoundinKwDb = "true";
                        }).error(
                            function(error)
                            {
                                $scope.alert = error;
                            }
                        );
                }

            }

            $scope.execUserRequestApprove = function(username) {

                     //   alert("::in exec acl"+$scope.execAclRequest.req_no);

                        $http({
                            method: "POST",
                            url: "execNewUserRequestApprove",
                            headers : { 'Content-Type' : 'application/json' },
                            params: {'username' : username },
                            data: {'username' : username}
                        }).success(function(output) {

                            $scope.alert = "User Approve Request : "+output.result;
                            $scope.getUserRequests();
                            if(output.result === 'success'){
                                swal({
                                     title: "",
                                     text: "User Approve Request : "+output.result,
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

                    $scope.execUserRequestDecline = function(username) {

                            $http({
                                method: "POST",
                                url: "execNewUserRequestDecline",
                                headers : { 'Content-Type' : 'application/json' },
                                params: {'username' : username },
                                data: {'username' : username}
                            }).success(function(output) {

                                $scope.alert = "User decline Request : "+output.result;
                                $scope.getUserRequests();
                                if(output.result === 'success'){
                                    swal({
                                         title: "",
                                         text: "User decline Request : "+output.result,
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


	$scope.registerUser = function() {

            var serviceInput = {};

          if(!$scope.registerUser.username && $scope.registerUser.username.indexOf(" "))
          {
              $scope.alertnote = "Please enter a valid username with no spaces.";
              $scope.showAlertToast();
              return;
          }

          if($scope.registerUser.username.length < 6)
          {
              $scope.alertnote = "Username should be atleast 6 characters.";
              $scope.showAlertToast();
              return;
          }

          if(!$scope.registerUser.usernamefull)
          {
              $scope.alertnote = "Please enter Full Name.";
              $scope.showAlertToast();
              return;
          }

          if($scope.registerUser.usernamefull.length < 6)
          {
              $scope.alertnote = "Please enter Full Name atleast 6 characters.";
              $scope.showAlertToast();
              return;
          }

          if(!$scope.registerUser.pwd)
          {
              $scope.alertnote = "Please enter a password.";
             $scope.showAlertToast();
              return;
          }

          if($scope.registerUser.pwd.length < 8)
          {
              $scope.alertnote = "Password should be atleast 8 characters.";
              $scope.showAlertToast();
              return;
          }

            if($scope.registerUser.pwd!=$scope.registerUser.reppwd)
            {
                $scope.alertnote = "Passwords are not equal.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.registerUser.emailid)
            {
                $scope.alertnote = "Email id is mandatory.";
                $scope.showAlertToast();
                return;
            }
            else if($scope.registerUser.emailid.length < 7)
            {
                $scope.alertnote = "Please enter a valid email id.";
                $scope.showAlertToast();
                return;
            }
            else if(!$scope.registerUser.emailid.includes("@"))
            {
                $scope.alertnote = "Please enter a valid email id.";
                $scope.showAlertToast();
                return;
            }

           var tenantName = "";
           if($scope.registerUser.tenantName)
            {
               var tenantName =  $scope.registerUser.tenantName.trim();
               if(tenantName.length < 12)
               {
                   $scope.alertnote = "Please enter a valid tenant.";
                   $scope.showAlertToast();
                   return;
               }
            }
            $scope.registrationStarted = 'true';
            serviceInput['username'] = $scope.registerUser.username;
            serviceInput['fullname'] = $scope.registerUser.usernamefull;
            serviceInput['pwd'] = $scope.registerUser.pwd;
            serviceInput['mailid'] = $scope.registerUser.emailid;
            serviceInput['tenantName'] = tenantName.trim();

            $http({
                method: "POST",
                url: "registerUser",
                headers : { 'Content-Type' : 'application/json' },
                data: serviceInput
            }).success(function(output) {
                if(output.result === 'success'){
                    swal({
                         title: "",
                         text: "Registration Request : "+output.result,
                         timer: 2000,
                         showConfirmButton: false
                     });
                    $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/registrationReview";
                 }else {
                       $scope.registrationStarted = 'false';
                       $scope.alert = "Registration Request : " + output.result + "." + output.error ;
//                       $scope.showSubmitFailed('','');
                       $scope.alertnote = $scope.alert;
                       $scope.showAlertToast();
                   }
            }).error(
                function(error)
                {
                    $scope.registrationStarted = 'false';
                    $scope.handleValidationErrors(error);
                }
            );

        };

       $scope.registrationStarted = 'false';

       $scope.registerUserSaas = function() {

                 var serviceInput = {};

                 if(!$scope.registerUser.usernamefull)
                 {
                     $scope.alertnote = "Please enter your Name.";
                     $scope.showAlertToast();
                     return;
                 }

                 if($scope.registerUser.usernamefull.length < 4)
                 {
                     $scope.alertnote = "Please enter your valid Name.";
                     $scope.showAlertToast();
                     return;
                 }

                   if(!$scope.registerUser.emailid)
                   {
                       $scope.alertnote = "Email id is mandatory.";
                       $scope.showAlertToast();
                       return;
                   }
                   else if($scope.registerUser.emailid.length < 7)
                   {
                       $scope.alertnote = "Please enter a valid email id.";
                       $scope.showAlertToast();
                       return;
                   }
                   else if(!$scope.registerUser.emailid.includes("@"))
                   {
                       $scope.alertnote = "Please enter a valid email id.";
                       $scope.showAlertToast();
                       return;
                   }

                   var tenantName = "";
                   if($scope.registerUser.tenantName)
                    {
                       var tenantName =  $scope.registerUser.tenantName.trim();
                       if(tenantName.length < 12)
                       {
                           $scope.alertnote = "Please enter a valid tenant.";
                           $scope.showAlertToast();
                           return;
                       }
                    }

                  if(!$scope.registerUser.agreeterms)
                  {
                      $scope.alertnote = "Please agree to the terms.";
                      $scope.showAlertToast();
                      return;
                  }

                  $scope.registrationStarted = 'true';

                   serviceInput['fullname'] = $scope.registerUser.usernamefull;
                   serviceInput['mailid'] = $scope.registerUser.emailid;
                   serviceInput['tenantName'] = tenantName.trim();
                   serviceInput['recaptchaStr'] = grecaptcha.getResponse();

                   $http({
                       method: "POST",
                       url: "registerUserSaas",
                       headers : { 'Content-Type' : 'application/json' },
                       data: serviceInput
                   }).success(function(output) {
                       if(output.result === 'success'){
                            $scope.alert = "Registration Request : "+output.result;
                           swal({
                                title: "",
                                text: "Registration Request : "+output.result,
                                timer: 2000,
                                showConfirmButton: false
                            });
                           $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/registrationReview";
                        }else {
                            $scope.registrationStarted = 'false';
                            $scope.alert = "Registration Request : " + output.result + "." + output.error ;
                            $scope.alertnote = $scope.alert;
                            $scope.showAlertToast();
                        }
                   }).error(
                       function(error)
                       {
                           $scope.registrationStarted = 'false';
                           $scope.handleValidationErrors(error);
                       }
                   );

               };

        $scope.registerUserLdap = function() {

            var serviceInput = {};

            if(!$scope.registerUser.username && $scope.registerUser.username.indexOf(" "))
              {
                  $scope.alertnote = "Please enter a valid username with no spaces.";
                  $scope.showAlertToast();
                  return;
              }

              if($scope.registerUser.username.length < 6)
              {
                  $scope.alertnote = "Username should be atleast 6 characters.";
                  $scope.showAlertToast();
                  return;
              }

              if(!$scope.registerUser.fullname)
              {
                  $scope.alertnote = "Please enter Full Name.";
                  $scope.showAlertToast();
                  return;
              }

              if($scope.registerUser.fullname.length < 6)
              {
                  $scope.alertnote = "Please enter Full Name atleast 6 characters.";
                  $scope.showAlertToast();
                  return;
              }

                if(!$scope.registerUser.emailid)
                {
                    $scope.alertnote = "Email id is mandatory.";
                    $scope.showAlertToast();
                    return;
                }
                else if($scope.registerUser.emailid.length < 7)
                {
                    $scope.alertnote = "Please enter a valid email id.";
                    $scope.showAlertToast();
                    return;
                }
                else if(!$scope.registerUser.emailid.includes("@"))
                {
                    $scope.alertnote = "Please enter a valid email id.";
                     $scope.showAlertToast();
                    return;
                }

               var tenantName = "";

                    $scope.registrationStarted = 'true';

                    serviceInput['username'] = $scope.registerUser.username;
                    serviceInput['fullname'] = $scope.registerUser.fullname;
                    serviceInput['mailid'] = $scope.registerUser.emailid;
                    serviceInput['tenantName'] = tenantName.trim();
                    serviceInput['pwd'] = '';

                    $http({
                        method: "POST",
                        url: "registerUser",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'newUser' : serviceInput },
                        data: serviceInput
                    }).success(function(output) {
                        $scope.alert = "Registration Request : "+output.result;
                        if(output.result == 'success'){

                            swal({
                                 title: "",
                                 text: "Registration Request : "+output.result,
                                 timer: 2000,
                                 showConfirmButton: false
                             });
                             $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/registrationReview";
                         }else  {
                                  $scope.registrationStarted = 'false';
                                  $scope.alert = "Registration Request : " + output.result + "." + output.error ;
                                  $scope.showSubmitFailed('','');
                              }

                    }).error(
                        function(error)
                        {
                            $scope.registrationStarted = 'false';
                            $scope.handleValidationErrors(error);
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
             };

    }
);