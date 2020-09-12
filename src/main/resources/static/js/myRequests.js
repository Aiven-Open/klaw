'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('myRequestsApp',[]);

app.controller("myRequestsCtrl", function($scope, $http, $location, $window) {
	
	// Set http service defaults
	// We force the "Accept" header to be only "application/json"
	// otherwise we risk the Accept header being set by default to:
	// "application/json; text/plain" and this can result in us
	// getting a "text/plain" response which is not able to be
	// parsed. 
	$http.defaults.headers.common['Accept'] = 'application/json';

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

    $scope.getMyTopicRequests = function(pageNoSelected) {
        $http({
            method: "GET",
            url: "getTopicRequests",
            headers : { 'Content-Type' : 'application/json' },
            params: {'pageNo' : pageNoSelected }
        }).success(function(output) {
            $scope.topicRequests = output;
            if(output!=null && output.length>0){
                $scope.resultPages = output[0].allPageNos;
                $scope.resultPageSelected = pageNoSelected;
            }

        }).error(
            function(error)
            {
                $scope.alert = error;
                $scope.topicRequests = null;
            }
        );
    }

        $scope.getMyAclRequests = function(pageNoSelected) {
            $http({
                method: "GET",
                url: "getAclRequests",
                headers : { 'Content-Type' : 'application/json' },
                 params: {'pageNo' : pageNoSelected }
            }).success(function(output) {
                $scope.aclRequests = output;
                if(output!=null && output.length>0){
                    $scope.resultPagesAcl = output[0].allPageNos;
                    $scope.resultPageSelectedAcl = pageNoSelected;
                }
            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.aclRequests = null;
                }
            );
        }

        $scope.getMySchemaRequests = function() {
            $http({
                method: "GET",
                url: "getSchemaRequests",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.schemaRequests = output;

            }).error(
                function(error)
                {
                    $scope.alert = error;
                    $scope.schemaRequests = null;
                }
            );
        }


    $scope.deleteTopicRequest = function(topic_selected,env_selected) {

        var topicname = topic_selected +","+ env_selected;
        $http({
            method: "GET",
            url: "deleteTopicRequests",
            headers : { 'Content-Type' : 'application/json' },
            params: {'topicName' : topicname },
            data: {'topicName' : topicname}
        }).success(function(output) {

            $scope.alert = "Topic Delete Request : "+output.result;
            $scope.getMyTopicRequests(1);

        }).error(
            function(error)
            {
                $scope.alert = error;
            }
        );
    }

        $scope.deleteAclRequest = function(req_no) {

            $http({
                method: "GET",
                url: "deleteAclRequests",
                headers : { 'Content-Type' : 'application/json' },
                params: {'req_no' : req_no },
                data: {'req_no' : req_no}
            }).success(function(output) {

                $scope.alert = "Acl Delete Request : "+output.result;
                $scope.getMyAclRequests(1);

            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

        $scope.deleteSchemaRequest = function() {

            $http({
                method: "POST",
                url: "deleteSchemaRequests",
                headers : { 'Content-Type' : 'application/json' },
                params: {'topicName' : $scope.deleteSchemaRequest.topicname },
                data: {'topicName' : $scope.deleteSchemaRequest.topicname}
            }).success(function(output) {

                $scope.alert = "Topic Delete Request : "+output.result;
                $scope.getMySchemaRequests();

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

	// We add the "time" query parameter to prevent IE
	// from caching ajax results

	$scope.getTopics = function() {
	//var authStatus = getExecAuth();

		var serviceInput = {};
		
		//serviceInput['clusterType'] = $scope.getTopics.clusterType.value;
		serviceInput['env'] = $scope.getTopics.envName.name;
		//alert("---"+$scope.getTopics.envName.value);
		if (!window.confirm("Are you sure, you would like to view the topics in Environment : " +
				$scope.getTopics.envName.name + " ?")) {
			return;
		}
		
		$http({
			method: "GET",
			url: "getTopics",
            headers : { 'Content-Type' : 'application/json' },
            params: {'env' : $scope.getTopics.envName.name }
		}).success(function(output) {
			$scope.resultBrowse = output;
		}).error(
			function(error) 
			{
				$scope.alert = error;
			}
		);
		
	};



}
);