'use strict';

angular.module('caseManagerApp.patients', ['ngResource'])

  .controller('PatientController',
    function ($window, $resource) {

      var self = this;
      this.newCaseInProgress = false;
      this.newCaseResult = null;
      this.upSertMode = false;

      var resetCurrent = function () {
        self.currentPatient = {};
        self.currentPatient.initialVisit = new Date();
      };

      resetCurrent();

      this.onNewPatient = function (event) {
        this.upSertMode = true;
        resetCurrent();
      };

      this.onClearPatientForm = function (event) {
        resetCurrent();
      };
      
      this.onCancelPatientForm = function (event) {
        this.upSertMode = false;
        resetCurrent();
      };
      
      this.onSubmitPatient = function (event) {
        console.log("onSubmitPatient", this.currentPatient);
        this.upSertMode = false;
        resetCurrent();
      };

    });
