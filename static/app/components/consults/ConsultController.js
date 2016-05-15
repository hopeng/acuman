'use strict';

angular.module('caseManagerApp.consults', ['ngResource'])

  .controller('ConsultController',
    function ($window, $resource, $mdDialog, $routeParams, $log, $mdToast, $timeout, $mdSidenav, $filter) {

      // region local var
      var self = this;
      var currentPatientId = $routeParams.patientId;

      var patientResource = $resource(CONF.URL.PATIENTS);
      var consultUpdator = $resource(CONF.URL.CONSULTS, null, {'update': {method: 'PUT'}});
      var consultResource = $resource(CONF.URL.CONSULTS);

      function upsertConsult (consult) {
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
      }

      function deleteConsult (consult) {
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
      }

      function showToast (message) {  // todo merge the same method in other service
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
      }

      var zhEnWordsResource = $resource(CONF.URL.ZH_EN_WORDS);
      var wordParentStacks = [];

      function getAllTags () {
        $log.debug('getting all tags');
        zhEnWordsResource.get().$promise.then(function (response) {
          $log.debug('received wordTree ' + response);
          var rootUiWordNode = response;
          for (var i=0; i<rootUiWordNode.children.length; i++) {
            var topLevelChild = rootUiWordNode.children[i];
            // each tag is an instance of UiWordNode with some children
            self.allTags.push(topLevelChild);
          }
          wordParentStacks = [];
          for (var i=0; i<self.allTags.length; i++) {
            // each tag has its own stack to keep track of expanded words, initialized as separate empty array
            wordParentStacks.push([]);
          }
        })
      }

      var editedConsultIndex = -1;
      var oldConsultRecord = {};
      
      getAllTags();
      // endregion local var


      // region scope var
      this.editedFieldLabel = null;
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

      this.onConsultEdit = function (ev, consult, index) {
        var isInsertion = !consult || !consult.consultId;
        if (isInsertion) {
          // init the record with default value
          consult = { visitedOn: new Date() };
        }

        this.currentConsult = consult;
        this.upserting = true;
        editedConsultIndex = index;
        oldConsultRecord = angular.copy(consult);
      };

      this.onSubmitConsult = function (ev, form) {
        if (!form.$valid) {
          return;
        }
        upsertConsult(this.currentConsult);
        this.upserting = false;
      };

      this.onCancelConsultForm = function (ev) {
        this.consultsList[editedConsultIndex] = oldConsultRecord;
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

      this.tagsToShow = [];

      this.filterTags = function (tag) {
        return !self.tagsToShow.length || self.tagsToShow.indexOf(tag.cs) >= 0;
      };

      // todo consult fiona this map 
      var consultFieldsMap = {
        chiefComplaint: [],
        medicalHistory: [],
        symptoms: [],
        pulse: [],
        tongue: [],
        tcmDiagnosis: [],
        tcmSyndromeDiff: [],
        westernMedicineDiagnosis: ['症状', '诊断', '治法', '穴位', '中药'],
        principleOfTreatment: [],
        treatment: [],
        acupoints: ['穴位'],
        herbs: ['中药'],
        advice: []
      };

      this.editedInputName = null;
      this.onOpenSearchDictPane = function (ev, editedInputName, editedFieldLabel) {
        this.editedInputName = editedInputName;
        this.editedFieldLabel = editedFieldLabel;
        this.tagsToShow = consultFieldsMap[editedInputName];
        $mdSidenav('searchDictPane').open();
      };

      this.onClickWord = function (word) {
        appendToInput(word);
      };

      this.allTags = [];

      this.onBackoutWordExpand = function () {
        var previousTag = wordParentStacks[self.selectedTabIndex].pop();
        if (previousTag) {
          self.allTags[self.selectedTabIndex] = previousTag;
        }
      };
      
      this.getPreviousTag = function () {
        return util.lastArrayElement(wordParentStacks[self.selectedTabIndex]);
      };

      this.onExpandWord = function (ev, word) {
        var selectedTag = self.allTags[self.selectedTabIndex];
        wordParentStacks[self.selectedTabIndex].push(selectedTag); // save the current tag for backout later
        self.allTags[self.selectedTabIndex] = word; // replace selectedTab with selected word and its children 
      };
      
      function appendToInput (word) {
        var appendedContent = '';
        if (self.currentConsult[self.editedInputName]) {
          appendedContent += '\n';
        }
        appendedContent += word.cs + ' ' + word.eng1;
        if (!self.currentConsult[self.editedInputName]) {
          self.currentConsult[self.editedInputName] = '';
        }
        self.currentConsult[self.editedInputName] += appendedContent;
        showToast('Appended "' + appendedContent);
      }

      this.searchKeyword = '';
      this.filterMode = false;
      this.onToggleFilterConsults = function () {
        this.filterMode = !this.filterMode;
        if (!this.filterMode) {
          this.searchKeyword = '';
        }
      };

      this.filterConsults = function (consult) {
        var visitedOnString = $filter('date')(consult.visitedOn, 'dd/MM/yyyy');
        var visitDateMatched = visitedOnString.indexOf(self.searchKeyword) >= 0;;
        return visitDateMatched;
      };
      // endregion scope var
    });
