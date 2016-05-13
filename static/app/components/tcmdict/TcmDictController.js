'use strict';

angular.module('caseManagerApp.tcmdict', ['ngResource'])

  .controller('TcmDictController',
    function ($window, $resource, $mdDialog, $routeParams, $log, $mdToast) {

      // region local var
      var self = this;
      var tcmDictResource = $resource(CONF.URL.TCMDICT);
      var zhEnWordsResource = $resource(CONF.URL.ZH_EN_WORDS);

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
      // endregion local var

      // region xlsx import
      var drop = document.getElementById('drop');
      function handleDrop(e) {
        self.loadingXlsx = true;
        e.stopPropagation();
        e.preventDefault();
        var files = e.dataTransfer.files;
        var f = files[0];
        var reader = new FileReader();
        var fileName = f.name;
        reader.onload = function(e) {
          console.log("xlsx onload ", new Date());
          var data = e.target.result;
          var wb;
          wb = XLSX.read(data, {type: 'binary'});
          process_wb(wb, fileName);
        };
        reader.readAsBinaryString(f);
      }

      function process_wb(workbook, fileName) {
        var wbData = {};
        workbook.SheetNames.forEach(function(sheetName) {
          var rows = XLSX.utils.sheet_to_row_object_array(workbook.Sheets[sheetName]);

          var nonEmptyRows = [];
          if(rows.length > 0){
            for (var i=0; i<rows.length; i++) {
              var row = rows[i];
              for (var fieldName in row) {
                // removed columns which has no header
                if (fieldName === "undefined") {
                  delete row[fieldName];
                }
              }
              if (Object.keys(row).length > 0) {
                nonEmptyRows.push(row);
              }
            }
            wbData[sheetName] = nonEmptyRows;
          }
        });

        // var formatted = JSON.stringify(wbData, 2, 2);
        console.log("xslx output: ", wbData);
        zhEnWordsResource.save(wbData).$promise.then(
          function () {
            showToast("Successfully imported " + fileName);
            self.loadingXlsx = false;

          },
          function () {
            showToast("FAILED to import " + fileName);
            self.loadingXlsx = false;
          }
        );
      }

      function handleDragover(e) {
        e.stopPropagation();
        e.preventDefault();
        e.dataTransfer.dropEffect = 'copy';
      }

      if(drop.addEventListener) {
        drop.addEventListener('dragenter', handleDragover, false);
        drop.addEventListener('dragover', handleDragover, false);
        drop.addEventListener('drop', handleDrop, false);
      }
      // endregion xlsx import

      
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

      getAllTags();
      
      // region scope var
      this.loadingXlsx = false;

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

      this.searchTerm = null;
      this.searchPageSize = 10;
      this.tcmDictSearchResult = [];
      this.selectedTabIndex = 0;
      
      this.onSearchDict = function (ev) {
        $log.debug('received search term: ' + this.searchTerm);
        this.tcmDictSearchResult = tcmDictResource.query({ q: this.searchTerm, p: this.searchPageSize });
      };
      
      this.onClearForm = function (ev) {
        this.searchTerm = null;
        this.searchPageSize = 10;
        this.tcmDictSearchResult = [];
      };

      this.onTagWord = function (ev, word) {
        var tagName = this.allTags[this.selectedTabIndex].tagName;
        $log.debug('tagging ' + word.eng1 + ' ' + tagName);

        tcmDictResource.save({id: word.mid, tagName: tagName}, '').$promise.then(
          function (data) {
            showToast("added '" + word.eng1 + "' to category " + tagName);
            self.tagMap[tagName].words.push(word);
          },
          function (data) {
            showToast("Failed to add '" + word.eng1 + "' to category " + tagName);
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
          tagName = tagName.toUpperCase();
          if (self.tagMap.hasOwnProperty(tagName)) {
            showToast('Tag already exist!');

          } else {
            var newTag = { tagName: tagName, words: [] };
            self.tagMap[tagName] = newTag;
            self.allTags.push(newTag);
            self.selectedTabIndex = self.allTags.length - 1;
          }
        });
      };
      // endregion scope var
    });
