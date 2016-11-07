$(document).ready(function() {
	getCSV();
});

function getCSV() {

	$.ajax({
		url: '../../AdminPage/rest/servlet/getCsv'
	}).done(function(source) {
		$('#tbl_ftpCSV').append(source);
	});
}

function btn_submitCsv() {
	alert('todo: get all the info needed...');
	// $.ajax({
	// 	url: '/AdminPage/rest/servlet/submitCSV'
	// });
}