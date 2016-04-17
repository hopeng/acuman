'use strict';

angular.module('caseManagerApp.patients', ['ngResource'])

  .controller('PatientController',
    function ($window, $resource, $mdDialog) {

      var self = this;
      this.newCaseInProgress = false;
      this.newCaseResult = null;


      this.onRefresh = function() {
        this.caseList = $resource(CONF.URL.PATIENTS).query();
      };

      this.onRefresh();

      this.onNewPatient = function(ev) {
        // Appending dialog to document.body to cover sidenav in docs app
        var confirm = $mdDialog.prompt()
          .title('Please Describe Your Case')
          .textContent('')
          .placeholder('Description')
          .targetEvent(ev)
          .ok('OK')
          .cancel('CANCEL');

        $mdDialog.show(confirm).then(function(description) {
          thisInstance.newCaseInProgress = true;

          $resource(sprintf(CONF.URL.NEW_CASE_ID, description)).save().$promise.then(
            function (data) {
              thisInstance.newCaseInProgress = false;
              $window.location.href = '#/search-doc/' + data.caseId;
            },
            function (data) {
              thisInstance.newCaseInProgress = false;
              thisInstance.newCaseResult = data.data;
            }
          );
        });
      };
    });
