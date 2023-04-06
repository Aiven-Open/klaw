'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('modifyEnvsApp',[]);

app.controller("modifyEnvsCtrl", function($scope, $http, $location, $window) {
	
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

        $scope.cancelRequest = function() {
            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/envs";
        }

        $scope.cancelClusterRequest = function() {
            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/clusters";
        }

        $scope.getStandardEnvNames = function() {
            $http({
                method: "GET",
                url: "getStandardEnvNames",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.standardEnvNamesList = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

        $scope.onChangeCluster = function(clusterId){
            $http({
                    method: "GET",
                    url: "getClusterDetails",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'clusterId' : clusterId },
                    data: {'clusterId' : clusterId}
                }).success(function(output) {
                    if(output != null){
                        $scope.clusterDetails = output;
                    }
                    else
                        $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/clusters";
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );
        }

        $scope.loadClusterDetails = function(){
            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');

            var clusterId, clusterType;

            for (var i = 0; i < sURLVariables.length; i++)
                {
                    var sParameterName = sURLVariables[i].split('=');
                    if (sParameterName[0] === "clusterId")
                    {
                        clusterId = sParameterName[1];
                    }
                    else if (sParameterName[0] === "clusterType")
                    {
                        clusterType = sParameterName[1];
                    }
                }

            if(!clusterId)
                return;

            if(!clusterType)
                return;

            $scope.clusterToEdit = clusterId;
            $scope.clusterTypeToEdit = clusterType;

            $scope.onChangeCluster(clusterId);
        }

        $scope.loadEnvDetails = function(){
            $scope.alert = "";
            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');

            var envId, envType;

            for (var i = 0; i < sURLVariables.length; i++)
                {
                    var sParameterName = sURLVariables[i].split('=');
                    if (sParameterName[0] === "envId")
                    {
                        envId = sParameterName[1];
                    }
                    else if (sParameterName[0] === "envType")
                    {
                        envType = sParameterName[1];
                    }
                }

            if(!envId)
                return;

            if(!envType)
                return;

            $scope.envToEdit = envId;
            $scope.envTypeToEdit = envType;

            $http({
                    method: "GET",
                    url: "getEnvDetails",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'envSelected' : envId, 'envType' : envType },
                    data: {'envSelected' : envId, 'envType' : envType}
                }).success(function(output) {
                    if(output != null && output !== ""){
                        $scope.envDetails = output;
                        $scope.onChangeCluster(output.clusterId);
                    }else
                        $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/envs";
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );
        }

        $scope.sslparams = "false";

        $scope.onChangeProtocol = function(protocol)
        {
            if(protocol === 'SSL')
                $scope.sslparams = "true";
            else
                $scope.sslparams = "false";
        }

        $scope.getSchemaRegistryClusters = function() {
                $http({
                        method: "GET",
                        url: "getClusters",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'clusterType' : 'schemaregistry' },
                        data: {'clusterType' : 'schemaregistry'}
                    }).success(function(output) {
                        $scope.allschemaclusters = output;
                    }).error(
                        function(error)
                        {
                            $scope.alert = error;
                        }
                    );
            }

        $scope.getKafkaClusters = function() {
            $http({
                    method: "GET",
                    url: "getClusters",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'clusterType' : 'kafka' },
                    data: {'clusterType' : 'kafka'}
                }).success(function(output) {
                    $scope.allclusters = output;
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );
        }

        $scope.getSchemaRegEnvs = function() {
                    $http({
                            method: "GET",
                            url: "getSchemaRegEnvs",
                            headers : { 'Content-Type' : 'application/json' }
                        }).success(function(output) {
                            $scope.allSchemaEnvMappings = output;
                        }).error(
                            function(error)
                            {
                                $scope.alert = error;
                            }
                        );
                }

         $scope.getKafkaEnvs = function() {
                            $http({
                                    method: "GET",
                                    url: "getEnvs",
                                    headers : { 'Content-Type' : 'application/json' }
                                }).success(function(output) {
                                    $scope.allKafkaEnvMappings = output;
                                }).error(
                                    function(error)
                                    {
                                        $scope.alert = error;
                                    }
                                );
                        }

        $scope.getKafkaConnectClusters = function() {
            $http({
                    method: "GET",
                    url: "getClusters",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'clusterType' : 'kafkaconnect' },
                    data: {'clusterType' : 'kafkaconnect'}
                }).success(function(output) {
                    $scope.allkafkaconnectclusters = output;
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

        $scope.editClusterDetails = function() {

                    $scope.clusterDetails.type = 'kafka';

                    if($scope.clusterDetails.bootstrapServers === undefined)
                        {
                            $scope.alertnote = "Please fill in host";
                            $scope.showAlertToast();
                            return;
                        }

                    if($scope.clusterDetails.clusterName === undefined)
                    {
                        $scope.alertnote = "Please fill in a name for cluster";
                        $scope.showAlertToast();
                        return;
                    }

                    if($scope.clusterDetails.clusterName.length > 20)
                    {
                        $scope.alertnote = "Cluster name cannot be more than 20 characters.";
                        $scope.showAlertToast();
                        return;
                    }

                    var serviceInput = {};

                    serviceInput['clusterId'] = $scope.clusterToEdit;
                    serviceInput['clusterName'] = $scope.clusterDetails.clusterName;
                    serviceInput['bootstrapServers'] = $scope.clusterDetails.bootstrapServers;
                    serviceInput['protocol'] = $scope.clusterDetails.protocol;
                    serviceInput['clusterType'] = $scope.clusterDetails.type;
                    serviceInput['projectName'] = $scope.clusterDetails.projectName;
                    serviceInput['serviceName'] = $scope.clusterDetails.serviceName;
                    serviceInput['kafkaFlavor'] = $scope.clusterDetails.kafkaFlavor;
                    serviceInput['associatedServers'] = $scope.clusterDetails.associatedServers;

                    $http({
                        method: "POST",
                        url: "addNewCluster",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'addNewCluster' : serviceInput },
                        data: serviceInput
                    }).success(function(output) {
                        $scope.alert = "Cluster updated : " + output.result;
                        if(output.result == 'success'){
                            swal({
                                 title: "",
                                 text: "Cluster updated : "+output.result,
                                 timer: 3000,
                                 showConfirmButton: true
                             }).then(function(isConfirm){
                                 $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/clusters";
                           });
                         }else $scope.showSubmitFailed('','');
                    }).error(
                        function(error)
                        {
                            $scope.handleValidationErrors(error);
                        }
                    );

                };

       $scope.editSchemaCluster = function() {

                $scope.clusterDetails.type = 'schemaregistry';

                if($scope.clusterDetails.bootstrapServers === undefined)
                    {
                        $scope.alertnote = "Please fill in bootstrapServer";
                        $scope.showAlertToast();
                        return;
                    }

                if($scope.clusterDetails.clusterName === undefined)
                {
                    $scope.alertnote = "Please fill in a name for cluster";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.clusterDetails.clusterName.length > 15)
                {
                    $scope.alertnote = "Cluster name cannot be more than 15 characters.";
                    $scope.showAlertToast();
                    return;
                }



                var serviceInput = {};
                serviceInput['clusterId'] = $scope.clusterToEdit;
                serviceInput['clusterName'] = $scope.clusterDetails.clusterName;
                serviceInput['bootstrapServers'] = $scope.clusterDetails.bootstrapServers;
                serviceInput['protocol'] = $scope.clusterDetails.protocol;
                serviceInput['clusterType'] = $scope.clusterDetails.type;
                serviceInput['kafkaFlavor'] = $scope.clusterDetails.kafkaFlavor;
                serviceInput['otherParams'] = "default.partitions=na,max.partitions=na,replication.factor=na";

                $http({
                    method: "POST",
                    url: "addNewCluster",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'addNewEnv' : serviceInput },
                    data: serviceInput
                }).success(function(output) {
                    $scope.alert = "Schema registry cluster updated: "+output.result;
                    if(output.result === 'success'){
                        swal({
                             title: "",
                             text: "Schema registry cluster updated : "+output.result,
                             timer: 2000,
                             showConfirmButton: true
                         }).then(function(isConfirm){
                             $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/clusters";
                       });
                     }else $scope.showSubmitFailed('','');
                }).error(
                    function(error)
                    {
                         $scope.handleValidationErrors(error);
                    }
                );

            };

       $scope.editKafkaConnectCluster = function() {

               $scope.clusterDetails.type = 'kafkaconnect';

               if($scope.clusterDetails.bootstrapServers == undefined)
                   {
                       $scope.alertnote = "Please fill in kafka connect host";
                       $scope.showAlertToast();
                       return;
                   }

               if($scope.clusterDetails.clusterName == undefined)
               {
                   $scope.alertnote = "Please fill in a name for cluster";
                   $scope.showAlertToast();
                   return;
               }

               if($scope.clusterDetails.clusterName.length > 15)
               {
                   $scope.alertnote = "Cluster name cannot be more than 15 characters.";
                   $scope.showAlertToast();
                   return;
               }

               var serviceInput = {};
               serviceInput['clusterId'] = $scope.clusterToEdit;
               serviceInput['clusterName'] = $scope.clusterDetails.clusterName;
               serviceInput['bootstrapServers'] = $scope.clusterDetails.bootstrapServers;
               serviceInput['protocol'] = $scope.clusterDetails.protocol;
               serviceInput['clusterType'] = $scope.clusterDetails.type;
               serviceInput['kafkaFlavor'] = $scope.clusterDetails.kafkaFlavor;
               serviceInput['otherParams'] = "default.partitions=na,max.partitions=na,replication.factor=na";

               $http({
                   method: "POST",
                   url: "addNewCluster",
                   headers : { 'Content-Type' : 'application/json' },
                   params: {'addNewEnv' : serviceInput },
                   data: serviceInput
               }).success(function(output) {
                   $scope.alert = "Schema registry cluster updated: "+output.result;
                   if(output.result == 'success'){
                       swal({
                            title: "",
                            text: "Kafka Connect cluster updated : "+output.result,
                            timer: 2000,
                            showConfirmButton: false
                        }).then(function(isConfirm){
                            $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/clusters";
                      });
                    }else $scope.showSubmitFailed('','');
               }).error(
                   function(error)
                   {
                        $scope.handleValidationErrors(error);
                   }
               );

           };

        $scope.editEnvDetails = function() {

                $scope.envDetails.type = 'kafka';

                if($scope.envDetails.name == undefined || !$scope.envDetails.name)
                {
                    $scope.alertnote = "Please fill in a name for Environment";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.envDetails.name.length > 10)
                {
                    $scope.alertnote = "Environment name cannot be more than 10 characters.";
                    $scope.showAlertToast();
                    return;
                }

                if(!$scope.envDetails.clusterId)
                {
                    $scope.alertnote = "Please select a cluster.";
                    $scope.showAlertToast();
                    return;
                }

                /// Partitions validation

                if($scope.envDetails.defaultPartitions.length<=0 || $scope.envDetails.defaultPartitions<=0)
                {
                    $scope.alertnote = "Default partitions should not be empty and should be greater than 0";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.envDetails.maxPartitions.length<=0 || $scope.envDetails.maxPartitions<=0)
                {
                    $scope.alertnote = "Maximum partitions should not be empty and should be greater than 0";
                    $scope.showAlertToast();
                    return;
                }

                if(isNaN($scope.envDetails.defaultPartitions)){
                    $scope.alertnote = "Default partitions should be a valid number";
                    $scope.showAlertToast();
                    return;
                }

                if(isNaN($scope.envDetails.maxPartitions)){
                    $scope.alertnote = "Maximum partitions should be a valid number";
                    $scope.showAlertToast();
                    return;
                }

                if(parseInt($scope.envDetails.defaultPartitions) > parseInt($scope.envDetails.maxPartitions)){
                    $scope.alertnote = "Default partitions should be less than Maximum partitions";
                    $scope.showAlertToast();
                    return;
                }

                // Replication factor validation
                if(parseInt($scope.envDetails.defaultReplicationFactor.length) <= 0 || parseInt($scope.envDetails.defaultReplicationFactor) <= 0)
                {
                    $scope.alertnote = "Default replication factor should not be empty and should be greater than 0";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.envDetails.maxReplicationFactor.length<=0 || $scope.envDetails.maxReplicationFactor<=0)
                {
                    $scope.alertnote = "Maximum replication factor should not be empty and should be greater than 0";
                    $scope.showAlertToast();
                    return;
                }

                if(isNaN($scope.envDetails.defaultReplicationFactor)){
                    $scope.alertnote = "Default replication factor should be a valid number";
                    $scope.showAlertToast();
                    return;
                }

                if(isNaN($scope.envDetails.maxReplicationFactor)){
                    $scope.alertnote = "Maximum replication factor should be a valid number";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.envDetails.defaultReplicationFactor > $scope.envDetails.maxReplicationFactor){
                    $scope.alertnote = "Default Replication factor should be less than Maximum Replication factor";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.envDetails.topicprefix && $scope.envDetails.topicprefix.length > 0)
                    $scope.envDetails.topicprefix = $scope.envDetails.topicprefix.trim();
                else
                    $scope.envDetails.topicprefix = "";

                if($scope.envDetails.topicsuffix && $scope.envDetails.topicsuffix.length > 0)
                    $scope.envDetails.topicsuffix = $scope.envDetails.topicsuffix.trim();
                else
                    $scope.envDetails.topicsuffix = "";

                var serviceInput = {};

                serviceInput['id'] = $scope.envToEdit;
                serviceInput['name'] = $scope.envDetails.name;
                serviceInput['clusterId'] = $scope.envDetails.clusterId;
                serviceInput['tenantId'] = $scope.envDetails.tenantId;
                serviceInput['type'] = 'kafka';
                serviceInput['otherParams'] = "default.partitions=" + $scope.envDetails.defaultPartitions
                 + ",max.partitions=" + $scope.envDetails.maxPartitions
                 + ",default.replication.factor=" + $scope.envDetails.defaultReplicationFactor
                 + ",max.replication.factor=" + $scope.envDetails.maxReplicationFactor
                 + ",topic.prefix=" + $scope.envDetails.topicprefix
                 + ",topic.suffix=" + $scope.envDetails.topicsuffix;

                $http({
                    method: "POST",
                    url: "addNewEnv",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'addNewEnv' : serviceInput },
                    data: serviceInput
                }).success(function(output) {
                    $scope.alert = "Environment updated: "+output.result;
                    if(output.result == 'success'){
                        swal({
                             title: "",
                             text: "Environment updated : "+output.result,
                             timer: 2000,
                             showConfirmButton: true
                         }).then(function(isConfirm){
                             $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/envs";
                       });
                     }else $scope.showSubmitFailed('','');
                }).error(
                    function(error)
                    {
                         $scope.handleValidationErrors(error);
                    }
                );

            };

           $scope.editSchemaEnv = function() {

                    $scope.envDetails.type = 'schemaregistry';

                    if($scope.envDetails.name == undefined)
                    {
                        $scope.alertnote = "Please fill in a name for Environment";
                        $scope.showAlertToast();
                        return;
                    }

                    if($scope.envDetails.name.length > 10)
                    {
                        $scope.alertnote = "Environment name cannot be more than 10 characters.";
                        $scope.showAlertToast();
                        return;
                    }

                    if(!$scope.envDetails.clusterId)
                    {
                        $scope.alertnote = "Please select a cluster.";
                        $scope.showAlertToast();
                        return;
                    }

                    var serviceInput = {};
                    serviceInput['id'] = $scope.envToEdit;
                    serviceInput['name'] = $scope.envDetails.name;
                    serviceInput['clusterId'] = $scope.envDetails.clusterId;
                    serviceInput['tenantId'] = $scope.envDetails.tenantId;
                    serviceInput['type'] = $scope.envDetails.type;

                    if($scope.envDetails.associatedEnv != undefined && $scope.envDetails.associatedEnv != null && $scope.envDetails.associatedEnv.id !=undefined && $scope.envDetails.associatedEnv.id !=null ) {
                    serviceInput['associatedEnv'] = { id: $scope.envDetails.associatedEnv.id , name : $scope.allKafkaEnvMappings.find(element => element.id === $scope.envDetails.associatedEnv.id).name };
                    } else {
                    serviceInput['associatedEnv'] = null;
                    }


                    $http({
                        method: "POST",
                        url: "addNewEnv",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'addNewEnv' : serviceInput },
                        data: serviceInput
                    }).success(function(output) {
                    $scope.alert = "Environment updated: "+output.result;
                    if(output.result == 'success'){
                        swal({
                             title: "",
                             text: "Environment updated : "+output.result,
                             timer: 2000,
                             showConfirmButton: true
                         }).then(function(isConfirm){
                             $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/envs";
                       });
                     }else $scope.showSubmitFailed('','');
                    }).error(
                        function(error)
                        {
                             $scope.handleValidationErrors(error);
                        }
                    );

                };

        $scope.editKafkaConnectEnv = function() {

            $scope.envDetails.type = 'kafkaconnect';

            if($scope.envDetails.name == undefined)
            {
                $scope.alertnote = "Please fill in a name for Environment";
                $scope.showAlertToast();
                return;
            }

            if($scope.envDetails.name.length > 10)
            {
                $scope.alertnote = "Environment name cannot be more than 10 characters.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.envDetails.clusterId)
            {
                $scope.alertnote = "Please select a cluster.";
                $scope.showAlertToast();
                return;
            }

            var serviceInput = {};
            serviceInput['id'] = $scope.envToEdit;
            serviceInput['name'] = $scope.envDetails.name;
            serviceInput['clusterId'] = $scope.envDetails.clusterId;
            serviceInput['tenantId'] = $scope.envDetails.tenantId;
            serviceInput['type'] = $scope.envDetails.type;

            $http({
                method: "POST",
                url: "addNewEnv",
                headers : { 'Content-Type' : 'application/json' },
                params: {'addNewEnv' : serviceInput },
                data: serviceInput
            }).success(function(output) {
            $scope.alert = "Environment updated: "+output.result;
            if(output.result == 'success'){
                swal({
                     title: "",
                     text: "Environment updated : "+output.result,
                     timer: 2000,
                     showConfirmButton: true
                 }).then(function(isConfirm){
//                     $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/envs";
               });
             }else $scope.showSubmitFailed('','');
            }).error(
                function(error)
                {
                     $scope.handleValidationErrors(error);
                }
            );
        };

        $scope.getKafkaSupportedProtocols = function() {
            $http({
                method: "GET",
                url: "getKafkaProtocols",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.kafkaProtocols = output;
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