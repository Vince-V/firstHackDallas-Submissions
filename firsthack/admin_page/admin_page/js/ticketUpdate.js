var tktId;
var closure_tracking_choice;
var resolution = '';
var maskedComment;
var unmaskedComment;
var origReferredTo ='';
var finalReferredTo = '';
var orig;
var addUserSignagture = true;
var origStatuses;
var rerouteSelects = "";
var origHoldStatus;
var origTktStatus;

$(function() {	
	// alert('hello there');
	hideSelects();
	$.ajaxSetup({ cache: false });
	$('#add_user').hide();
  	$('#all_users').hide();
 	$('#btn_ftp').hide();

	getTicketStatuses();
	$('#div_maskText').hide();	
	showUnmaskedCheckbox();
	$('#txtarea_comment').hide();

	$('#ticket_status').change(function() {
		addSelect();
	});

	addSelect();
	addTrackingSelect();
	addIssueSelect();
});

function showUnmaskedCheckbox() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/showUnmaskedCheckbox'
	}).done(function(show) {
		if (show == 'true') {
			$('#div_maskText').show();
		}
		else {
			$('#div_maskText').hide();	
			$('#div_maskText').html('');
		}
	});
}

function getReferredToList() {
	if (rerouteSelects == "") {
		$.ajax({
			url: '../../AdminPage/rest/servlet/getRerouteMap'
		}).done(function(reroutes) {
			rerouteSelects = reroutes;
			$('#ticket_status').html(rerouteSelects);
			// $('#ticket_status').html(reroutes);
		});
	}

	if ($('#cbox_reroute').is(":checked") || $('#cbox_misdirect').is(":checked")) {
		$('#ticket_status').html(rerouteSelects);
		$('.hold_status').hide();
	}
	else {
		$('#ticket_status').html(origStatuses);
		$('#ticket_status').val(origTktStatus);

		if (origTktStatus == "On Hold") {
			getOnHoldCodes(origHoldStatus);
		}
	}

	// for internal use only...
	$.ajax({
		url: '../../AdminPage/rest/servlet/getReferredToList'
	}).done(function(referredToList) {
		$('#referred_to_sel').html(referredToList);
	});
}

function hideSelects() {
	$('#resolution').hide();
	$('.hold_status').hide();
	$('#closure_tracking').prop('disabled', true);
	$('#closure_issue').prop('disabled', true);
	$('#closure_ordering').prop('disabled', true);
	$('#closure_prov').prop('disabled', true);
	$('#closure_billing').prop('disabled', true);
	$('#closure_cat').prop('disabled', true);
}

function getTicketStatuses() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getStatuses'
	}).done(function(statuses) {
		origStatuses = statuses;
		$('#ticket_status').html(statuses);
		getTicketInfo();
	});
}

function maskText() {
	var cbox_maskText = $('#cbox_maskText');
	var isChecked = cbox_maskText.is(':checked');

	if (isChecked==false) {
		$('#div_comment').show();
		$('#txtarea_comment').hide();
		$('#div_comment').stop().animate({
  			scrollTop: $('#div_comment')[0].scrollHeight
	  	}, 800);
	}
	else {
		$('#div_comment').hide();
		$('#txtarea_comment').show();
		$('#txtarea_comment').stop().animate({
  			scrollTop: $('#txtarea_comment')[0].scrollHeight
	  	}, 800);
	}
}

function getTicketInfo() {
	tktId = window.location.search.split("=")[1];

	$.ajax({
		url: '../../AdminPage/rest/servlet/getTicket=' + tktId 
	}).done(function(ticket) {
		if (null != ticket) {
			$('#ticket_id').html("<b>Ticket:</b> " + ticket.rocTicketId);
			$('#referred_to').html(ticket.referredTo);
			origTktStatus = ticket.ticketStatus;

			$('#ticket_status').val(ticket.ticketStatus);
			$('#div_comment').html(ticket.maskedComment);
			if (ticket.comment != "")
				$('#txtarea_comment').html(ticket.comment);
			$('#pcan').html("<b>PCAN:</b> " + ticket.pcan);
			$('#btn').html("<b>BTN:</b> " + ticket.btn);
			$('#account').html("<b>Account:</b> " + ticket.account);
			$('#div_comment').stop().animate({
	  			scrollTop: $('#div_comment')[0].scrollHeight
		  	}, 800);
		  	orig = ticket.referredTo;

		  	if (ticket.onHoldStatus != "") {
		  		console.log("going in");
		  		origHoldStatus = ticket.onHoldStatus;
		  		getOnHoldCodes(origHoldStatus);
		  	}

		  	console.log(ticket.userComment);
		  	if (ticket.userComment != '') {
		  		$('#add_comment').html(ticket.userComment);
		  		addUserSignagture = false;
		  	}

			addCommentHasFocus();
			document.title = "ROC Daily Report";
			document.body.style.cursor = 'default';
			// table.show();
			$("#loadingdiv2").hide();
		}
		else {
			alert('Ticket was not found in DB...');
			window.history.back();
		}
	}).fail(function(){
      alert("Something went wrong...");
  	});
}

function addSelect() {
	var status = $('#ticket_status').val();
	console.log("status: " + status);
	getClosureCategories();
	if (status == 'On Hold') {
		console.log("hello world");
		$('.hold_status').show();
		$('#cbox_reroute').prop('disabled', true);
		$('#cbox_misdirect').prop('disabled', true);
		$('#cbox_onsiteRefer').prop('disabled', true);
		getOnHoldCodes();
	} 
	else {
		resolution = "~Worked by IT";
		$('#resolution').html("Resolution: ~Worked by IT</br><input type=checkbox onclick='setResolution(\"true\");'>Education</input>");	
		$('#resolution').show();
		$('.hold_status').hide();
		$('#cbox_reroute').prop('disabled', false);
		$('#cbox_misdirect').prop('disabled', false);
		$('#cbox_onsiteRefer').prop('disabled', false);
	}
}

