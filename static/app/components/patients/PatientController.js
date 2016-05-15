'use strict';

angular.module('caseManagerApp.patients', ['ngResource', 'focus-if'])

  .controller('PatientController',
    function ($resource, $mdMedia, $mdDialog, $mdToast, $filter, $log) {
      // region local var
      var self = this;

      var patientUpdator = $resource(CONF.URL.PATIENTS, null, {'update': {method: 'PUT'}});
      var patientResource = $resource(CONF.URL.PATIENTS);
      var editedPatientIndex = -1;
      var oldPatientRecord = {};

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
      // endregion local var

      
      // region scope var
      this.upserting = false;
      this.patientList = [];
      this.patientListPromise = patientResource.query().$promise;
      this.patientListPromise.then(function (data) {
        for (var i=0; i<data.length; i++) {
          util.convertStringFieldToDate(data[i], 'dob');
          self.patientList.push(data[i]);
        }
      });

      // sourced from http://www.privatehealth.gov.au/dynamic/healthfundlist.aspx, todo save in DB
      this.healthFundList = $resource(CONF.URL.HEALTH_FUNDS).query();

      //todo get user from server side
      this.doctor = 'Fiona Family TCM';

      this.onPatientEdit = function (ev, patient, index) {
        this.currentPatient = patient;
        this.upserting = true;
        editedPatientIndex = index;
        oldPatientRecord = angular.copy(patient);
      };

      this.onSubmitPatient = function (ev) {
        upsertPatient(this.currentPatient);
        this.upserting = false;
      };

      this.onCancelPatientForm = function (ev, patient) {
        this.patientList[editedPatientIndex] = oldPatientRecord;
        self.upserting = false;
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

      this.searchKeyword = '';
      this.filterMode = false;
      this.onToggleFilterPatient = function () {
        this.filterMode = !this.filterMode;
        if (!this.filterMode) {
          this.searchKeyword = '';
        }
      };
      
      this.filterPatients = function (patient) {
        var name = patient.firstName + ' ' + patient.lastName;
        var nameMatched = name.toLowerCase().indexOf(self.searchKeyword.toLowerCase()) >= 0;
        var dobString = $filter('date')(patient.dob, 'dd/MM/yyyy');
        var dobMatched = dobString && dobString.indexOf(self.searchKeyword) >= 0;
        var phoneMatched = patient.phone && patient.phone.indexOf(self.searchKeyword) >= 0;

        return dobMatched || nameMatched || phoneMatched;
      };

      // endregion scope var

    });
