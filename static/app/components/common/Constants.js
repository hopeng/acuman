'use strict';

var APP_HOST = 'https://api.etcm.com.au/';
// var APP_HOST = ''; // uncomment this for running local instance

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
