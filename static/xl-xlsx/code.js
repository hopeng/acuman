var X = XLSX;

var drop = document.getElementById('drop');
function handleDrop(e) {
  e.stopPropagation();
  e.preventDefault();
  var files = e.dataTransfer.files;
  var f = files[0];
  {
    var reader = new FileReader();
    var name = f.name;
    reader.onload = function(e) {
      console.log("onload", new Date());
      var data = e.target.result;
      var wb;
      wb = X.read(data, {type: 'binary'});
      process_wb(wb);
    };
    reader.readAsBinaryString(f);
  }
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


var xlf = document.getElementById('xlf');
function handleFile(e) {
  use_worker = false;
  var files = e.target.files;
  var f = files[0];
  {
    var reader = new FileReader();
    var name = f.name;
    reader.onload = function(e) {
      console.log("onload", new Date());
      var data = e.target.result;
      var wb;
      wb = X.read(data, {type: 'binary'});
      process_wb(wb);
    };
    reader.readAsBinaryString(f);    
  }
}

function process_wb(workbook) {
  var rows = {};
  workbook.SheetNames.forEach(function(sheetName) {
    var roa = X.utils.sheet_to_row_object_array(workbook.Sheets[sheetName]);
    if(roa.length > 0){
      rows[sheetName] = roa;
    }
  });

  var output = "";
  output = JSON.stringify(rows, 2, 2);
  out.innerText = output;
  console.log("output", new Date());
}


xlf.addEventListener('change', handleFile, false);
