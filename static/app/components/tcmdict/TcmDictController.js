'use strict';

angular.module('caseManagerApp.tcmdict', ['ngResource'])

  .controller('TcmDictController',
    function ($window, $resource, $mdDialog, $routeParams, $log, $mdToast) {

      // region local var
      var self = this;
      var tcmDictResource = $resource(CONF.URL.TCMDICT);

      var getAllTags = function () {
        $log.debug('getting all tags ');
        tcmDictResource.get({ allTags: true }).$promise.then(function (response) {
          self.tagMap = response;
          self.allTags = Object.keys(self.tagMap).map(function (key) {
            return self.tagMap[key];
          });
        })
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
      this.tagMap  = {};
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

      this.onTagWord = function (ev, word) {
        var tagName = this.allTags[this.selectedTabIndex].tagName;
        $log.debug('tagging ' + word.eng1 + ' ' + tagName);

        tcmDictResource.save({id: word.mid, tagName: tagName}, '').$promise.then(
          function (data) {
            showToast("tagged " + word.eng1 + " with " + tagName);
            self.tagMap[tagName].words.push(word);
          },
          function (data) {
            showToast("Failed to tag " + word.eng1 + " with " + tagName);
          }
        )
      };

      this.onUnTagWord = function (ev, word) {
        var tagName = this.allTags[this.selectedTabIndex].tagName;
        $log.debug('un-tagging ' + word.eng1 + ' ' + tagName);

        tcmDictResource.delete({id: word.mid, tagName: tagName}, '').$promise.then(
          function (data) {
            showToast("un-tagged " + word.eng1 + " from " + tagName);
            var wordList = self.tagMap[tagName].words;
            var index = wordList.indexOf(word);
            wordList.splice(index, 1);
          },
          function (data) {
            showToast("Failed to un-tag " + word.eng1 + " from " + tagName);
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
          if (self.tagMap.hasOwnProperty(tagName)) {
            showToast('Tag already exist!');

          } else {
            var newTag = { tagName: tagName, words: [] };
            self.tagMap[tagName] = newTag;
            self.allTags.push(newTag);
            self.selectedTabIndex = self.allTags.length - 1;
          }
        });
      }
    });
