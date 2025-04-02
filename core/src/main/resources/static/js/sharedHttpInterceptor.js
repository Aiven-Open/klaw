'use strict';

var app = angular.module('sharedHttpInterceptor', []);

app.config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push(['$q', function($q) {
        return {
            request: function(config) {
                config.headers['X-XSRF-TOKEN'] = document.cookie.match(/XSRF-TOKEN=([^;]+)/)?.[1] || '';
                return config;
            },
            responseError: function(rejection) {
                console.error('HTTP Request Error:', rejection);
                return $q.reject(rejection);
            }
        };
    }]);
}]);
