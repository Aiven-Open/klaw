'use strict'

// confirmation of delete
// edit 
// solution for transaction
// message store / key / gui
var app = angular.module('envsApp',[]);

app.directive('onReadFile', function ($parse) {
	return {
		restrict: 'A',
		scope: false,
		link: function(scope, element, attrs) {
            var fn = $parse(attrs.onReadFile);

			element.on('change', function(onChangeEvent) {
				var reader = new FileReader();

				reader.onload = function(onLoadEvent) {
					scope.$apply(function() {
						fn(scope, {$fileContent:onLoadEvent.target.result});
					});
				};

				reader.readAsArrayBuffer((onChangeEvent.srcElement || onChangeEvent.target).files[0]);
			});
		}
	};
});

app.controller("envsCtrl", function($scope, $http, $location, $window) {


	$scope.aclCommandSsl = "";
	$scope.aclCommandPlaintext = "";

	$scope.kafkaClusters = [ { label: 'Non-Aiven', value: 'nonaiven' }, { label: 'Aiven', value: 'aiven' }	];

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
                    }else $scope.alert = "Unable to process the request.";

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

        $scope.onLoadClustersPage = function(){
            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');
            for (var i = 0; i < sURLVariables.length; i++)
            {
                var sParameterName = sURLVariables[i].split('=');
                if (sParameterName[0] == "clusterId")
                {
                    $scope.clusterIdFromUrl = sParameterName[1];
                }
            }
        }

        $scope.onLoadEnvsPage = function(){
            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');
            for (var i = 0; i < sURLVariables.length; i++)
            {
                var sParameterName = sURLVariables[i].split('=');
                if (sParameterName[0] == "envId")
                {
                    $scope.envIdFromUrl = sParameterName[1];
                }
            }
        }

        $scope.getUpdateEnvStatus = function(envId){
            $http({
                    method: "GET",
                    url: "getUpdateEnvStatus",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'envId' : envId },
                    data: {'envId' : envId}
                }).success(function(output) {
                    $scope.envUpdatedStatus = output;

                    swal({
                         title: "",
                         text: "Current Status: "+output.envstatus,
                         timer: 2000,
                         showConfirmButton: false
                     });

                     if(output.result == 'success'){
                         $scope.getEnvsPaginated(1);
                         $scope.getSchemaRegEnvs();
                         $scope.getKafkaConnectEnvs();
                     }
                }).error(
                    function(error)
                    {
                        $scope.alert = "Unable to connect to Kafka environment.";
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
                    $scope.clusterDetails = output;
                }).error(
                    function(error)
                    {
                        $scope.alert = error;
                    }
                );
        }

        $scope.downloadPubKey = function(){
                    $http({
                        method: "GET",
                        url: "getKwPubkey",
                        headers : { 'Content-Type' : 'application/json' }
                    }).success(function(output, status, headers) {
                        var binary_string = window.atob(output.data);
                        var len = binary_string.length;
                        var bytes = new Uint8Array(len);
                        for (var i = 0; i < len; i++) {
                            bytes[i] = binary_string.charCodeAt(i);
                        }

                        var a = window.document.createElement('a');
                        a.href = window.URL.createObjectURL(new Blob([bytes.buffer], { type:'application/octet-stream' }));
                        a.download = output.filename;
                        document.body.appendChild(a)
                        a.click();

                        swal({
                             title: "",
                             text: "PublicKey is being downloaded.",
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

        $scope.getAclCommand = function(){
            $http({
                    method: "GET",
                    url: "getAclCommands",
                    headers : { 'Content-Type' : 'application/json' }
                }).success(function(output) {
                    $scope.aclCommandSsl = output.aclCommandSsl;
                    $scope.aclCommandPlaintext = output.aclCommandPlaintext;
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

        $scope.searchClusters = function(){
            $scope.clusterIdFromUrl = "";
            if($scope.searchClusterParam)
            {
               $scope.searchClusterParam = $scope.searchClusterParam.trim();
            }

            $scope.getAllClusters(1);
        }

        $scope.getAllClusters = function(pageNo) {
                $http({
                        method: "GET",
                        url: "getClustersPaginated",
                        headers : { 'Content-Type' : 'application/json' },
                        params: {'clusterType' : 'all', 'clusterId' : $scope.clusterIdFromUrl,
                         'pageNo' : pageNo, 'searchClusterParam' : $scope.searchClusterParam},
                        data: {'clusterType' : 'all'}
                    }).success(function(output) {
                        $scope.allclustersset = output;
                        if(output[0] != null){
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

        $scope.deleteTenant = function(){
            swal({
                    title: "Are you sure?",
                    text: "You would like to delete the tenant ? Note : This will delete all the associated data. You cannot login later.",
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
                                url: "deleteTenant",
                                headers : { 'Content-Type' : 'application/json' }
                            }).success(function(output) {
                                $scope.alert = "Delete Tenant Request : "+output.result;
                                if(output.result=='success'){
                                    swal({
                                         title: "",
                                         text: "Delete Tenant Request : "+output.result,
                                         timer: 2000,
                                         showConfirmButton: false
                                     });
                                    $scope.logoutCustom();
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

        $scope.logoutCustom = function(tenantId) {
                $http({
                    method: "POST",
                    url: "logout",
                    headers : { 'Content-Type' : 'application/json' }
                }).success(function(output) {
                    $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/feedback";
                }).error(
                    function(error)
                    {
                        $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/feedback";
                    }
                );
            }

        $scope.updateTenant = function(){
            if($scope.myTenantInfo.orgName == undefined)
                {
                    $scope.alertnote = "Please fill in an Organization name";
                    $scope.showAlertToast();
                    return;
                }
             var orgName = $scope.myTenantInfo.orgName.trim();
             if(orgName.length == 0 || orgName.length > 50)
             {
                 $scope.alertnote = "Please fill in a valid Organization name with less than 50 chars.";
                 $scope.showAlertToast();
                 return;
             }

            $http({
                method: "POST",
                url: "udpateTenant",
                headers : { 'Content-Type' : 'application/json' },
                params: {'orgName' : orgName},
                data: {'orgName' : orgName}
            }).success(function(output) {
                    $scope.alert = "Update Tenant Request : "+output.result;
                    if(output.result=='success'){
                        swal({
                             title: "",
                             text: "Update Tenant Request : "+output.result,
                             timer: 2000,
                             showConfirmButton: false
                         });

                     }else $scope.showSubmitFailed('','');
            }).error(
                function(error)
                {

                }
            );
        }

        $scope.submitTenantExtension = function(){
             if($scope.selectedTenantExtensionPeriod == "" || $scope.selectedTenantExtensionPeriod == "selected")
                    {
                        $scope.alertnote = "Please select an extension period";
                        $scope.showAlertToast();
                        return;
                    }
                 var orgName = $scope.myTenantInfo.orgName.trim();
                 if(orgName.length == 0 || orgName.length > 50)
                 {
                     $scope.alertnote = "Please fill in a valid Organization name with less than 50 chars.";
                     $scope.showAlertToast();
                     return;
                 }

                $http({
                    method: "POST",
                    url: "udpateTenantExtension",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'selectedTenantExtensionPeriod' : $scope.selectedTenantExtensionPeriod},
                    data: {'selectedTenantExtensionPeriod' : $scope.selectedTenantExtensionPeriod}
                }).success(function(output) {
                        $scope.alert = "Update Tenant extension Request : "+output.result;
                        if(output.result=='success'){
                            swal({
                                 title: "",
                                 text: "Update Tenant extension Request : "+output.result + ". You will hear from us very soon. Thank you !!",
                                 timer: 2000,
                                 showConfirmButton: false
                             });
                         }else $scope.showSubmitFailed('','');
                }).error(
                    function(error)
                    {

                    }
                );
        }

        $scope.getTenantExtensionPeriods = function(){
            $http({
                    method: "GET",
                          url: "getExtensionPeriods",
                          headers : { 'Content-Type' : 'application/json' }
                      }).success(function(output) {
                          $scope.tenantExtensionPeriods = output;
                      }).error(
                          function(error)
                          {
                              $scope.alert = error;
                          }
                      );
        }

        $scope.getTenantInfo = function(){
            $http({
                method: "GET",
                      url: "getMyTenantInfo",
                      headers : { 'Content-Type' : 'application/json' }
                  }).success(function(output) {
                      $scope.myTenantInfo = output;

                      if(output.numberOfDays < 62)
                            $scope.myTenantInfo.showExtension = "true";

                      if(output.numberOfDays < 0)
                            $scope.myTenantInfo.expired = "true";

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


        $scope.searchEnvs = function(){
            $scope.envIdFromUrl = "";

            if($scope.searchEnvParam)
            {
               $scope.searchEnvParam = $scope.searchEnvParam.trim();
            }

            $scope.getEnvsPaginated(1);
        }

        $scope.getEnvsPaginated = function(pageNo) {

            $http({
                method: "GET",
                url: "getEnvsPaginated",
                headers : { 'Content-Type' : 'application/json' },
                params: {'pageNo' : pageNo, 'envId' : $scope.envIdFromUrl, 'searchEnvParam' : $scope.searchEnvParam},
            }).success(function(output) {
                $scope.allenvs = output;
                if(output[0] != null){
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

        $scope.sslparams = "false";
        $scope.plaintextparams = "false";

        $scope.onChangeProtocol = function(protocol)
        {
            if(protocol == 'SSL')
                $scope.sslparams = "true";
            else
                $scope.sslparams = "false";

            if(protocol == 'PLAINTEXT')
                $scope.plaintextparams = "true";
            else
                $scope.plaintextparams = "false";
        }

        $scope.deleteCluster = function(idval) {

            swal({
                    title: "Are you sure?",
                    text: "You would like to delete the cluster ?",
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
                                url: "deleteCluster",
                                headers : { 'Content-Type' : 'application/json' },
                                params: {'clusterId' : idval },
                                data: {'clusterId' : idval}
                            }).success(function(output) {
                                $scope.alert = "Delete Cluster Request : "+output.result;
                                if(output.result=='success'){
                                    swal({
                                         title: "",
                                         text: "Delete Cluster Request : "+output.result,
                                         timer: 2000,
                                         showConfirmButton: true
                                     }).then(function(isConfirm){
                                          $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/clusters";
                                    });
                                 }else $scope.showSubmitFailed('','');
                                $scope.getAllClusters(1);
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

        $scope.deleteEnv = function(idval, envType) {
            swal({
            		title: "Are you sure?",
            		text: "You would like to delete the Environment ?",
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
                                url: "deleteEnvironmentRequest",
                                headers : { 'Content-Type' : 'application/json' },
                                params: {'envId' : idval, 'envType' : envType },
                                data: {'envId' : idval}
                            }).success(function(output) {
                                $scope.alert = "Delete Environment Request : "+output.result;
                                if(output.result=='success'){
                                    swal({
                                         title: "",
                                         text: "Delete Environment Request : "+output.result,
                                         timer: 2000,
                                         showConfirmButton: true
                                     }).then(function(isConfirm){
                                         $window.location.href = $window.location.origin + $scope.dashboardDetails.contextPath + "/envs";
                                   });
                                 }else $scope.showSubmitFailed('','');
                                $scope.getEnvsPaginated(1);
                                $scope.getSchemaRegEnvs();
                                $scope.getKafkaConnectEnvs();
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

        $scope.saveContent = function($fileContent){
            $scope.addNewCluster.pubkeyUploadedFromUI = $fileContent;
        };

        $scope.kafkaFlavor = 'Apache Kafka';

        $scope.onChangeKafkaFlavor = function(kafkaFlavor){
            $scope.kafkaFlavor = kafkaFlavor;
        }

        $scope.addNewCluster = function() {

                        $scope.addNewCluster.type = $scope.addNewCluster.clusterType;

                        if($scope.addNewCluster.host == undefined)
                            {
                                $scope.alertnote = "Please fill in bootstrap servers";
                                $scope.showAlertToast();
                                return;
                            }

                        if($scope.addNewCluster.envname == undefined)
                        {
                            $scope.alertnote = "Please fill in a name for cluster";
                            $scope.showAlertToast();
                            return;
                        }

                        if($scope.addNewCluster.envname.length > 20)
                        {
                            $scope.alertnote = "Cluster name cannot be more than 20 characters.";
                            $scope.showAlertToast();
                            return;
                        }

                        if(!$scope.addNewCluster.protocol)
                        {
                            $scope.alertnote = "Please select a protocol.";
                            $scope.showAlertToast();
                            return;
                        }

                        if($scope.addNewCluster.protocol == 'SSL' && $scope.dashboardDetails.saasEnabled == 'saas'){
                            if(!$scope.addNewCluster.pubkeyUploadedFromUI)
                            {
                                $scope.alertnote = "Please select a valid Public key file";
                                $scope.showAlertToast();
                                return;
                            }
                        }

                        if($scope.kafkaFlavor === 'Aiven for Apache Kafka')
                        {
                            if($scope.addNewCluster.projectName === undefined || !$scope.addNewCluster.projectName)
                            {
                                $scope.alertnote = "Please fill in Project Name as defined in Aiven console";
                                $scope.showAlertToast();
                                return;
                            }

                            if($scope.addNewCluster.serviceName === undefined || !$scope.addNewCluster.serviceName)
                            {
                                $scope.alertnote = "Please fill in service Name as defined in Aiven console";
                                $scope.showAlertToast();
                                return;
                            }
                        }

                        var serviceInput = {};

                        serviceInput['clusterName'] = $scope.addNewCluster.envname;
                        serviceInput['bootstrapServers'] = $scope.addNewCluster.host;
                        serviceInput['protocol'] = $scope.addNewCluster.protocol;
                        serviceInput['clusterType'] = $scope.addNewCluster.type;
                        var pubkeyStr = window.btoa(String.fromCharCode.apply(null, new Uint8Array($scope.addNewCluster.pubkeyUploadedFromUI)));
                        serviceInput['publicKey'] = pubkeyStr;
                        serviceInput['projectName'] = $scope.addNewCluster.projectName;
                        serviceInput['serviceName'] = $scope.addNewCluster.serviceName;
                        serviceInput['kafkaFlavor'] = $scope.kafkaFlavor;

                        $http({
                            method: "POST",
                            url: "addNewCluster",
                            headers : { 'Content-Type' : 'application/json' },
                            params: {'addNewCluster' : serviceInput },
                            data: serviceInput
                        }).success(function(output) {
                            $scope.alert = "New cluster added : "+output.result;
                            $scope.addNewCluster.envname = "";
                            $scope.addNewCluster.host = "";
                            $scope.addNewCluster.pubKeyFile = "";
                            if(output.result=='success'){
                                swal({
                                     title: "",
                                     text: "New cluster added : "+output.result,
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

        $scope.addNewEnv = function() {

                // Validation partitions
                if($scope.addNewEnv.defparts.length<=0 || $scope.addNewEnv.defparts<=0)
                {
                	$scope.alertnote = "Default partitions should not be empty and should be greater than 0";
                	$scope.showAlertToast();
                	return;
                }

                if(isNaN($scope.addNewEnv.defparts)){
                    $scope.alertnote = "Default partitions should be a valid number";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addNewEnv.defmaxparts.length<=0 || $scope.addNewEnv.defmaxparts<=0)
                {
                	$scope.alertnote = "Maximum partitions should not be empty and should be greater than 0";
                	$scope.showAlertToast();
                	return;
                }

                if(isNaN($scope.addNewEnv.defmaxparts)){
                    $scope.alertnote = "Maximum partitions should be a valid number";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addNewEnv.defparts > $scope.addNewEnv.defmaxparts){
                    $scope.alertnote = "Default partitions should be less than Maximum partitions";
                    $scope.showAlertToast();
                    return;
                }

                // Validation replication factor

                if($scope.addNewEnv.defrepfctr.length<=0 || $scope.addNewEnv.defrepfctr<=0)
                {
                	$scope.alertnote = "Default replication factor should not be empty and should be greater than 0";
                	$scope.showAlertToast();
                	return;
                }

                if(isNaN($scope.addNewEnv.defrepfctr)){
                    $scope.alertnote = "Default replication factor should be a valid number";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addNewEnv.maxrepfctr.length<=0 || $scope.addNewEnv.maxrepfctr<=0)
                {
                    $scope.alertnote = "Maximum Replication factor should not be empty and should be greater than 0";
                    $scope.showAlertToast();
                    return;
                }

                if(isNaN($scope.addNewEnv.maxrepfctr)){
                    $scope.alertnote = "Maximum Replication factor should be a valid number";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addNewEnv.defrepfctr > $scope.addNewEnv.maxrepfctr){
                    $scope.alertnote = "Default Replication factor should be less than Maximum Replication factor";
                    $scope.showAlertToast();
                    return;
                }

                // Prefix and suffix validations

                if($scope.addNewEnv.topicprefix && $scope.addNewEnv.topicprefix.length > 0)
                	$scope.addNewEnv.topicprefix = $scope.addNewEnv.topicprefix.trim();
                else
                	$scope.addNewEnv.topicprefix = "";

                if($scope.addNewEnv.topicsuffix && $scope.addNewEnv.topicsuffix.length > 0)
                    $scope.addNewEnv.topicsuffix = $scope.addNewEnv.topicsuffix.trim();
                else
                    $scope.addNewEnv.topicsuffix = "";

                if($scope.addNewEnv.envname == undefined || !$scope.addNewEnv.envname)
                {
                    $scope.alertnote = "Please fill in a name for environment";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addNewEnv.envname.length > 3)
                {
                    $scope.alertnote = "Environment name cannot be more than 3 characters.";
                    $scope.showAlertToast();
                    return;
                }

                if(!$scope.addNewEnv.cluster)
                {
                    $scope.alertnote = "Please select a cluster.";
                    $scope.showAlertToast();
                    return;
                }

//                if(!$scope.addNewEnv.tenant)
//                {
//                    $scope.alertnote = "Please select a tenant.";
//                    $scope.showAlertToast();
//                    return;
//                }

                var serviceInput = {};

                serviceInput['name'] = $scope.addNewEnv.envname;
                serviceInput['clusterId'] = $scope.addNewEnv.cluster;
//                serviceInput['tenantId'] = $scope.addNewEnv.tenant;
                serviceInput['type'] = 'kafka';

                serviceInput['otherParams'] = "default.partitions=" + $scope.addNewEnv.defparts
                                         + ",max.partitions=" + $scope.addNewEnv.defmaxparts
                                         + ",default.replication.factor=" + $scope.addNewEnv.defrepfctr
                                         + ",max.replication.factor=" + $scope.addNewEnv.maxrepfctr
                                         + ",topic.prefix=" + $scope.addNewEnv.topicprefix
                                         + ",topic.suffix=" + $scope.addNewEnv.topicsuffix;

                $http({
                    method: "POST",
                    url: "addNewEnv",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'addNewEnv' : serviceInput },
                    data: serviceInput
                }).success(function(output) {
                    $scope.alert = "New environment status : "+output.result;
                    $scope.addNewEnv.envname = "";
                    if(output.result=='success'){
                        swal({
                             title: "",
                             text: "New environment status : "+output.result,
                             timer: 3000,
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

           $scope.addNewSchemaEnv = function() {

                $scope.addNewSchemaEnv.type = 'schemaregistry';

                if($scope.addNewSchemaEnv.envname == undefined)
                {
                    $scope.alertnote = "Please fill in a name for environment";
                    $scope.showAlertToast();
                    return;
                }

                if($scope.addNewSchemaEnv.envname.length > 7)
                {
                    $scope.alertnote = "Environment name cannot be more than 7 characters.";
                    $scope.showAlertToast();
                    return;
                }

                if(!$scope.addNewSchemaEnv.cluster)
                {
                    $scope.alertnote = "Please select a cluster.";
                    $scope.showAlertToast();
                    return;
                }

//                if(!$scope.addNewSchemaEnv.tenant)
//                {
//                    $scope.alertnote = "Please select a tenant.";
//                    $scope.showAlertToast();
//                    return;
//                }

                var serviceInput = {};

                serviceInput['name'] = $scope.addNewSchemaEnv.envname;
                serviceInput['type'] = $scope.addNewSchemaEnv.type;
                serviceInput['clusterId'] = $scope.addNewSchemaEnv.cluster;
//                serviceInput['tenantId'] = $scope.addNewSchemaEnv.tenant;

                $http({
                    method: "POST",
                    url: "addNewEnv",
                    headers : { 'Content-Type' : 'application/json' },
                    params: {'addNewEnv' : serviceInput },
                    data: serviceInput
                }).success(function(output) {
                    $scope.alert = "New Environment added status: " + output.result;
                    if(output.result == 'success'){
                        swal({
                             title: "",
                             text: "New Schema environment status : "+output.result,
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

         }

        $scope.addNewKafkaConnectEnv = function(){
            $scope.addNewKafkaConnectEnv.type = 'kafkaconnect';

            if($scope.addNewKafkaConnectEnv.envname == undefined)
            {
                $scope.alertnote = "Please fill in a name for environment";
                $scope.showAlertToast();
                return;
            }

            if($scope.addNewKafkaConnectEnv.envname.length > 7)
            {
                $scope.alertnote = "Environment name cannot be more than 7 characters.";
                $scope.showAlertToast();
                return;
            }

            if(!$scope.addNewKafkaConnectEnv.cluster)
            {
                $scope.alertnote = "Please select a cluster.";
                $scope.showAlertToast();
                return;
            }

//            if(!$scope.addNewKafkaConnectEnv.tenant)
//            {
//                $scope.alertnote = "Please select a tenant.";
//                $scope.showAlertToast();
//                return;
//            }

            var serviceInput = {};

            serviceInput['name'] = $scope.addNewKafkaConnectEnv.envname;
            serviceInput['type'] = $scope.addNewKafkaConnectEnv.type;
            serviceInput['clusterId'] = $scope.addNewKafkaConnectEnv.cluster;
//            serviceInput['tenantId'] = $scope.addNewKafkaConnectEnv.tenant;

            $http({
                method: "POST",
                url: "addNewEnv",
                headers : { 'Content-Type' : 'application/json' },
                params: {'addNewEnv' : serviceInput },
                data: serviceInput
            }).success(function(output) {
                $scope.alert = "New Environment added status: " + output.result;
                if(output.result == 'success'){
                    swal({
                         title: "",
                         text: "New KafkaConnect environment status : "+output.result,
                         timer: 3000,
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
        }

        $scope.getSchemaRegEnvs = function() {

            $http({
                method: "GET",
                url: "getSchemaRegEnvs",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.allschenvs = output;
            }).error(
                function(error)
                {
                    $scope.alert = error;
                }
            );
        }

        $scope.getKafkaConnectEnvs = function() {

            $http({
                method: "GET",
                url: "getKafkaConnectEnvs",
                headers : { 'Content-Type' : 'application/json' }
            }).success(function(output) {
                $scope.allkafkaconnectenvs = output;
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