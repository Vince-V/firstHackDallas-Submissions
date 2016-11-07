$(function() {
	$('#adminList').hide();
	getSignature();
});

function getSignature() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getName'
	}).done(function(name) {
		if (name != null)
			$('#comment_signature').text(name);
		getEmail();
		getUserRole();
	});
}

function getEmail() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getEmail'
	}).done(function(email) {
		$('#email').text(email);
	});
}

function submitSignature() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/updateSignature',
		contentType: "application/json",
		dataType: "json",
		data: JSON.stringify({
			signature: $('#comment_signature').val()
		}),
		type: "POST",
		success: function() {
			alert('Signature Updated');
		},
		error: function() {
			alert('Something went wrong...');
		}
	});
}

function submitEmail() {
	var email = $('#email').val();

	var emailRegex = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

	if (emailRegex.test(email)) {
		$.ajax({
			url: '../../AdminPage/rest/servlet/updateUsersEmail/' + $('#email').val()
		}).done(function(foo) {
			alert('Email updated.');
		});
	}
	else {
		alert('Must have a valid email address, like: example@example.com');
	}
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