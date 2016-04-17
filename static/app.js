'use strict';

/* App Module */

var caseManagerApp = angular.module('caseManagerApp', [
  'ngRoute',
  'ngMaterial',
  'md.data.table',
  'caseManagerApp.patients',
  'caseManagerApp.consults'
]);

caseManagerApp.config(
  function ($routeProvider, $mdThemingProvider) {
    $mdThemingProvider.theme('default')
      .primaryPalette('blue')
      .accentPalette('pink');

    $routeProvider
      .when('/manage-patients', {
        templateUrl: 'app/components/patients/patients.html',
        controller: 'PatientController as patientCtrl'
      })
      .when('/manage-consults/:patientId', {
        templateUrl: 'app/components/consults/consults.html',
        controller: 'ConsultController as consultCtrl'
      })
      .otherwise({redirectTo: '/manage-patients'});
  });
