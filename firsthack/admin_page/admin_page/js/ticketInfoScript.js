$(function() {
	var id = window.location.search.split("=")[1];

	$.ajax({
		url: '../../AdminPage/rest/servlet/getTicketInfo/' + id
	}).done(function(tickets) {
		table.append(tickets);
		first = "false";
	});
});

function btn_goBack() {
	window.history.back();
}