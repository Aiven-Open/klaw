'use strict'

// confirmation of delete
// edit
// solution for transaction
// message store / key / gui
var app = angular.module('requestAclsApp',[]);

app.controller("requestAclsCtrl", function($scope, $http, $location, $window) {

	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed.
	$http.defaults.headers.common['Accept'] = 'application/json';

	$scope.disable_ssl=true;
    $scope.disable_ip=false;
    $scope.disable_consumergrp=true;

    $scope.showAlertToast = function() {
                  var x = document.getElementById("alertbar");
                  x.className = "show";
                  setTimeout(function(){ x.className = x.className.replace("show", ""); }, 2000);
                }

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

    $scope.TopReqTypeList = [ { label: 'Producer', value: 'Producer' }, { label: 'Consumer', value: 'Consumer' }	];

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

                    if(output.requestItems!='Authorized')
                    {
                        swal({
                                 title: "Not Authorized !",
                                 text: "",
                                 showConfirmButton: true
                             }).then(function(isConfirm){
                                    $scope.alertnote = "You are not authorized to request.";
                                    $scope.showAlertToast();
                                    $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/index";
                             });
                    }

                    if(output.companyinfo == null){
                        $scope.companyinfo = "Company not defined!!";
                    }
                    else
                        $scope.companyinfo = output.companyinfo;

                    if($scope.userlogged != null)
                        $scope.loggedinuser = "true";

                    $scope.checkPendingApprovals();
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

				if($scope.dashboardDetails.pendingApprovalsRedirectionPage == '')
					return;

				var sPageURL = window.location.search.substring(1);
				var sURLVariables = sPageURL.split('&');
				var foundLoggedInVar  = "false";
				for (var i = 0; i < sURLVariables.length; i++)
				{
					var sParameterName = sURLVariables[i].split('=');
					if (sParameterName[0] == "loggedin")
					{
						foundLoggedInVar  = "true";
						if(sParameterName[1] != "true")
							return;
					}
				}
				if(foundLoggedInVar == "true")
					$scope.redirectToPendingReqs($scope.dashboardDetails.pendingApprovalsRedirectionPage);
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

            // set default Aiven cluster
            $scope.aivenCluster = 'false';
            $scope.acl_ip_ssl = 'IP';
            $scope.onChangeEnvironment = function(envName){
                $http({
                        method: "GET",
                        url: "getClusterInfoFromEnv",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'envSelected' : envName, 'envType' : 'kafka' },
                    }).success(function(output) {
                        $scope.aivenCluster = output.aivenCluster;
                        if($scope.aivenCluster === 'false')
                            $scope.acl_ip_ssl = 'IP';
                        else
                            $scope.acl_ip_ssl = 'SSL';
                    }).error(
                        function(error)
                        {
                            $scope.alert = error;
                        }
                    );
            }

            // set default
            $scope.disable_consumergrp = false;

            $scope.changeTopicType = function(){
                if($scope.addAcl.topicreqtype.value == "Consumer"){
                    $scope.disable_consumergrp=false;
                    $scope.enablePrefixedTopicPattern = 'false';
                    $scope.aclpattern = 'LITERAL';
                    $scope.addAcl.acl_lit_pre = 'LITERAL';
                }
                else{
                    $scope.disable_consumergrp=true;
                    $scope.enablePrefixedTopicPattern = 'true';
                    }
            }

            $scope.selectedAclType="IP_ADDRESS";
            $scope.onSelectAcl = function(selectedAclType){
                    if(selectedAclType =='SSL'){
                        $scope.disable_ssl=false;
                        $scope.disable_ip=true;
                        $scope.acl_ipaddress=[""];
                        $scope.alc_ipaddresslength = $scope.acl_ipaddress.length;
                        $scope.selectedAclType="PRINCIPAL";  // Principal can be Username or CN certificate string
                    }else{
                        $scope.disable_ssl=true;
                        $scope.disable_ip=false;
                        $scope.acl_ssl=[""];
                        $scope.alc_ssllength = $scope.acl_ssl.length;
                        $scope.selectedAclType="IP_ADDRESS";
                    }
                }


            $scope.onSelectAclPattern = function(selectedAclPatternType){
                if(selectedAclPatternType == 'PREFIXED'){
                    $scope.aclpattern = "PREFIXED";

                    if($scope.addAcl.topicpattern && $scope.addAcl.topicpattern != null && $scope.addAcl.topicpattern.length > 3)
                    {}
                     else
                     {
                        return;
                     }

                    $scope.getTopicTeam($scope.addAcl.topicpattern);
                }else{
                    $scope.aclpattern = "LITERAL";
                    $scope.getTopicTeam($scope.addAcl.topicname);
                }
            }

            $scope.getAllTopics = function() {

                    $scope.alltopics = null;
                            $http({
                                method: "GET",
                                url: "getTopicsOnly",
                                headers : { 'Content-Type' : 'application/json' },
                                params: {'isMyTeamTopics' : 'false' },
                            }).success(function(output) {
                                $scope.alltopics = output;
                            }).error(
                                function(error)
                                {
                                    $scope.alert = error;
                                }
                            );
                        }


        $scope.getTopicTeam = function(topicName) {

            if($scope.addAcl.acl_lit_pre == 'PREFIXED')
            {
                if(topicName && topicName.length < 3)
                {
                    $scope.alertnote = "Topic prefix should be atleast 3 characters.";
                    $scope.showAlertToast();
                    return;
                }
                else if(!topicName)
                {
                    $scope.alertnote = "Please fill in Topic prefix.";
                    $scope.showAlertToast();
                    return;
                }
            }

            $http({
                method: "GET",
                url: "getTopicTeam",
                headers : { 'Content-Type' : 'application/json' },
                params: {'topicName' : topicName,
                 'patternType' : $scope.addAcl.acl_lit_pre}
            }).success(function(output) {
                $scope.topicteamname = output.team;
                $scope.errorFound = output.error;
                if($scope.errorFound){
                    $scope.addAcl.team="";
                    $scope.alert = $scope.errorFound;
                    $scope.alertnote = $scope.errorFound;
                    $scope.showAlertToast();
                }
                else if(!$scope.topicteamname){
                        alert("There is no team found for this topic : " +  topicName);
                        $scope.alertnote = "Topic prefix should be atleast 3 characters.";
                        $scope.showAlertToast();

                        $scope.addAcl.team="";
                        if($scope.addAcl.acl_lit_pre == 'PREFIXED')
                            $scope.addAcl.topicpattern.focus();
                         else $scope.addAcl.topicname.focus();

                        return;
                }
                $scope.addAcl.team = $scope.topicteamname;
            }).error(
                function(error)
                {
                    $scope.addAcl.team="";
                    $scope.alert = error.message;
                    $scope.alertnote = error.message;
                    $scope.showAlertToast();
                }
            );

        };

        $scope.cancelRequest = function() {
                    $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/browseTopics";
                }

        $scope.addAcl = function() {

            $scope.alert = null;
            $scope.alertnote = null;
            var serviceInput = {};

            var aclpatterntypetype;

            if(!$scope.addAcl.envName  || $scope.addAcl.envName == "")
            {
                $scope.alertnote = "Please select an environment";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.addAcl.topicreqtype)
            {
               $scope.alertnote = "Please select an ACL type";
               $scope.showAlertToast();
               return;
            }

            if($scope.aivenCluster == 'true'){
                $scope.addAcl.consumergroup = '-na-';
            }

            if($scope.addAcl.topicreqtype.value == 'Consumer' && !$scope.addAcl.consumergroup)
            {
                $scope.alertnote = "Consumer group is not filled."
                $scope.showAlertToast();
                return;
            }

            if($scope.addAcl.topicreqtype.value == 'Producer' && !$scope.addAcl.acl_lit_pre)
            {
                $scope.alertnote = "Please select acl literal type."
                $scope.showAlertToast();
                return;
            }

            if($scope.addAcl.acl_lit_pre == 'PREFIXED'){
                if($scope.addAcl.topicpattern && $scope.addAcl.topicpattern != null && $scope.addAcl.topicpattern.length > 2)
                    {
                    }
                 else
                 {
                    $scope.alertnote = "Please fill in Topic prefix. (atleast 3 characters)";
                    $scope.showAlertToast();
                    return;
                 }
                $scope.addAcl.topicname = $scope.addAcl.topicpattern;
                aclpatterntypetype = 'PREFIXED';
                $scope.getTopicTeam($scope.addAcl.topicpattern);
            }
            else
                aclpatterntypetype = 'LITERAL';

            if($scope.aivenCluster === 'false'){
                if($scope.acl_ip_ssl === 'IP')
                    $scope.acl_ssl = [""];
                else if($scope.acl_ip_ssl === 'SSL')
                    $scope.acl_ipaddress = [""];
            }

            if(!$scope.addAcl.team || !$scope.addAcl.topicname )
            {
                //alert("This topic is not owned by any team. Synchronize the metadata.");
                if($scope.addAcl.acl_lit_pre === 'PREFIXED'){
                    $scope.alertnote = "There are no matching topics with this prefix. Synchronize the metadata.";
                }
                else
                    $scope.alertnote = "This topic is not owned by any team. Synchronize the metadata.";

                $scope.showAlertToast();
                return false;
            }

            if($scope.acl_ipaddress !=null){
                for (var i = 0; i < $scope.acl_ipaddress.length; i++) {
                    if($scope.acl_ipaddress[i].length === 0 && $scope.acl_ip_ssl === 'IP' && $scope.aivenCluster === 'false')
                    {
                      $scope.alertnote = "Please fill in a valid IP address of the Producer/Consumer client";
                      $scope.showAlertToast();
                      return;
                    }
                }
            }

            if($scope.acl_ssl != null){
                for (var i = 0; i < $scope.acl_ssl.length; i++) {
                    if($scope.acl_ssl[i].length === 0 && $scope.acl_ip_ssl === 'SSL')
                    {
                      $scope.alertnote = "Please fill in a valid Principal of the Producer/Consumer client";
                      $scope.showAlertToast();
                      return;
                    }
                }
            }

            if(($scope.acl_ipaddress !=null) ||  ($scope.acl_ssl !=null)){}
             else
             {
                $scope.alertnote = "Please fill in a valid IP address or Principal of the Producer/Consumer client";
                 $scope.showAlertToast();
                return;
             }

            // reset values to null
            if($scope.acl_ip_ssl === 'IP')
                $scope.acl_ssl = null;
            else if($scope.acl_ip_ssl === 'SSL')
                $scope.acl_ipaddress = null;

             serviceInput['environment'] = $scope.addAcl.envName;
             serviceInput['topicname'] = $scope.addAcl.topicname;
             serviceInput['topictype'] = $scope.addAcl.topicreqtype.value;
             serviceInput['teamname'] = $scope.addAcl.team;
             serviceInput['appname'] = "App";//$scope.addAcl.app;
             serviceInput['remarks'] = $scope.addAcl.remarks;
             serviceInput['acl_ip'] = $scope.acl_ipaddress;
             serviceInput['acl_ssl'] = $scope.acl_ssl;
             serviceInput['consumergroup'] = $scope.addAcl.consumergroup;
             serviceInput['aclPatternType'] = aclpatterntypetype;
             serviceInput['transactionalId'] = $scope.addAcl.transactionalId;
             serviceInput['aclIpPrincipleType'] = $scope.selectedAclType;

            $http({
                method: "POST",
                url: "createAcl",
                headers : { 'Content-Type' : 'application/json' },
                params: {'addAclRequest' : serviceInput },
                data: serviceInput
            }).success(function(output) {
                if(output.result == 'success'){
                        swal({
                                 title: "Awesome !",
                                 text: "Subscription Request : "+output.result,
                                 showConfirmButton: true
                             }).then(function(isConfirm){
                                    $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/myAclRequests?reqsType=created&aclCreated=true";
                             });
                    }
                else{
                        $scope.alert = "Subscription Request : "+output.result,
                        $scope.showSubmitFailed('','');
                    }
            }).error(
                function(error)
                {
                    $scope.handleValidationErrors(error);
                }
            );

        };

        $scope.loadParams = function() {
                // default setting
                $scope.aclpattern = "LITERAL";

                var topicSelected;

                var sPageURL = window.location.search.substring(1);
                var sURLVariables = sPageURL.split('&');
                for (var i = 0; i < sURLVariables.length; i++)
                {
                    var sParameterName = sURLVariables[i].split('=');
                    if (sParameterName[0] == "topicname")
                    {
                        $scope.topicSelectedFromUrl = sParameterName[1]
                        $scope.addAcl.topicname = $scope.topicSelectedFromUrl;

                        $scope.getAllTopics();
                        $scope.getTopicTeam($scope.addAcl.topicname);
                    }
                }
            }

        $scope.acl_ipaddress=[""];
        $scope.acl_ssl=[""];
        $scope.alc_ipaddresslength = $scope.acl_ipaddress.length;
        $scope.alc_ssllength = $scope.acl_ssl.length;

        $scope.addAclRecord = function(indexToAdd){
             $scope.acl_ipaddress.push("");
             $scope.alc_ipaddresslength = $scope.acl_ipaddress.length;
        }

        $scope.removeAclRecord = function(indexToRemove){
             if($scope.acl_ipaddress.length ==1){}
             else{
                    $scope.acl_ipaddress.splice(indexToRemove, 1);
             }
             $scope.alc_ipaddresslength = $scope.acl_ipaddress.length;
        }

        $scope.addAclSslRecord = function(indexToAdd){
             $scope.acl_ssl.push("");
             $scope.alc_ssllength = $scope.acl_ssl.length;
        }

        $scope.removeAclSslRecord = function(indexToRemove){
             if($scope.acl_ssl.length ==1){}
             else{
                    $scope.acl_ssl.splice(indexToRemove, 1);
             }
             $scope.alc_ssllength = $scope.acl_ssl.length;
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
