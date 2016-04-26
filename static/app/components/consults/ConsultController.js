'use strict';

angular.module('caseManagerApp.consults', ['ngResource'])

  .controller('ConsultController',
    function ($window, $resource, $mdDialog, $routeParams, $log, $mdToast) {

      var self = this;
      var currentPatientId = $routeParams.patientId;

      var patientResource = $resource(CONF.URL.PATIENTS + '/:id');
      var consultUpdator = $resource(CONF.URL.CONSULTS + '/:id', null, {'update': {method: 'PUT'}});
      var consultResource = $resource(CONF.URL.CONSULTS + '/:id');

      var upsertConsult = function (consult) {
        $log.debug('upserting cosultation: ', consult);

        var consultId = consult.consultId;
        if (consultId) {
          consultUpdator.update({id: consultId}, consult).$promise.then(
            function (response) {
              showToast('Successfully updated consultation ' + consultId);

            },
            function (response) {
              showToast('Failed to update consultation ' + consultId);
            }
          );

        } else {
          consultResource.save({patientId: currentPatientId}, consult).$promise.then(
            function (response) {
              self.consultsList.unshift(response);
              showToast('Successfully created new consultation ' + response.id);
            },
            function () {
              showToast('Failed to created new patient');
            }
          );
        }
      };

      var deleteConsult = function (consult) {
        $log.debug('deleting cosultation: ', consult);

        var consultId = consult.consultId;
        consultResource.delete({id: consultId}).$promise.then(
          function () {
            var index = self.consultsList.indexOf(consult);
            self.consultsList.splice(index, 1);
            showToast('Successfully deleted consultation ' + consultId);
          },
          function (response) {
            showToast('Failed to delete consultation ' + consultId);
          }
        );
      };

      var showToast = function (message) {  // todo merge the same method in other service
        $mdToast.hide().then(
          function () {
            var simpleToast = $mdToast.simple()
              .textContent(message)
              .action('OK')
              .position('top right')
              .hideDelay(5000);
            $mdToast.show(simpleToast);
          }
        );
      };

      this.upserting = false;
      this.currentConsult = {};

      this.patient = patientResource.get({ id: currentPatientId });
      
      this.consultsList = [];

      this.onConsultEdit = function (ev, consult) {
        this.currentConsult = consult;
        this.upserting = true;
      };

      this.onSubmitConsult = function (ev) {
        upsertConsult(this.currentConsult);
        this.upserting = false;
      };

      this.onCancelConsultForm = function (ev) {
        this.upserting = false;
      };

      this.onDeleteConsult = function (ev) {
        var confirm = $mdDialog.confirm()
          .title('Would you like to delete consultation ' + this.currentConsult.consultId + '?')
          .textContent('You cannot undo this action')
          .ariaLabel('Lucky day')
          .targetEvent(ev)
          .ok('DELETE THIS CONSULTATION')
          .cancel('CANCEL');

        $mdDialog.show(confirm).then(
          function () {
            deleteConsult(self.currentConsult);
            self.upserting = false;
          });
      };

    });
