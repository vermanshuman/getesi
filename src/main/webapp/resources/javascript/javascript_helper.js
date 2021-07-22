function fixExpandRow() {
	for (var i = 0; $('.ui-expanded-row-content').length > i; i++) {
		for (var j = 0; $('.ui-expanded-row-content')[i].parentNode.rows.length > j; j++) {
			if ($('.ui-expanded-row-content')[i].parentNode.rows[j] == $('.ui-expanded-row-content')[i]) {
				$($('.ui-expanded-row-content')[i])
						.addClass(
								$('.ui-expanded-row-content')[i].parentNode.rows[j - 1].className
										.split(' ')[1]);
			}
		}
	}
}

function searchOnEnter(event, but_id) {
	if (window.event) {
		key = window.event.keyCode;
	} else if (event) {
		key = event.which;
	} else {
		return true;
	}

	if (key == 13) {
		$(document.getElementById(but_id)).click();
		return false;
	}
	return true;
}

function fixCalendarInput() {
	for (var i = 0; i < $(".ui-inputfield.hasDatepicker").length; i++) {
		if ($(".ui-inputfield.hasDatepicker")[i].alt != "") {
			$($(".ui-inputfield.hasDatepicker")[i]).mask(
					$(".ui-inputfield.hasDatepicker")[i].alt);
			continue;
		}
		$($(".ui-inputfield.hasDatepicker")[i]).mask('99/99/9999');
	}

	$(".ui-inputfield.hasTimepicker:not(.time_only)").mask('99:99');
}

function fixImageUpload() {

	for (var i = 0; i < $('.image_upload').length; i++) {
		$('.image_upload')[i].accept = "image/jpeg,image/png,image/bmp,image/gif";
	}

	$('.image_upload').change(
			function() {
				var filename = $(this).val();
				if (!/\.jpg$/.test(filename) && !/\.jpeg$/.test(filename)
						&& !/\.png$/.test(filename) && !/\.bmp$/.test(filename)
						&& !/\.gif$/.test(filename)) {
					alert('Please select a image');
					$(this).val('');
				}
			});

	for (var i = 0; i < $('.csv_upload').length; i++) {
		$('.csv_upload')[i].accept = "text/x-comma-separated-values";
	}

	$('.csv_upload').change(function() {
		var filename = $(this).val();
		if (!/\.csv$/.test(filename)) {
			alert('Please select a CSV file');
			$(this).val('');
		}
	});

	for (var i = 0; i < $('.mp3_upload').length; i++) {
		$('.mp3_upload')[i].accept = "audio/mpeg";
	}

	$('.mp3_upload').change(function() {
		var filename = $(this).val();
		if (!/\.mp3$/.test(filename)) {
			alert('Please select a MP3 file');
			$(this).val('');
		}
	});

	for (var i = 0; i < $('.ogg_upload').length; i++) {
		$('.ogg_upload')[i].accept = "audio/ogg";
	}

	$('.ogg_upload').change(function() {
		var filename = $(this).val();
		if (!/\.ogg$/.test(filename)) {
			alert('Please select a OGG file');
			$(this).val('');
		}
	});

	for (var i = 0; i < $('.xml_upload').length; i++) {
		$('.xml_upload')[i].accept = "text/xml";
	}

	$('.xml_upload').change(function() {
		var filename = $(this).val();
		if (!/\.xml$/.test(filename)) {
			alert('Please select a XML file');
			$(this).val('');
		}
	});

}

function startTime() {
	var today = new Date();
	var h = today.getHours();
	var m = today.getMinutes();
	var s = today.getSeconds();

	m = checkTime(m);
	s = checkTime(s);
	document.getElementById('time').innerHTML = h + ":" + m + ":" + s;
	t = setTimeout(function() {
		startTime();
	}, 500);
}

function checkTime(i) {
	if (i < 10) {
		i = "0" + i;
	}
	return i;
}

function removeAllTooltips(editorSelector) {
	var elements = $(editorSelector).find('*');
	$.each(elements, function(index, item) {
		$(item).removeAttr('title');
	});
}

function onCheck(component, column) {
	var tbody = component.parentNode.parentNode.parentNode;
	var id = parseInt(component.parentNode.parentNode.attributes['data-ri'].nodeValue);

	if (component.parentNode.className.split(' ')[0] == "sub_header") {
		id++;
		var item = tbody.rows[id];
		while (item.cells[column].className.split(' ')[0] != "sub_header") {
			item.childNodes[column].firstChild.checked = component.checked;
			id++;
			item = tbody.rows[id];
		}
	} else {
		id--;
		var item = tbody.rows[id];
		while (item.childNodes[column].className.split(' ')[0] != "sub_header") {
			id--;
			item = tbody.rows[id];
		}
		item.childNodes[column].firstChild.checked = false;
	}
}
function getCurrentDate() {
	var today = new Date();
	var dd = today.getDate();
	var mm = today.getMonth()+1; //January is 0!
	var yyyy = today.getFullYear();

	if(dd<10) {
	    dd='0'+dd
	} 

	if(mm<10) {
	    mm='0'+mm
	} 

	today = mm+'/'+dd+'/'+yyyy;
	
	return today;
}
function setIcon() {
    var hiddenCode = $("#hiddenButton").val();
    if(hiddenCode !== undefined) {
        var str = "<span class = 'fa " + hiddenCode + " fa-2x' />";
        $('#icon_label').html(str);
        $('#icon_label').css({textAlign: "center",opacity: 1});
    }
}