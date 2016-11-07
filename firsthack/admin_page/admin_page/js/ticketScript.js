var allTicketList = [];
var lastClicked = "";
var secondLastClicked = "";
var difference = "";
var selectedCount = 0;
var tktSelected = [];
var clear = false;

$(document).ready(function() {
  populateVzIdsInFooter();

  $('#vzIdsInFooterVDSI').change(function() {
  $('#vzIdsInFooterOnSHORE').prop("disabled", true);
  });

  $('#vzIdsInFooterOnSHORE').change(function() {
  $('#vzIdsInFooterVDSI').prop("disabled", true);
  });

  $.ajaxSetup({ cache: false });
  $('.table').stickyhead({
  'css':{
    'background': 'gainsboro', 
    'text-align': 'left',
    'cursor': 'default',
    'color': 'black',
    'text-align': 'center',
    'font-family': 'helvetica, sans-serif'
  }
  })

  var wasUpdated = '';
  if (window.location.search != '') {
  wasUpdated = window.location.search.split("=")[1];
  }

  $('#updated_ticket_notice').hide();

  if (wasUpdated=="true") {
  $('#updated_ticket_notice').show();
  }

  $('#add_user').hide();
  $('#all_users').hide();
  $('#btn_ftp').hide();
  $('#ticketSelect').hide();

  $("#ticket_id_field").keyup(function(event){
  if(event.keyCode == 13){
    id = $('#ticket_id_field').val();
    if (id.length >= 7)
    getTicketInfo();
  }
  });

  var table = $("#ticketTable");
  table.clear;

  getTickets();
  
  
  "use strict";
  $.fn.multifilter = function(options) {
  var settings = $.extend( {
    'target'    : $("#ticketTable"),
    'method'    : 'thead' // This can be thead or class
  }, options);

  jQuery.expr[":"].Contains = function(a, i, m) {
    return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
  };
  var fooie = true;

  this.each(function() {
    var $this = $(this);
    var container = settings.target;
    var row_tag = 'tr';
    var item_tag = 'td';
    var rows = container.find($(row_tag));

    if (settings.method === 'thead') {
    // Match the data-col attribute to the text in the thead
    var col = container.find('th:Contains(' + $this.data('col') + ')');
    var col_index = container.find($('thead th')).index(col);
    };

    if (settings.method === 'class') {
    // Match the data-col attribute to the class on each column
    var col = rows.first().find('td.' + $this.data('col') + '');
    var col_index = rows.first().find($('td')).index(col);
    };

    $this.change(function() {
    var filter = $this.val();
    rows.each(function() {
      var row = $(this);
      var cell = $(row.children(item_tag)[col_index]);
      if (filter) {
      if (cell.text().toLowerCase().indexOf(filter.toLowerCase()) !== -1) {
        cell.attr('data-filtered', 'positive');
      } else {
        cell.attr('data-filtered', 'negative');
      }
      if (row.find(item_tag + "[data-filtered=negative]").size() > 0) {
         row.hide();
      } else {
        if (row.find(item_tag + "[data-filtered=positive]").size() > 0) {
        row.show();
        }
      }
      } else {
      cell.attr('data-filtered', 'positive');
      if (row.find(item_tag + "[data-filtered=negative]").size() > 0) {
        row.hide();
      } else {
        if (row.find(item_tag + "[data-filtered=positive]").size() > 0) {
        row.show();
        }
      }
      }
    });
    return false;
    }).keyup(function() {
    $this.change();
    updateTicketArray();
    });
  });
  };
});

function getFilterHtml() {
  $.ajax({
  url: '../../AdminPage/rest/servlet/getFilterHtml',
  type: "POST"
  }).done(function(filters) {
  if (filters != "1337") {
    $('.filterContainer').html(filters);
    updateTicketArray();
    setPage();
  }
  else {
    window.location = '/AdminPage/index.html';
  }
  }).fail(function(){
    alert("Something went wrong...");
  });
}

function getUserRole() {
  $.ajax({
  url: '../../AdminPage/rest/servlet/getRole'
  }).done(function(role) {
  if (role[0] == 'admin') {
    $('#add_user').show();
    $('#all_users').show();
    $('#btn_ftp').show();
  }
  else if (role[0] == "0") {
    window.location = '/AdminPage/index.html';
  }

  if (role[1] != "-1" && role[1] != "0") {
    $('#pending_title').html("<b>" + role[1] + " PENDING TICKETS</b>");
  }
  if (role[2] != "-1") {
    $('#user_name').html("Welcome: " + role[2]);
  }
  }).fail(function(){
    alert("Something went wrong...");
    document.title = "ROC Daily Report";
    document.body.style.cursor = 'default';
    table.show();
    $("#loadingdiv2").hide();
  });
}

