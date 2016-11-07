$(document).ready(function() {
	$('#addMap').hide();
	var slideDown = true;

	// Get all the queues in the db and 
	// populate them in oldQueues select
	$.ajax({
		url: '../../AdminPage/rest/servlet/getStatuses'
	}).done(function(queues) {
		if (queues == "1337")
			window.location = "index.html";
		else 
			$('#statusQueues').html(queues);
	});

	$.ajax({
		url: '../../AdminPage/rest/servlet/getRerouteMap'
	}).done(function(queues) {
		if (queues == "1337")
			window.location = "index.html";
		else 
			$('#rerouteQueues').html(queues);
	});

	$('#newRerouteQueue').on('input', function() {
		if (slideDown) {
			$('#addMap').slideToggle();
			$('#addStatus').slideToggle();
			slideDown = false;
		}
		if ($('#newRerouteQueue').val() == "") {
			$('#addMap').slideToggle();
			$('#addStatus').slideToggle();
			slideDown = true;
		}
	});
});

// remove the queue selected in oldQueues 
function removeQueue() {
	if ($('#statusQueues').val() == "null" && $('#rerouteQueues').val() != "null")
		removeTheQueue($('#rerouteQueues').val(), "reroute");
	else if ($('#statusQueues').val() != "null")
		removeTheQueue($('#statusQueues').val(), "status");
}

function removeTheQueue(queue, whichType) {
	console.log("removing " + whichType + " queue: " + queue);

	$.ajax({
		url: '../../AdminPage/rest/servlet/removeQueue/' + whichType,
		data: queue,
		type: "POST"
	}).done(function(data) {
		if (data == "true") {
			alert(queue + " has been removed");
			location.reload();
		}
	});
}

// add the queue in newQueue
function addQueue() {
	if ($('#newStatusQueue').val() == "" && $('#newRerouteQueue').val() != "")
		addTheQueue($('#newRerouteQueue').val(), $('#newRerouteMap').val(), "reroute");
	else if ($('#newStatusQueue').val() != "")
		addTheQueue($('#newStatusQueue').val(), "null", "status");
}

function addTheQueue(queue, map, whichType) {
	if (map == "")
		map = "No Map";
	console.log("add " + whichType + "Queue: " + queue + "; with map: " + map);
	var dataStr;

	if (map == "null")
		dataStr = queue;
	else 
		dataStr = queue + "`" + map;

	console.log(dataStr);

	$.ajax({
		url: '../../AdminPage/rest/servlet/addQueue/' + whichType,
		contentType: "application/json",
		data: dataStr,
		type: "POST"
	}).done(function(data) {
		if (data != "false") {
			alert(queue + " has been added");
			location.reload();
		}
	});
}