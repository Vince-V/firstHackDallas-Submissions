$(function(){
	$("#password").keyup(function(event){
    if(event.keyCode == 13){
      btn_loginClick();
    }
  });
})

function btn_loginClick() {
	var id = $("#username").val();
	var pw = $("#password").val();

	document.body.style.cursor = 'progress';
    document.title = "loading...";

	if ((id.match(/\bv/) != null || id.match(/\bz/) != null) && id.match(/.\d/g) != null) {
		if (id.length > 0 && pw.length > 0) {
			$.ajax({
				url: '../../AdminPage/rest/servlet/validateUser/' + id + '-' + pw
			}).done(function(isValid) {
				if (isValid == "1") {
					$.ajax({
						url: '../../AdminPage/rest/servlet/getusergroup'
					}).done(function(usergroup) {
						if (usergroup == null) {
							window.location = "/AdminPage/index.html";
						}
						else {
							window.location = "/AdminPage/home.html";
						}
					});
				}
				else if (isValid == "-400") {
					alert('There was a connection problem in the back end. Try again later...');
				}
				else {
					alert("Username or password incorrect, try again.");
					$("#username").val("");
					$("#password").val("");
				}
			});
		}
	}
	else {
		alert('You must enter all fields correctly.');
	}
}