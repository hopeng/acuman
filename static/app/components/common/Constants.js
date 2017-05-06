'use strict';

var APP_HOST = 'http://acuman-lb-304446328.us-east-1.elb.amazonaws.com/';

var CONF = {
  URL: {
    HEALTH_FUNDS: 'app/reference_data/health-funds.json',
    PATIENTS: APP_HOST + 'v1/patients/:id',
    CONSULTS: APP_HOST + 'v1/consults/:id',

    // @deprecated
    TCMDICT: APP_HOST + 'v1/tcmdict/:id',
    ZH_EN_WORDS: APP_HOST + 'v1/tcm-zh-en-words'
  }
};