function getOnHoldCodes(status) {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getHoldCodes',
		type: "POST"
	}).done(function(codes) {
		console.log(codes);
		$('#holdCodes').html(codes);

		if (status != "")
			$('#holdCodes').val(status);

		$('.hold_status').show();
	}); 
}

function setResolution(cked) {
	if (cked == 'true') {
		resolution = 'Education Provided*1m';
		$('#resolution').html("Resolution: Education Provided*1m</br><input type=checkbox onclick='setResolution(\"false\");' checked>Education</input>");
	}
	else {
		resolution = '~Worked by IT';
		$('#resolution').html("Resolution: ~Worked by IT</br><input type=checkbox onclick='setResolution(\"true\");'>Education</input>");
	}
}

function addCommentHasFocus() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getName'
	}).done(function(name) {
		if (name != null && addUserSignagture)
			$('#add_comment').text(name);
	}).fail(function(){
      alert("Something went wrong...");
      document.title = "ROC Daily Report";
      document.body.style.cursor = 'default';
      table.show();
      $("#loadingdiv2").hide();
  });
}

function getClosureCategories() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getClosureCategories'
	}).done(function(categories) {
		$('#closure_cat').html(categories);
		$('#closure_cat').prop('disabled', false);
	}).fail(function(){
      alert("Something went wrong...");
  });
}

function addTrackingSelect() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getTracking'
	}).done(function(tracking) {
		$('#closure_tracking').html(tracking);
		$('#closure_tracking').prop('disabled', false);
	}).fail(function(){
      alert("Something went wrong...");
  });
}

function addIssueSelect() {
	closure_tracking_choice = $('#closure_tracking').val();
	$.ajax({
		url: '../../AdminPage/rest/servlet/getIssue/' + 6
	}).done(function (issues) {
		$('#closure_issue').html(issues);
		$('#closure_issue').prop('disabled', false);
		addOrderingSelect();

	}).fail(function(){
      alert("Something went wrong...");
  });
}

function addOrderingSelect() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getOrdering'
	}).done(function (ordering) {
		$('#closure_ordering').html(ordering);
		$('#closure_ordering').prop('disabled', false);
		addProvSelect();
	}).fail(function(){
      alert("Something went wrong...");
  });
}

function addProvSelect() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getProv'
	}).done(function (provisioning) {
		$('#closure_prov').html(provisioning);
		$('#closure_prov').prop('disabled', false);
		addBillingSelect();
	}).fail(function(){
      alert("Something went wrong...");
  });
}

function addBillingSelect() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getBilling'
	}).done(function (billing) {
		$('#closure_billing').html(billing);
		$('#closure_billing').prop('disabled', false);
	}).fail(function(){
      alert("Something went wrong...");
  	});
}

function btn_updateTicketClick() {
	var comm = $('#add_comment').val();
	var status = $('#ticket_status').val();
	var onHold;
	document.body.style.cursor = 'progress';
    document.title = "loading...";

    if ($('#referred_to').html().indexOf("select") > -1)
    	finalReferredTo = $('#referred_to_sel').val();
    else if (origReferredTo == '')
    	finalReferredTo = $('#referred_to').html();
    else
    	finalReferredTo = origReferredTo;

    if ($('#holdCodes').val() != 'undefined'
    	&& $('#holdCodes').val() != 'null') {
    	onHold = $('#holdCodes').val();
    	// console.log(onHold);
    }
    
	 if ((status != "Referred" && status != "null" && status != "Reroute Queues") || $('#cbox_onsiteRefer').is(':checked')) {
		$.ajax({
			url: '../../AdminPage/rest/servlet/updateTicket',
			contentType: "application/json",
			dataType: "json",
			data: JSON.stringify({
					 id 		 	: tktId,
					 status 	 	: status,
					 cat 			: $('#closure_cat').val(),
					 track 	 		: $('#closure_tracking').val(),
					 issue 	 		: $('#closure_issue').val(), 
					 ordering    	: $('#closure_ordering').val(),
					 prov 			: $('#closure_prov').val(),
					 billing 		: $('#closure_billing').val(),
					 misdirect   	: $('#cbox_misdirect').is(':checked'),
					 reroute 		: $('#cbox_reroute').is(':checked'),
					 redirect 		: $('#cbox_redirect').is(':checked'),
					 resolution   	: resolution,
					 comment 		: comm,
					 referredTo 	: finalReferredTo,
					 onSiteReferral : $('#cbox_onsiteRefer').is(':checked'),
					 onHoldStatus   : onHold
				}),
			type: "POST"
		}).done(function() {
			// do nothing
		}).fail(function(data) {
			if (data.responseText == "done") {
				window.location = '/AdminPage/home.html?updated=true';
			}
			else 
				alert("Something went wrong...");	

			document.title = "ROC Daily Report";
			document.body.style.cursor = 'default';
		});
	}
	else {
		alert("You need to change the Status.");
	}
}

function getUserRole() {
  $.ajax({
    url: '../../AdminPage/rest/servlet/getRole'
  }).done(function(role) {
    if (role[0] == 'admin') {
      $('#add_user').show();
      $('#all_users').show();
      $('#btn_ftp').show();
    }
    else if (role[0] == "0") {
      window.location = '/AdminPage/oops.html';
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