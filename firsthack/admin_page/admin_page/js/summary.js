$(document).ready(function() {
	$('#summary1').hide();
	$('#summary2').hide();
	$('#summary3').hide();
	$.ajax({
		url: '../../AdminPage/rest/servlet/getTicketCount'
	}).done(function(data) {
		if (data[0] == "1337")
			window.location = '/AdminPage/index.html';
		$('#summary1').show();
		$('#summary1').append(data[0]);

		$('#summary2').show();
		$('#summary2').append(data[1]);

		$('#summary3').show();
		$('#summary3').append(data[2]);
		
		$("#loadingdiv2").hide();
	});
});