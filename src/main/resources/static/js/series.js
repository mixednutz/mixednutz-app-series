$("#newseries_title").change(function(){
	var str = $(this).val();
	str = str
		.toLowerCase()
		.trim()
		.replace(/[^\w\s]|_/g, "")
		.replace(/\s+/g, '-');
	$("#newseries_titleKey").val(str);
});
$("#newchapter_title").change(function(){
	var str = $(this).val();
	str = str
		.toLowerCase()
		.trim()
		.replace(/[^\w\s]|_/g, "")
		.replace(/\s+/g, '-');
	$("#newchapter_titleKey").val(str);
});