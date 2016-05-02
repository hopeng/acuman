'use strict';

angular.module('caseManagerApp.tcmdict', ['ngResource'])

  .controller('TcmDictController',
    function ($window, $resource, $mdDialog, $routeParams, $log, $mdToast) {

      // region local var
      var self = this;
      var tcmDictResource = $resource(CONF.URL.TCMDICT);

      var getAllTags = function () {
        $log.debug('getting all tags ');
        self.allTags = tcmDictResource.query({ allTags: true });
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

      this.allTags = [];
      getAllTags();

      this.searchTerm = null;
      this.searchPageSize = 10;
      this.tchDictSearchResult = [];
      this.selectedTabIndex = 0;
      
      this.onSearchDict = function (ev) {
        $log.debug('received search term: ' + this.searchTerm);
        this.tchDictSearchResult = tcmDictResource.query({ q: this.searchTerm, p: this.searchPageSize });
      };
      
      this.onClearForm = function (ev) {
        this.searchTerm = null;
        this.searchPageSize = 10;
        this.tchDictSearchResult = [];
      };

      this.onTagWord = function (ev, wordId) {
        var tagName = this.allTags[this.selectedTabIndex].tagName;
        $log.debug('tagging ' + wordId + ' ' + tagName);

        tcmDictResource.save({id: wordId, tagName: tagName}, '').$promise.then(
          function (data) {
            showToast("tagged " + tagName);
          },
          function (data) {
            showToast("failed to tag " + tagName);
          }
        )
      };

      this.onUntagWord = function (ev, word) {
        var tagName = this.allTags[this.selectedTabIndex].tagName;
        $log.debug('un-tagging ' + word + ' ' + tagName);

        tcmDictResource.delete({id: wordId, tagName: tagName}, '').$promise.then(
          function (data) {
            showToast("un-tagged " + tagName);
          },
          function (data) {
            showToast("failed to un-tag " + tagName);
          }
        )
      };

      this.onAddTag = function (ev) {
        var confirm = $mdDialog.prompt()
          .title('Please Enter New Tag Name')
          .textContent('')
          .placeholder('Description')
          .targetEvent(ev)
          .ok('OK')
          .cancel('CANCEL');

        $mdDialog.show(confirm).then(function(tagName) {
          //todo check for duplicate tag
          self.allTags.push({ tagName: tagName });
          self.selectedTabIndex = self.allTags.length - 1;
        });
        
        // todo save tag to DB
        
      }
    });
