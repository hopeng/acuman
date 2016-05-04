'use strict';

angular.module('caseManagerApp.consults', ['ngResource', 'mentio'])

  .controller('ConsultController',
    function ($window, $resource, $mdDialog, $routeParams, $log, $mdToast, $timeout, $mdSidenav) {

      // region local var
      var self = this;
      var currentPatientId = $routeParams.patientId;

      var patientResource = $resource(CONF.URL.PATIENTS);
      var consultUpdator = $resource(CONF.URL.CONSULTS, null, {'update': {method: 'PUT'}});
      var consultResource = $resource(CONF.URL.CONSULTS);

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
              showToast('Successfully created new consultation ' + response.consultId);
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
      // endregion local var


      // region scope var
      this.upserting = false;
      this.currentConsult = null;
      this.patient = patientResource.get({ id: currentPatientId });
      this.consultsList = [];
      this.consultsListPromise = consultResource.query({ patientId: currentPatientId }).$promise;
      this.consultsListPromise.then(function (data) {
        for (var i=0; i<data.length; i++) {
          util.convertStringFieldToDate(data[i], 'visitedOn');
        }
        self.consultsList = data;
      });

      this.onConsultEdit = function (ev, consult) {
        var isInsertion = !consult || !consult.consultId;
        if (isInsertion) {
          // init the record with default value
          consult = { visitedOn: new Date() };
        }

        this.currentConsult = consult;
        this.upserting = true;
      };

      this.onSubmitConsult = function (ev, form) {
        if (!form.$valid) {
          return;
        }
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
          .ok('DELETE CONSULTATION')
          .cancel('CANCEL');

        $mdDialog.show(confirm).then(
          function () {
            deleteConsult(self.currentConsult);
            self.upserting = false;
          });
      };

      //mentio specific clean up later
      this.words = [];
      var queryDebounce = $timeout(function(){});
      var queryDebounceTime = 1500;

      var tcmDictSearchResource = $resource('/v1/tcm-words');

      this.lookupWord = function(term) {
        $log.debug("lookupWord called");

        $timeout.cancel(queryDebounce); //cancel the last timeout
        queryDebounce = $timeout(function() {
          tcmDictSearchResource.query({ q: term }).$promise.then(function (response) {
            self.words = response;
          })
        }, queryDebounceTime);
      };

      this.displayWord = function(item) {
        return item.cc + ' | ' + item.sc + ' | ' + item.eng1;
      };
      //mentio specific clean up later

      this.editedInputName = null;
      this.onOpenSearchDictPane = function (ev, editedInputName) {
        this.editedInputName = editedInputName;
        $mdSidenav('searchDictPane').open();
      };
      this.onCloseSearchDictPane = function (ev, editedInputName) {
        this.editedInputName = editedInputName;
        this.tchDictSearchResult = [];
        
        $mdSidenav('searchDictPane').close();
      };

      this.appendToInput = function (word) {
        var appendedContent = '';
        if (this.currentConsult[this.editedInputName]) {
            appendedContent += '\n';
        }
        appendedContent += word.cs + ' ' + word.eng1;
        this.currentConsult[this.editedInputName] += appendedContent;
      };

      this.allTags = [];
      var getAllTags = function () {
        // todo move to a service and share
        var tcmDictResource = $resource(CONF.URL.TCMDICT);
        tcmDictResource.get({ allTags: true }).$promise.then(function (response) {
          self.allTags = Object.keys(response).map(function (key) {
            return response[key];
          });
        });
      };
      getAllTags();

      // endregion scope var
    });
