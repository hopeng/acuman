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
      .when('/manage-case', {
        templateUrl: 'app/components/manage-case/manage-case.html',
        controller: 'CaseController as caseCtrl'
      })
      .when('/search-doc', {
        templateUrl: 'app/components/search-doc/search-doc.html',
        controller: 'SearchController as searchCtrl'
      })
      .when('/search-doc/:caseId', {
        templateUrl: 'app/components/search-doc/search-doc.html',
        controller: 'SearchController as searchCtrl'
      })
      .otherwise({redirectTo: '/manage-case'});
  });
