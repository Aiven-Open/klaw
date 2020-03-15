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
            $scope.statusauthexectopics = output.statusauthexectopics_su;
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

        $scope.updatedSyncStr="";
        $scope.getDetails = function(req_no, teamselected, topic, consumergroup, acl_ip, acl_ssl, acltype) {

            $scope.updatedSyncStr  = $scope.updatedSyncStr + req_no + "-----" + topic + "-----" + teamselected+"-----"
            +consumergroup+"-----"+acl_ip+"-----"+acl_ssl+"-----"+acltype+"\n";
           // alert("updatedSyncStr "+$scope.updatedSyncStr);
        }

        $scope.synchAcls = function() {

            var serviceInput = {};

            if(!$scope.getAcls.envName)
                return;

            if (!window.confirm("Are you sure, you would like to Synchronize this info on "+$scope.getAcls.envName.name+ " ?")) {
                $scope.updatedSyncStr="";
                return;
            }

            $http({
                method: "POST",
                url: "updateSyncAcls",
                headers : { 'Content-Type' : 'application/json' },
                params: {'updatedSyncAcls' : $scope.updatedSyncStr , 'envSelected': $scope.getAcls.envName.name},
                data: {'updatedSyncAcls' : $scope.updatedSyncStr}
            }).success(function(output) {
                $scope.alert = "Acl Sync Request : "+output.result;
                $scope.updatedSyncStr="";
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

	// We add the "time" query parameter to prevent IE
	// from caching ajax results

	$scope.getAcls = function(pageNoSelected) {

        var serviceInput = {};
		
		//serviceInput['clusterType'] = $scope.getAcls.clusterType.value;
		serviceInput['env'] = $scope.getAcls.envName.name;
		//alert("---"+$scope.getTopics.envName.value);

		$http({
			method: "GET",
			url: "getSyncAcls",
            headers : { 'Content-Type' : 'application/json' },
            params: {'env' : $scope.getAcls.envName.name, 'topicnamesearch' : $scope.getAcls.topicnamesearch,
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