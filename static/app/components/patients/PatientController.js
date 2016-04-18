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

      // this.onNewPatient = function(ev) {
      //   // Appending dialog to document.body to cover sidenav in docs app
      //   var confirm = $mdDialog.prompt()
      //     .title('Please Describe Your Case')
      //     .textContent('')
      //     .placeholder('Description')
      //     .targetEvent(ev)
      //     .ok('OK')
      //     .cancel('CANCEL');
      //
      //   $mdDialog.show(confirm).then(function(description) {
      //     thisInstance.newCaseInProgress = true;
      //
      //     $resource(sprintf(CONF.URL.NEW_CASE_ID, description)).save().$promise.then(
      //       function (data) {
      //         thisInstance.newCaseInProgress = false;
      //         $window.location.href = '#/search-doc/' + data.caseId;
      //       },
      //       function (data) {
      //         thisInstance.newCaseInProgress = false;
      //         thisInstance.newCaseResult = data.data;
      //       }
      //     );
      //   });
      // };
      this.onNewPatient = function (event) {
        $mdDialog.show({
            controller: DialogController,
            template: '<md-dialog aria-label="Mango (Fruit)"> <md-content class="md-padding"> <form name="userForm"> <div layout layout-sm="column"> <md-input-container flex> <label>First Name</label> <input ng-model="user.firstName" placeholder="Placeholder text"> </md-input-container> <md-input-container flex> <label>Last Name</label> <input ng-model="theMax"> </md-input-container> </div> <md-input-container flex> <label>Address</label> <input ng-model="user.address"> </md-input-container> <div layout layout-sm="column"> <md-input-container flex> <label>City</label> <input ng-model="user.city"> </md-input-container> <md-input-container flex> <label>State</label> <input ng-model="user.state"> </md-input-container> <md-input-container flex> <label>Postal Code</label> <input ng-model="user.postalCode"> </md-input-container> </div> <md-input-container flex> <label>Biography</label> <textarea ng-model="user.biography" columns="1" md-maxlength="150"></textarea> </md-input-container> </form> </md-content> <div class="md-actions" layout="row"> <span flex></span> <md-button ng-click="answer(\'not useful\')"> Cancel </md-button> <md-button ng-click="answer(\'useful\')" class="md-primary"> Save </md-button> </div></md-dialog>',
            targetEvent: event,
          })
          .then(function(answer) {
            self.alert = 'You said the information was "' + answer + '".';
          }, function() {
            self.alert = 'You cancelled the dialog.';
          });
      };
    });

function DialogController($scope, $mdDialog) {
  $scope.hide = function() {
    $mdDialog.hide();
  };
  $scope.cancel = function() {
    $mdDialog.cancel();
  };
  $scope.answer = function(answer) {
    $mdDialog.hide(answer);
  };
};