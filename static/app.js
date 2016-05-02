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
  function ($routeProvider, $mdThemingProvider, $logProvider) {
    var isProd = false; // todo get this from machine env variables
    $logProvider.debugEnabled(!isProd);
    
    $mdThemingProvider.theme('default')
      .primaryPalette('blue')
      .accentPalette('pink');

    $routeProvider
      .when('/patients', {
        templateUrl: 'app/components/patients/patients.html',
        controller: 'PatientController as patientCtrl'
      })
      .when('/consults/:patientId', {
        templateUrl: 'app/components/consults/consults.html',
        controller: 'ConsultController as consultCtrl'
      })
      .when('/consults/:patientId/:consultId', {
        templateUrl: 'app/components/consults/edit-consults.html',
        controller: 'EditConsultController as editConsultCtrl'
      })
      .otherwise({redirectTo: '/patients'});
  });
