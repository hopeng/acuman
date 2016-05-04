'use strict';

angular.module('caseManagerApp.patients', ['ngResource'])

  .controller('PatientController',
    function ($resource, $mdMedia, $mdDialog, $mdToast, $log) {

      var self = this;
      this.upserting = false;

      var patientUpdator = $resource(CONF.URL.PATIENTS, null, {'update': {method: 'PUT'}});
      var patientResource = $resource(CONF.URL.PATIENTS);
      this.patientList = [];
      this.patientListPromise = patientResource.query().$promise;
      this.patientListPromise.then(function (data) {
        for (var i=0; i<data.length; i++) {
          util.convertStringFieldToDate(data[i], 'dob');
        }
        self.patientList = data;
      });

      // sourced from http://www.privatehealth.gov.au/dynamic/healthfundlist.aspx, todo save in DB
      this.healthFundList = $resource(CONF.URL.HEALTH_FUNDS).query();

      //todo get user from server side
      this.doctor = 'Fiona Family TCM';

      this.onPatientEdit = function (ev, patient) {
        this.currentPatient = patient;
        this.upserting = true;
      };

      this.onSubmitPatient = function (ev) {
        upsertPatient(this.currentPatient);
        this.upserting = false;
      };

      this.onCancelPatientForm = function (ev) {
        this.upserting = false;
      };

      this.onDeletePatient = function (ev) {
        var confirm = $mdDialog.confirm()
          .title('Would you like to delete patient ' + this.currentPatient.patientId + '?')
          .textContent('You cannot undo this action')
          .ariaLabel('Lucky day')
          .targetEvent(ev)
          .ok('DELETE THIS PATIENT')
          .cancel('CANCEL');

        $mdDialog.show(confirm).then(
          function () {
            deletePatient(self.currentPatient);
            self.upserting = false;
          });
      };
      

      var showToast = function (message) {
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

      var upsertPatient = function (patient) {
        console.log("onSubmitPatient", patient);
        var patientId = patient.patientId;
        if (patientId) {
          patientUpdator.update({id: patientId}, patient).$promise.then(
            function (response) {
              showToast('Successfully updated patient ' + patientId);

            },
            function (response) {
              showToast('Failed to update patient ' + patientId);
            }
          );

        } else {
          patientResource.save(patient).$promise.then(
            function (response) {
              self.patientList.unshift(response);
              showToast('Successfully created new patient ' + response.id);
            },
            function () {
              showToast('Failed to created new patient');
            }
          );
        }
      };

      var deletePatient = function (patient) {
        var id = patient.patientId;
        patientResource.delete({id: id}).$promise.then(
          function () {
            var index = self.patientList.indexOf(patient);
            self.patientList.splice(index, 1);
            showToast('Successfully deleted patient ' + id);
          },
          function (response) {
            showToast('Failed to delete patient ' + id);
          }
        );
      };
    });
