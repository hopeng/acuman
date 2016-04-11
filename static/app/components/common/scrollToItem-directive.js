'use strict';

// todo enhance this directive to be a html attribute which gets field name to be validated from parent element ng-model
angular.module('commonModule').directive('scrollToItem', function () {
  return {
    restrict: 'A',
    scope: {
      scrollTo: "@"
    },
    link: function (scope, $elm, attr) {

      $elm.on('click', function () {
        $('html,body').animate({scrollTop: $(scope.scrollTo).offset().top}, "slow");
      });
    }
  }
});
