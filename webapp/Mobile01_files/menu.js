jQuery(function(){
	$("#member-tools,.sf-menu").superfish({
		delay: 500,
		pathClass: 'current',
		dropShadows: false,
		speed: 300,
		autoArrows: false
	});
	$("li:has(ul)>a").addClass("sub-menu");
	$(".sf-menu li li a").addClass("item");
	$(".sf-menu>li>a").addClass("main");
	$(".sf-menu>li>ul").addClass("lv02");
	$(".sf-menu>li>ul>li>ul").addClass("lv03");
	$(".sf-menu>li>ul>li>ul>li>ul").addClass("lv04");
	$(".sf-menu ul ul li:first-child").addClass("first");

	$("#tabs").tabs();
	if($.browser.mozilla){
		$(".ui-tabs-panel a,#index-category h3").ellipsis();
		$("#tabs a").click(function(){
			$(".ui-tabs-panel a").ellipsis(true);
		});
	}

});