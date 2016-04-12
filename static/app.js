'use strict';

/* App Module */

var caseManagerApp = angular.module('caseManagerApp', [
  'ngRoute',
  'ngMaterial',
  'md.data.table',
  'caseManagerApp.searchDoc',
  'caseManagerApp.manageCase'
]);

caseManagerApp.config(
  function ($routeProvider, $mdThemingProvider) {
    $mdThemingProvider.theme('default')
      .primaryPalette('blue')
      .accentPalette('pink');

    $routeProvider
      .when('/patients', {
        templateUrl: 'app/components/patients/patients.html',
        controller: 'PatientsController as patientsCtrl'
      })
      .when('/consultations', {
        templateUrl: 'app/components/consultations/consults.html',
        controller: 'ConsultsController as consultsCtrl'
      })
      .when('/searches', {
        templateUrl: 'app/components/searches/search.html',
        controller: 'SearchController as searchCtrl'
      })
      .otherwise({redirectTo: '/patients'});
  });
