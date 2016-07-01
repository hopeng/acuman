'use strict';

angular.module('caseManagerApp.common', ['ngResource'])
  .service('AuthService', function ($http) {
    var self = this;

    self.authenticated = false;

    self.getUser = function (x) {
      return null;
    };

    $http.get("/user").success(function(data) {
      self.user = data.userAuthentication.details.name;
      self.authenticated = true;
    }).error(function() {
      self.user = "N/A";
      self.authenticated = false;
    });

    self.logout = function() {
      $http.post('logout', {}).success(function() {
        self.authenticated = false;
        $location.path("/");
      }).error(function(data) {
        console.log("Logout failed")
        self.authenticated = false;
      });
    };


  });
