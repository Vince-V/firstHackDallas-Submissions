var first = "true";
var vz_id;
$(function() {
	$('#adminList').hide();
	getUsers();
});

function getUsers() {
	document.title = "loading...";
    document.body.style.cursor = 'progress';
    $("#userTable").hide();
    $("#loadingdiv").show();


	var table = $("#userTable");
	$.ajax({
		url: '../../AdminPage/rest/servlet/getUsers'
	}).done(function(users) {
		$("#userTable td").remove();
		table.append(users);
		first = "false";

		document.title = "ROC Daily Report";
        document.body.style.cursor = 'default';
        $("#userTable").show();
        $("#loadingdiv").hide();

	});
}

function getCategories(id) {
	window.location = '/AdminPage/dragDrop.html?vzid=' + id;
}

function getTicketInfo(id) {
	window.location = '/AdminPage/updateTicket.html?id=' + id;
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