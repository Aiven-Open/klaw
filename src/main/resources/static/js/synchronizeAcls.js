'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('synchronizeAclsApp',[]);

app.controller("synchronizeAclsCtrl", function($scope, $http, $location, $window) {


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

	$scope.getEnvs = function() {
	        $http({
                method: "GET",
                url: "getSyncEnv",
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
            $scope.notificationsSchemas = output.notificationsSchemas;
            $scope.notificationsUsers = output.notificationsUsers;
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

        $scope.updatedSyncArray = [];
        $scope.getDetails = function(sequence, req_no, teamselected, topic, consumergroup, acl_ip, acl_ssl, acltype) {

            var serviceInput = {};

            serviceInput['sequence'] = sequence;
            serviceInput['req_no'] = req_no;
            serviceInput['topicName'] = topic;
            serviceInput['teamSelected'] = teamselected;
            serviceInput['consumerGroup'] = consumergroup;
            serviceInput['aclIp'] = acl_ip;
            serviceInput['aclSsl'] = acl_ssl;
            serviceInput['aclType'] = acltype;
            serviceInput['envSelected'] = $scope.getAcls.envName.name.key;

            $scope.updatedSyncArray.push(serviceInput);
        }

        $scope.synchAcls = function() {

            var serviceInput = {};

            if(!$scope.getAcls.envName)
                return;

            if (!window.confirm("Are you sure, you would like to Synchronize this info on "+$scope.getAcls.envName.name.key+ " ?")) {
                $scope.updatedSyncArray = [];
                return;
            }

            $http({
                method: "POST",
                url: "updateSyncAcls",
                headers : { 'Content-Type' : 'application/json' },
                params: {'syncAclUpdates' : $scope.updatedSyncArray },
                data: $scope.updatedSyncArray
            }).success(function(output) {
                $scope.alert = "Acl Sync Request : "+output.result;
                $scope.updatedSyncArray = [];
                $scope.showSuccessToast();
                $scope.getAcls(1);
            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.alertnote = error;
                    $scope.showAlertToast();
                }
            );

        };

	// We add the "time" query parameter to prevent IE
	// from caching ajax results

	$scope.getAcls = function(pageNoSelected) {

        var serviceInput = {};
		serviceInput['env'] = $scope.getAcls.envName.name.key;

		$http({
			method: "GET",
			url: "getSyncAcls",
            headers : { 'Content-Type' : 'application/json' },
            params: {'env' : $scope.getAcls.envName.name.key, 'topicnamesearch' : $scope.getAcls.topicnamesearch,
                'pageNo' : pageNoSelected }
		}).success(function(output) {
			$scope.resultBrowse = output;
			if(output!=null){
                $scope.resultPages = output[0].allPageNos;
                $scope.resultPageSelected = pageNoSelected;
            }
		}).error(
			function(error) 
			{
			    $scope.resultBrowse = [];
				$scope.alert = error;
			}
		);
		
	};

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


}
);