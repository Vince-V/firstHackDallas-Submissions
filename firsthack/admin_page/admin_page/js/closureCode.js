$(document).ready(function() {
	$(".submitCodesBtn").click(function() {
		var closureCodes = '';
		$("div > input").each(function(){
			closureCodes += $(this).attr('id') + "|" + $(this).val() + "`";
		});
		submitClosureCodes(closureCodes);
		// console.log(closureCodes);
	});

	$(".removeCodesBtn").click(function() {
		var closureCodes = '';
		$("div > select").each(function(){
			var id = $(this).attr('id');
			closureCodes += id.substring(4, id.length) + "|" + $(this).val() + "`";
		});
		removeClosureCodes(closureCodes);
	});

	getAllCodes();

	$('#add_user').hide();
	$('#all_users').hide();
	$('#btn_ftp').hide();
	$('#ticketSelect').hide();
	getUserRole();
});

function getAllCodes() {
	getClosureCodes();
	getInternalCodes();
	getIssueCodes();
	getOrderingCodes();
	getProvisioningCodes();
	getBillingCodes();
}

function getClosureCodes() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getClosureCategories'
	}).done(function(categories) {
		$('#REM_CATEGORY_DESC').html(categories);
	}).fail(function(){
      alert("Something went wrong...");
  	});
}

function getBillingCodes() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getBilling'
	}).done(function (billing) {
		$('#REM_BILLING_STAGE_DESC').html(billing);
	}).fail(function(){
      alert("Something went wrong...");
  	});
}

function getProvisioningCodes() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getProv'
	}).done(function (provisioning) {
		$('#REM_PROVISION_STAGE_DESC').html(provisioning);
	}).fail(function(){
      alert("Something went wrong...");
  });
}

function getOrderingCodes() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getOrdering'
	}).done(function (ordering) {
		$('#REM_ORDERING_STAGE_DESC').html(ordering);
	}).fail(function(){
      alert("Something went wrong...");
  });
}

function getIssueCodes() {
	closure_tracking_choice = $('#closure_tracking').val();
	$.ajax({
		url: '../../AdminPage/rest/servlet/getIssue/' + 6
	}).done(function (issues) {
		$('#REM_ISSUE_STAGE_DESC').html(issues);
	}).fail(function(){
      alert("Something went wrong...");
  	});
}

function getInternalCodes() {
	$.ajax({
		url: '../../AdminPage/rest/servlet/getTracking'
	}).done(function(tracking) {
		$('#REM_TRACKING_CATEGORY').html(tracking);
	}).fail(function(){
      alert("Something went wrong...");
  });
}

function submitClosureCodes(codes) {
	console.log(codes);
	$.ajax({
		url: '../../AdminPage/rest/servlet/addClosureCodes',
		data: codes,
		type: "POST"
	}).done(function(updated) {
		if (updated=="1337") {
			window.location = '/index.html';
		}
		if (updated == "1") {
			location.reload();
		}
	});
}

function removeClosureCodes(codes) {
	console.log(codes);
	$.ajax({
		url: '../../AdminPage/rest/servlet/removeClosureCodes',
		data: codes,
		type: "POST"
	}).done(function(removed) {
		if (removed=="1337") {
			window.location = '/index.html';
		}
		else {
			location.reload();
		}
	});
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