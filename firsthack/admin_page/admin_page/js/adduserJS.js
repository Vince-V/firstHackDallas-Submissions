function btn_submitClick() {
	document.body.style.cursor = 'progress';
    document.title = "loading...";

    var numTickets = $('#numTickets').val();
    var vzid = $('#vzid').val();
    var usergroup = $('#usergroup').val();
    var tktperday = $('#numTickets').val();
    var pwd = $('#pwd').val();
    var fname = $('#firstname').val();
    var lname = $('#lastname').val();
    var bestcat = $('#bestCat').val();
    var ugroup = $('#usergroup').val();
    var isOffshore = $('#offshoreUser').is(':checked');

    if (numTickets.match(/[0-9]/g) != null) {
    	if (usergroup.match(/[1-2]/) != null) {
	    	if ((vzid.match(/\bv/) != null || vzid.match(/\bz/) != null) 
	    		&& vzid.match(/.\d/g) != null && vzid.length == 7) {    		
			    $.ajax({
			    	url: '../../AdminPage/rest/servlet/addUser',
			    	contentType: "application/json",
			    	dataType: "json",
			    	data: JSON.stringify({
			    		firstname: fname,
			    		lastname: lname,
			    		vzId: vzid,
			    		role: ugroup,
			    		usersBestCategory: bestcat,
			    		password: pwd,
			    		usergroup: "0",
			    		ticketsPerDay: tktperday,
			    		ticketsTaken: "0",
			    		ticketsPending: "0",
			    		ticketsClosed: "0",
			    		isActive: "Y",
						isOffshore: isOffshore
			    	}),
			    	type: "POST"
			    }).done(function(data) {
			    	if (data == "1") {
				    	alert("User " + fname + " was added.");
				    	window.history.back();
			    	}
			    }).fail(function() {
			    	alert("Something went wrong...");
			    });
	    	}
	    	else {
	    		alert('Error... Try again.');
	    	}	
    	}    
    	else {
    		alert('Error... Try again.');
    	}
	}	
	else {
		alert('Error... Try again.');
	}
	document.title = "ROC Daily Report";
	document.body.style.cursor = 'default';
}