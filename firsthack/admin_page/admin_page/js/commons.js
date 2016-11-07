$(document).ready(function() {
	$('#search').keyup(function(event){
    if(event.keyCode == 13){
      id = $('#search').val();
      if (id.length >= 7)
        lookInDB();
    }
  });
});

function signout() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/signout'
	}).done(function() {
		window.location = '/AdminPage/index.html';
	});
	document.cookie="";
}

function settings() {
	window.location = '/AdminPage/settings.html';
}

function goHome() {
	window.location = '/AdminPage/home.html';
}

function lookInDB() {
	window.location = "/AdminPage/updateTicket.html?id=" + $('#search').val();
}

