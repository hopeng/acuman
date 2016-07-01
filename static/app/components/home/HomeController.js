'use strict';

angular.module('caseManagerApp.home', ['ngResource'])

  .controller('HomeController',
    function ($resource, $mdMedia, $mdDialog, $mdToast, $filter, $log, AuthService) {
      var self = this;
      
      this.authenticated = AuthService.authenticated;

    });