function getTickets() {
  document.title = "loading...";
  document.body.style.cursor = 'progress';

  var table = $("#ticketTable");

  table.hide();
  $("#loadingdiv2").show();

  $.ajax({
  url: '../../AdminPage/rest/servlet/getTickets'
  }).done(function(tickets) {
  if (tickets != '1337') {
    table.append(tickets);
    first = "false";
    $('.filter').multifilter();
    
    document.title = "ROC Daily Report";
    document.body.style.cursor = 'default';
    table.show();
    $("#loadingdiv2").hide();
    getFilterHtml();
    updateTicketArray();
    getUserRole();
  }
  else {
    window.location = '/AdminPage/index.html';
  }
  }).fail(function(){
    alert("Something went wrong...");
    document.title = "ROC Daily Report";
    document.body.style.cursor = 'default';
    table.show();
    $("#loadingdiv2").hide();
  });
}

function getTicketInfo(id) {
  window.location = "/AdminPage/updateTicket.html?id=" + id;
}


function refreshTicketList() {
  $.ajax({
  url: '../../AdminPage/rest/servlet/refreshRocTickets'
  }).done(function(){
  getTickets();
  }).fail(function(){
    alert("Something went wrong...");
    document.title = "ROC Daily Report";
    document.body.style.cursor = 'default';
    table.show();
    $("#loadingdiv2").hide();
  });
}

function updateOnClick() {
  if (clear == false) {
  $('#selectTickets').html("Clear All");
    $('#ticketSelect').show();
    $('#ticketTable tbody').children().each(function() {
      $(this).attr("onclick", "highlight('"+$(this).attr("id")+"',1)");
    });
    clear = true;
  }
  else {
  // console.log(tktSelected);
  for (i=0; i <= tktSelected.length; i++) {
    highlight(tktSelected[i], 1337);
  }
  $('#selectTickets').html("Select Tickets");
  clear = false;
  }

} 

function highlight(rkTkt,id) {
  if ($("#" + rkTkt).attr("isclicked") == "false") {
    $("#" + rkTkt).css("background-color", "#ccc");
    selectedCount++;
    $("#"+rkTkt).attr("isclicked", "true");
    if(id == 1){
    tktSelected.push(rkTkt);
    secondLastClicked = lastClicked;
    lastClicked = allTicketList.indexOf(rkTkt);
    }
  }
  else if (id == 1337) {
  $('.ticket').css("background-color", "#fff");
  tktSelected = [];
  selectedCount = 0;
  }
  else {
  $("#" + rkTkt).css("background-color", "#fff");
  selectedCount--;
  $("#"+rkTkt).attr("isclicked", "false");
  tktSelected.pop(rkTkt);
  }
  $('#info').html("Assign " + selectedCount + " tickets to");
}

function updateTicketArray() {
  allTicketList = [];
  $('#ticketTable tbody').children().each(function() {
    if($(this).css("display") != "none"){
    allTicketList.push($(this).attr("id"));
    }
  });
  $('#pending_title').html("<b>" + allTicketList.length + " PENDING TICKETS</b>");
} 

function multiSelect(paramOne,paramTwo){
  for (i=paramOne+1; i<paramTwo; i++) {
    tktSelected.push(allTicketList[i]);
    highlight(allTicketList[i],0);
  }
}


$(document).click(function(e) {
  if (e.shiftKey) {
  shift = true;
  difference = (lastClicked-secondLastClicked); 
  endIndex = $('#ticketTable').index();
  if(difference < 0){
    multiSelect(lastClicked,secondLastClicked);
  } else {
    multiSelect(secondLastClicked,lastClicked);
  }
  // console.log(lastClicked + ", " + secondLastClicked);
  } 
}); 


function populateVzIdsInFooter() {
  $.ajax({
    url: '../../AdminPage/rest/servlet/getVzIdsForFooter',
    type: "POST"
  }).done(function(vzIds) {
    $('#vzIdsInFooterVDSI').html(vzIds[0]);
    $('#vzIdsInFooterOnSHORE').html(vzIds[1]);
  });
}


function manSign() {
  if ($('#vzIdsInFooterVDSI').val() == "foo")
    vzId = $('#vzIdsInFooterOnSHORE').val();
  else
    vzId = $('#vzIdsInFooterVDSI').val();

  if (vzId != "foo") {
    $.ajax({
      url: '../../AdminPage/rest/servlet/manualAssignment/' + vzId + "/" + tktSelected.toString(),
      type: "POST",
      beforeSend: function() {
        $('#makeAssingment').html("Reassigning...");
        $('#makeAssingment').prop("disabled", true);
      }
    }).done(function(data) {
      if (data == 'assigned') {
        alert(tktSelected.length + " tickets have been reassigned to " + vzId);
        location.reload();
      }
        else {
        alert('Something went wrong...');
      }
    });
  }
}