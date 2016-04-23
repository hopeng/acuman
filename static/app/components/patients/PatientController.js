'use strict';

angular.module('caseManagerApp.patients', ['ngResource'])

  .controller('PatientController',
    function ($resource, $mdMedia, $mdDialog) {

      var self = this;
      this.newCaseInProgress = false;
      this.newCaseResult = null;
      this.upSertMode = false;

      var patientUpdator = $resource(CONF.URL.PATIENTS + '/:id', null, { 'update': { method:'PUT' } });
      var patientResource = $resource(CONF.URL.PATIENTS + '/:id');
      this.patientList = patientResource.query();

      this.upsertPatient = function (patient) {
        console.log("onSubmitPatient", patient);
        var patientId = patient.patientId;
        if (patientId) {
          patientUpdator.update({ id: patientId }, patient);

        } else {
          patientResource.save(patient);
        }
      };

      this.deletePatient = function (patient) {
        var id = patient.patientId;
        patientResource.delete({ id: id});
      };

      this.onPatientEdit = function(ev, patient) {
        var useFullScreen = $mdMedia('sm') || $mdMedia('xs');
        $mdDialog.show({
          controller: EditPatientController,
          controllerAs: 'svc',
          templateUrl: 'app/components/patients/edit-patient.html',
          parent: angular.element(document.body),
          targetEvent: ev,
          fullscreen: useFullScreen,
          locals: {
            currentPatient: patient
          }
        })
          .then(function(response) {
            if (response.action === 'insert') {
              self.upsertPatient(response.patient);

            } else if (response.action === 'delete') {
              self.deletePatient(response.patient);

            } else {
              console.log('unknown action ', response.action, '. patient ', response.patient);
            }

          }, function() {
            // 'You cancelled the dialog.'
          });
      };

      function EditPatientController ($resource, $mdDialog, currentPatient) {
        var editCtrlSelf = this;
        editCtrlSelf.currentPatient = currentPatient;

        // sourced from http://www.privatehealth.gov.au/dynamic/healthfundlist.aspx, todo save in DB
        editCtrlSelf.healthFundList = $resource(CONF.URL.HEALTH_FUNDS).query();

        editCtrlSelf.onSubmitEditPatient = function() {
          $mdDialog.hide({ patient: editCtrlSelf.currentPatient, action: 'insert' });
        };

        editCtrlSelf.onCancelDialog = function() {
          $mdDialog.cancel();
        };
        
        editCtrlSelf.onClearPatientForm = function () {
          editCtrlSelf.currentPatient = {};
        };

        editCtrlSelf.onDeletePatient = function () {
          $mdDialog.hide({ patient: editCtrlSelf.currentPatient, action: 'delete' });
        };
      }
    });
