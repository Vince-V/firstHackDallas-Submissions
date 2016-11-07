var opened = [];

var highlightObj = {
	selected: [],
	addToSelected: function(nodeObj){
		if(this.isInSelected(nodeObj) < 0){
			this.selected.push(nodeObj);
		} 
	},
	removeFromSelected: function(nodeObj){
		var index = this.isInSelected(nodeObj);
		if( index > -1){
			this.selected.splice(index, 1);
		}
	},
	isInSelected: function(nodeObj){
		var index = -1;
		var count = 0;
		if(this.selected.length > 0){
			this.selected.forEach(function(item){
				if(item.parent == nodeObj.parent 
					&& item.child == nodeObj.child){
						index = count;
				}
				count++;
			});
		}
		return index;
	}
}

var filterObj = {

	objList: [],

	addToParent: function(parent,element){
		var added = 0;
		this.objList.forEach(function(arrayItem){
			if(arrayItem.parent == parent){
				arrayItem.list.push(element);
				added = 1;
			}
		});
		if(added == 0){
			var obj = new filterListObj(parent);
			obj["list"].push(element);
			this.objList.push(obj);
		}
		added = 0;
	},
	removeFromParent: function(parent,element){
		var removeParent = '';;
		this.objList.forEach(function(arrayItem){
			if(arrayItem.parent == parent){
				var index = arrayItem.list.indexOf(element);
				if (index > -1) {
				    arrayItem.list.splice(index, 1);
				}
				if(arrayItem.list.length == 0){
					removeParent = arrayItem.parent;
				}
			}
		});
		if('' != removeParent){
			this.objList = this.objList.filter(function(el){
				return el.parent !== removeParent;
			})
			removeParent = '';
		}
	},
	printString: function(){
		var filterString = '';
		this.objList.forEach(function(arrayItem){
			filterString += "<div class='lightHighlight'>" +arrayItem.parent + ":</div> " + arrayItem.list.toString() + " ";
		});
		return filterString;
	},
	printQueryString: function(){
		var filterString = '';
		this.objList.forEach(function(arrayItem){
			var childString = '';
			arrayItem.list.forEach(function(item){
				childString += '\''+ item + '\',';
			});
			
			if (childString.substring(0,childString.length-1).indexOf("null") >= 0)
				filterString += arrayItem.parent + " is null`"; 
			else
				filterString += arrayItem.parent + " in (" + childString.substring(0,childString.length-1) + ")`";
		});
		return filterString;
	},
	addArray: function(nodeObjList){
		nodeObjList.forEach(function(item){
			$(item.node).toggleClass(function(){
				if(item.node.hasClass("select")){
					filterObj.removeFromParent(item.parent,item.child);
				}
				else {
					filterObj.addToParent(item.parent,item.child);
				}
				return "select";
			});
		});
	}
} 


var filterListObj = function(parent){
	this.list = [];
	this.parent = parent;
}

var filterObject = function(parent,child,$node){
	this.parent = parent;
	this.child = child;
	this.node = $node;
}

function recursiveIterate($node,input) {
    $node.children().each(function () {
    	var pageId = $(this).attr('id');
    	var pageEl = $(this).text();
    	console.log(input + ": " +pageEl);
    	if(typeof pageEl != 'undefined' && '' != input && $(this).parent().parent().css("display") != "none"
    		&& typeof pageId != 'undefined'){
    		if(pageEl.toLowerCase().indexOf(input) > -1){
	    		$(this).addClass("filterSelected");
	    		highlightObj.addToSelected(new filterObject($(this).parent().parent().attr('id'),$(this).attr('id'),$(this)));
	    	} else {
	    		$(this).removeClass("filterSelected");
	    		highlightObj.removeFromSelected(new filterObject($(this).parent().parent().attr('id'),$(this).attr('id'),$(this)));
	    	}
    	} else if ('' == input){
    		$(this).removeClass("filterSelected");
    		highlightObj.removeFromSelected(new filterObject($(this).parent().parent().attr('id'),$(this).attr('id'),$(this)));
    	}
        recursiveIterate($(this),input);
    });
}


function setPage() {

	$(".filterOptions").hide();
	$(".filterSearch").hide();
	$(".filterSearchBottom").hide();
	$(".filterLabel").click(function() {
		$(".allOptions").slideDown();
		var id = $(this).attr('id');
		var index = opened.indexOf(id);
		if (index < 0) {
			opened.push(id);
			$(this).addClass("highlight");
		} else {
			opened.splice(index, 1);
			$(this).removeClass("highlight");
		}
		if(opened.length > 0){
			$(".filterSearch").slideDown();
			$("#filterEl").slideDown();
			$(".filterSearchBottom").slideDown();
		} else {
			if(filterObj.objList.length == 0){
				$(".filterSearch").slideUp();
				$(".filterSearchBottom").slideUp();
			} else {
				$(".filterSearch").slideUp();
			}
		}
	 	$("#"+id+".filterOptions").slideToggle();	 	
	});

	$('.filterSearch').keypress(function (e) {
		var key = e.which;
		if(key == 13)  // the enter key code
		{
			filterObj.addArray(highlightObj.selected);
			$(".filterByList").html(filterObj.printString());
			return false;  
		}
	});   

	$("li").click(function() {
		var id = $(this).attr('id');

		$(this).toggleClass(function(){
			if($(this).hasClass("select")){
				filterObj.removeFromParent($(this).parent().parent().attr('id'),id);
			}
			else {
				filterObj.addToParent($(this).parent().parent().attr('id'),id);
			}
			return "select";
		});
		$(".filterByList").html(filterObj.printString());
	});

	$('.filterInput').on('input', function() {
    	var input = $(this).val();
		recursiveIterate($(".allOptions > .filterOptions "),input);
	});

	$(".filterBtn").click(function() {
		$('#ticketTable').hide();
		$('.helpOption').hide();
		$('#loadingdiv2').show();
		//ajax call here
		$.ajax({
			url: '../../AdminPage/rest/servlet/doFilter',
			contentType: "application/json",
			data: window.location.pathname + '`' + filterObj.printQueryString(),
			type: "POST"
		}).success(function(data) {
			$('#ticketTable').show();
			$('#loadingdiv2').hide();
			$(".filterSearch").slideUp();
			$(".allOptions").slideUp();
			$('#tableBody').html(data);
			updateTicketArray();
		});
	});
}
