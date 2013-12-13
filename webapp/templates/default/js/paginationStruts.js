function goToAnotherPage(max, recordsPerPage, contextPath, namespace, action, pageToGo)
{
	var page =  pageToGo * 1;

	if (!isNaN(page) && page <= max && page > 0) {
		var path = contextPath + namespace + "/" + action + ".action?start=" + ((page - 1) * recordsPerPage);
		document.location = path;
	}
}
