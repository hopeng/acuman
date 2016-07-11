'use strict';

/* App Module */

var caseManagerApp = angular.module('caseManagerApp', [
  'ngRoute',
  'ngMaterial',
  'md.data.table',
  'caseManagerApp.common',
  'caseManagerApp.home',
  'caseManagerApp.patients',
  'caseManagerApp.consults',
  'caseManagerApp.tcmdict',
  'caseManagerApp.profile'
]);

caseManagerApp.config(
  function ($routeProvider, $mdThemingProvider, $logProvider, $locationProvider) {
    var isProd = false; // todo get this from machine env variables
    $logProvider.debugEnabled(!isProd);

    $mdThemingProvider.theme('default')
      .primaryPalette('blue')
      .accentPalette('light-blue');

    $routeProvider
      .when('/home', {
        templateUrl: 'app/components/home/home.html',
        controller: 'HomeController as homeCtrl'
      })
      .when('/patients', {
        templateUrl: 'app/components/patients/patients.html',
        controller: 'PatientController as patientCtrl'
      })
      .when('/consults/:patientId', {
        templateUrl: 'app/components/consults/consults.html',
        controller: 'ConsultController as consultCtrl'
      })
      .when('/tcmdict', {
        templateUrl: 'app/components/tcmdict/tcmdict.html',
        controller: 'TcmDictController as tcmDictCtrl'
      })
      .when('/profile', {
        templateUrl: 'app/components/profile/profile.html',
        controller: 'ProfileController as profileCtrl'
      })
      .otherwise({redirectTo: '/home'});
  });
