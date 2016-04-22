'use strict';

angular.module('caseManagerApp.patients', ['ngResource'])

  .controller('PatientController',
    function ($resource) {

      var self = this;
      this.newCaseInProgress = false;
      this.newCaseResult = null;
      this.upSertMode = false;

      var patientUpdator = $resource(CONF.URL.PATIENTS + '/:id', null, { 'update': { method:'PUT' } });
      var patientResource = $resource(CONF.URL.PATIENTS + '/:id');

      // source from http://www.privatehealth.gov.au/dynamic/healthfundlist.aspx, todo save in DB
      this.healthFundList = $resource(CONF.URL.HEALTH_FUNDS).query();
      this.patientList = patientResource.query();

      var resetCurrent = function () {
        self.currentPatient = {};
        // self.currentPatient.initialVisit = new Date();
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
        var id = this.currentPatient.patientId;
        if (id) {
          patientUpdator.update({ id: id }, this.currentPatient);

        } else {
          patientResource.save(this.currentPatient);
        }
        this.upSertMode = false;
        resetCurrent();
      };

      this.onEditPatient = function (p) {
        this.upSertMode = true;
        this.currentPatient = p;
      };

      this.onDeletePatient = function () {
        var id = this.currentPatient.patientId;
        patientResource.delete({ id: id});
      }
    });
