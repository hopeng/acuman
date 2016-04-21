'use strict';

angular.module('caseManagerApp.patients', ['ngResource'])

  .controller('PatientController',
    function ($resource) {

      var self = this;
      this.newCaseInProgress = false;
      this.newCaseResult = null;
      this.upSertMode = false;
      // source from http://www.privatehealth.gov.au/dynamic/healthfundlist.aspx, todo save in DB
      this.healthFundList = [
        {code: 'ACA', desc: 'ACA Health Benefits Fund'},
        {code: 'AHM', desc: 'ahm Health Insurance'},
        {code: 'AUF', desc: 'Australian Unity Health Limited'},
        {code: 'BUP', desc: 'Bupa Australia Pty Ltd'},
        {code: 'CBH', desc: 'CBHS Health Fund Limited'},
        {code: 'CDH', desc: 'CDH Benefits Fund'},
        {code: 'CPS', desc: 'CUA Health Limited'},
        {code: 'AHB', desc: 'Defence Health Limited'},
        {code: 'AMA', desc: 'Doctors\' Health Fund'},
        {code: 'GMF', desc: 'GMF Health'},
        {code: 'GMH', desc: 'GMHBA Limited'},
        {code: 'FAI', desc: 'Grand United Corporate Health'},
        {code: 'HBF', desc: 'HBF Health Limited'},
        {code: 'HCF', desc: 'HCF'},
        {code: 'HCI', desc: 'Health Care Insurance Limited'},
        {code: 'HIF', desc: 'Health Insurance Fund of Australia Limited (HIF)'},
        {code: 'SPS', desc: 'Health Partners'},
        {code: 'HEA', desc: 'health.com.au'},
        {code: 'LHS', desc: 'Latrobe Health Services'},
        {code: 'MBP', desc: 'Medibank Private Limited'},
        {code: 'MDH', desc: 'Mildura Health Fund'},
        {code: 'OMF', desc: 'National Health Benefits Australia Pty Ltd (onemedifund)'},
        {code: 'NHB', desc: 'Navy Health Ltd'},
        {code: 'NIB', desc: 'NIB Health Funds Ltd.'},
        {code: 'LHM', desc: 'Peoplecare Health Insurance'},
        {code: 'PWA', desc: 'Phoenix Health Fund Limited'},
        {code: 'SPE', desc: 'Police Health'},
        {code: 'QCH', desc: 'Queensland Country Health Fund Ltd'},
        {code: 'RTE', desc: 'Railway and Transport Health Fund Limited'},
        {code: 'RBH', desc: 'Reserve Bank Health Society Ltd'},
        {code: 'SLM', desc: 'St.Lukes Health'},
        {code: 'NTF', desc: 'Teachers Health Fund'},
        {code: 'TFS', desc: 'Transport Health Pty Ltd'},
        {code: 'QTU', desc: 'TUH'},
        {code: 'WFD', desc: 'Westfund Limited'}
      ];

      var resetCurrent = function () {
        self.currentPatient = {};
        self.currentPatient.initialVisit = new Date();
      };

      resetCurrent();

      this.onNewPatient = function (event) {
        this.upSertMode = true;
        resetCurrent();
      };

      this.onClearPatientForm = function (event) {
        resetCurrent();
      };
      
      this.onCancelPatientForm = function (event) {
        this.upSertMode = false;
        resetCurrent();
      };
      
      this.onSubmitPatient = function (event) {
        console.log("onSubmitPatient", this.currentPatient);
        $resource(CONF.URL.PATIENTS).save(this.currentPatient);
        this.upSertMode = false;
        resetCurrent();
      };

    });
