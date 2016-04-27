'use strict';
// todo move toast function here

var util = {
  convertStringFieldToDate: function (object) {
    for (var i=1; i<arguments.length; i++) {
      if (object[arguments[i]]) {
        object[arguments[i]] = new Date(object[arguments[i]]);
      }
    }
  }
};