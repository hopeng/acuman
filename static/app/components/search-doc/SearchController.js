'use strict';

angular.module('caseManagerApp.searchDoc', ['ngResource'])

  .controller('SearchController',
    function ($resource, $routeParams, $window, $filter) {
      var thisInstance = this;  // used in closures
      this.domains = $resource('app/reference_data/domain-filters.json').query();
      this.caseId = $routeParams.caseId;
      this.textOperatorDef = [
        {symbol: '=', code: ':'},
      ];

      this.rangeOperatorDef = [
        {symbol: '<', code: ' LT '},
        {symbol: '<=', code: ' LE '},
        {symbol: '>', code: ' GT '},
        {symbol: '>=', code: ' GE '},
        {symbol: '=', code: ':'},
        {symbol: 'between', code: 'BT'}
      ];

      this.resetBindings = function () {
        this.filterValues = [];
        this.searchResult = null;
        this.docContent = null;
        this.tagStates = {};
        this.selectedDomain = null;
        this.docUpdateInProgress = {};
        this.caseTagStates = {};
      };

      this.searchDocByCase = function () {
        this.resetBindings();
        this.docListInCase = $resource(sprintf(CONF.URL.SEARCH_DOC_BY_CASE, this.caseId)).query();
      };

      this.resetBindings();
      this.searchDocByCase();

      this.onSelect = function (domain) {
        console.log(domain);

        this.resetBindings();
        this.selectedDomain = domain;
        for (var i = 0; i < domain.filters.length; i++) {
          this.filterValues.push({
            name: domain.filters[i].name,
            type: domain.filters[i].type,
            operator: ':',
            operatorList: domain.filters[i].type === 'text' ? this.textOperatorDef : this.rangeOperatorDef,
            value: null,
            anotherValue: null
          });
        }
        console.log(this.filterValues);
      };

      this.onSearch = function () {
        var filterString = this.constructFilterString(this.filterValues);

        console.log("filterValues:", this.filterValues, ", filterString:", filterString);
        var url = sprintf(CONF.URL.SEARCH_DOC_BY_DOMAIN, this.selectedDomain.name, filterString);
        url = url.replace(/:/g, '\\:');
        this.searchResult = $resource(url).query();
      };

      this.constructFilterString = function(filterValues) {
        var filterString = '';
        for (var i=0; i<filterValues.length; i++) {
          var item = filterValues[i];
          if (item.type === 'date') {
            // format date value to DD/MM/YYYY
            item.value = item.value == null ? null : $filter('date')(new Date(item.value),'yyyy-MM-dd');
            item.anotherValue = item.anotherValue == null ? null : $filter('date')(new Date(item.anotherValue),'yyyy-MM-dd');
          }

          if (item.value) {
            if (item.operator === 'BT') {
              // range condition
              if (item.anotherValue) {
                filterString += sprintf('%s GE %s AND %s LE %s AND ', item.name, item.value, item.name, item.anotherValue);
              } else {
                console.log('cannot specify between with only one value');
              }

            } else {
              // non range condition
              if (item.name === 'keyword') {
                filterString += item.value + ' AND ';
              } else {
                filterString += sprintf('%s%s%s AND ', item.name, item.operator, item.value);
              }
            }
          }
        }
        filterString = filterString.substring(0, filterString.length - 5);
        return filterString;
      };

      this.onViewDoc = function (doc) {
        console.log(doc);

        $window.open('prettify-code.html?uri=' + encodeURIComponent(doc.uri), 'newwindow', 'width=640, height=800');
      };

      this.onViewWorm = function (doc) {
        console.log('onViewWorm', doc);

        $window.open('prettify-code.html?uri=' + encodeURIComponent(doc.uri) + '&storage=Worm', 'newwindow', 'width=640, height=800');
      };

      this.onTagDoc = function (doc, index, states) {
        console.log('tagging', doc);

        this.docUpdateInProgress[index] = true;
        $resource(sprintf(CONF.URL.TAG_DOC, doc.uri, this.caseId)).save()
          .$promise.then(
          function (data) {
            states[index] = 'Tagged';
            thisInstance.docUpdateInProgress[index] = false;
          },
          function (data) {
            states[index] = 'Failed';
            thisInstance.docUpdateInProgress[index] = false;
          }
        )
      };

      this.onUnTagDoc = function (doc, index, states) {
        console.log('un-tagging', doc);

        this.docUpdateInProgress[index] = true;
        $resource(sprintf(CONF.URL.UNTAG_DOC, doc.uri, this.caseId)).save()
          .$promise.then(
          function (data) {
            states[index] = 'Untagged';
            thisInstance.docUpdateInProgress[index] = false;
          },
          function (data) {
            states[index] = 'Failed';
            thisInstance.docUpdateInProgress[index] = false;
          }
        );
      };
    });
