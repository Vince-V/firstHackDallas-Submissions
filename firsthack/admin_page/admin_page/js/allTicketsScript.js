$(document).ready(function() {
  // getFilterHtml();
  $.ajaxSetup({ cache: false });
  $("#loadingdiv2").hide();
  $('#adminList').hide();
  $('#ticketTable').hide();

  $('.table').stickyhead({
    'css':{
      'background': 'gainsboro', 
      'text-align': 'left',
      'cursor': 'default',
      'color': 'black',
      'text-align': 'center',
      'font-family': 'helvetica, sans-serif'
    }
  });

  "use strict";
  $.fn.multifilter = function(options) {
    var settings = $.extend( {
      'target'    : $("#ticketTable"),
      'method'    : 'thead' // This can be thead or class
    }, options);

    jQuery.expr[":"].Contains = function(a, i, m) {
      return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
    };

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
   getFilterHtml();
   getUserRole();
  // getTickets();
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

function getTickets() {
  document.title = "loading...";
  document.body.style.cursor = 'progress';
  
  var table = $("#tableBody");
  $('#ticketTable').hide();
  $("#loadingdiv2").show();
  // $('#tktCount').html("<b>0 PENDING TIKETS</b>");
  $.ajax({
    url: '../../AdminPage/rest/servlet/getAllTickets'
  }).done(function(tickets) {
    if (tickets != '1337') {
      table.html(tickets);
      first = "false";
      // $('.filter').multifilter();
      
      document.title = "ROC Daily Report";
      document.body.style.cursor = 'default';
      updateTicketArray();
      getFilterHtml();
      getUserRole();
      $("#loadingdiv2").hide();
      $('#ticketTable').show();
      // SortTable(6,'D', 'ymd');
    }
    else {
      window.location = '/AdminPage/oops.html';
    }
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
  });
}

function getUserRole() {
  $.ajax({
    url: '../../AdminPage/rest/servlet/getRole'
  }).done(function(role) {
    if (role[0] == 'admin') {
      $('#adminList').show();
    }
    else if (role[0] == "0") {
      window.location = '/AdminPage/index.html';
    }

    if (role[1] != "-1" && role[1] != "0") {
      // $('#pending_title').html("<b>" + role[1] + " PENDING TICKETS</b>");
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