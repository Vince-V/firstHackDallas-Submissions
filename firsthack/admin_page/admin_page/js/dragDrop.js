var id;
var order = "";
$(function() {
    $('#adminList').hide();
    id = window.location.search.split("=")[1];
    $('#vzId').html("Making changes for VZID: " + id );
    $.ajax({
        url: '../../AdminPage/rest/servlet/getDNDCats/' + id
    }).done(function(data) {
        if (null != data) {
            if (data[0][0] == "Y")
                    $('#isActive').prop('checked', true);
            else
                $('#isActive').prop('checked', false);
            $('#numTickets').val(data[0][1]);
            $('#usersCats').html(data[0][2]);
            $('#allCats').html(data[1]);
        }
        else
            window.history.back();
    });
    getUserRole();
});

function updateUser() {
    $.ajax({
        url: '../../AdminPage/rest/servlet/updateUser',
        contentType: "application/json",
        dataType: "json",
        data: JSON.stringify({
            firstname: "",
            lastname: "",
            vzId: id,
            role: "",
            usersBestCategory: order,
            password: "",
            usergroup: "",
            ticketsPerDay: $('#numTickets').val(),
            ticketsTaken: "",
            ticketsPending: "",
            ticketsClosed: "",
            isActive: $("#isActive").is(":checked"),
            isOffshore: ""
        }),
        type: "POST"
    }).done(function(data) {
        if (data == "1") {
            alert("User " + id + " was updated.");
            window.history.back();
        }
    }).fail(function() {
        alert("Something went wrong...");
    });
}

function showOrder() {
    document.getElementById("btn_update").style.display='none';
    var count = 1;
    var total = $('#usersCats').children().length;
    $('#usersCats').children().each(function() {
      var hastext = $(this).text().length != 0;
      if(hastext) {
        if (count < total)
            order += $(this).text() + ",";
        else
            order += $(this).text();
        count++;
      }
    });
    updateUser();
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