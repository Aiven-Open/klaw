// angular-webmethods module
// =========================
//
// Usage:
// 
// Short form:
//   wm.invoke('pub.flow:tracePipeline', { some: 'pipeline' })
//
// 
// Long form:
//   wm.invoke({
//     host: 'host'
//     port: 1234,
//     service: 'pub.flow:tracePipeline'
//     pipeline:
//     user:
//   })
//
// Both forms return a promise object which can be used like this
//
// var promise = wm.invoke(...);
//
// promise.success(function(pipeline) {
//     console.log(pipeline);
// });
//
// promise.error(function(error) {
//     console.log(error);
// });
// 

angular.module('angular-webmethods', [])
// angular.module('wm', ['$http', '$location'])


    // .constant('MODULE_VERSION', 'bootstrap.bootstrap.1')

    .factory('wm', ['$http', '$location', function($http, $location) {

	    var wm = {};
	    
	    wm.invoke = function(optionsOrService, pipeline) {

	        // If short form wm.invoke(service, pipeline) was used, then re-invoke same function
	        // using long form wm.invoke(options)
	        if (typeof optionsOrService === 'string') {
	            return wm.invoke({
	                service: optionsOrService,
	                pipeline: pipeline
	            })
	        }

	        // Beyond this point, it we are only dealing with the wm.invoke(options) form
	        // So, we rename the variable to be more clear in the code below.
	        var options = optionsOrService;

	        console.log("Invoking Service2: " + options.service + " with pipeline: " + options.pipeline);

	        // Set a flag indicating whether we should use relative or absolute URIs.
	        // We use absolute URIs if the caller specified protocol, host, or port
	        // in the options. Otherwise, a relative URI is used (/invoke/...), which 
	        // will implicitly use whatever protocol, host, and port this page was 
	        // retrieved from.
	        var useAbsoluteURI = options.protocol !== undefined 
	            || options.host !== undefined
	            || options.port !== undefined;

	        // Set Options Defaults        
	        options.protocol = options.protocol || 'http';
	        options.host = options.host || $location.host();
	        options.port = (options.port || '5555') + ''; // +'' coersces numbers to strings
	        options.user = options.user || 'Administrator';
	        options.pass = options.pass || 'manage';
	        options.pipeline = options.pipeline || {};

	        // Construct http params object to pass in $http config
	        var httpParams = {};
	        httpParams.method = 'POST';

	        // If invoke is on same host (indicated by config.host not being specified), 
	        // then we only use a relative URI. If a host was specivied, then we
	        // construct the absolute URI with protocol://host:port
	        httpParams.url = '/invoke/' + options.service;
	        if (useAbsoluteURI) {
	            httpParams.url = options.protocol + "://" 
	                                    + options.host 
	                                    + ':' 
	                                    + options.port
	                                    + httpParams.url;
	        } 

	        // Here we need to foil undesirable IE caching of XMLHTTPRequests.
	        // To do this, we add a t parameter to the query string, with a value
	        // that will be different for every invocation.
	        // NOTE: This has the side effect of adding an additional t variable to the pipeline.
	        // TODO: Need to find a better way to do this, or make it an option
	        httpParams.params = { 't': new Date().getTime() }; 
	        httpParams.headers = { Accept: 'application/json' };
	        httpParams.data = options.pipeline;

	        console.log("http params", httpParams);

	        var promise = $http(httpParams);
	        return promise;
	    }

	    return wm;
	}]) // end of factory 'wm'

; // end of module definition

