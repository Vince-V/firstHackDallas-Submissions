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
  $('#ticketSelect').hide();
});

function updateOnClick() {
  console.log('here');
  if (clear == false) {
    $('#selectTickets').html("Clear All");
    $('#ticketSelect').show();
    $('#ticketTable tbody').children().each(function() {
        $(this).attr("onclick", "highlight('"+$(this).attr("id")+"',1)");
    });
    clear = true;
  }
  else {
    console.log(tktSelected);
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
        console.log("updating last and first: " + lastClicked + ", " + secondLastClicked);

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

  $('#tktCount').html("<b>" + allTicketList.length + " PENDING TICKETS</b>");
} 

function multiSelect(paramOne,paramTwo){
  for (i=paramOne+1; i<paramTwo; i++) {
      tktSelected.push(allTicketList[i]);
      highlight(allTicketList[i],0);
  }
}


$(document).click(function(e) {
  if (e.shiftKey) {
    console.log(lastClicked + ","+ secondLastClicked + ", " + (lastClicked-secondLastClicked));
    shift = true;
    difference = (lastClicked-secondLastClicked); 
    if(difference < 0){
        multiSelect(lastClicked,secondLastClicked);
    } else {
        multiSelect(secondLastClicked,lastClicked);
    }
    console.log(lastClicked + ", " + secondLastClicked);
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
  var vzId;
  if ($('#vzIdsInFooterVDSI').val() == "foo")
    vzId = $('#vzIdsInFooterOnSHORE').val();
  else
    vzId = $('#vzIdsInFooterVDSI').val();
  
  if (vzId != "foo") {
    $.ajax({
        url: '../../AdminPage/rest/servlet/manualAssignment/' + vzId + "/" + tktSelected.toString(),
        type: "POST",
        beforeSend: function() {
          $('#ticketTable').hide();
          $('#loadingdiv2').show();
          $('#makeAssingment').html("Reassigning...");
          $('#makeAssingment').prop("disabled", true);
        }
    }).done(function(data) {
        alert(tktSelected.length + " tickets have been reassigned to " + vzId);
        location.reload();
    });
  }
}

function getAllTickets() {
  $.ajax({
    url: '../../AdminPage/rest/servlet/allUnassignedTickets',
    type: "POST",
    beforeSend: function() {
      $('#loadingdiv2').show();
      $('#ticketTable').hide();
      $('#allBtn').prop("disabled", true);
    }
  }).done(function(tickets) {
    if (null != tickets) {
      $('#tableBody').html(tickets);
      $('.filter').multifilter();
      $('#allBtn').prop("disabled", false);
      $('#loadingdiv2').hide();
      $('#ticketTable').show();
      updateTicketArray();
    }
    else {
      window.location = '/index.html';
    }
  });
}