'use strict';

angular.module('caseManagerApp.consults', ['ngResource'])

  .controller('ConsultController',
    function ($window, $resource, $mdDialog, $routeParams) {

      var self = this;
      var patientId = $routeParams.patientId;

      var patientResource = $resource(CONF.URL.PATIENTS + '/:id');

      this.patient = patientResource.get({ id: patientId });
      
      this.consultsList = [];
      
      

    });
